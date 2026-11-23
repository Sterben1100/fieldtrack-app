package com.aswfoods.fieldtrack.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import androidx.core.util.Consumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Hasil pengambilan titik koordinat GPS perangkat untuk check-in dan validasi geotag.
 */
sealed class LocationResult {
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val accuracyMeters: Float
    ) : LocationResult()

    data object PermissionMissing : LocationResult()
    data object GpsDisabled : LocationResult()
    data class Unavailable(val reason: String) : LocationResult()
}

/**
 * Utilitas akses GPS lapangan dan reverse geocoding alamat berbasis Android SDK
 * dengan target akurasi maksimal 15 meter.
 */
object LocationHelper {

    /** Target akurasi maksimal GPS agar pengambilan titik presisi di lokasi toko. */
    const val MAX_ACCURACY_METERS = 15f

    /** Radius jarak maksimal pengambilan titik survei terhadap koordinat master toko (meter). */
    const val MAX_GEOTAG_RADIUS_METERS = 15.0

    /**
     * Memeriksa apakah izin lokasi presisi atau perkiraan telah diberikan oleh pengguna.
     *
     * @param context Context aplikasi atau Activity
     * @return true jika salah satu izin lokasi aktif
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    /**
     * Mengambil koordinat GPS aktual perangkat dengan menyaring akurasi <= 15 meter terlebih dahulu,
     * lalu fallback ke provider terbaik yang tersedia apabila sinyal satelit terhalang (indoor).
     *
     * @param context Context aplikasi
     * @param timeoutMs Batas waktu pencarian titik GPS presisi dalam milidetik
     * @return LocationResult berisi koordinat aktual atau alasan kegagalan
     */
    suspend fun getCurrentLocation(
        context: Context,
        timeoutMs: Long = 7000L
    ): LocationResult = withContext(Dispatchers.Main) {
        if (!hasLocationPermission(context)) {
            return@withContext LocationResult.PermissionMissing
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext LocationResult.Unavailable("Layanan lokasi tidak tersedia di perangkat.")

        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            return@withContext LocationResult.GpsDisabled
        }

        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        // 1. Cari titik GPS presisi (target akurasi <= 15 meter)
        if (gpsEnabled) {
            val preciseGps = requestAccurateGpsLocation(locationManager, timeoutMs)
            if (preciseGps != null) {
                return@withContext LocationResult.Success(
                    latitude = preciseGps.latitude,
                    longitude = preciseGps.longitude,
                    accuracyMeters = preciseGps.accuracy
                )
            }
        }

        // 2. Fallback ke Network Provider jika sinyal GPS satelit terhalang (indoor)
        if (networkEnabled) {
            val freshNetwork = requestSingleLocation(context, locationManager, LocationManager.NETWORK_PROVIDER, 4000L)
            if (freshNetwork != null) {
                return@withContext LocationResult.Success(
                    latitude = freshNetwork.latitude,
                    longitude = freshNetwork.longitude,
                    accuracyMeters = freshNetwork.accuracy
                )
            }
        }

        // 3. Fallback terakhir ke koordinat terakhir yang tersimpan di sistem
        val lastKnown = getBestLastKnownLocation(locationManager)
        if (lastKnown != null) {
            return@withContext LocationResult.Success(
                latitude = lastKnown.latitude,
                longitude = lastKnown.longitude,
                accuracyMeters = lastKnown.accuracy
            )
        }

        LocationResult.Unavailable("Sinyal GPS belum ditemukan. Pastikan berada di area terbuka lalu coba lagi.")
    }

    /**
     * Mengonversi koordinat Latitude/Longitude menjadi alamat jalan yang mudah dibaca (Reverse Geocoding).
     *
     * @param context Context aplikasi
     * @param latitude Garis lintang (LA)
     * @param longitude Garis bujur (LG)
     * @return Alamat hasil geocoding atau null jika perangkat offline / layanan tidak tersedia
     */
    suspend fun getAddressFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null
        val locale = Locale.Builder().setLanguage("id").setRegion("ID").build()
        val geocoder = Geocoder(context, locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            withTimeoutOrNull(4000L) {
                suspendCancellableCoroutine { continuation ->
                    runCatching {
                        geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (continuation.isActive) {
                                    continuation.resume(addresses.firstOrNull()?.let { formatAddress(it) })
                                }
                            }

                            override fun onError(errorMessage: String?) {
                                if (continuation.isActive) {
                                    continuation.resume(null)
                                }
                            }
                        })
                    }.onFailure {
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            }
        } else {
            runCatching {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)
                    ?.firstOrNull()
                    ?.let { formatAddress(it) }
            }.getOrNull()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestAccurateGpsLocation(
        locationManager: LocationManager,
        timeoutMs: Long
    ): Location? {
        var bestLocation: Location? = null
        val timedResult = withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (bestLocation == null || location.accuracy < bestLocation!!.accuracy) {
                            bestLocation = location
                        }
                        if (location.hasAccuracy() && location.accuracy <= MAX_ACCURACY_METERS) {
                            locationManager.removeUpdates(this)
                            if (continuation.isActive) {
                                continuation.resume(location)
                            }
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                continuation.invokeOnCancellation {
                    locationManager.removeUpdates(listener)
                }

                runCatching {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        500L,
                        0f,
                        listener,
                        Looper.getMainLooper()
                    )
                }.onFailure {
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        }
        return timedResult ?: bestLocation
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleLocation(
        context: Context,
        locationManager: LocationManager,
        provider: String,
        timeoutMs: Long
    ): Location? = withTimeoutOrNull(timeoutMs) {
        suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()
            continuation.invokeOnCancellation { cancellationSignal.cancel() }

            LocationManagerCompat.getCurrentLocation(
                locationManager,
                provider,
                cancellationSignal,
                ContextCompat.getMainExecutor(context),
                Consumer { location ->
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun getBestLastKnownLocation(locationManager: LocationManager): Location? {
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val candidate = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || candidate.time > bestLocation.time) {
                bestLocation = candidate
            }
        }
        return bestLocation
    }

    private fun formatAddress(address: Address): String? {
        val fullLine = address.getAddressLine(0)?.takeIf { it.isNotBlank() }
        if (fullLine != null) return fullLine
        val parts = listOfNotNull(
            address.thoroughfare,
            address.subLocality,
            address.locality,
            address.subAdminArea,
            address.adminArea
        ).filter { it.isNotBlank() }
        return if (parts.isNotEmpty()) parts.joinToString(", ") else null
    }
}
