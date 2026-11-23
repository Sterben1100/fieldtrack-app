package com.aswfoods.fieldtrack.data.remote.dto

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

private val MASTER_DTO_GSON = Gson()

/**
 * Respon daftar rute toko harian dari endpoint `/api/v1/master/daily-routes`.
 * Mendukung format `data` berupa Array langsung (`memory_mobile 5.md`) maupun Object (`DailyRoutesDataDto`).
 */
data class DailyRoutesResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: JsonElement
) {
    /**
     * Mengekstrak objek rencana rute beserta daftar toko secara aman dari payload JSON `data`.
     */
    fun extractRoutePlan(gson: Gson = MASTER_DTO_GSON): DailyRoutesDataDto {
        return when {
            data.isJsonArray -> {
                val listType = object : TypeToken<List<OutletDto>>() {}.type
                val outlets: List<OutletDto> = gson.fromJson(data, listType) ?: emptyList()
                DailyRoutesDataDto(
                    totalOutlets = outlets.size,
                    outlets = outlets
                )
            }
            data.isJsonObject -> gson.fromJson(data, DailyRoutesDataDto::class.java) ?: DailyRoutesDataDto()
            else -> DailyRoutesDataDto()
        }
    }
}

/**
 * Rincian rencana kunjungan harian beserta daftar outlet rute.
 */
data class DailyRoutesDataDto(
    @SerializedName("plan_id") val planId: Long? = null,
    @SerializedName("plan_date") val planDate: String? = null,
    @SerializedName("branch_code") val branchCode: String? = null,
    @SerializedName("price_code") val priceCode: String? = null,
    @SerializedName("total_outlets") val totalOutlets: Int = 0,
    @SerializedName("partner_name") val partnerName: String? = null,
    @SerializedName("outlets") val outlets: List<OutletDto> = emptyList()
)

/**
 * Representasi DTO toko beserta informasi rute harian, 5 Pilar, kode harga, status kolaborasi pasangan, dan riwayat survei.
 */
data class OutletDto(
    @SerializedName("id") val id: Long,
    @SerializedName("branch_id") val branchId: Long = 1L,
    @SerializedName("kode_toko") val kodeToko: String,
    @SerializedName("nama_toko") val namaToko: String,
    @SerializedName("alamat") val alamat: String,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("class_outlet") val classOutlet: String,
    @SerializedName("is_pareto") val isPareto: Boolean = false,
    @SerializedName("price_code") val priceCode: String = "AHRTMDN",
    @SerializedName("effective_price_code") val effectivePriceCode: String? = null,
    @SerializedName("is_sewa_display") val isSewaDisplay: Boolean,
    @SerializedName("owner_name") val ownerName: String? = null,
    @SerializedName("contact_phone") val contactPhone: String? = null,
    @SerializedName("route_plan_id") val routePlanId: Long? = null,
    @SerializedName("stop_order") val stopOrder: Int = 0,
    @SerializedName("outlet_type") val outletType: String = "wajib",
    @SerializedName("is_visited_by_me") val isVisitedByMe: Boolean = false,
    @SerializedName("is_visited_leader") val isVisitedLeader: Boolean = false,
    @SerializedName("is_visited_member") val isVisitedMember: Boolean = false,
    @SerializedName("is_survey_complete") val isSurveyComplete: Boolean = false,
    @SerializedName("final_combined_score") val finalCombinedScore: Double? = null,
    @SerializedName("needs_revisit") val needsRevisit: Boolean = false,
    @SerializedName("revisit_reason") val revisitReason: String? = null,
    @SerializedName("partner_name") val partnerName: String? = null,
    @SerializedName("has_surveyed_before") val hasSurveyedBefore: Boolean = false,
    @SerializedName("last_survey_date") val lastSurveyDate: String? = null,
    @SerializedName("last_survey_date_formatted") val lastSurveyDateFormatted: String? = null,
    @SerializedName("last_survey_score") val lastSurveyScore: Double? = null
)

/**
 * Respon daftar master kuesioner survei dari `/api/v1/master/survey-questions`.
 */
data class SurveyQuestionsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("role") val role: String,
    @SerializedName("total_questions") val totalQuestions: Int,
    @SerializedName("data") val data: List<SurveyQuestionDto>
)

/**
 * Butir pertanyaan survei baku dari server.
 */
data class SurveyQuestionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("question_code") val questionCode: String,
    @SerializedName("aspect") val aspect: String,
    @SerializedName("question_text") val questionText: String,
    @SerializedName("answer_type") val answerType: String,
    @SerializedName("scoring_guide") val scoringGuide: String? = null,
    @SerializedName("weight") val weight: Double = 1.0,
    @SerializedName("is_active") val isActive: Boolean = true
)

/**
 * Respon daftar katalog 6 SKU fokus audit (Top 5 + 1 NPL ASW Foods).
 */
data class ProductsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("price_code") val priceCode: String? = null,
    @SerializedName("data") val data: List<ProductDto>
)

data class ProductDto(
    @SerializedName("id") val id: Long,
    @SerializedName("sku_code") val skuCode: String,
    @SerializedName("sku_name") val skuName: String,
    @SerializedName("category") val category: String? = null,
    @SerializedName("is_top5") val isTop5: Boolean,
    @SerializedName("is_npl") val isNpl: Boolean,
    @SerializedName("planogram_image_url") val planogramImageUrl: String?,
    @SerializedName("price_range") val priceRange: PriceRangeDto?
)

data class PriceRangeDto(
    @SerializedName("price_code") val priceCode: String? = null,
    @SerializedName("min_price") val minPrice: Double,
    @SerializedName("max_price") val maxPrice: Double
)

/**
 * Respon daftar supervisor cabang untuk blind test L12.
 */
data class SupervisorsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<SupervisorDto>
)

data class SupervisorDto(
    @SerializedName("id") val id: Long,
    @SerializedName("sales_code") val salesCode: String? = null,
    @SerializedName("nip") val nip: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("area_name") val areaName: String? = null,
    @SerializedName("coverage_distributors") val coverageDistributors: String? = null,
    @SerializedName("photo_url") val photoUrl: String = "",
    @SerializedName("is_target") val isTarget: Boolean = false
)
