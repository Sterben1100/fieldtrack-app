package com.aswfoods.fieldtrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aswfoods.fieldtrack.data.local.entity.SupervisorEntity

/**
 * DAO untuk manajemen daftar foto SPV cabang pada kuesioner L12.
 */
@Dao
interface SupervisorDao {
    /**
     * Mengambil seluruh data supervisor cabang yang tersimpan lokal.
     *
     * @return List data supervisor
     */
    @Query("SELECT * FROM supervisors ORDER BY name ASC")
    suspend fun getAllSupervisors(): List<SupervisorEntity>

    /**
     * Menyimpan data supervisor hasil sinkronisasi API.
     *
     * @param supervisors Daftar entitas supervisor
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(supervisors: List<SupervisorEntity>)

    /**
     * Mengosongkan cache supervisor lokal.
     */
    @Query("DELETE FROM supervisors")
    suspend fun clearAll()
}

