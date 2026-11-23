package com.aswfoods.fieldtrack.data.repository

import com.aswfoods.fieldtrack.data.local.dao.PendingVisitDao
import com.aswfoods.fieldtrack.data.local.entity.PendingVisitEntity
import com.aswfoods.fieldtrack.data.remote.api.FieldTrackApiService
import com.aswfoods.fieldtrack.data.remote.dto.BatchSyncRequest
import com.aswfoods.fieldtrack.data.remote.dto.LeaderSurveyRequest
import com.aswfoods.fieldtrack.data.remote.dto.MemberSurveyRequest
import com.aswfoods.fieldtrack.data.remote.dto.SurveySubmitResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

/**
 * Repository utama untuk transaksi survey lapangan.
 * Bertanggung jawab terhadap penyimpanan offline-first dan sinkronisasi antrean kunjungan
 * melalui endpoint `/api/v1/survey/batch-sync` serta fallback per kunjungan (`/leader/submit`, `/member/submit`).
 */
class SurveyRepository(
    private val apiService: FieldTrackApiService,
    private val pendingVisitDao: PendingVisitDao,
    private val gson: Gson = Gson()
) {
    /**
     * Memantau seluruh riwayat dan antrean kunjungan di perangkat.
     *
     * @return Flow koleksi antrean kunjungan
     */
    fun getAllVisits(): Flow<List<PendingVisitEntity>> = pendingVisitDao.getAllVisitsFlow()

    /**
     * Memantau jumlah antrean survey yang belum tersinkronisasi.
     *
     * @return Flow angka pending
     */
    fun getPendingCount(): Flow<Int> = pendingVisitDao.countPendingVisits()

    /**
     * Memantau jumlah antrean survey yang gagal sinkronisasi agar pengguna dapat melakukan retry.
     *
     * @return Flow jumlah kunjungan berstatus gagal
     */
    fun getFailedCount(): Flow<Int> = pendingVisitDao.countFailedVisits()

    /**
     * Memantau daftar kunjungan hari ini berdasarkan role untuk memetakan progres di dasbor.
     *
     * @param roleType Peran surveyor ("leader" atau "member")
     * @param visitDate Tanggal kunjungan format YYYY-MM-DD
     * @return Flow daftar kunjungan hari ini
     */
    fun getTodayVisitsByRole(roleType: String, visitDate: String): Flow<List<PendingVisitEntity>> =
        pendingVisitDao.getTodayVisitsByRoleFlow(roleType, visitDate)

    /**
     * Mencari data kunjungan yang sudah tersimpan lokal pada hari yang sama agar tidak terjadi duplikasi.
     *
     * @param outletId ID toko target
     * @param roleType Peran surveyor ("leader" atau "member")
     * @param visitDate Tanggal kunjungan format YYYY-MM-DD
     * @return Entitas kunjungan jika ditemukan atau null
     */
    suspend fun findTodayVisit(outletId: Long, roleType: String, visitDate: String): PendingVisitEntity? =
        pendingVisitDao.findVisitByOutletAndRole(outletId, roleType, visitDate)

    /**
     * Menyimpan hasil kuesioner Leader ke Room lokal terlebih dahulu.
     *
     * @param request Payload survey Leader
     * @param outletName Nama toko untuk keperluan tampilan UI
     * @param localScore Estimasi skor lokal Leader jika tersedia
     */
    suspend fun saveLeaderSurveyLocally(
        request: LeaderSurveyRequest,
        outletName: String,
        localScore: Double? = null
    ) {
        val revisitReasonText = request.picAbsenceReason ?: request.incompleteReason
        val entity = PendingVisitEntity(
            offlineSyncId = request.offlineSyncId,
            outletId = request.outletId,
            outletName = outletName,
            roleType = "leader",
            visitDate = request.visitDate,
            checkinLatitude = request.checkinLatitude,
            checkinLongitude = request.checkinLongitude,
            checkinAt = request.checkinAt,
            checkoutAt = request.checkoutAt,
            payloadJson = gson.toJson(request),
            syncStatus = PendingVisitEntity.SYNC_STATUS_PENDING,
            isIncomplete = request.isIncomplete,
            needsRevisit = request.needsRevisit,
            revisitReason = revisitReasonText,
            localScore = localScore
        )
        pendingVisitDao.insertOrUpdate(entity)
    }

    /**
     * Menyimpan hasil audit fisik Member ke Room lokal terlebih dahulu.
     *
     * @param request Payload audit Member
     * @param outletName Nama toko untuk keperluan tampilan UI
     * @param localScore Estimasi skor proporsional Member (M1-M4)
     */
    suspend fun saveMemberSurveyLocally(
        request: MemberSurveyRequest,
        outletName: String,
        localScore: Double? = null
    ) {
        val entity = PendingVisitEntity(
            offlineSyncId = request.offlineSyncId,
            outletId = request.outletId,
            outletName = outletName,
            roleType = "member",
            visitDate = request.visitDate,
            checkinLatitude = request.actualLatitude,
            checkinLongitude = request.actualLongitude,
            checkinAt = request.checkinAt,
            checkoutAt = request.checkoutAt,
            payloadJson = gson.toJson(request),
            syncStatus = PendingVisitEntity.SYNC_STATUS_PENDING,
            isIncomplete = request.isIncomplete,
            needsRevisit = request.needsRevisit,
            revisitReason = request.incompleteReason,
            localScore = localScore
        )
        pendingVisitDao.insertOrUpdate(entity)
    }

    /**
     * Mengunggah foto bukti lokal lalu mengirim seluruh antrean survey pending melalui
     * `/api/v1/survey/batch-sync` secara idempotent (via `offline_sync_id`), dengan fallback
     * pengiriman per kunjungan apabila endpoint batch tidak tersedia atau terdapat validasi 4xx.
     *
     * @return Result jumlah kunjungan yang berhasil disinkronkan ke server
     */
    suspend fun pushPendingVisits(): Result<Int> {
        return try {
            val pendingList = pendingVisitDao.getPendingVisitsToSync()
            if (pendingList.isEmpty()) {
                return Result.success(0)
            }

            val leaderVisits = mutableListOf<LeaderSurveyRequest>()
            val memberVisits = mutableListOf<MemberSurveyRequest>()

            for (item in pendingList) {
                when (item.roleType) {
                    "leader" -> {
                        val req = runCatching {
                            gson.fromJson(item.payloadJson, LeaderSurveyRequest::class.java)
                        }.getOrNull()
                        if (req != null) {
                            leaderVisits.add(req)
                        } else {
                            pendingVisitDao.markAsFailed(item.offlineSyncId, "Format payload Leader tidak valid")
                        }
                    }
                    "member" -> {
                        val rawReq = runCatching {
                            gson.fromJson(item.payloadJson, MemberSurveyRequest::class.java)
                        }.getOrNull()
                        if (rawReq != null) {
                            val syncedReq = uploadPendingMemberPhotos(item, rawReq)
                            memberVisits.add(syncedReq)
                        } else {
                            pendingVisitDao.markAsFailed(item.offlineSyncId, "Format payload Member tidak valid")
                        }
                    }
                }
            }

            if (leaderVisits.isEmpty() && memberVisits.isEmpty()) {
                return Result.success(0)
            }

            val batchResponse = apiService.batchSync(
                BatchSyncRequest(
                    leaderVisits = leaderVisits,
                    memberVisits = memberVisits
                )
            )

            when {
                batchResponse.isSuccessful && batchResponse.body() != null -> {
                    val batchBody = batchResponse.body()!!
                    val syncedIds = buildSet {
                        batchBody.data?.syncedLeaderSyncIds?.let { addAll(it) }
                        batchBody.data?.syncedMemberSyncIds?.let { addAll(it) }
                        batchBody.results
                            ?.filter { it.status.equals("synced", ignoreCase = true) || it.status.isNullOrBlank() }
                            ?.forEach { add(it.offlineSyncId) }
                    }
                    syncedIds.forEach { syncId ->
                        pendingVisitDao.markAsSynced(syncId)
                    }
                    Result.success(syncedIds.size)
                }
                batchResponse.code() in 400..499 -> {
                    // Fallback per item untuk mengisolasi kunjungan 4xx sementara item valid tetap terkirim
                    pushPendingIndividually(leaderVisits, memberVisits)
                }
                else -> {
                    Result.failure(
                        Exception("Server error HTTP ${batchResponse.code()}: ${batchResponse.message()}")
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mengirim antrean survey Leader dan Member satu per satu sebagai fallback dari batch-sync.
     */
    private suspend fun pushPendingIndividually(
        leaderVisits: List<LeaderSurveyRequest>,
        memberVisits: List<MemberSurveyRequest>
    ): Result<Int> {
        var syncedCount = 0
        val submissions: List<Pair<String, suspend () -> Response<SurveySubmitResponse>>> =
            leaderVisits.map { req -> req.offlineSyncId to suspend { apiService.submitLeaderSurvey(req) } } +
                memberVisits.map { req -> req.offlineSyncId to suspend { apiService.submitMemberSurvey(req) } }

        for ((syncId, submitCall) in submissions) {
            val response = submitCall()
            when {
                response.isSuccessful -> {
                    pendingVisitDao.markAsSynced(syncId)
                    syncedCount++
                }
                response.code() in 400..499 -> {
                    val errMsg = "HTTP ${response.code()}: ${response.message().ifBlank { "Data ditolak server" }}"
                    pendingVisitDao.markAsFailed(syncId, errMsg)
                }
                else -> {
                    return Result.failure(
                        Exception("Server error HTTP ${response.code()}: ${response.message()}")
                    )
                }
            }
        }

        return Result.success(syncedCount)
    }

    /**
     * Mengubah seluruh kunjungan berstatus gagal menjadi pending lalu mencoba mengirimkannya kembali ke server.
     *
     * @return Result jumlah kunjungan yang berhasil disinkronkan
     */
    suspend fun retryFailedVisits(): Result<Int> {
        pendingVisitDao.resetFailedToPending()
        return pushPendingVisits()
    }

    /**
     * Mengunggah seluruh berkas foto lokal pada payload Member sebelum dikirim ke server,
     * lalu memperbarui payloadJson lokal agar foto yang sudah terunggah tidak diulang saat retry.
     */
    private suspend fun uploadPendingMemberPhotos(
        entity: PendingVisitEntity,
        request: MemberSurveyRequest
    ): MemberSurveyRequest {
        var changed = false

        val uploadedRakPath = uploadLocalPhotoIfNeeded(request.rakPhotoPath, "rak")
        if (uploadedRakPath != request.rakPhotoPath) {
            changed = true
        }

        val updatedSkus = request.skuAudits.map { sku ->
            val uploadedSkuPath = uploadLocalPhotoIfNeeded(sku.skuPhotoPath, "sku")
            if (uploadedSkuPath != sku.skuPhotoPath) {
                changed = true
            }
            sku.copy(skuPhotoPath = uploadedSkuPath)
        }

        val updatedRequest = request.copy(
            rakPhotoPath = uploadedRakPath,
            skuAudits = updatedSkus
        )

        if (changed) {
            pendingVisitDao.insertOrUpdate(
                entity.copy(payloadJson = gson.toJson(updatedRequest))
            )
        }

        return updatedRequest
    }

    private suspend fun uploadLocalPhotoIfNeeded(pathOrRemote: String?, category: String): String? {
        if (pathOrRemote.isNullOrBlank()) return null
        val localFile = File(pathOrRemote)
        if (!localFile.exists()) {
            // Sudah berupa path server atau bukan file lokal
            return pathOrRemote
        }

        val requestFile = localFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val photoPart = MultipartBody.Part.createFormData("photo", localFile.name, requestFile)
        val categoryBody = category.toRequestBody("text/plain".toMediaTypeOrNull())

        val uploadResponse = apiService.uploadPhoto(photoPart, categoryBody)
        if (uploadResponse.isSuccessful && uploadResponse.body() != null) {
            return uploadResponse.body()!!.data.filePath
        } else {
            throw IllegalStateException("Gagal mengunggah foto ($category): HTTP ${uploadResponse.code()}")
        }
    }
}
