package com.aswfoods.fieldtrack.data.remote.api

import com.aswfoods.fieldtrack.data.remote.dto.BatchSyncRequest
import com.aswfoods.fieldtrack.data.remote.dto.BatchSyncResponse
import com.aswfoods.fieldtrack.data.remote.dto.DailyRoutesResponse
import com.aswfoods.fieldtrack.data.remote.dto.LeaderSurveyRequest
import com.aswfoods.fieldtrack.data.remote.dto.LoginRequest
import com.aswfoods.fieldtrack.data.remote.dto.LoginResponse
import com.aswfoods.fieldtrack.data.remote.dto.MemberSurveyRequest
import com.aswfoods.fieldtrack.data.remote.dto.PhotoUploadResponse
import com.aswfoods.fieldtrack.data.remote.dto.ProductsResponse
import com.aswfoods.fieldtrack.data.remote.dto.SupervisorsResponse
import com.aswfoods.fieldtrack.data.remote.dto.SurveyQuestionsResponse
import com.aswfoods.fieldtrack.data.remote.dto.SurveySubmitResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * Interface Retrofit untuk komunikasi jaringan dengan Backend Laravel 12 FieldTrack Pro.
 */
interface FieldTrackApiService {

    // --- Otentikasi ---
    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<Map<String, Any>>

    // --- Master Data & Rute Harian ---
    @GET("api/v1/master/daily-routes")
    suspend fun getDailyRoutes(
        @Query("date") date: String? = null
    ): Response<DailyRoutesResponse>

    @GET("api/v1/master/survey-questions")
    suspend fun getSurveyQuestions(
        @Query("role") role: String = "leader"
    ): Response<SurveyQuestionsResponse>

    @GET("api/v1/master/products")
    suspend fun getProducts(
        @Query("outlet_id") outletId: Long? = null
    ): Response<ProductsResponse>

    @GET("api/v1/master/supervisors")
    suspend fun getSupervisors(): Response<SupervisorsResponse>

    // --- Pengunggahan Media ---
    @Multipart
    @POST("api/v1/media/upload")
    suspend fun uploadPhoto(
        @Part photo: MultipartBody.Part,
        @Part("category") category: RequestBody
    ): Response<PhotoUploadResponse>

    // --- Submit Survey Lapangan (Idempotent via offline_sync_id) ---
    @POST("api/v1/survey/leader/submit")
    suspend fun submitLeaderSurvey(
        @Body request: LeaderSurveyRequest
    ): Response<SurveySubmitResponse>

    @POST("api/v1/survey/member/submit")
    suspend fun submitMemberSurvey(
        @Body request: MemberSurveyRequest
    ): Response<SurveySubmitResponse>

    // --- Batch Sync Offline Queue ---
    @POST("api/v1/survey/batch-sync")
    suspend fun batchSync(
        @Body request: BatchSyncRequest
    ): Response<BatchSyncResponse>
}
