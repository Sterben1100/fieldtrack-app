package com.aswfoods.fieldtrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aswfoods.fieldtrack.data.local.entity.SurveyQuestionEntity

/**
 * DAO untuk mengelola daftar master kuesioner survei di database lokal Room.
 */
@Dao
interface SurveyQuestionDao {
    /**
     * Mengambil daftar pertanyaan aktif berdasarkan role surveyor.
     *
     * @param role Peran target ("leader" atau "member")
     * @return List entitas pertanyaan aktif
     */
    @Query("SELECT * FROM survey_questions WHERE role = :role AND isActive = 1 ORDER BY id ASC")
    suspend fun getQuestionsByRole(role: String): List<SurveyQuestionEntity>

    /**
     * Menyimpan daftar pertanyaan hasil unduh dari server.
     *
     * @param questions Daftar entitas pertanyaan survei
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<SurveyQuestionEntity>)

    /**
     * Menghapus cache pertanyaan berdasarkan role sebelum menyimpan daftar terbaru.
     *
     * @param role Peran target yang akan diperbarui
     */
    @Query("DELETE FROM survey_questions WHERE role = :role")
    suspend fun clearByRole(role: String)
}
