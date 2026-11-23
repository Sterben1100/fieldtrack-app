package com.aswfoods.fieldtrack.data.local.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas Room untuk menyimpan salinan lokal daftar toko jadwal kunjungan harian surveyor
 * beserta status kolaborasi tim (Leader/Member), skor gabungan, dan indikator revisit.
 *
 * @property id ID toko unik dari sistem
 * @property branchId ID cabang pengelola
 * @property kodeToko Kode pelanggan unik dari Eskalink
 * @property namaToko Nama resmi toko terdaftar
 * @property alamat Alamat fisik lokasi toko
 * @property latitude Garis lintang (LA) referensi database
 * @property longitude Garis bujur (LG) referensi database
 * @property classOutlet Klasifikasi outlet (RWO, Pareto, Trader, GT Biasa, Grooming, dll.)
 * @property priceCode Kode harga acuan GM Area untuk outlet (mis. AHRTMDN, AHCOMDN)
 * @property isSewaDisplay Apakah toko memiliki kontrak sewa rak display ASW Foods
 * @property ownerName Nama pemilik / penanggung jawab toko
 * @property contactPhone Nomor telepon kontak toko
 * @property stopOrder Nomor urut pemberhentian pada rute harian (1..15)
 * @property outletType Jenis penugasan outlet ("wajib" atau "tambahan")
 * @property isVisitedByMe Status apakah sudah dikunjungi oleh surveyor pengguna aktif di server
 * @property isVisitedLeader Status apakah Surveyor Leader sudah menyelesaikan survei toko ini
 * @property isVisitedMember Status apakah Surveyor Member sudah menyelesaikan audit toko ini
 * @property isSurveyComplete Status apakah survei tim (Leader + Member) sudah lengkap dan skor dikunci
 * @property finalCombinedScore Skor gabungan final (85% Member + 15% L12 Leader), bernilai null jika parsial
 * @property needsRevisit Penanda apakah toko perlu kunjungan ulang (Revisit L12 atau Check-Out Dini)
 * @property revisitReason Alasan mengapa toko ditandai perlu revisit
 * @property partnerName Nama rekan satu pasangan tim surveyor pada rute ini
 * @property isCoordinateCalibrated Penanda apakah titik koordinat toko telah diperbarui surveyor di lapangan
 * @property isPareto Penanda apakah outlet berstatus Pareto pada master database
 * @property routePlanId ID rencana rute harian dari server
 * @property hasSurveyedBefore Penanda apakah outlet pernah disurvei pada kunjungan sebelumnya
 * @property lastSurveyDate Tanggal survei terakhir format terformat / ISO
 * @property lastSurveyScore Skor survei terakhir sebelum sesi ini
 */
@Immutable
@Entity(tableName = "outlets")
data class OutletEntity(
    @PrimaryKey
    val id: Long,
    val branchId: Long,
    val kodeToko: String,
    val namaToko: String,
    val alamat: String,
    val latitude: Double?,
    val longitude: Double?,
    val classOutlet: String,
    val priceCode: String = "AHRTMDN",
    val isSewaDisplay: Boolean,
    val ownerName: String?,
    val contactPhone: String?,
    val stopOrder: Int = 0,
    val outletType: String = "wajib",
    val isVisitedByMe: Boolean = false,
    val isVisitedLeader: Boolean = false,
    val isVisitedMember: Boolean = false,
    val isSurveyComplete: Boolean = false,
    val finalCombinedScore: Double? = null,
    val needsRevisit: Boolean = false,
    val revisitReason: String? = null,
    val partnerName: String? = null,
    val isCoordinateCalibrated: Boolean = false,
    val isPareto: Boolean = false,
    val routePlanId: Long? = null,
    val hasSurveyedBefore: Boolean = false,
    val lastSurveyDate: String? = null,
    val lastSurveyScore: Double? = null
)
