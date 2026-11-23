package com.aswfoods.fieldtrack.data.local.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas Room untuk menyimpan katalog 6 SKU fokus audit (Top 5 + 1 NPL ASW Foods)
 * beserta batas rentang harga jual wajar konsumen yang disetujui GM Area.
 *
 * @property id ID unik produk
 * @property skuCode Kode SKU produk (misal: SKU-HTR-01)
 * @property skuName Nama produk (misal: Hatari See Hong Puff 260g)
 * @property category Kategori lini biskuit ASW Foods
 * @property isTop5 Menandakan produk termasuk dalam 5 produk fokus utama
 * @property isNpl Menandakan produk baru diluncurkan (New Product Launch)
 * @property planogramImageUrl URL/path panduan visual penataan rak resmi
 * @property minPrice Batas bawah harga jual wajar GM Area
 * @property maxPrice Batas atas harga jual wajar GM Area
 */
@Immutable
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: Long,
    val skuCode: String,
    val skuName: String,
    val category: String,
    val isTop5: Boolean,
    val isNpl: Boolean,
    val planogramImageUrl: String?,
    val minPrice: Double,
    val maxPrice: Double
)

