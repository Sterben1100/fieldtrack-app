package com.aswfoods.fieldtrack.data.remote.interceptor

import com.aswfoods.fieldtrack.data.remote.dto.AuthData
import com.aswfoods.fieldtrack.data.remote.dto.BatchSyncItemResultDto
import com.aswfoods.fieldtrack.data.remote.dto.BatchSyncRequest
import com.aswfoods.fieldtrack.data.remote.dto.BatchSyncResponse
import com.aswfoods.fieldtrack.data.remote.dto.BatchSyncResultData
import com.aswfoods.fieldtrack.data.remote.dto.DailyRoutesResponse
import com.aswfoods.fieldtrack.data.remote.dto.LeaderSurveyRequest
import com.aswfoods.fieldtrack.data.remote.dto.LoginRequest
import com.aswfoods.fieldtrack.data.remote.dto.LoginResponse
import com.aswfoods.fieldtrack.data.remote.dto.MemberSurveyRequest
import com.aswfoods.fieldtrack.data.remote.dto.OutletDto
import com.aswfoods.fieldtrack.data.remote.dto.PhotoUploadData
import com.aswfoods.fieldtrack.data.remote.dto.PhotoUploadResponse
import com.aswfoods.fieldtrack.data.remote.dto.PriceRangeDto
import com.aswfoods.fieldtrack.data.remote.dto.ProductDto
import com.aswfoods.fieldtrack.data.remote.dto.ProductsResponse
import com.aswfoods.fieldtrack.data.remote.dto.SupervisorDto
import com.aswfoods.fieldtrack.data.remote.dto.SupervisorsResponse
import com.aswfoods.fieldtrack.data.remote.dto.SurveyQuestionDto
import com.aswfoods.fieldtrack.data.remote.dto.SurveyQuestionsResponse
import com.aswfoods.fieldtrack.data.remote.dto.SurveyResultData
import com.aswfoods.fieldtrack.data.remote.dto.SurveySubmitResponse
import com.aswfoods.fieldtrack.data.remote.dto.UserProfileDto
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer

/**
 * Interceptor OkHttp untuk simulasi respons REST API backend secara lokal (Mode Demo).
 * Memungkinkan pengujian penuh alur Login, Unduh Rute (`/api/v1/master/daily-routes`), Master Kuesioner,
 * Audit Leader & Member (termasuk L12 N/A & Check-Out Dini), Upload Foto, dan Submit Antrean Offline.
 */
