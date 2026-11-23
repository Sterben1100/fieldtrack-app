# Panduan Resmi Integrasi APK Android & Backend API FieldTrack Pro
**Dokumen:** Spesifikasi Integrasi Tim Mobile Android (Handover Document)  
**Paket Aplikasi:** `com.aswfoods.fieldtrack` (`fieldtrack-app`)  
**Target Platform:** Android Native (Kotlin 2.0+, Jetpack Compose, Material 3, Room, WorkManager)  
**Min SDK:** 24 (Android 7.0) | **Target SDK:** 35 (Android 15)  
**Backend:** Laravel 12 REST API (Sanctum Token) + PostgreSQL 18  
**Versi Dokumen:** 2.1.0 (Release Terpadu Rute Mingguan & Master Data Region Sumatera 3)  
**Terakhir Diperbarui:** 20 November 2026  

---

## 1. Quick Start Koneksi APK ke Backend

### A. Base URL Lingkungan
| Lingkungan Pengujian | Base URL REST API | Keterangan |
| :--- | :--- | :--- |
| **Android Emulator (AVD)** | `http://10.0.2.2:8000/api/v1/` | Alias loopback host PC lokal untuk emulator Android Studio |
| **Real Device (Wi-Fi Lokal)** | `http://<IP_PC_DEV>:8000/api/v1/` | IP LAN laptop/PC pengembang (HP dan PC wajib terhubung ke 1 Wi-Fi yang sama) |
| **Server Staging / Cloud** | `https://api-survey.aswfoods.com/api/v1/` | Domain HTTPS resmi server staging ASW Foods |

> [!IMPORTANT]
> **Cleartext Traffic Android 9+ (API 28+):**  
> Saat pengujian di `http://10.0.2.2:8000/` atau IP lokal HTTP, Android memblokir koneksi tidak terenkripsi secara default. Pastikan di `AndroidManifest.xml` terpasang:
> ```xml
> <application
>     android:usesCleartextTraffic="true"
>     android:networkSecurityConfig="@xml/network_security_config"
>     ...>
> ```
> Serta buat berkas `res/xml/network_security_config.xml`:
> ```xml
> <?xml version="1.0" encoding="utf-8"?>
> <network-security-config>
>     <base-config cleartextTrafficPermitted="true" />
> </network-security-config>
> ```

---

## 2. Kredensial Akun Surveyor untuk Pengujian

Semua akun terdaftar menggunakan password standar: **`password123`**

### A. Akun Riil Operasional (Cabang Batam):
| Peran | Username | Password | Nama Lengkap | Cabang Penugasan | Grup Harga |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Leader (Auditor)** | `leader_batam` | `password123` | Muhammad Fachrozy | PT. DWI NIAGA INDONESIA BATAM (`DABTM001`, ID: 91) | `AHRTKEP` |
| **Member (Merchandiser)** | `member_batam` | `password123` | Salomo Gusto Leonard | PT. DWI NIAGA INDONESIA BATAM (`DABTM001`, ID: 91) | `AHRTKEP` |

### B. Akun Testing Tambahan (Cabang Lainnya):
| Peran | Username | Password | Nama Lengkap | Cabang Penugasan | Grup Harga |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Leader 01** | `leader01` | `password123` | Budi Santoso | PT. Karunia Citra Sukses Lampung (`DABDL001`) | `AHRTLPG` |
| **Member 01** | `member01` | `password123` | Rian Hidayat | PT. Karunia Citra Sukses Lampung (`DABDL001`) | `AHRTLPG` |
| **Leader 02** | `leader02` | `password123` | Agus Pratama | PT. Marhum Rodamas Abadi Bengkulu (`DABGL001`) | `AHRTMDN` |
| **Member 02** | `member02` | `password123` | Diki Wahyudi | PT. Marhum Rodamas Abadi Bengkulu (`DABGL001`) | `AHRTMDN` |

---

## 3. Konsep Bisnis & Alur Kerja Survei Lapangan

