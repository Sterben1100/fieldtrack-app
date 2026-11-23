package com.aswfoods.fieldtrack.ui.screens.dashboard

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aswfoods.fieldtrack.data.local.SessionManager
import com.aswfoods.fieldtrack.data.local.database.AppDatabase
import com.aswfoods.fieldtrack.data.local.entity.OutletEntity
import com.aswfoods.fieldtrack.data.local.entity.PendingVisitEntity
import com.aswfoods.fieldtrack.data.remote.RetrofitClient
import com.aswfoods.fieldtrack.data.repository.MasterDataRepository
import com.aswfoods.fieldtrack.data.repository.SurveyRepository
import com.aswfoods.fieldtrack.worker.SyncManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Status operasional kunjungan untuk setiap outlet pada jadwal harian.
 */
enum class OutletVisitStatus(val label: String) {
    BELUM_DISURVEI("Belum Disurvei"),
    PENDING("Menunggu Sync"),
    TERSINKRON("Tersinkron"),
    PERLU_REVISIT("Perlu Revisit"),
    GAGAL("Gagal Sync")
}

/**
 * Filter daftar outlet di dasbor agar surveyor cepat menemukan toko yang tersisa.
 */
enum class DashboardFilter(val label: String) {
    SEMUA("Semua"),
    BELUM("Belum"),
    PENDING("Pending"),
    SELESAI("Tersinkron")
}

/**
 * Model gabungan data toko dengan status survei hari ini untuk tampilan kartu dasbor.
 *
 * @property outlet Entitas toko dari database lokal
 * @property visitStatus Status kunjungan hari ini pada role aktif
 * @property errorMessage Pesan error sinkronisasi jika status GAGAL
 */
@Immutable
data class OutletRouteItem(
    val outlet: OutletEntity,
    val visitStatus: OutletVisitStatus,
    val needsRevisit: Boolean = false,
    val revisitReason: String? = null,
    val errorMessage: String? = null
)

/**
 * State UI untuk layar Dashboard utama surveyor.
 */
@Immutable
data class DashboardUiState(
    val isSyncing: Boolean = false,
    val isDemoMode: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val selectedFilter: DashboardFilter = DashboardFilter.SEMUA
)

/**
 * ViewModel pengelola daftar rute toko harian, progres survei, dan sinkronisasi offline-first.
 */
