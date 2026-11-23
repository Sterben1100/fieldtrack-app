package com.aswfoods.fieldtrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aswfoods.fieldtrack.data.local.entity.OutletEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) untuk pengelolaan data toko/outlet di SQLite lokal.
 */
@Dao
interface OutletDao {
    /**
     * Mengambil seluruh daftar toko jadwal harian secara reaktif terurut berdasarkan stopOrder rute.
     *
     * @return Flow koleksi seluruh toko terdaftar
     */
    @Query("SELECT * FROM outlets ORDER BY stopOrder ASC, namaToko ASC")
    fun getAllOutlets(): Flow<List<OutletEntity>>

    /**
     * Mengambil detail satu toko berdasarkan ID.
     *
     * @param outletId ID toko target
     * @return Entitas toko atau null jika tidak ditemukan
     */
    @Query("SELECT * FROM outlets WHERE id = :outletId LIMIT 1")
    suspend fun getOutletById(outletId: Long): OutletEntity?

    /**
     * Menyimpan atau memperbarui daftar toko hasil unduh dari API rute harian.
     *
     * @param outlets Daftar entitas toko yang akan disimpan
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(outlets: List<OutletEntity>)

    /**
     * Memperbarui koordinat titik lokasi (LA/LG) master toko secara lokal
     * saat surveyor memperbarui titik toko dari lokasi aktual perangkat.
     *
     * @param outletId ID toko target
     * @param latitude Garis lintang (LA) terbaru
     * @param longitude Garis bujur (LG) terbaru
     */
    @Query("UPDATE outlets SET latitude = :latitude, longitude = :longitude, isCoordinateCalibrated = 1 WHERE id = :outletId")
    suspend fun updateOutletCoordinates(outletId: Long, latitude: Double, longitude: Double)

    /**
     * Mengambil daftar toko yang koordinatnya telah diperbarui secara lokal oleh surveyor
     * agar tidak tertimpa kembali saat sinkronisasi ulang rute harian.
     */
    @Query("SELECT * FROM outlets WHERE isCoordinateCalibrated = 1")
    suspend fun getCalibratedOutlets(): List<OutletEntity>

    /**
     * Menghapus seluruh data toko lokal sebelum memuat rute harian yang baru.
     */
    @Query("DELETE FROM outlets")
    suspend fun clearAll()
}
