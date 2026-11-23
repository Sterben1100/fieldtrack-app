package com.aswfoods.fieldtrack.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Payload pengiriman kuesioner objektif Leader (L1-L12) termasuk kondisi L12 N/A dan Check-Out Dini.
 */
data class LeaderSurveyRequest(
    @SerializedName("offline_sync_id") val offlineSyncId: String,
    @SerializedName("outlet_id") val outletId: Long,
    @SerializedName("visit_date") val visitDate: String,
    @SerializedName("checkin_latitude") val checkinLatitude: Double?,
    @SerializedName("checkin_longitude") val checkinLongitude: Double?,
    @SerializedName("checkin_at") val checkinAt: String?,
    @SerializedName("checkout_at") val checkoutAt: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("pic_absence_reason") val picAbsenceReason: String? = null,
    @SerializedName("is_incomplete") val isIncomplete: Boolean = false,
    @SerializedName("incomplete_reason_type") val incompleteReasonType: String? = null,
    @SerializedName("incomplete_reason") val incompleteReason: String? = null,
    @SerializedName("needs_revisit") val needsRevisit: Boolean = false,
    @SerializedName("revisit_notes") val revisitNotes: String? = null,
    @SerializedName("responses") val responses: List<LeaderQuestionResponseDto>
)

data class LeaderQuestionResponseDto(
    @SerializedName("question_code") val questionCode: String,
    @SerializedName("answer_value") val answerValue: String, // "Ya", "Tidak", "N/A", atau teks
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("rating_value") val ratingValue: Int? = null,
    @SerializedName("selected_supervisor_id") val selectedSupervisorId: Long? = null
)

/**
 * Payload pengiriman audit fisik Member (Kriteria M1 - M4) termasuk dukungan Check-Out Dini.
 */
data class MemberSurveyRequest(
    @SerializedName("offline_sync_id") val offlineSyncId: String,
    @SerializedName("outlet_id") val outletId: Long,
    @SerializedName("visit_date") val visitDate: String,
    @SerializedName("actual_latitude") val actualLatitude: Double?,
    @SerializedName("actual_longitude") val actualLongitude: Double?,
    @SerializedName("checkin_at") val checkinAt: String?,
    @SerializedName("checkout_at") val checkoutAt: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("has_sewa_display") val hasSewaDisplay: Boolean = false,
    @SerializedName("rak_condition") val rakCondition: String?,
    @SerializedName("rak_photo_path") val rakPhotoPath: String?,
    @SerializedName("is_incomplete") val isIncomplete: Boolean = false,
    @SerializedName("incomplete_reason_type") val incompleteReasonType: String? = null,
    @SerializedName("incomplete_reason") val incompleteReason: String? = null,
    @SerializedName("needs_revisit") val needsRevisit: Boolean = false,
    @SerializedName("revisit_notes") val revisitNotes: String? = null,
    @SerializedName("sku_audits") val skuAudits: List<MemberSkuAuditDto>
)

data class MemberSkuAuditDto(
    @SerializedName("product_id") val productId: Long,
    @SerializedName("has_price_tag") val hasPriceTag: Boolean,
    @SerializedName("is_price_tag_compliant") val isPriceTagCompliant: Boolean,
    @SerializedName("actual_price") val actualPrice: Double,
    @SerializedName("sku_photo_path") val skuPhotoPath: String?
)

/**
 * Respon umum submit survey dari backend.
 */
data class SurveySubmitResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: SurveyResultData
)

data class SurveyResultData(
    @SerializedName("visit_id") val visitId: Long,
    @SerializedName("outlet_id") val outletId: Long? = null,
    @SerializedName("offline_sync_id") val offlineSyncId: String? = null,
    @SerializedName("total_score") val totalScore: Double = 0.0,
    @SerializedName("status") val status: String? = null,
    @SerializedName("status_text") val statusText: String? = null,
    @SerializedName("geotag_score_rate") val geotagScoreRate: Double? = null,
    @SerializedName("rak_score_rate") val rakScoreRate: Double? = null,
    @SerializedName("price_tag_score_rate") val priceTagScoreRate: Double? = null,
    @SerializedName("price_range_score_rate") val priceRangeScoreRate: Double? = null,
    @SerializedName("is_survey_complete") val isSurveyComplete: Boolean? = null,
    @SerializedName("final_combined_score") val finalCombinedScore: Double? = null
)

/**
 * Payload pengiriman batch offline (mendukung field `leader_visits`/`member_visits` dan `leader_surveys`/`member_surveys`).
 */
data class BatchSyncRequest(
    @SerializedName("leader_visits") val leaderVisits: List<LeaderSurveyRequest>,
    @SerializedName("member_visits") val memberVisits: List<MemberSurveyRequest>,
    @SerializedName("leader_surveys") val leaderSurveys: List<LeaderSurveyRequest> = leaderVisits,
    @SerializedName("member_surveys") val memberSurveys: List<MemberSurveyRequest> = memberVisits
)

data class BatchSyncResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null,
    @SerializedName("synced_count") val syncedCount: Int? = null,
    @SerializedName("results") val results: List<BatchSyncItemResultDto>? = null,
    @SerializedName("data") val data: BatchSyncResultData? = null
)

data class BatchSyncItemResultDto(
    @SerializedName("offline_sync_id") val offlineSyncId: String,
    @SerializedName("status") val status: String? = null
)

data class BatchSyncResultData(
    @SerializedName("synced_leader_sync_ids") val syncedLeaderSyncIds: List<String> = emptyList(),
    @SerializedName("synced_member_sync_ids") val syncedMemberSyncIds: List<String> = emptyList(),
    @SerializedName("failed_items") val failedItems: List<Any> = emptyList()
)

/**
 * Respon unggah foto bukti lapangan.
 */
data class PhotoUploadResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PhotoUploadData
)

data class PhotoUploadData(
    @SerializedName("file_path") val filePath: String,
    @SerializedName("full_url") val fullUrl: String
)