Survei dilakukan oleh **1 Pasangan Surveyor (1 Leader & 1 Member)** yang mengunjungi rute toko yang sama secara bersamaan:

```
                  ┌──────────────────────────────────────────────┐
                  │          RUTE KUNJUNGAN HARIAN (1-15)         │
                  └──────────────────────┬───────────────────────┘
                                         │
                 ┌───────────────────────┴───────────────────────┐
                 ▼                                               ▼
     ┌───────────────────────┐                       ┌───────────────────────┐
     │    SURVEYOR LEADER    │                       │    SURVEYOR MEMBER    │
     │   (Auditor Bisnis)    │                       │ (Auditor Fisik Toko)  │
     └───────────┬───────────┘                       └───────────┬───────────┘
                 │                                               │
                 ├─ L1-L11: Wawancara Pemilik                    ├─ M1: Geotag Koordinat GPS (25%)
                 │  (SOP, Promosi, Reward, NPS)                  ├─ M2: Kondisi Rak Sewa (20%)
                 │                                               ├─ M3: Price Tag Top 6 + NPL (20%)
                 └─ L12: Uji Buta Foto SPV (15%)                 └─ M4: Rentang Harga Jual GM (20%)
                 │                                               │
                 └───────────────────────┬───────────────────────┘
                                         ▼
                 ┌───────────────────────────────────────────────┐
                 │       LOCKING SCORE: SUBMITTED BERDUA         │
                 │ Backend menghitung Total Skor Gabungan (100%) │
                 └───────────────────────────────────────────────┘
```

### A. Tugas Surveyor Leader (Auditor Bisnis):
1. Melakukan wawancara langsung dengan pemilik/penanggung jawab toko berdasarkan kuesioner baku L1–L11.
2. **Validasi Blind Test SPV (L12 - Bobot 15%):**
   - Menampilkan 3 foto wajah acak (1 foto SPV target yang meng-cover cabang tersebut + 2 foto SPV pengecoh acak).
   - Menanyakan kepada pemilik toko: *"Apakah Anda mengenali salah satu dari ketiga orang ini sebagai Supervisor dari ASW Foods?"*
   - Jika pemilik menunjuk foto SPV yang benar $\rightarrow$ Nilai 100%. Jika salah $\rightarrow$ Nilai 0%. Jika SPV belum pernah berkunjung $\rightarrow$ N/A (Revisit).

### B. Tugas Surveyor Member (Auditor Fisik Toko):
1. **M1 — Geotag Koordinat GPS (Bobot 25%):**
   - Mengambil titik koordinat GPS aktual di depan pintu toko via Fused Location Provider.
   - Dihitung deviasi jaraknya terhadap koordinat master toko via Haversine:
     - $\le 10\text{ m} \rightarrow 100\%$, $11-25\text{ m} \rightarrow 75\%$, $26-50\text{ m} \rightarrow 50\%$, $51-100\text{ m} \rightarrow 25\%$, $>100\text{ m} \rightarrow 0\%$.
2. **M2 — Rak Display Sewa Terpasang (Bobot 20%):**
   - Validasi langsung di lapangan apakah toko terpasang rak display sewa atau tidak (`has_sewa_display: Boolean`).
   - Jika Ya: foto bukti rak dan periksa kerapian planogram (Penuh = 100%, 5 item = 75%, 3 item = 50%, 2 item = 25%, Kosong = 0%).
   - Jika toko tidak ikut sewa atau tipe Trader $\rightarrow$ **N/A** (bobot diredistribusikan).
3. **M3 — Price Tag Planogram Top 6 + NPL (Bobot 20%):**
   - Memeriksa kelengkapan label harga pada produk fokus (Top 6 varian + NPL).
   - Rasio kepatuhan: $\frac{\text{Jumlah Item Ber-price tag}}{\text{Jumlah Item Diaudit}} \times 100\%$.
4. **M4 — Rentang Harga Jual GM Area (Bobot 20%):**
   - Menginput harga jual riil toko per SKU.
   - Sistem mencocokkan apakah harga riil berada di antara rentang `min_price` dan `max_price` berdasarkan `price_code` toko tersebut.

