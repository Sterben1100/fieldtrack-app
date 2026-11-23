package com.aswfoods.fieldtrack.ui.screens.member

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aswfoods.fieldtrack.data.local.SessionManager
import com.aswfoods.fieldtrack.data.local.database.AppDatabase
import com.aswfoods.fieldtrack.data.local.entity.ProductEntity
import com.aswfoods.fieldtrack.data.remote.RetrofitClient
import com.aswfoods.fieldtrack.data.remote.dto.MemberSkuAuditDto
import com.aswfoods.fieldtrack.data.remote.dto.MemberSurveyRequest
import com.aswfoods.fieldtrack.data.repository.MasterDataRepository
import com.aswfoods.fieldtrack.data.repository.SurveyRepository
import com.aswfoods.fieldtrack.util.ImageHelper
import com.aswfoods.fieldtrack.util.LocationHelper
import com.aswfoods.fieldtrack.util.LocationResult
import com.aswfoods.fieldtrack.worker.SyncManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Representasi satu baris audit SKU fokus (Top 5 + 1 NPL) pada layar Member.
 * Input `actualPriceInput` diawali kosong agar sistem mendeteksi audit yang belum lengkap.
 */
@Immutable
data class SkuAuditItem(
    val product: ProductEntity,
    val hasPriceTag: Boolean = true,
    val isPriceTagCompliant: Boolean = true,
    val actualPriceInput: String = "",
    val isPriceInRange: Boolean = false,
    val skuPhotoPath: String? = null
)

/**
 * State UI untuk layar Audit Fisik Member (Kriteria M1 - M4) beserta kalkulasi skor proporsional.
 */
@Immutable
data class MemberSurveyUiState(
    val outletId: Long = 0,
    val outletName: String = "",
    val existingSyncId: String? = null,
    val checkinAt: String? = null,
    val outletLat: Double? = null,
    val outletLng: Double? = null,
    val outletAddress: String? = null,
    val isSewaDisplay: Boolean = false,
    val classOutlet: String = "GT Biasa",

    // Kriteria M1: Geotag nyata perangkat (Bobot 25%, radius max 15 meter)
    val currentLat: Double? = null,
    val currentLng: Double? = null,
    val gpsAddress: String? = null,
    val isResolvingAddress: Boolean = false,
    val gpsAccuracyMeters: Float? = null,
    val isFetchingGps: Boolean = false,
    val gpsStatusMessage: String = "Menunggu koordinat GPS lapangan...",
    val hasLocationPermission: Boolean = false,
    val distanceMeters: Double? = null,
    val geotagScorePercent: Int = 0,

    // Kriteria M2: Rak Display & Foto Rak (Bobot 20%, kondisional non-Trader sewa rak)
    val selectedRakCondition: String? = null,
    val rakPhotoPath: String? = null,
    val rakScorePercent: Int = 0,

    // Kriteria M3 & M4: Audit 6 SKU (Bobot M3 20%, M4 20%)
    val skuAudits: List<SkuAuditItem> = emptyList(),
    val priceTagScorePercent: Double = 0.0,
    val priceRangeScorePercent: Double = 0.0,
    val estimatedMemberScore: Double = 0.0,

    val showIncompleteModal: Boolean = false,
    val showOutOfRadiusDialog: Boolean = false,
    val showUpdateOutletPointDialog: Boolean = false,
    val isUpdatingOutletPoint: Boolean = false,
    val isDemoMode: Boolean = false,
    val infoMessage: String? = null,
    val validationError: String? = null,
    val isSaved: Boolean = false
) {
    /** Menentukan apakah posisi perangkat masih berada dalam radius maksimal 15 meter dari toko. */
    val isWithinRadius: Boolean
        get() = distanceMeters != null && distanceMeters <= LocationHelper.MAX_GEOTAG_RADIUS_METERS

    /** Menentukan apakah M2 (Rak Sewa Display) berlaku untuk outlet ini. */
    val isRakApplicable: Boolean
        get() = isSewaDisplay && !classOutlet.equals("Trader", ignoreCase = true)

    /** Menentukan apakah M3 (Price Tag Planogram) berlaku untuk outlet ini. */
    val isPriceTagApplicable: Boolean
        get() = !classOutlet.equals("Trader", ignoreCase = true)
}

