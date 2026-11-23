package com.aswfoods.fieldtrack.data.local.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entitas Room utama untuk menyimpan antrean offline seluruh hasil survey Leader dan Member.
 * Data disimpan secara lokal di SQLite perangkat terlebih dahulu sebelum dikirimkan ke server.
 *
 * @property offlineSyncId UUID unik perangkat untuk idempotency sinkronisasi REST API
 * @property outletId ID toko sasaran survey
 * @property outletName Nama toko (untuk kemudahan display UI offline)
 * @property roleType Peran survey: "leader" atau "member"
 * @property visitDate Tanggal pelaksanaan survey (format YYYY-MM-DD)
 * @property checkinLatitude Koordinat garis lintang check-in surveyor
 * @property checkinLongitude Koordinat garis bujur check-in surveyor
 * @property checkinAt Waktu mulai survey
 * @property checkoutAt Waktu selesai survey
 * @property payloadJson Serialisasi JSON lengkap dari kuesioner L1-L12 atau audit fisik M1-M4
 * @property syncStatus Status pengiriman: 0 (Pending), 1 (Tersinkron), 2 (Gagal/Error)
 * @property errorMessage Pesan galat dari server jika pengiriman mengalami kendala validasi
 * @property isIncomplete Penanda apakah check-out dilakukan secara dini saat survei belum lengkap
 * @property needsRevisit Penanda apakah toko perlu kunjungan ulang (Revisit L12 atau Check-Out Dini)
 * @property revisitReason Alasan penandaan revisit (misal alasan ketidakhadiran PIC)
 * @property localScore Estimasi skor lokal yang dihitung di perangkat sebelum disinkronkan
 * @property createdAt Timestamp pencatatan lokal di perangkat
 */
@Immutable
@Entity(tableName = "pending_visits")
data class PendingVisitEntity(
    @PrimaryKey
    val offlineSyncId: String = UUID.randomUUID().toString(),
    val outletId: Long,
    val outletName: String,
    val roleType: String, // "leader" atau "member"
    val visitDate: String,
    val checkinLatitude: Double?,
    val checkinLongitude: Double?,
    val checkinAt: String?,
    val checkoutAt: String?,
    val payloadJson: String,
    val syncStatus: Int = SYNC_STATUS_PENDING,
    val errorMessage: String? = null,
    val isIncomplete: Boolean = false,
    val needsRevisit: Boolean = false,
    val revisitReason: String? = null,
    val localScore: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val SYNC_STATUS_PENDING = 0
        const val SYNC_STATUS_SUCCESS = 1
        const val SYNC_STATUS_FAILED = 2
    }
}