### C. Aturan Locking Score (Kunci Penilaian Terpadu):
* Skor gabungan (`final_combined_score`) **hanya akan dihitung oleh server jika KEDUA surveyor telah submit**.
* Jika baru Member yang submit, status kartu toko di aplikasi berstatus: **`Parsial (Menunggu Leader)`**.
* Begitu Leader submit, status berubah menjadi **`Selesai (Skor: XX%)`**.

### D. Intersepsi Check-Out Dini (Incomplete Survey):
Jika surveyor menekan tombol **Check-Out Toko** padahal pertanyaan/audit belum lengkap diisi:
- Aplikasi **wajib memunculkan dialog/modal peringatan kendala lapangan**.
- Surveyor wajib memilih salah satu `incomplete_reason_type`:
  - `pic_absent`: Pemilik / PIC Toko Tidak Berada di Tempat.
  - `busy_store`: Toko Sedang Sangat Ramai Pembeli.
  - `store_closed`: Toko Tutup / Sedang Renovasi / Tidak Beroperasi.
  - `refused`: Pemilik Menolak untuk Disurvei.
  - `emergency`: Kondisi Darurat / Cuaca Buruk / Akses Terhalang.
  - `other`: Lainnya (wajib mengetik alasan spesifik di kolom teks).
- Hasil submit ditandai `is_incomplete: true` dan `needs_revisit: true` agar dijadwalkan ulang pada rute berikutnya.

---

## 4. Spesifikasi Kontrak REST API Lengkap

Semua request setelah login wajib menyertakan HTTP Header:
```http
Authorization: Bearer <TOKEN_SANCTUM>
Accept: application/json
Content-Type: application/json
```

---

### A. Login Surveyor
* **Endpoint:** `POST /api/v1/auth/login`
* **Request Body:**
  ```json
  {
    "username": "leader_batam",
    "password": "password123",
    "device_id": "android_uuid_opsional"
  }
  ```
* **Response Sukses (200 OK):**
  ```json
  {
    "status": "success",
    "message": "Autentikasi surveyor berhasil.",
    "data": {
      "token": "1|sanctum_token_string_here...",
      "user": {
        "id": 23,
        "name": "Muhammad Fachrozy",
        "username": "leader_batam",
        "role": "leader",
        "branch_id": 91,
        "branch_code": "DABTM001",
        "branch_name": "PT. DWI NIAGA INDONESIA BATAM",
        "price_code": "AHRTKEP"
      }
    }
  }
  ```

---

### B. Ambil Rute Kunjungan Harian (Daily Routes)
Mengambil daftar toko yang ditugaskan ke surveyor pada tanggal tertentu (urutan stop 1 sampai 15).
* **Endpoint:** `GET /api/v1/master/daily-routes`
* **Query Params:**
  - `date` (opsional, format `YYYY-MM-DD`, default: tanggal hari ini).
* **Response Sukses (200 OK):**
  ```json
  {
    "status": "success",
    "data": [
      {
        "id": 4035,
        "branch_id": 91,
        "kode_toko": "CABTM00410",
        "nama_toko": "EKO 7500",
        "alamat": "RUKO MARBELA 2 BLOK D3 NO. 6 N 7 - BOTANIA BATAM",
        "latitude": 1.106045,
        "longitude": 104.076021,
        "class_outlet": "RWO",
        "is_pareto": false,
        "price_code": "AHRTKEP",
        "effective_price_code": "AHRTKEP",
        "is_sewa_display": false,
        "owner_name": null,
        "contact_phone": null,
        "route_plan_id": 95,
        "stop_order": 1,
        "outlet_type": "wajib",
        "is_visited_by_me": false,
        "is_visited_leader": false,
        "is_visited_member": false,
        "is_survey_complete": false,
        "final_combined_score": null,
        "needs_revisit": false,
        "revisit_reason": null,
        "partner_name": "Salomo Gusto Leonard",
        "has_surveyed_before": true,
        "last_survey_date": "2026-11-10",
        "last_survey_date_formatted": "10 Nov 2026",
        "last_survey_score": 85.5
      }
    ]
  }
  ```