/**
 * ViewModel pengelola audit fisik lapangan Member (M1 Geotag, M2 Rak Sewa, M3 Price Tag, M4 Harga GM)
 * dengan redistribusi bobot proporsional (Bab 2) dan Modal Check-Out Dini (Bab 5).
 */
@Stable
class MemberSurveyViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val surveyRepository = SurveyRepository(RetrofitClient.apiService, database.pendingVisitDao())
    private val gson = Gson()

    private val _uiState = MutableStateFlow(MemberSurveyUiState())
    val uiState: StateFlow<MemberSurveyUiState> = _uiState.asStateFlow()

    /**
     * Memuat detail toko langsung dari Room DB, katalog 6 SKU fokus, draft/pending hari ini,
     * serta memicu pengambilan koordinat GPS perangkat jika izin telah aktif.
     *
     * @param outletId ID toko target audit fisik
     */
    fun initSurvey(outletId: Long) {
        viewModelScope.launch {
            val outlet = database.outletDao().getOutletById(outletId)
            val products = database.productDao().getProductsSync()
            val today = todayDateString()
            val now = nowTimestampString()
            val existingVisit = surveyRepository.findTodayVisit(outletId, "member", today)

            val savedRequest = existingVisit?.let {
                runCatching { gson.fromJson(it.payloadJson, MemberSurveyRequest::class.java) }.getOrNull()
            }
            val savedSkuMap = savedRequest?.skuAudits?.associateBy { it.productId } ?: emptyMap()

            val skuItems = products.map { p ->
                val prev = savedSkuMap[p.id]
                if (prev != null && prev.actualPrice > 0.0) {
                    val inRange = prev.actualPrice >= p.minPrice && prev.actualPrice <= p.maxPrice
                    SkuAuditItem(
                        product = p,
                        hasPriceTag = prev.hasPriceTag,
                        isPriceTagCompliant = prev.isPriceTagCompliant,
                        actualPriceInput = prev.actualPrice.toInt().toString(),
                        isPriceInRange = inRange,
                        skuPhotoPath = prev.skuPhotoPath
                    )
                } else {
                    SkuAuditItem(
                        product = p,
                        hasPriceTag = true,
                        isPriceTagCompliant = true,
                        actualPriceInput = "",
                        isPriceInRange = false,
                        skuPhotoPath = prev?.skuPhotoPath
                    )
                }
            }

            val refLat = outlet?.latitude
            val refLng = outlet?.longitude
            val savedLat = savedRequest?.actualLatitude
            val savedLng = savedRequest?.actualLongitude
            val permissionGranted = LocationHelper.hasLocationPermission(getApplication())

            val initialState = MemberSurveyUiState(
                outletId = outletId,
                outletName = outlet?.namaToko ?: "Toko #$outletId",
                existingSyncId = existingVisit?.offlineSyncId,
                checkinAt = existingVisit?.checkinAt ?: now,
                outletLat = refLat,
                outletLng = refLng,
                outletAddress = outlet?.alamat,
                isSewaDisplay = outlet?.isSewaDisplay ?: false,
                classOutlet = outlet?.classOutlet ?: "GT Biasa",
                currentLat = savedLat,
                currentLng = savedLng,
                gpsAddress = null,
                isResolvingAddress = false,
                hasLocationPermission = permissionGranted,
                gpsStatusMessage = if (savedLat != null && savedLng != null) {
                    "Menggunakan koordinat tersimpan. Ketuk 'Perbarui GPS' untuk refresh."
                } else {
                    "Mengambil titik GPS aktual..."
                },
                selectedRakCondition = savedRequest?.rakCondition,
                rakPhotoPath = savedRequest?.rakPhotoPath,
                skuAudits = skuItems,
                showIncompleteModal = false,
                showOutOfRadiusDialog = false,
                isDemoMode = sessionManager.isDemoMode(),
                validationError = null,
                isSaved = false
            )
            _uiState.value = recalculateScores(initialState)

            if (refLat != null && refLng != null && savedLat != null && savedLng != null) {
                calculateGeotag(refLat, refLng, savedLat, savedLng)
                resolveGpsAddress(savedLat, savedLng)
            }

            if (permissionGranted) {
                refreshGpsLocation()
            } else {
                _uiState.value = _uiState.value.copy(
                    gpsStatusMessage = "Izin lokasi belum aktif. Berikan izin GPS untuk validasi geotag."
                )
            }

            // Sinkronkan range harga spesifik outlet jika online (berdasarkan effective_price_code toko)
            refreshOutletProductsPriceRange(outletId)
        }
    }

    private fun refreshOutletProductsPriceRange(outletId: Long) {
        viewModelScope.launch {
            val response = runCatching { RetrofitClient.apiService.getProducts(outletId = outletId) }.getOrNull()
            if (response != null && response.isSuccessful && response.body() != null) {
                val freshProducts = MasterDataRepository.mapFocus6ProductEntities(response.body()!!.data)
                if (freshProducts.isEmpty()) return@launch
                val currentAuditsById = _uiState.value.skuAudits.associateBy { it.product.id }
                val updatedAudits = freshProducts.map { freshProduct ->
                    val existing = currentAuditsById[freshProduct.id]
                    val inputPrice = existing?.actualPriceInput?.toDoubleOrNull()
                    val inRange = inputPrice != null && inputPrice > 0.0 &&
                        inputPrice >= freshProduct.minPrice && inputPrice <= freshProduct.maxPrice
                    existing?.copy(
                        product = freshProduct,
                        isPriceInRange = inRange
                    ) ?: SkuAuditItem(
                        product = freshProduct,
                        hasPriceTag = true,
                        isPriceTagCompliant = true,
                        actualPriceInput = "",
                        isPriceInRange = false,
                        skuPhotoPath = null
                    )
                }
                _uiState.value = recalculateScores(_uiState.value.copy(skuAudits = updatedAudits))
            }
        }
    }

    /**
     * Mengambil titik koordinat GPS perangkat terbaru dan menghitung skor jarak terhadap koordinat master toko.
     */
    fun refreshGpsLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isFetchingGps = true,
                hasLocationPermission = LocationHelper.hasLocationPermission(getApplication()),
                gpsStatusMessage = "Mencari sinyal GPS perangkat..."
            )

            when (val result = LocationHelper.getCurrentLocation(getApplication())) {
                is LocationResult.Success -> {
                    val refLat = _uiState.value.outletLat
                    val refLng = _uiState.value.outletLng
                    val accuracyInt = result.accuracyMeters.toInt()
                    val accuracyInfo = if (result.accuracyMeters <= LocationHelper.MAX_ACCURACY_METERS) {
                        "GPS terkunci (akurasi ±$accuracyInt m)"
                    } else {
                        "GPS terkunci (akurasi ±$accuracyInt m, ideal ≤ 15 m)"
                    }
                    val updated = _uiState.value.copy(
                        currentLat = result.latitude,
                        currentLng = result.longitude,
                        gpsAccuracyMeters = result.accuracyMeters,
                        isFetchingGps = false,
                        hasLocationPermission = true,
                        gpsStatusMessage = if (refLat != null && refLng != null) {
                            accuracyInfo
                        } else {
                            "$accuracyInfo. Koordinat master toko belum tersedia."
                        }
                    )
                    _uiState.value = recalculateScores(updated)
                    if (refLat != null && refLng != null) {
                        calculateGeotag(refLat, refLng, result.latitude, result.longitude)
                    }
                    resolveGpsAddress(result.latitude, result.longitude)
                }
                LocationResult.PermissionMissing -> {
                    _uiState.value = _uiState.value.copy(
                        isFetchingGps = false,
                        hasLocationPermission = false,
                        gpsStatusMessage = "Izin akses GPS diperlukan untuk Kriteria M1."
                    )
                }
                LocationResult.GpsDisabled -> {
                    _uiState.value = _uiState.value.copy(
                        isFetchingGps = false,
                        gpsStatusMessage = "GPS perangkat nonaktif. Aktifkan Lokasi di pengaturan lalu coba lagi."
                    )
                }
                is LocationResult.Unavailable -> {
                    _uiState.value = _uiState.value.copy(
                        isFetchingGps = false,
                        gpsStatusMessage = result.reason
                    )
                }
            }
        }
    }

    private fun calculateGeotag(lat1: Double, lon1: Double, lat2: Double, lon2: Double) {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val dist = earthRadius * c

        val score = when {
            dist <= LocationHelper.MAX_GEOTAG_RADIUS_METERS -> 100
            dist <= 25.0 -> 75
            dist <= 50.0 -> 50
            dist <= 100.0 -> 25
            else -> 0
        }

        _uiState.value = recalculateScores(
            _uiState.value.copy(
                distanceMeters = dist,
                geotagScorePercent = score
            )
        )
    }

    /**
     * Mengambil alamat jalan dari koordinat hasil tangkapan GPS untuk ditampilkan di kartu Geotag.
     */
    private fun resolveGpsAddress(
        latitude: Double,
        longitude: Double,
        fallbackAddress: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isResolvingAddress = true)
            val resolved = LocationHelper.getAddressFromLocation(getApplication(), latitude, longitude)
            _uiState.value = _uiState.value.copy(
                isResolvingAddress = false,
                gpsAddress = resolved ?: fallbackAddress ?: "Alamat tidak tersedia (offline)"
            )
        }
    }

    /**
     * Mensimulasikan posisi GPS tepat di titik koordinat master toko (khusus Mode Demo)
     * agar pengujian skoring Geotag M1 (100%) dapat dilakukan di perangkat fisik dari mana saja.
     */
    fun simulateAtOutletLocation() {
        val refLat = _uiState.value.outletLat ?: return
        val refLng = _uiState.value.outletLng ?: return
        val updated = _uiState.value.copy(
            currentLat = refLat,
            currentLng = refLng,
            gpsAccuracyMeters = 5f,
            gpsStatusMessage = "Simulasi Mode Demo: berada tepat di titik koordinat toko."
        )
        _uiState.value = recalculateScores(updated)
        calculateGeotag(refLat, refLng, refLat, refLng)
        resolveGpsAddress(refLat, refLng, fallbackAddress = _uiState.value.outletAddress)
    }

    /**
     * Memperbarui pilihan kondisi rak sewa display (Kriteria M2).
     *
     * @param condition Kode kondisi rak standar (`compliant`, `items_5_non_compliant`, dll.)
     */
    fun updateRakCondition(condition: String) {
        _uiState.value = recalculateScores(_uiState.value.copy(selectedRakCondition = condition))
    }

    /**
     * Mengompres dan menyimpan path foto bukti rak sewa display ke state.
     *
     * @param rawPhotoPath Path file foto hasil kamera sebelum kompresi
     */
    fun onRakPhotoCaptured(rawPhotoPath: String) {
        viewModelScope.launch {
            val compressedPath = ImageHelper.compressJpegInPlace(rawPhotoPath)
            _uiState.value = _uiState.value.copy(rakPhotoPath = compressedPath)
        }
    }

    /**
     * Mengompres dan menyimpan path foto bukti SKU tertentu ke state.
     *
     * @param productId ID produk sasaran foto
     * @param rawPhotoPath Path file foto hasil kamera sebelum kompresi
     */
    fun onSkuPhotoCaptured(productId: Long, rawPhotoPath: String) {
        viewModelScope.launch {
            val compressedPath = ImageHelper.compressJpegInPlace(rawPhotoPath)
            val updated = _uiState.value.skuAudits.map { item ->
                if (item.product.id == productId) {
                    item.copy(skuPhotoPath = compressedPath)
                } else {
                    item
                }
            }
            _uiState.value = _uiState.value.copy(skuAudits = updated)
        }
    }

    /**
     * Memperbarui harga jual aktual di toko dan memvalidasi apakah berada dalam rentang GM Area.
     *
     * @param productId ID produk target
     * @param priceInput Input teks nominal harga dari surveyor
     */
    fun updateSkuPrice(productId: Long, priceInput: String) {
        val price = priceInput.toDoubleOrNull()
        val updated = _uiState.value.skuAudits.map { item ->
            if (item.product.id == productId) {
                val inRange = price != null && price > 0.0 &&
                    price >= item.product.minPrice && price <= item.product.maxPrice
                item.copy(actualPriceInput = priceInput, isPriceInRange = inRange)
            } else {
                item
            }
        }
        _uiState.value = recalculateScores(_uiState.value.copy(skuAudits = updated))
    }

    /**
     * Mengubah status ketersediaan price tag pada suatu SKU.
     *
     * @param productId ID produk target
     */
    fun toggleHasPriceTag(productId: Long) {
        val updated = _uiState.value.skuAudits.map { item ->
            if (item.product.id == productId) {
                val nextHasTag = !item.hasPriceTag
                item.copy(
                    hasPriceTag = nextHasTag,
                    isPriceTagCompliant = if (nextHasTag) item.isPriceTagCompliant else false
                )
            } else {
                item
            }
        }
        _uiState.value = recalculateScores(_uiState.value.copy(skuAudits = updated))
    }

    /**
     * Mengubah status kepatuhan posisi planogram price tag pada suatu SKU.
     *
     * @param productId ID produk target
     */
    fun togglePriceTagCompliant(productId: Long) {
        val updated = _uiState.value.skuAudits.map { item ->
            if (item.product.id == productId && item.hasPriceTag) {
                item.copy(isPriceTagCompliant = !item.isPriceTagCompliant)
            } else {
                item
            }
        }
        _uiState.value = recalculateScores(_uiState.value.copy(skuAudits = updated))
    }

    /**
     * Membuka Modal Intersepsi Check-Out Dini secara manual (tombol Terkendala di header).
     */
    fun openIncompleteModal() {
        _uiState.value = _uiState.value.copy(showIncompleteModal = true)
    }

    /**
     * Menutup Modal Intersepsi Check-Out Dini agar surveyor dapat melanjutkan audit fisik.
     */
    fun dismissIncompleteModal() {
        _uiState.value = _uiState.value.copy(showIncompleteModal = false)
    }

    /**
     * Menghapus pesan error validasi setelah ditampilkan oleh Snackbar.
     */
    fun clearValidationError() {
        _uiState.value = _uiState.value.copy(validationError = null)
    }

    /**
     * Menghapus pesan informasi sukses setelah ditampilkan oleh Snackbar.
     */
    fun clearInfoMessage() {
        _uiState.value = _uiState.value.copy(infoMessage = null)
    }

    /**
     * Membuka pop up peringatan sebelum memperbarui titik koordinat lokasi toko.
     */
    fun openUpdateOutletPointDialog() {
        _uiState.value = _uiState.value.copy(showUpdateOutletPointDialog = true)
    }

    /**
     * Menutup pop up peringatan pembaruan titik koordinat lokasi toko.
     */
    fun dismissUpdateOutletPointDialog() {
        _uiState.value = _uiState.value.copy(showUpdateOutletPointDialog = false)
    }

    /**
     * Mengambil koordinat GPS aktual posisi pengguna berada saat ini dan menyimpannya
     * sebagai titik lokasi toko terbaru di database lokal maupun state survei M1.
     */
    fun confirmUpdateOutletPoint() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showUpdateOutletPointDialog = false,
                isUpdatingOutletPoint = true,
                gpsStatusMessage = "Mengambil koordinat lokasi Anda untuk titik toko terbaru..."
            )

            when (val result = LocationHelper.getCurrentLocation(getApplication())) {
                is LocationResult.Success -> {
                    val currentState = _uiState.value
                    database.outletDao().updateOutletCoordinates(
                        outletId = currentState.outletId,
                        latitude = result.latitude,
                        longitude = result.longitude
                    )

                    val accuracyInt = result.accuracyMeters.toInt()
                    val updatedState = currentState.copy(
                        outletLat = result.latitude,
                        outletLng = result.longitude,
                        currentLat = result.latitude,
                        currentLng = result.longitude,
                        gpsAccuracyMeters = result.accuracyMeters,
                        isUpdatingOutletPoint = false,
                        isFetchingGps = false,
                        hasLocationPermission = true,
                        gpsStatusMessage = "Titik toko diperbarui ke posisi Anda (akurasi ±$accuracyInt m)",
                        infoMessage = "Titik toko ${currentState.outletName} berhasil diperbarui ke posisi Anda saat ini."
                    )
                    _uiState.value = recalculateScores(updatedState)
                    calculateGeotag(result.latitude, result.longitude, result.latitude, result.longitude)
                    resolveGpsAddress(result.latitude, result.longitude)
                }
                LocationResult.PermissionMissing -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdatingOutletPoint = false,
                        hasLocationPermission = false,
                        validationError = "Izin akses GPS diperlukan untuk memperbarui titik toko."
                    )
                }
                LocationResult.GpsDisabled -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdatingOutletPoint = false,
                        validationError = "GPS perangkat nonaktif. Aktifkan Lokasi di pengaturan lalu coba lagi."
                    )
                }
                is LocationResult.Unavailable -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdatingOutletPoint = false,
                        validationError = result.reason
                    )
                }
            }
        }
    }

    /**
     * Mereset flag navigasi tersimpan agar tidak memicu popBackStack berulang.
     */
    fun consumeSavedEvent() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }

    /**
     * Menutup dialog peringatan radius > 15 meter agar surveyor dapat memperbarui titik GPS.
     */
    fun dismissOutOfRadiusDialog() {
        _uiState.value = _uiState.value.copy(showOutOfRadiusDialog = false)
    }

    /**
     * Mengonfirmasi penyimpanan audit fisik meskipun jarak GPS melebihi radius maksimal 15 meter.
     */
    fun confirmOutOfRadiusCheckout() {
        val state = _uiState.value.copy(showOutOfRadiusDialog = false)
        _uiState.value = state
        performSaveAudit(state)
    }

    /**
     * Memeriksa kelengkapan audit fisik Member saat menekan Check-Out:
     * - Jika GPS belum terkunci, kondisi rak wajib belum dipilih, atau harga SKU masih kosong,
     *   otomatis menampilkan Modal Intersepsi Check-Out Dini (Bab 5).
     * - Jika jarak melebihi radius maksimal 15 meter, menampilkan dialog konfirmasi radius.
     * - Jika lengkap dan dalam radius, menyimpan audit ke Room lokal beserta estimasi skor proporsional.
     */
    fun requestCheckout() {
        val state = _uiState.value
        val gpsReady = state.currentLat != null && state.currentLng != null
        val rakReady = !state.isRakApplicable || !state.selectedRakCondition.isNullOrBlank()
        val allPricesFilled = state.skuAudits.isNotEmpty() && state.skuAudits.all {
            (it.actualPriceInput.toDoubleOrNull() ?: 0.0) > 0.0
        }

        if (!gpsReady || !rakReady || !allPricesFilled) {
            _uiState.value = state.copy(showIncompleteModal = true)
            return
        }

        if (state.distanceMeters != null && !state.isWithinRadius) {
            _uiState.value = state.copy(showOutOfRadiusDialog = true)
            return
        }

        performSaveAudit(state)
    }

    private fun performSaveAudit(state: MemberSurveyUiState) {
        viewModelScope.launch {
            val today = todayDateString()
            val now = nowTimestampString()

            val skuPayloads = state.skuAudits.map { s ->
                MemberSkuAuditDto(
                    productId = s.product.id,
                    hasPriceTag = s.hasPriceTag,
                    isPriceTagCompliant = s.isPriceTagCompliant,
                    actualPrice = s.actualPriceInput.toDoubleOrNull() ?: 0.0,
                    skuPhotoPath = s.skuPhotoPath
                )
            }

            val request = MemberSurveyRequest(
                offlineSyncId = state.existingSyncId ?: UUID.randomUUID().toString(),
                outletId = state.outletId,
                visitDate = today,
                actualLatitude = state.currentLat,
                actualLongitude = state.currentLng,
                checkinAt = state.checkinAt ?: now,
                checkoutAt = now,
                notes = "Audit fisik Member selesai dicatat",
                hasSewaDisplay = state.isSewaDisplay,
                rakCondition = if (state.isRakApplicable) state.selectedRakCondition else null,
                rakPhotoPath = if (state.isRakApplicable) state.rakPhotoPath else null,
                isIncomplete = false,
                needsRevisit = false,
                skuAudits = skuPayloads
            )

            surveyRepository.saveMemberSurveyLocally(
                request = request,
                outletName = state.outletName,
                localScore = state.estimatedMemberScore
            )
            SyncManager.enqueueImmediateSync(getApplication())

            _uiState.value = _uiState.value.copy(
                showOutOfRadiusDialog = false,
                validationError = null,
                isSaved = true
            )
        }
    }

    /**
     * Menyimpan kunjungan Member dengan status Check-Out Dini (`is_incomplete = true`, `needs_revisit = true`)
     * dari Modal Intersepsi Bab 5 ke penyimpanan lokal Room.
     *
     * @param reasonType Kode alasan standar (`pic_absent`, `busy_store`, `store_closed`, dll.)
     * @param reasonText Penjelasan alasan kendala di lapangan
     */
    fun submitIncompleteCheckout(reasonType: String, reasonText: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val today = todayDateString()
            val now = nowTimestampString()

            // Kirim hanya item SKU yang sudah sempat diisi harganya
            val partialSkus = state.skuAudits.mapNotNull { s ->
                val price = s.actualPriceInput.toDoubleOrNull()
                if (price != null && price > 0.0) {
                    MemberSkuAuditDto(
                        productId = s.product.id,
                        hasPriceTag = s.hasPriceTag,
                        isPriceTagCompliant = s.isPriceTagCompliant,
                        actualPrice = price,
                        skuPhotoPath = s.skuPhotoPath
                    )
                } else {
                    null
                }
            }

            val request = MemberSurveyRequest(
                offlineSyncId = state.existingSyncId ?: UUID.randomUUID().toString(),
                outletId = state.outletId,
                visitDate = today,
                actualLatitude = state.currentLat,
                actualLongitude = state.currentLng,
                checkinAt = state.checkinAt ?: now,
                checkoutAt = now,
                notes = reasonText,
                hasSewaDisplay = state.isSewaDisplay,
                rakCondition = if (state.isRakApplicable) state.selectedRakCondition else null,
                rakPhotoPath = if (state.isRakApplicable) state.rakPhotoPath else null,
                isIncomplete = true,
                incompleteReasonType = reasonType,
                incompleteReason = reasonText,
                needsRevisit = true,
                revisitNotes = "Jadwalkan kunjungan susulan untuk melengkapi audit fisik Member",
                skuAudits = partialSkus
            )

            surveyRepository.saveMemberSurveyLocally(
                request = request,
                outletName = state.outletName,
                localScore = 0.0
            )
            SyncManager.enqueueImmediateSync(getApplication())

            _uiState.value = _uiState.value.copy(
                showIncompleteModal = false,
                isSaved = true
            )
        }
    }

    /**
     * Menghitung estimasi skor Member (M1-M4) sesuai rubrik Bab 2A & redistribusi proporsional Bab 2B.
     */
    private fun recalculateScores(state: MemberSurveyUiState): MemberSurveyUiState {
        val rakScore = when (state.selectedRakCondition) {
            "terpasang_rapih", "compliant" -> 100
            "items_5_non_compliant" -> 75
            "items_3_non_compliant" -> 50
            "items_2_non_compliant" -> 25
            "no_items" -> 0
            else -> 0
        }

        val totalSkus = state.skuAudits.size
        val priceTagCompliantCount = state.skuAudits.count { it.hasPriceTag && it.isPriceTagCompliant }
        val priceInRangeCount = state.skuAudits.count { it.isPriceInRange }

        val m3Score = if (totalSkus > 0) (priceTagCompliantCount.toDouble() / totalSkus) * 100.0 else 0.0
        val m4Score = if (totalSkus > 0) (priceInRangeCount.toDouble() / totalSkus) * 100.0 else 0.0

        val m1Weight = 0.25
        val m2Weight = if (state.isRakApplicable) 0.20 else 0.0
        val m3Weight = if (state.isPriceTagApplicable) 0.20 else 0.0
        val m4Weight = 0.20

        val activeWeightSum = m1Weight + m2Weight + m3Weight + m4Weight
        val weightedSum = (state.geotagScorePercent * m1Weight) +
            (rakScore * m2Weight) +
            (m3Score * m3Weight) +
            (m4Score * m4Weight)

        val estimated = if (activeWeightSum > 0.0) weightedSum / activeWeightSum else 0.0

        return state.copy(
            rakScorePercent = rakScore,
            priceTagScorePercent = m3Score,
            priceRangeScorePercent = m4Score,
            estimatedMemberScore = estimated
        )
    }

    private fun todayDateString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun nowTimestampString(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
}
