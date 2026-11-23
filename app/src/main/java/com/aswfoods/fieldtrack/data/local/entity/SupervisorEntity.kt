package com.aswfoods.fieldtrack.data.local.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas Room untuk menyimpan daftar foto supervisor cabang.
 * Digunakan untuk pengujian blind test pengenalan foto SPV (Kuesioner Leader L12).
 *
 * @property id ID unik supervisor
 * @property salesCode Kode Sales resmi supervisor (mis. SPVKEPRI1)
 * @property nip Nomor Induk Pegawai / alias kode SPV untuk kompatibilitas mundur
 * @property name Nama lengkap supervisor sales
 * @property areaName Nama area pengawasan supervisor
 * @property coverageDistributors Daftar distributor/cabang binaan supervisor
 * @property photoUrl URL atau path berkas foto wajah SPV
 * @property isTarget Penanda apakah SPV ini adalah SPV resmi cabang target uji buta L12
 */
@Immutable
@Entity(tableName = "supervisors")
data class SupervisorEntity(
    @PrimaryKey
    val id: Long,
    val salesCode: String = "",
    val nip: String = "",
    val name: String,
    val areaName: String? = null,
    val coverageDistributors: String? = null,
    val photoUrl: String,
    val isTarget: Boolean = false
)

