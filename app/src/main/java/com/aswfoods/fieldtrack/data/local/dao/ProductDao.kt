package com.aswfoods.fieldtrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aswfoods.fieldtrack.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO untuk manajemen katalog produk fokus audit (Top 5 + 1 NPL ASW Foods) dan range harga GM.
 */
@Dao
interface ProductDao {
    /**
     * Mengambil seluruh produk fokus audit untuk form survei Member.
     *
     * @return Flow koleksi seluruh produk fokus audit
     */
    @Query("SELECT * FROM products ORDER BY isTop5 DESC, isNpl DESC, id ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    /**
     * Mengambil daftar produk secara sinkron/langsung untuk validasi kalkulasi offline.
     *
     * @return List produk fokus audit
     */
    @Query("SELECT * FROM products ORDER BY id ASC")
    suspend fun getProductsSync(): List<ProductEntity>

    /**
     * Menyimpan katalog produk dari API master ke penyimpanan lokal.
     *
     * @param products Daftar entitas produk
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    /**
     * Mengosongkan data produk lokal.
     */
    @Query("DELETE FROM products")
    suspend fun clearAll()
}