@Stable
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val database = AppDatabase.getDatabase(application)
    private var lastSyncedDemoMode: Boolean? = null
    private val masterRepository = MasterDataRepository(
        RetrofitClient.apiService,
        database.outletDao(),
        database.productDao(),
        database.supervisorDao(),
        application,
        database.surveyQuestionDao()
    )
    private val surveyRepository = SurveyRepository(
        RetrofitClient.apiService,
        database.pendingVisitDao()
    )

    private val activeRole = MutableStateFlow("member")

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val todayVisitsFlow = activeRole.flatMapLatest { role ->
        surveyRepository.getTodayVisitsByRole(role.lowercase(), todayDateString())
    }

    // Gabungan daftar toko dan status kunjungan hari ini secara reaktif per role
    val outletItems: StateFlow<List<OutletRouteItem>> = combine(
        masterRepository.getOutlets(),
        todayVisitsFlow,
        activeRole
    ) { outletList, visitList, role ->
        val visitMap = visitList.associateBy { it.outletId }
        outletList.map { outlet ->
            val visit = visitMap[outlet.id]
            val visitedOnServerByRole = outlet.isVisitedByMe ||
                (role == "leader" && outlet.isVisitedLeader) ||
                (role == "member" && outlet.isVisitedMember)

            val status = when {
                visit?.syncStatus == PendingVisitEntity.SYNC_STATUS_FAILED -> OutletVisitStatus.GAGAL
                visit?.syncStatus == PendingVisitEntity.SYNC_STATUS_PENDING -> OutletVisitStatus.PENDING
                visit?.syncStatus == PendingVisitEntity.SYNC_STATUS_SUCCESS &&
                    (visit.needsRevisit || visit.isIncomplete) -> OutletVisitStatus.PERLU_REVISIT
                visit?.syncStatus == PendingVisitEntity.SYNC_STATUS_SUCCESS -> OutletVisitStatus.TERSINKRON
                visitedOnServerByRole -> OutletVisitStatus.TERSINKRON
                outlet.needsRevisit -> OutletVisitStatus.PERLU_REVISIT
                else -> OutletVisitStatus.BELUM_DISURVEI
            }
            val effectiveRevisit = (visit?.needsRevisit == true || visit?.isIncomplete == true) ||
                (outlet.needsRevisit && !visitedOnServerByRole)
            val effectiveRevisitReason = visit?.revisitReason ?: outlet.revisitReason
            OutletRouteItem(
                outlet = outlet,
                visitStatus = status,
                needsRevisit = effectiveRevisit,
                revisitReason = effectiveRevisitReason,
                errorMessage = visit?.errorMessage
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flow jumlah antrean survey pending
    val pendingCount: StateFlow<Int> = surveyRepository.getPendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Flow jumlah antrean survey yang gagal validasi server
    val failedCount: StateFlow<Int> = surveyRepository.getFailedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * Mengatur peran surveyor aktif ("leader" atau "member") agar status kunjungan akurat per role,
     * sekaligus memuat master data awal jika cache toko lokal masih kosong setelah login.
     *
     * @param role Nama peran pengguna yang sedang login
     */
    fun setActiveRole(role: String) {
        val normalized = role.lowercase()
        if (activeRole.value != normalized) {
            activeRole.value = normalized
        }
        val demoActive = sessionManager.isDemoMode()
        val modeChanged = lastSyncedDemoMode != null && lastSyncedDemoMode != demoActive
        lastSyncedDemoMode = demoActive
        if (_uiState.value.isDemoMode != demoActive) {
            _uiState.value = _uiState.value.copy(isDemoMode = demoActive)
        }
        viewModelScope.launch {
            val cachedOutlets = masterRepository.getOutlets().first()
            if ((cachedOutlets.isEmpty() || modeChanged) && !_uiState.value.isSyncing) {
                syncMasterRoutes()
            }
        }
    }

    /**
     * Mengubah filter daftar outlet di layar dasbor.
     *
     * @param filter Pilihan filter status kunjungan
     */
    fun setFilter(filter: DashboardFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    /**
     * Mengunduh pembaruan rute toko harian, katalog produk, dan data SPV dari API master.
     */
    fun syncMasterRoutes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSyncing = true,
                message = null,
                isError = false
            )
            val result = masterRepository.syncMasterData()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    message = "Jadwal rute harian dan master data berhasil diperbarui.",
                    isError = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    message = "Gagal mengunduh master terbaru. Menggunakan data offline lokal.",
                    isError = true
                )
            }
        }
    }

    /**
     * Mendorong seluruh antrean survey pending ke server sekarang juga.
     */
    fun pushPendingDataNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSyncing = true,
                message = null,
                isError = false
            )
            val result = surveyRepository.pushPendingVisits()
            if (result.isSuccess) {
                val total = result.getOrDefault(0)
                val info = if (total > 0) {
                    "$total data survei berhasil disinkronkan ke server."
                } else {
                    "Tidak ada antrean baru yang perlu dikirim."
                }
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    message = info,
                    isError = false
                )
            } else {
                // Jadwalkan retry otomatis melalui WorkManager ketika koneksi kembali stabil
                SyncManager.enqueueImmediateSync(getApplication())
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    message = "Gagal terhubung ke server. Data aman di antrean lokal dan akan dikirim otomatis.",
                    isError = true
                )
            }
        }
    }

    /**
     * Mengirimkan ulang kunjungan yang sebelumnya ditandai gagal sinkronisasi.
     */
    fun retryFailedData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSyncing = true,
                message = null,
                isError = false
            )
            val result = surveyRepository.retryFailedVisits()
            if (result.isSuccess) {
                val total = result.getOrDefault(0)
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    message = "$total data survei berhasil dikirim ulang.",
                    isError = false
                )
            } else {
                SyncManager.enqueueImmediateSync(getApplication())
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    message = "Pengiriman ulang belum berhasil. Antrean dijadwalkan otomatis.",
                    isError = true
                )
            }
        }
    }

    /**
     * Membersihkan pesan notifikasi setelah ditampilkan oleh Snackbar.
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    private fun todayDateString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}
