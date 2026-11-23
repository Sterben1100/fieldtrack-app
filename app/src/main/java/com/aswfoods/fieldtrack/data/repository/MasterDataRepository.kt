package com.aswfoods.fieldtrack.data.repository

import android.content.Context
import com.aswfoods.fieldtrack.data.local.dao.OutletDao
import com.aswfoods.fieldtrack.data.local.dao.ProductDao
import com.aswfoods.fieldtrack.data.local.dao.SupervisorDao
import com.aswfoods.fieldtrack.data.local.dao.SurveyQuestionDao
import com.aswfoods.fieldtrack.data.local.entity.OutletEntity
import com.aswfoods.fieldtrack.data.local.entity.ProductEntity
import com.aswfoods.fieldtrack.data.local.entity.SupervisorEntity
import com.aswfoods.fieldtrack.data.local.entity.SurveyQuestionEntity
import com.aswfoods.fieldtrack.data.remote.api.FieldTrackApiService
import com.aswfoods.fieldtrack.data.remote.dto.ProductDto
import com.aswfoods.fieldtrack.util.ImageHelper
import kotlinx.coroutines.flow.Flow

/**
 * Repository untuk pengelolaan master data jadwal rute harian, katalog produk, data SPV,
 * dan daftar master kuesioner survei. Mengimplementasikan strategi "Single Source of Truth" Room.
 */