class MockApiInterceptor(
    private val isDemoModeEnabled: () -> Boolean
) : Interceptor {

    private val gson = Gson()

    /**
     * Mencegat permintaan jaringan dan mengembalikan respons JSON tiruan jika Mode Demo aktif.
     *
     * @param chain Rantai eksekusi request OkHttp
     * @return Response HTTP tiruan (Mode Demo) atau respons asli dari jaringan
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!isDemoModeEnabled()) {
            return chain.proceed(request)
        }

        val path = request.url.encodedPath
        val method = request.method.uppercase()

        return when {
            path.endsWith("/api/v1/auth/login") && method == "POST" -> handleLogin(request)
            path.endsWith("/api/v1/auth/logout") && method == "POST" -> buildJsonResponse(
                request,
                200,
                gson.toJson(mapOf("status" to "success", "message" to "Logout demo berhasil"))
            )
            (path.endsWith("/api/v1/routes/daily") || path.endsWith("/api/v1/master/daily-routes")) &&
                method == "GET" -> handleDailyRoutes(request)
            path.endsWith("/api/v1/master/survey-questions") && method == "GET" -> handleSurveyQuestions(request)
            path.endsWith("/api/v1/master/products") && method == "GET" -> handleProducts(request)
            path.endsWith("/api/v1/master/supervisors") && method == "GET" -> handleSupervisors(request)
            path.endsWith("/api/v1/media/upload") && method == "POST" -> handlePhotoUpload(request)
            path.endsWith("/api/v1/survey/batch-sync") && method == "POST" -> handleBatchSync(request)
            path.endsWith("/api/v1/survey/leader/submit") && method == "POST" -> handleSingleLeaderSubmit(request)
            path.endsWith("/api/v1/survey/member/submit") && method == "POST" -> handleSingleMemberSubmit(request)
            else -> buildJsonResponse(
                request,
                404,
                gson.toJson(mapOf("status" to "error", "message" to "Endpoint demo tidak ditemukan: $path"))
            )
        }
    }

    private fun handleLogin(request: Request): Response {
        val bodyJson = readRequestBodyAsString(request)
        val loginReq = runCatching { gson.fromJson(bodyJson, LoginRequest::class.java) }.getOrNull()
        val rawUsername = loginReq?.username?.trim().orEmpty()

        if (rawUsername.isBlank() || loginReq?.password.isNullOrBlank()) {
            return buildJsonResponse(
                request,
                422,
                gson.toJson(mapOf("status" to "error", "message" to "Username dan password wajib diisi."))
            )
        }

        if (loginReq?.password != "123456" && loginReq?.password != "password123") {
            return buildJsonResponse(
                request,
                401,
                gson.toJson(mapOf("status" to "error", "message" to "Kata sandi akun demo salah."))
            )
        }

        val isLeader = rawUsername.contains("leader", ignoreCase = true) ||
            rawUsername.contains("ketua", ignoreCase = true)
        val user = when (rawUsername.lowercase()) {
            "leader_batam" -> UserProfileDto(
                id = 23L,
                name = "Muhammad Fachrozy",
                username = rawUsername,
                role = "leader",
                branchId = 91L,
                branchCode = "DABTM001",
                branchName = "PT. DWI NIAGA INDONESIA BATAM",
                priceCode = "AHRTKEP"
            )
            "member_batam" -> UserProfileDto(
                id = 22L,
                name = "Salomo Gusto Leonard",
                username = rawUsername,
                role = "member",
                branchId = 91L,
                branchCode = "DABTM001",
                branchName = "PT. DWI NIAGA INDONESIA BATAM",
                priceCode = "AHRTKEP"
            )
            "leader_bengkulu" -> UserProfileDto(
                id = 85L,
                name = "Whevman Marbun",
                username = rawUsername,
                role = "leader",
                branchId = 96L,
                branchCode = "DABGL001",
                branchName = "PT. MARHUM RODAMAS ABADI BENGKULU",
                priceCode = "AHRTMDN"
            )
            "member_bengkulu" -> UserProfileDto(
                id = 86L,
                name = "Rizky Ramadhan Tampubolon",
                username = rawUsername,
                role = "member",
                branchId = 96L,
                branchCode = "DABGL001",
                branchName = "PT. MARHUM RODAMAS ABADI BENGKULU",
                priceCode = "AHRTMDN"
            )
            "leader01" -> UserProfileDto(
                id = 95L,
                name = "Budi Santoso (Leader)",
                username = rawUsername,
                role = "leader",
                branchId = 96L,
                branchCode = "DABGL001",
                branchName = "PT. MARHUM RODAMAS ABADI BENGKULU",
                priceCode = "AHRTMDN"
            )
            "member01" -> UserProfileDto(
                id = 96L,
                name = "Rian Hidayat (Member)",
                username = rawUsername,
                role = "member",
                branchId = 96L,
                branchCode = "DABGL001",
                branchName = "PT. MARHUM RODAMAS ABADI BENGKULU",
                priceCode = "AHRTMDN"
            )
            else -> {
                val prettyUser = rawUsername.replaceFirstChar { it.uppercase() }
                val roleLabel = if (isLeader) "Demo Leader" else "Demo Member"
                val isJakartaBranch = rawUsername.contains("03")
                UserProfileDto(
                    id = if (isLeader) 101L else 202L,
                    name = "$prettyUser ($roleLabel)",
                    username = rawUsername,
                    role = if (isLeader) "leader" else "member",
                    branchId = if (isJakartaBranch) 2L else 91L,
                    branchCode = if (isJakartaBranch) "BR-JKT-01" else "DABTM001",
                    branchName = if (isJakartaBranch) "ASW Foods Jakarta" else "PT. DWI NIAGA INDONESIA BATAM",
                    priceCode = if (isJakartaBranch) "AHCOMDN" else "AHRTKEP"
                )
            }
        }

        val response = LoginResponse(
            status = "success",
            message = "Login Mode Demo berhasil",
            data = AuthData(
                token = "demo-sanctum-token-${user.role}-${System.currentTimeMillis()}",
                user = user
            )
        )
        return buildJsonResponse(request, 200, gson.toJson(response))
    }

    private fun handleDailyRoutes(request: Request): Response {
        // Gabungan data toko riil Batam & Bengkulu (fieldtrack_db.sql) + 3 toko dummy koordinat lapangan
        val outlets = listOf(
            OutletDto(
                id = 4780L,
                branchId = 91L,
                kodeToko = "CABTM01215",
                namaToko = "UD SENTRAL MANDIRI",
                alamat = "JL. HANG JEBAT BATU BESAR NO. 126-131, BATAM",
                latitude = 1.15441210,
                longitude = 104.12837830,
                classOutlet = "RWO",
                isPareto = true,
                priceCode = "AHRTKEP",
                effectivePriceCode = "AHRTKEP",
                isSewaDisplay = true,
                routePlanId = 99L,
                stopOrder = 1,
                outletType = "wajib",
                isVisitedLeader = false,
                isVisitedMember = true,
                needsRevisit = true,
                revisitReason = "PIC toko sedang keluar kota, lakukan blind test SPV susulan",
                partnerName = "Salomo Gusto Leonard",
                hasSurveyedBefore = true,
                lastSurveyDate = "2026-11-10",
                lastSurveyDateFormatted = "10 Nov 2026",
                lastSurveyScore = 88.5
            ),
            OutletDto(
                id = 3938L,
                branchId = 91L,
                kodeToko = "CABTM00041",
                namaToko = "HARAPAN INDAH NONGSA",
                alamat = "RUKO ARIRA GARDEN BLOK A NO.3-7, BATAM",
                latitude = 1.14382700,
                longitude = 104.11923200,
                classOutlet = "RWO",
                isPareto = true,
                priceCode = "AHRTKEP",
                effectivePriceCode = "AHRTKEP",
                isSewaDisplay = true,
                routePlanId = 99L,
                stopOrder = 2,
                outletType = "wajib",
                isVisitedLeader = true,
                isVisitedMember = false,
                partnerName = "Salomo Gusto Leonard"
            ),
            OutletDto(
                id = 3914L,
                branchId = 91L,
                kodeToko = "CABTM00036",
                namaToko = "GROSS BOTANIA",
                alamat = "PASAR BOTANIA GARDEN, BATAM",
                latitude = 1.12659400,
                longitude = 104.09854900,
                classOutlet = "RWO",
                isPareto = true,
                priceCode = "AHRTKEP",
                effectivePriceCode = "AHRTKEP",
                isSewaDisplay = false,
                routePlanId = 99L,
                stopOrder = 3,
                outletType = "wajib",
                partnerName = "Salomo Gusto Leonard"
            ),
            OutletDto(
                id = 4762L,
                branchId = 91L,
                kodeToko = "CABTM00037",
                namaToko = "TOP 100 NIAGA",
                alamat = "RUKO NIAGA MAS NO 8 -15, BATAM",
                latitude = 1.11467000,
                longitude = 104.06300000,
                classOutlet = "Grooming",
                isPareto = false,
                priceCode = "AHRTKEP",
                effectivePriceCode = "AHRTKEP",
                isSewaDisplay = false,
                routePlanId = 99L,
                stopOrder = 4,
                outletType = "wajib",
                isVisitedByMe = true,
                isVisitedLeader = true,
                isVisitedMember = true,
                isSurveyComplete = true,
                finalCombinedScore = 92.5,
                partnerName = "Salomo Gusto Leonard"
            ),
            OutletDto(
                id = 4035L,
                branchId = 91L,
                kodeToko = "CABTM00038",
                namaToko = "JC SUPERMARKET OBM",
                alamat = "ONE BATAM MALL BASEMENT 1 BLOK B1 NO 1, BATAM",
                latitude = 1.12903500,
                longitude = 104.04550900,
                classOutlet = "Grooming",
                isPareto = false,
                priceCode = "AHRTKEP",
                effectivePriceCode = "AHRTKEP",
                isSewaDisplay = false,
                routePlanId = 99L,
                stopOrder = 5,
                outletType = "wajib",
                partnerName = "Salomo Gusto Leonard",
                hasSurveyedBefore = true,
                lastSurveyDate = "2026-11-10",
                lastSurveyDateFormatted = "10 Nov 2026",
                lastSurveyScore = 85.5
            ),
            OutletDto(
                id = 44L,
                branchId = 96L,
                kodeToko = "CABGL00003",
                namaToko = "3 R.",
                alamat = "UNIT 1 DEKAT TOKO FITRI KURO TIDUR, BENGKULU",
                latitude = -3.38278800,
                longitude = 102.14941400,
                classOutlet = "RWO",
                isPareto = true,
                priceCode = "AHRTMDN",
                effectivePriceCode = "AHRTMDN",
                isSewaDisplay = false,
                routePlanId = 110L,
                stopOrder = 6,
                outletType = "wajib",
                partnerName = "Rian Hidayat"
            ),
            OutletDto(
                id = 272L,
                branchId = 96L,
                kodeToko = "CABGL01618",
                namaToko = "DAVA",
                alamat = "JL. Padang Bendar Sesudah Toko Mujur, BENGKULU",
                latitude = -3.38282100,
                longitude = 102.14942700,
                classOutlet = "NGVO",
                isPareto = false,
                priceCode = "AHRTMDN",
                effectivePriceCode = "AHRTMDN",
                isSewaDisplay = false,
                routePlanId = 110L,
                stopOrder = 7,
                outletType = "wajib",
                partnerName = "Rian Hidayat"
            ),
            OutletDto(
                id = 995L,
                branchId = 96L,
                kodeToko = "CABGL01233",
                namaToko = "VIANTO SIHOMBING",
                alamat = "DS. Marga Sakti RT.06 RW.01 Kec. Padang Jaya, BENGKULU",
                latitude = -3.38283700,
                longitude = 102.14941800,
                classOutlet = "Grooming",
                isPareto = false,
                priceCode = "AHRTMDN",
                effectivePriceCode = "AHRTMDN",
                isSewaDisplay = false,
                routePlanId = 110L,
                stopOrder = 8,
                outletType = "tambahan",
                needsRevisit = true,
                revisitReason = "Data belum lengkap (toko ramai saat kunjungan sebelumnya)",
                partnerName = "Rian Hidayat"
            ),
            OutletDto(
                id = 6L,
                branchId = 91L,
                kodeToko = "ESK-MDN-006",
                namaToko = "Toko Yanti Pranala",
                alamat = "Jl. SM Raja Km 7,5 Amplas, Medan",
                latitude = 3.5387041047551815,
                longitude = 98.71998667687002,
                classOutlet = "Pareto",
                isPareto = true,
                priceCode = "AHRTKEP",
                effectivePriceCode = "AHRTKEP",
                isSewaDisplay = true,
                ownerName = "Ibu Yanti Pranala",
                contactPhone = "081260004455",
                routePlanId = 99L,
                stopOrder = 9,
                outletType = "wajib",
                partnerName = "Salomo Gusto Leonard"
            ),
            OutletDto(
                id = 7L,
                branchId = 91L,
                kodeToko = "ESK-MDN-007",
                namaToko = "Yanto's Dashboard",
                alamat = "Jl. Timah Putih / Amplas Area, Medan",
                latitude = 3.5387041047551815,
                longitude = 98.71998667687002,
                classOutlet = "RWO",
                isPareto = false,
                priceCode = "AHRTKEP",
                effectivePriceCode = "AHRTKEP",
                isSewaDisplay = true,
                ownerName = "Pak Yanto",
                contactPhone = "081370007788",
                routePlanId = 99L,
                stopOrder = 10,
                outletType = "wajib",
                partnerName = "Salomo Gusto Leonard"
            ),
            OutletDto(
                id = 8L,
                branchId = 91L,
                kodeToko = "ESK-MDN-008",
                namaToko = "Admin Datang",
                alamat = "Jl. Panglima Denai No. 18, Medan Amplas",
                latitude = 3.5387041047551815,
                longitude = 98.71998667687002,
                classOutlet = "NOO",
                isPareto = false,
                priceCode = "AHRTKEP",
                effectivePriceCode = "AHRTKEP",
                isSewaDisplay = false,
                ownerName = "Admin Toko",
                contactPhone = "085261112233",
                routePlanId = 99L,
                stopOrder = 11,
                outletType = "tambahan",
                partnerName = "Salomo Gusto Leonard"
            )
        )
        val payload = DailyRoutesResponse(
            status = "success",
            data = gson.toJsonTree(outlets)
        )
        return buildJsonResponse(request, 200, gson.toJson(payload))
    }

    private fun handleSurveyQuestions(request: Request): Response {
        val role = request.url.queryParameter("role") ?: "leader"
        val questions = listOf(
            SurveyQuestionDto(1L, "L1", "Kunjungan Sales", "Apakah Sales mengunjungi toko ini dalam 14 hari terakhir?", "yes_no_na", "Ya = 1, Tidak = 0, N/A tidak dihitung", 1.0, true),
            SurveyQuestionDto(2L, "L2", "Kunjungan Sales", "Apakah Sales melakukan penataan display ketika berkunjung ke toko?", "yes_no_na", "Ya = 1, Tidak = 0, N/A tidak dihitung", 1.0, true),
            SurveyQuestionDto(3L, "L3", "Kunjungan Sales", "Apakah Sales menanyakan kebutuhan stok atau pesanan toko saat kunjungan?", "yes_no_na", "Ya = 1, Tidak = 0, N/A tidak dihitung", 1.0, true),
            SurveyQuestionDto(4L, "L4", "Informasi Program", "Apakah Sales menjelaskan program marketing yang sedang berjalan saat kunjungan? (Prioritas produk SHP & ATB, potongan harga, bonus barang)", "yes_no_na", "Ya = 1, Tidak = 0, N/A tidak dihitung", 1.0, true),
            SurveyQuestionDto(5L, "L5", "Kejelasan Program", "Apakah Sales menyampaikan syarat dan periode program tersebut?", "yes_no_na", "Ya = 1, Tidak = 0, N/A tidak dihitung", 1.0, true),
            SurveyQuestionDto(6L, "L6", "Pemenuhan Pesanan", "Apakah pesanan barang tiba pada atau sebelum tanggal yang dijanjikan kepada toko?", "yes_no_na", "Ya = 1, Tidak = 0, N/A tidak dihitung", 1.0, true),
            SurveyQuestionDto(7L, "L7", "Kesesuaian Pesanan", "Apakah barang pesanan yang tiba dalam kondisi baik (tidak rusak dan tidak kedaluwarsa)?", "yes_no_na", "Ya = 1, Tidak = 0, N/A tidak dihitung", 1.0, true),
            SurveyQuestionDto(8L, "L8", "Penanganan Keluhan", "Dalam 3 bulan terakhir, apakah keluhan atau retur terakhir yang diajukan toko sudah diselesaikan?", "yes_no_na", "Ya = 1, Tidak = 0, N/A tidak dihitung", 1.0, true),
            SurveyQuestionDto(9L, "L9", "Penerimaan Reward", "Apakah ada pencairan reward yang belum diterima? (Wajib isi catatan jika jawaban Ya)", "yes_no_na", "Ya = 1, Tidak = 0, N/A tidak dihitung", 1.0, true),
            SurveyQuestionDto(10L, "L10", "Tindak Lanjut Masukan", "Apakah Bapak/Ibu ada masukan atau saran terakhir yang toko sampaikan kepada Sales sudah mendapat tanggapan?", "text_notes", "Catatan kualitatif saran toko (tidak mempengaruhi skor numerik)", 0.0, true),
            SurveyQuestionDto(11L, "L11", "Kepuasan Pelayanan", "Apakah Bapak/Ibu puas dengan pelayanan dari tim Sales kami? (Skor 0 - 10)", "rating_0_10", "Skor NPS skala 0 sampai 10", 0.0, true),
            SurveyQuestionDto(12L, "L12", "Pengenalan SPV", "Toko diminta mengenali FOTO SPV dari 3 pilihan foto yang diperlihatkan", "photo_choice", "Benar = 1, Salah = 0, N/A = Revisit (Bobot 15% skor akhir)", 0.15, true),
            SurveyQuestionDto(67L, "L99", "Audit Tambahan Khusus", "Apakah sales membawa katalog fisik produk edisi terbaru?", "yes_no_na", "Ya = 1, Tidak = 0, N/A tidak dihitung", 1.0, true)
        )
        val payload = SurveyQuestionsResponse(
            status = "success",
            role = role,
            totalQuestions = questions.size,
            data = questions
        )
        return buildJsonResponse(request, 200, gson.toJson(payload))
    }

    private fun handleProducts(request: Request): Response {
        // 6 SKU fokus audit (5 Top SKU + 1 NPL ASW Foods) dari master produk fieldtrack_db.sql
        val products = listOf(
            ProductDto(
                id = 17L,
                skuCode = "CC-CCK-36",
                skuName = "HATARI CREAM CRACKERS 6 PACK X 6 BKS X 245 GR",
                category = "Crackers",
                isTop5 = true,
                isNpl = false,
                planogramImageUrl = null,
                priceRange = PriceRangeDto(priceCode = "AHRTKEP", minPrice = 5800.0, maxPrice = 7300.0)
            ),
            ProductDto(
                id = 32L,
                skuCode = "CR-CHM-21",
                skuName = "HATARI CREAM CHOCOLATE MILK 3 PACK X 7 BKS X 190 GR",
                category = "Biskuit",
                isTop5 = true,
                isNpl = false,
                planogramImageUrl = null,
                priceRange = PriceRangeDto(priceCode = "AHRTKEP", minPrice = 5500.0, maxPrice = 7000.0)
            ),
            ProductDto(
                id = 44L,
                skuCode = "CR-COR-21",
                skuName = "HATARI TWINS CHO & ORANGE 3 PACK X 7 BKS X 190 GR",
                category = "Biskuit",
                isTop5 = true,
                isNpl = false,
                planogramImageUrl = null,
                priceRange = PriceRangeDto(priceCode = "AHRTKEP", minPrice = 4800.0, maxPrice = 6000.0)
            ),
            ProductDto(
                id = 20L,
                skuCode = "MB-ATB-24",
                skuName = "ATB MARIE SUSU 6 PACK X 4 BKS X 180 GR",
                category = "Marie",
                isTop5 = true,
                isNpl = false,
                planogramImageUrl = null,
                priceRange = PriceRangeDto(priceCode = "AHRTKEP", minPrice = 6000.0, maxPrice = 7500.0)
            ),
            ProductDto(
                id = 23L,
                skuCode = "BI-BNN-40B",
                skuName = "HATARI BANANA (K) 4 PACK X 10 BKS X 90 GR",
                category = "Biskuit",
                isTop5 = true,
                isNpl = false,
                planogramImageUrl = null,
                priceRange = PriceRangeDto(priceCode = "AHRTKEP", minPrice = 5000.0, maxPrice = 6500.0)
            ),
            ProductDto(
                id = 2L,
                skuCode = "MB-ATB-02",
                skuName = "ATB MARIE COCONUT 6 PACK X 4 BKS X 180 GR",
                category = "Marie",
                isTop5 = false,
                isNpl = true,
                planogramImageUrl = null,
                priceRange = PriceRangeDto(priceCode = "AHRTKEP", minPrice = 5200.0, maxPrice = 6500.0)
            )
        )
        val payload = ProductsResponse(status = "success", priceCode = "AHRTKEP", data = products)
        return buildJsonResponse(request, 200, gson.toJson(payload))
    }

    private fun handleSupervisors(request: Request): Response {
        val supervisors = listOf(
            SupervisorDto(
                id = 8L,
                salesCode = "SPVKEPRI1",
                nip = "SPVKEPRI1",
                name = "BAMBANG WAHYUDI",
                areaName = "Kepri",
                coverageDistributors = "[DABTM001] PT. Dwi Niaga Indonesia (Batam)",
                photoUrl = "",
                isTarget = true
            ),
            SupervisorDto(
                id = 7L,
                salesCode = "SPVKEPRI2",
                nip = "SPVKEPRI2",
                name = "SIGIT WICAKSONO",
                areaName = "Kepri",
                coverageDistributors = "[DATPG001] PT. Prima Bintan Muliatama (Tjg Pinang)",
                photoUrl = "",
                isTarget = false
            ),
            SupervisorDto(
                id = 6L,
                salesCode = "SPVKEPRI3",
                nip = "SPVKEPRI3",
                name = "HADIRAT HALAWA",
                areaName = "Kepri",
                coverageDistributors = "[DATBK001] CV. Olina Pangestu (Tanjung Balai Karimun)",
                photoUrl = "",
                isTarget = false
            ),
            SupervisorDto(
                id = 9L,
                salesCode = "SPVBENGKULU3",
                nip = "SPVBENGKULU3",
                name = "ANDREAS",
                areaName = "Bengkulu",
                coverageDistributors = "[DABGL001] PT. Marhum Rodamas Abadi (Bengkulu)",
                photoUrl = "",
                isTarget = false
            )
        )
        val payload = SupervisorsResponse(status = "success", data = supervisors)
        return buildJsonResponse(request, 200, gson.toJson(payload))
    }

    private fun handlePhotoUpload(request: Request): Response {
        val fileName = "demo_${System.currentTimeMillis()}.jpg"
        val payload = PhotoUploadResponse(
            status = "success",
            message = "Foto berhasil diunggah (Mode Demo)",
            data = PhotoUploadData(
                filePath = "uploads/demo/$fileName",
                fullUrl = "https://demo.local/uploads/demo/$fileName"
            )
        )
        return buildJsonResponse(request, 200, gson.toJson(payload))
    }

    private fun handleBatchSync(request: Request): Response {
        val bodyJson = readRequestBodyAsString(request)
        val batchReq = runCatching { gson.fromJson(bodyJson, BatchSyncRequest::class.java) }.getOrNull()

        val leaderIds = (batchReq?.leaderVisits ?: emptyList()).ifEmpty { batchReq?.leaderSurveys ?: emptyList() }
            .map { it.offlineSyncId }
        val memberIds = (batchReq?.memberVisits ?: emptyList()).ifEmpty { batchReq?.memberSurveys ?: emptyList() }
            .map { it.offlineSyncId }
        val results = (leaderIds + memberIds).map { BatchSyncItemResultDto(offlineSyncId = it, status = "synced") }

        val payload = BatchSyncResponse(
            status = "success",
            message = "Sinkronisasi batch Mode Demo berhasil",
            syncedCount = results.size,
            results = results,
            data = BatchSyncResultData(
                syncedLeaderSyncIds = leaderIds,
                syncedMemberSyncIds = memberIds,
                failedItems = emptyList()
            )
        )
        return buildJsonResponse(request, 200, gson.toJson(payload))
    }

    private fun handleSingleLeaderSubmit(request: Request): Response {
        val bodyJson = readRequestBodyAsString(request)
        val req = runCatching { gson.fromJson(bodyJson, LeaderSurveyRequest::class.java) }.getOrNull()
        val isIncomplete = req?.isIncomplete == true
        val payload = SurveySubmitResponse(
            status = "success",
            message = "Survei Leader berhasil disimpan.",
            data = SurveyResultData(
                visitId = System.currentTimeMillis(),
                outletId = req?.outletId,
                offlineSyncId = req?.offlineSyncId.orEmpty(),
                totalScore = if (isIncomplete) 0.0 else 100.0,
                status = "submitted",
                statusText = "Menunggu Member menyelesaikan audit fisik",
                isSurveyComplete = false,
                finalCombinedScore = null
            )
        )
        return buildJsonResponse(request, 200, gson.toJson(payload))
    }

    private fun handleSingleMemberSubmit(request: Request): Response {
        val bodyJson = readRequestBodyAsString(request)
        val req = runCatching { gson.fromJson(bodyJson, MemberSurveyRequest::class.java) }.getOrNull()
        val isIncomplete = req?.isIncomplete == true
        val payload = SurveySubmitResponse(
            status = "success",
            message = "Audit fisik Member berhasil disimpan.",
            data = SurveyResultData(
                visitId = System.currentTimeMillis(),
                outletId = req?.outletId,
                offlineSyncId = req?.offlineSyncId.orEmpty(),
                totalScore = if (isIncomplete) 0.0 else 95.0,
                status = "submitted",
                statusText = if (isIncomplete) "Perlu Kunjungan Ulang (Revisit)" else "Survei Lengkap! Skor Terpadu: 95.75%",
                geotagScoreRate = if (isIncomplete) 0.0 else 1.0,
                rakScoreRate = if (isIncomplete) 0.0 else 1.0,
                priceTagScoreRate = if (isIncomplete) 0.0 else 1.0,
                priceRangeScoreRate = if (isIncomplete) 0.0 else 1.0,
                isSurveyComplete = !isIncomplete,
                finalCombinedScore = if (isIncomplete) null else 95.75
            )
        )
        return buildJsonResponse(request, 200, gson.toJson(payload))
    }

    private fun readRequestBodyAsString(request: Request): String {
        val body = request.body ?: return ""
        return runCatching {
            val buffer = Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        }.getOrDefault("")
    }

    private fun buildJsonResponse(request: Request, code: Int, jsonBody: String): Response {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        return Response.Builder()
            .code(code)
            .message(if (code in 200..299) "OK" else "Error")
            .protocol(Protocol.HTTP_1_1)
            .request(request)
            .body(jsonBody.toResponseBody(mediaType))
            .build()
    }
}
