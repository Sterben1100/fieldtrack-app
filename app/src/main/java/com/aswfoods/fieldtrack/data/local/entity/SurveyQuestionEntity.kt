package com.aswfoods.fieldtrack.data.local.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas Room untuk menyimpan master daftar kuesioner survei dari server
 * agar teks pertanyaan baku tetap tersedia saat perangkat bekerja offline.
 *
 * @property id ID unik butir pertanyaan dari server
 * @property role Peran target pertanyaan ("leader" atau "member")
 * @property questionCode Kode pertanyaan (misal: "L1".."L12")
 * @property aspect Aspek audit pertanyaan (misal: "Kunjungan Sales", "Pengenalan SPV")
 * @property questionText Teks pertanyaan baku yang dibacakan surveyor
 * @property answerType Tipe jawaban ("yes_no_na", "rating", "photo_choice", dll.)
 * @property scoringGuide Penjelasan aturan penskoran pertanyaan
 * @property weight Bobot pertanyaan pada rubrik penilaian
 * @property isActive Penanda apakah pertanyaan masih aktif digunakan
 */
@Immutable
@Entity(tableName = "survey_questions")
data class SurveyQuestionEntity(
    @PrimaryKey
    val id: Long,
    val role: String,
    val questionCode: String,
    val aspect: String,
    val questionText: String,
    val answerType: String,
    val scoringGuide: String?,
    val weight: Double,
    val isActive: Boolean
)