class MasterDataRepository(
    private val apiService: FieldTrackApiService,
    private val outletDao: OutletDao,
    private val productDao: ProductDao,
    private val supervisorDao: SupervisorDao,
    private val appContext: Context? = null,
    private val surveyQuestionDao: SurveyQuestionDao? = null
) {
    companion object {
        /**
         * Memilih tepat 6 SKU fokus audit (5 Top SKU + 1 NPL) apabila server mengembalikan
         * seluruh daftar master produk, dengan prioritas produk yang sudah memiliki rentang harga aktif.
         */
        fun selectFocus6Products(dtos: List<ProductDto>): List<ProductDto> {
            if (dtos.size <= 6) return dtos

            val top5 = dtos
                .filter { it.isTop5 && !it.isNpl }
                .sortedByDescending { (it.priceRange?.maxPrice ?: 0.0) > 0.0 }
                .take(5)

            val npl1 = dtos
                .filter { it.isNpl }
                .sortedByDescending { (it.priceRange?.maxPrice ?: 0.0) > 0.0 }
                .take(1)

            val picked = (top5 + npl1).toMutableList()
            if (picked.size < 6) {
                val pickedIds = picked.map { it.id }.toSet()
                val fillers = dtos
                    .filter { it.id !in pickedIds }
                    .sortedByDescending { (it.priceRange?.maxPrice ?: 0.0) > 0.0 }
                    .take(6 - picked.size)
                picked.addAll(fillers)
            }
            return picked
        }

        /**
         * Mengonversi daftar DTO produk fokus menjadi daftar entitas Room siap simpan.
         */
        fun mapFocus6ProductEntities(dtos: List<ProductDto>): List<ProductEntity> {
            return selectFocus6Products(dtos).map {
                ProductEntity(
                    id = it.id,
                    skuCode = it.skuCode,
                    skuName = it.skuName,
                    category = it.category.orEmpty(),
                    isTop5 = it.isTop5,
                    isNpl = it.isNpl,
                    planogramImageUrl = it.planogramImageUrl,
                    minPrice = it.priceRange?.minPrice ?: 0.0,
                    maxPrice = it.priceRange?.maxPrice ?: 9999999.0
                )
            }
        }
    }
    /**
     * Memantau daftar toko jadwal harian dari database lokal (terurut berdasarkan stopOrder).
     *
     * @return Flow koleksi toko terdaftar
     */
    fun getOutlets(): Flow<List<OutletEntity>> = outletDao.getAllOutlets()

    /**
     * Memantau katalog produk Top 5 + 1 NPL dari database lokal.
     *
     * @return Flow koleksi produk
     */
    fun getProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    /**
     * Mengambil daftar supervisor secara sinkron untuk form L12.
     *
     * @return List data supervisor cabang
     */
    suspend fun getSupervisors(): List<SupervisorEntity> = supervisorDao.getAllSupervisors()

    /**
     * Melakukan sinkronisasi penuh master data dan rute harian dari backend ke basis data lokal Room.
     * Data lokal hanya diganti apabila respons API berhasil agar cache offline tetap aman saat server bermasalah.
     *
     * @return Result status keberhasilan sinkronisasi
     */
    suspend fun syncMasterData(): Result<Unit> {
        return try {
            val errors = mutableListOf<String>()

            // 1. Sinkronisasi daftar rute harian (10 Wajib + 5 Tambahan beserta 5 Pilar, kode harga, status tim & riwayat)
            val routesResponse = apiService.getDailyRoutes()
            if (routesResponse.isSuccessful && routesResponse.body() != null) {
                val calibratedMap = outletDao.getCalibratedOutlets().associateBy { it.id }
                val routePlan = routesResponse.body()!!.extractRoutePlan()
                val outletEntities = routePlan.outlets.map {
                    val calibrated = calibratedMap[it.id]
                    val resolvedPriceCode = it.effectivePriceCode
                        ?.takeIf { code -> code.isNotBlank() }
                        ?: it.priceCode.ifBlank { routePlan.priceCode ?: "AHRTMDN" }
                    OutletEntity(
                        id = it.id,
                        branchId = it.branchId,
                        kodeToko = it.kodeToko,
                        namaToko = it.namaToko,
                        alamat = it.alamat,
                        latitude = calibrated?.latitude ?: it.latitude,
                        longitude = calibrated?.longitude ?: it.longitude,
                        classOutlet = it.classOutlet,
                        priceCode = resolvedPriceCode,
                        isSewaDisplay = it.isSewaDisplay,
                        ownerName = it.ownerName,
                        contactPhone = it.contactPhone,
                        stopOrder = it.stopOrder,
                        outletType = it.outletType,
                        isVisitedByMe = it.isVisitedByMe,
                        isVisitedLeader = it.isVisitedLeader,
                        isVisitedMember = it.isVisitedMember,
                        isSurveyComplete = it.isSurveyComplete,
                        finalCombinedScore = it.finalCombinedScore,
                        needsRevisit = it.needsRevisit,
                        revisitReason = it.revisitReason,
                        partnerName = it.partnerName ?: routePlan.partnerName,
                        isCoordinateCalibrated = calibrated != null,
                        isPareto = it.isPareto,
                        routePlanId = it.routePlanId ?: routePlan.planId,
                        hasSurveyedBefore = it.hasSurveyedBefore,
                        lastSurveyDate = it.lastSurveyDateFormatted?.takeIf { d -> d.isNotBlank() } ?: it.lastSurveyDate,
                        lastSurveyScore = it.lastSurveyScore
                    )
                }
                outletDao.clearAll()
                outletDao.insertAll(outletEntities)
            } else {
                errors.add("Rute (${routesResponse.code()})")
            }

            // 2. Sinkronisasi katalog produk fokus (5 Top SKU + 1 NPL)
            val productsResponse = apiService.getProducts()
            if (productsResponse.isSuccessful && productsResponse.body() != null) {
                val productEntities = mapFocus6ProductEntities(productsResponse.body()!!.data)
                productDao.clearAll()
                productDao.insertAll(productEntities)
            } else {
                errors.add("Produk (${productsResponse.code()})")
            }

            // 3. Sinkronisasi supervisor cabang sekaligus cache foto wajah untuk blind test offline L12
            val spvResponse = apiService.getSupervisors()
            if (spvResponse.isSuccessful && spvResponse.body() != null) {
                val spvEntities = spvResponse.body()!!.data.map { dto ->
                    val cachedPath = appContext?.let { ctx ->
                        ImageHelper.downloadSupervisorPhoto(ctx, dto.photoUrl, dto.id)
                    }
                    SupervisorEntity(
                        id = dto.id,
                        salesCode = dto.salesCode ?: dto.nip.orEmpty(),
                        nip = dto.nip ?: dto.salesCode.orEmpty(),
                        name = dto.name,
                        areaName = dto.areaName,
                        coverageDistributors = dto.coverageDistributors,
                        photoUrl = cachedPath ?: dto.photoUrl,
                        isTarget = dto.isTarget
                    )
                }
                supervisorDao.clearAll()
                supervisorDao.insertAll(spvEntities)
            } else {
                errors.add("Supervisor (${spvResponse.code()})")
            }

            // 4. Sinkronisasi master kuesioner Leader agar pertanyaan baku selalu mutakhir offline
            if (surveyQuestionDao != null) {
                val questionResponse = apiService.getSurveyQuestions("leader")
                if (questionResponse.isSuccessful && questionResponse.body() != null) {
                    val body = questionResponse.body()!!
                    val entities = body.data.map { q ->
                        SurveyQuestionEntity(
                            id = q.id,
                            role = body.role.ifBlank { "leader" },
                            questionCode = q.questionCode,
                            aspect = q.aspect,
                            questionText = q.questionText,
                            answerType = q.answerType,
                            scoringGuide = q.scoringGuide,
                            weight = q.weight,
                            isActive = q.isActive
                        )
                    }
                    surveyQuestionDao.clearByRole("leader")
                    surveyQuestionDao.insertAll(entities)
                } else {
                    errors.add("Kuesioner (${questionResponse.code()})")
                }
            }

            if (errors.isEmpty()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Sebagian/seluruh master data gagal diunduh: ${errors.joinToString(", ")}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