> [!TIP]
> **Petunjuk UI Mobile:**
> - `stop_order`: Tampilkan sebagai nomor urut pin/kartu stop kunjungan (#1, #2, dst).
> - `outlet_type`: Jika `"wajib"` beri badge bintang merah (`★ Toko Wajib`), jika `"tambahan"` beri badge abu-abu (`Toko Tambahan`).
> - `class_outlet`: Tampilkan badge 5 pilar (RWO = Merah `#b2000c`, Pareto = Ungu `#8b5cf6`, NGVO = Hijau `#059669`, Grooming = Kuning Emas `#d97706`, NOO = Abu-abu `#64748b`).
> - Tombol navigasi rute: Jalankan Intent Google Maps langsung ke koordinat `latitude,longitude`.

---

### C. Ambil Master Produk SKU & Rentang Harga GM (Kriteria 4)
* **Endpoint:** `GET /api/v1/master/products?outlet_id={outlet_id}`
* **Response Sukses (200 OK):**
  ```json
  {
    "status": "success",
    "price_code": "AHRTKEP",
    "data": [
      {
        "id": 1,
        "sku_code": "CC-SHP-36",
        "sku_name": "HATARI SEE HONG PUFF 6 PACK X 6 BKS X 245 GR",
        "category": "See Hong Puff",
        "is_top5": true,
        "is_npl": false,
        "planogram_image_url": "https://api-survey.aswfoods.com/images/planograms/cc-shp-36.jpg",
        "price_range": {
          "price_code": "AHRTKEP",
          "min_price": 6000.0,
          "max_price": 7500.0
        }
      }
    ]
  }
  ```

---

### D. Ambil Master Supervisor Cabang (Blind Test L12)
Mengambil daftar SPV yang bertugas di wilayah/cabang surveyor.
* **Endpoint:** `GET /api/v1/master/supervisors`
* **Response Sukses (200 OK):**
  ```json
  {
    "status": "success",
    "data": [
      {
        "id": 14,
        "sales_code": "SPVKEPRI1",
        "nip": "SPVKEPRI1",
        "name": "ANDI PRASETYO",
        "area_name": "ASW KEPRI",
        "coverage_distributors": "[DABTM001] PT. DWI NIAGA INDONESIA BATAM",
        "photo_url": "https://api-survey.aswfoods.com/storage/uploads/supervisors/spv_14.jpg"
      }
    ]
  }
  ```

---

### E. Ambil Kuesioner Pertanyaan Dinamis
* **Endpoint:** `GET /api/v1/master/survey-questions?role=leader` (atau `?role=member`)
* **Response Sukses (200 OK):**
  ```json
  {
    "status": "success",
    "role": "leader",
    "total_questions": 12,
    "data": [
      {
        "id": 1,
        "question_code": "L1",
        "aspect": "Kunjungan Sales",
        "question_text": "Apakah Sales Representative mengunjungi toko ini secara teratur?",
        "answer_type": "yes_no_na",
        "scoring_guide": "Ya = 100%, Tidak = 0%, N/A tidak dihitung",
        "weight": 1.0,
        "is_active": true
      }
    ]
  }
  ```

---

### F. Upload Bukti Foto (Multipart)
* **Endpoint:** `POST /api/v1/media/upload`
* **Form-Data:**
  - `photo`: File berkas gambar (JPG, PNG, WebP)
  - `category`: `"rak"` | `"sku"` | `"kendala"`
* **Response Sukses (200 OK):**
  ```json
  {
    "status": "success",
    "data": {
      "file_path": "storage/uploads/surveys/rak/rak_4035_1732087200.jpg",
      "full_url": "https://api-survey.aswfoods.com/storage/uploads/surveys/rak/rak_4035_1732087200.jpg"
    }
  }
  ```

---

### G. Submit Survei Leader (Wawancara L1–L12)
* **Endpoint:** `POST /api/v1/survey/leader/submit`
* **Request Body:**
  ```json
  {
    "offline_sync_id": "550e8400-e29b-41d4-a716-446655440000",
    "outlet_id": 4035,
    "visit_date": "2026-11-20",
    "checkin_latitude": 1.106045,
    "checkin_longitude": 104.076021,
    "checkin_at": "2026-11-20 09:15:00",
    "checkout_at": "2026-11-20 09:40:00",
    "notes": "Toko kooperatif dan display rapi",
    "is_incomplete": false,
    "responses": [
      { "question_code": "L1", "answer_value": "Ya" },
      { "question_code": "L2", "answer_value": "Ya" },
      { "question_code": "L3", "answer_value": "Ya" },
      { "question_code": "L4", "answer_value": "Tidak" },
      { "question_code": "L5", "answer_value": "Ya" },
      { "question_code": "L6", "answer_value": "Ya" },
      { "question_code": "L7", "answer_value": "Ya" },
      { "question_code": "L8", "answer_value": "Ya" },
      { "question_code": "L9", "answer_value": "Ya" },
      { "question_code": "L10", "answer_value": "Ya" },
      { "question_code": "L11", "answer_value": "9", "rating_value": 9 },
      { "question_code": "L12", "answer_value": "Ya", "selected_supervisor_id": 14 }
    ]
  }
  ```
* **Response Sukses (200 OK):**
  ```json
  {
    "status": "success",
    "message": "Survei Leader berhasil disimpan.",
    "data": {
      "visit_id": 101,
      "outlet_id": 4035,
      "is_survey_complete": false,
      "status_text": "Menunggu Member menyelesaikan audit fisik"
    }
  }
  ```

---

### H. Submit Survei Member (Audit Fisik M1–M4)
* **Endpoint:** `POST /api/v1/survey/member/submit`
* **Request Body:**
  ```json
  {
    "offline_sync_id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
    "outlet_id": 4035,
    "visit_date": "2026-11-20",
    "actual_latitude": 1.106042,
    "actual_longitude": 104.076019,
    "checkin_at": "2026-11-20 09:15:00",
    "checkout_at": "2026-11-20 09:42:00",
    "has_sewa_display": true,
    "rak_condition": "compliant",
    "rak_photo_path": "storage/uploads/surveys/rak/rak_4035_1732087200.jpg",
    "is_incomplete": false,
    "sku_audits": [
      {
        "product_id": 1,
        "has_price_tag": true,
        "is_price_tag_compliant": true,
        "actual_price": 6500,
        "sku_photo_path": "storage/uploads/surveys/sku/sku_1_4035.jpg"
      }
    ]
  }
  ```
* **Response Sukses (200 OK):**
  ```json
  {
    "status": "success",
    "message": "Audit fisik Member berhasil disimpan.",
    "data": {
      "visit_id": 101,
      "outlet_id": 4035,
      "is_survey_complete": true,
      "final_combined_score": 92.5,
      "status_text": "Survei Lengkap! Skor Terpadu: 92.5%"
    }
  }
  ```

---

### I. Submit Check-Out Dini (Incomplete Survey / Kendala Lapangan)
* **Endpoint:** `POST /api/v1/survey/leader/submit` (atau `/api/v1/survey/member/submit`)
* **Request Body:**
  ```json
  {
    "offline_sync_id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "outlet_id": 4035,
    "visit_date": "2026-11-20",
    "checkin_latitude": 1.106045,
    "checkin_longitude": 104.076021,
    "checkin_at": "2026-11-20 09:15:00",
    "checkout_at": "2026-11-20 09:22:00",
    "is_incomplete": true,
    "incomplete_reason_type": "pic_absent",
    "incomplete_reason": "Pemilik sedang keluar kota, toko hanya dijaga kasir pengganti",
    "needs_revisit": true,
    "responses": []
  }
  ```

---

### J. Batch Sync Saat Online (WorkManager)
Mengirim seluruh data survei lokal yang tertunda (pending) dalam satu payload saat koneksi pulih.
* **Endpoint:** `POST /api/v1/survey/batch-sync`
* **Request Body:**
  ```json
  {
    "leader_surveys": [ /* array payload submit leader */ ],
    "member_surveys": [ /* array payload submit member */ ]
  }
  ```
* **Response Sukses (200 OK):**
  ```json
  {
    "status": "success",
    "synced_count": 5,
    "results": [
      { "offline_sync_id": "550e8400-...", "status": "synced" }
    ]
  }
  ```

---

## 5. Implementasi Room Entity Android (Kotlin)

### A. Outlet Entity (Room)
```kotlin
package com.aswfoods.fieldtrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outlets")
data class OutletEntity(
    @PrimaryKey val id: Long,
    val branchId: Long,
    val kodeToko: String,
    val namaToko: String,
    val alamat: String,
    val latitude: Double,
    val longitude: Double,
    val classOutlet: String, // RWO, Pareto, NGVO, Grooming, NOO
    val isPareto: Boolean,   // Badge status Pareto
    val priceCode: String,   // Kode harga toko (misal: AHRTKEP)
    val isSewaDisplay: Boolean,
    val routePlanId: Long,
    val stopOrder: Int,      // Stop #1 s/d #15
    val outletType: String,  // 'wajib' atau 'tambahan'
    var isVisitedByMe: Boolean = false,
    var isSurveyComplete: Boolean = false,
    var finalCombinedScore: Double? = null,
    var needsRevisit: Boolean = false,
    var revisitReason: String? = null,
    val hasSurveyedBefore: Boolean = false,
    val lastSurveyDate: String? = null,
    val lastSurveyScore: Double? = null
)
```

### B. Offline Survey Queue Entity (Room)
```kotlin
package com.aswfoods.fieldtrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "survey_sync_queue")
data class SurveySyncQueueEntity(
    @PrimaryKey val offlineSyncId: String, // UUID v4
    val outletId: Long,
    val role: String, // "leader" atau "member"
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    var syncStatus: String = "PENDING", // PENDING, SYNCING, SYNCED, FAILED
    var retryCount: Int = 0,
    var lastError: String? = null
)
```

---

## 6. Retrofit API Service (Kotlin)

```kotlin
package com.aswfoods.fieldtrack.data.remote

import com.aswfoods.fieldtrack.data.remote.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface FieldTrackApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<BaseResponse<LoginResponseData>>

    @GET("master/daily-routes")
    suspend fun getDailyRoutes(
        @Query("date") date: String? = null
    ): Response<BaseResponse<List<OutletDto>>>

    @GET("master/products")
    suspend fun getProducts(
        @Query("outlet_id") outletId: Long? = null
    ): Response<BaseResponse<List<ProductDto>>>

    @GET("master/supervisors")
    suspend fun getSupervisors(): Response<BaseResponse<List<SupervisorDto>>>

    @GET("master/survey-questions")
    suspend fun getSurveyQuestions(
        @Query("role") role: String
    ): Response<BaseResponse<List<SurveyQuestionDto>>>

    @Multipart
    @POST("media/upload")
    suspend fun uploadPhoto(
        @Part photo: MultipartBody.Part,
        @Part("category") category: RequestBody
    ): Response<BaseResponse<UploadPhotoResponseData>>

    @POST("survey/leader/submit")
    suspend fun submitLeaderSurvey(
        @Body request: LeaderSurveySubmitRequest
    ): Response<BaseResponse<SurveySubmitResultData>>

    @POST("survey/member/submit")
    suspend fun submitMemberSurvey(
        @Body request: MemberSurveySubmitRequest
    ): Response<BaseResponse<SurveySubmitResultData>>

    @POST("survey/batch-sync")
    suspend fun submitBatchSync(
        @Body request: BatchSyncRequest
    ): Response<BaseResponse<BatchSyncResultData>>
}
```

---

## 7. Implementasi Offline-First dengan WorkManager (`SyncWorker`)

```kotlin
package com.aswfoods.fieldtrack.worker

import android.content.Context
import androidx.work.*
import com.aswfoods.fieldtrack.data.local.dao.SurveySyncQueueDao
import com.aswfoods.fieldtrack.data.remote.FieldTrackApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val apiService: FieldTrackApiService,
    private val queueDao: SurveySyncQueueDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val pendingItems = queueDao.getPendingSurveys()
        if (pendingItems.isEmpty()) return@withContext Result.success()

        var hasError = false

        for (item in pendingItems) {
            try {
                // Update status ke SYNCING
                queueDao.updateStatus(item.offlineSyncId, "SYNCING")

                val response = if (item.role == "leader") {
                    // Parse JSON payload & call submitLeaderSurvey
                    apiService.submitLeaderSurvey(parseLeaderPayload(item.payloadJson))
                } else {
                    apiService.submitMemberSurvey(parseMemberPayload(item.payloadJson))
                }

                if (response.isSuccessful && response.body()?.status == "success") {
                    queueDao.updateStatus(item.offlineSyncId, "SYNCED")
                } else {
                    hasError = true
                    queueDao.updateFailed(item.offlineSyncId, response.message())
                }
            } catch (e: Exception) {
                hasError = true
                queueDao.updateFailed(item.offlineSyncId, e.localizedMessage)
            }
        }

        if (hasError) Result.retry() else Result.success()
    }

    companion object {
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "FieldTrackSyncWork",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
```

---

## 8. Helper Haversine Formula & Navigasi Intent (Kotlin)

### A. Perhitungan Jarak GPS Geotag (Haversine di Android):
```kotlin
package com.aswfoods.fieldtrack.util

import kotlin.math.*

object GeoUtils {
    /**
     * Menghitung deviasi jarak (meter) antara GPS surveyor dan master toko.
     */
    fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371000.0 // Radius bumi dalam meter
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Konversi deviasi meter ke skor persentase Kriteria 1 (Geotag M1).
     */
    fun calculateGeotagScore(distanceMeters: Double): Int {
        return when {
            distanceMeters <= 10.0 -> 100
            distanceMeters <= 25.0 -> 75
            distanceMeters <= 50.0 -> 50
            distanceMeters <= 100.0 -> 25
            else -> 0
        }
    }
}
```

### B. Buka Rute di Aplikasi Google Maps:
```kotlin
fun openGoogleMapsNavigation(context: Context, latitude: Double, longitude: Double, storeName: String) {
    val gmmIntentUri = android.net.Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")
    val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri).apply {
        setPackage("com.google.android.apps.maps")
    }
    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        // Fallback browser / generic geo
        val fallbackIntent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($storeName)")
        )
        context.startActivity(fallbackIntent)
    }
}
```

---

## 9. Checklist Kesiapan Tim Mobile Developer

- [ ] **Konektivitas:** Pastikan `AndroidManifest.xml` mengizinkan cleartext traffic dan Base URL mengarah ke port `8000`.
- [ ] **Login:** Tes login menggunakan akun riil `leader_batam` dan `member_batam` (password: `password123`).
- [ ] **Pemberian Rute Harian:** Panggil `GET /api/v1/master/daily-routes` dan pastikan 10-15 urutan stop tampil di UI kartu kunjungan.
- [ ] **Katalog & Harga:** Panggil `GET /api/v1/master/products?outlet_id={id}` untuk memvalidasi rentang harga GM (`min_price` - `max_price`).
- [ ] **Uji Buta L12:** Tampilkan 3 foto supervisor acak dari `GET /api/v1/master/supervisors` dan validasi pilihan penanggung jawab toko.
- [ ] **Check-Out Dini:** Pastikan modal intersepsi muncul saat surveyor check-out tanpa mengisi pertanyaan lengkap, dengan opsi `incomplete_reason_type`.
- [ ] **Offline Sync:** Verifikasi bahwa saat sinyal terputus, data tersimpan di Room SQLite dan otomatis disinkronkan oleh `SyncWorker` saat jaringan aktif kembali.
