package com.aswfoods.fieldtrack.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Klasifikasi kualitas koneksi jaringan pengguna berdasarkan latensi ping (ms).
 */
enum class NetworkQuality(val label: String) {
    GOOD("Good"),
    MEDIUM("Medium"),
    BAD("Bad"),
    OFFLINE("Offline")
}

/**
 * Snapshot status konektivitas dan hasil pengukuran ping pengguna.
 *
 * @property quality Kategori kualitas jaringan (Good, Medium, Bad, atau Offline)
 * @property pingMs Latensi jaringan dalam milidetik, atau null saat perangkat offline
 */
@Immutable
data class NetworkStatus(
    val quality: NetworkQuality = NetworkQuality.OFFLINE,
    val pingMs: Long? = null
)

/**
 * Utilitas pemantau latensi ping dan kualitas jaringan secara berkala di latar belakang (IO thread).
 */
object NetworkMonitor {

    private const val PING_INTERVAL_MS = 8000L
    private const val SOCKET_TIMEOUT_MS = 1800

    /**
     * Memancarkan pembaruan status ping dan kualitas jaringan secara berkala selama UI aktif.
     *
     * @param context Context aplikasi
     * @return Flow berisi NetworkStatus terkini
     */
    fun observeNetworkStatus(context: Context): Flow<NetworkStatus> = flow {
        val appContext = context.applicationContext
        while (currentCoroutineContext().isActive) {
            emit(measureNetworkStatus(appContext))
            delay(PING_INTERVAL_MS)
        }
    }.distinctUntilChanged().flowOn(Dispatchers.IO)

    /**
     * Mengukur latensi ping jaringan saat ini menggunakan koneksi TCP ringan non-blocking.
     *
     * @param context Context aplikasi
     * @return NetworkStatus berisi kategori kualitas (Good/Medium/Bad/Offline) dan angka ping ms
     */
    fun measureNetworkStatus(context: Context): NetworkStatus {
        if (!hasActiveInternetCapability(context)) {
            return NetworkStatus(quality = NetworkQuality.OFFLINE, pingMs = null)
        }

        val rttMs = measureTcpPing("8.8.8.8", 53) ?: measureTcpPing("1.1.1.1", 443)
        if (rttMs == null) {
            return NetworkStatus(quality = NetworkQuality.OFFLINE, pingMs = null)
        }

        val quality = when {
            rttMs <= 100L -> NetworkQuality.GOOD
            rttMs <= 250L -> NetworkQuality.MEDIUM
            else -> NetworkQuality.BAD
        }
        return NetworkStatus(quality = quality, pingMs = rttMs)
    }

    private fun hasActiveInternetCapability(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun measureTcpPing(host: String, port: Int): Long? {
        return runCatching {
            val startNs = System.nanoTime()
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), SOCKET_TIMEOUT_MS)
            }
            val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L
            elapsedMs.coerceAtLeast(1L)
        }.getOrNull()
    }
}
