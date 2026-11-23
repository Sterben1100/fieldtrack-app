package com.aswfoods.fieldtrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aswfoods.fieldtrack.data.local.entity.PendingVisitEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO untuk mengelola antrean pengiriman offline (Pending Visits Queue).
 * Menjadi jantung dari mekanisme offline-first synchronization pada FieldTrack App.
 */
@Dao
interface PendingVisitDao {
    /**
     * Memantau seluruh antrean kunjungan secara real-time untuk tampilan dasbor surveyor.
     *
     * @return Flow daftar antrean kunjungan
     */
    @Query("SELECT * FROM pending_visits ORDER BY createdAt DESC")
    fun getAllVisitsFlow(): Flow<List<PendingVisitEntity>>

    /**
     * Mengambil daftar kunjungan yang masih berstatus PENDING untuk dieksekusi oleh SyncWorker.
     *
     * @return List kunjungan yang belum terkirim ke server
     */
    @Query("SELECT * FROM pending_visits WHERE syncStatus = 0 ORDER BY createdAt ASC")
    suspend fun getPendingVisitsToSync(): List<PendingVisitEntity>

    /**
     * Memantau daftar kunjungan hari ini berdasarkan role surveyor untuk menampilkan status tiap toko di dasbor.
     *
     * @param roleType Peran surveyor ("leader" atau "member")
     * @param visitDate Tanggal kunjungan format YYYY-MM-DD
     * @return Flow daftar kunjungan hari ini untuk role terkait
     */
    @Query("SELECT * FROM pending_visits WHERE roleType = :roleType AND visitDate = :visitDate ORDER BY createdAt DESC")
    fun getTodayVisitsByRoleFlow(roleType: String, visitDate: String): Flow<List<PendingVisitEntity>>

    /**
     * Menghitung jumlah antrean survey yang belum tersinkronisasi untuk badge indikator dasbor.
     *
     * @return Flow jumlah antrean pending
     */
    @Query("SELECT COUNT(*) FROM pending_visits WHERE syncStatus = 0")
    fun countPendingVisits(): Flow<Int>

    /**
     * Menghitung jumlah antrean survey yang gagal sinkronisasi agar surveyor dapat melakukan pengiriman ulang.
     *
     * @return Flow jumlah kunjungan berstatus gagal
     */
    @Query("SELECT COUNT(*) FROM pending_visits WHERE syncStatus = 2")
    fun countFailedVisits(): Flow<Int>

    /**
     * Mengembalikan status kunjungan yang gagal menjadi pending agar diproses ulang oleh mekanisme sync.
     *
     * @return Jumlah baris yang diperbarui
     */
    @Query("UPDATE pending_visits SET syncStatus = 0, errorMessage = NULL WHERE syncStatus = 2")
    suspend fun resetFailedToPending(): Int

    /**
     * Memeriksa apakah toko tertentu sudah disurvei oleh role tertentu hari ini.
     *
     * @param outletId ID toko
     * @param roleType "leader" atau "member"
     * @param visitDate Tanggal survey (YYYY-MM-DD)
     * @return Entitas kunjungan jika sudah ada, atau null jika belum disurvei
     */
    @Query("SELECT * FROM pending_visits WHERE outletId = :outletId AND roleType = :roleType AND visitDate = :visitDate LIMIT 1")
    suspend fun findVisitByOutletAndRole(outletId: Long, roleType: String, visitDate: String): PendingVisitEntity?

    /**
     * Menyimpan atau memperbarui data survey lokal (Draft / Selesai Offline).
     *
     * @param visit Entitas kunjungan
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(visit: PendingVisitEntity)

    /**
     * Memperbarui status menjadi TERSINKRON setelah server merespons sukses.
     *
     * @param syncId UUID kunjungan
     */
    @Query("UPDATE pending_visits SET syncStatus = 1, errorMessage = NULL WHERE offlineSyncId = :syncId")
    suspend fun markAsSynced(syncId: String)

    /**
     * Memperbarui status menjadi GAGAL dan mencatat pesan galat dari backend.
     *
     * @param syncId UUID kunjungan
     * @param error Pesan galat validasi
     */
    @Query("UPDATE pending_visits SET syncStatus = 2, errorMessage = :error WHERE offlineSyncId = :syncId")
    suspend fun markAsFailed(syncId: String, error: String)
}

