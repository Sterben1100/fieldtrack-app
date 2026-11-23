# Memory Dokumen: Arsitektur Backend, Skema Database, & Integrasi API Mobile
**Aplikasi Mobile:** FieldTrack Pro (`com.aswfoods.fieldtrack`)  
**Backend:** Laravel 12 REST API (Sanctum Authentication) + PostgreSQL 18  
**Versi Sistem:** 1.0.0 (Field Survey & Audit Lapangan ASW Foods)  
**Terakhir Diperbarui:** 17 November 2026  

---

## 1. Panduan Koneksi APK Android ke Backend Lokal

### A. Base URL Sesuai Lingkungan Pengujian
| Lingkungan Device | Base URL API | Keterangan |
| :--- | :--- | :--- |
| **Android Emulator (AVD)** | `http://10.0.2.2:8080/api/v1/` | `10.0.2.2` adalah alias IP localhost PC host dari emulator |
| **Real Device (Kabel USB / Wi-Fi)** | `http://<IP_PC_PENGEMBANG>:8080/api/v1/` | Contoh: `http://192.168.1.15:8080/api/v1/` (pastikan satu jaringan Wi-Fi) |
| **Server UAT / Staging** | `https://api-survey.aswfoods.com/api/v1/` | HTTPS resmi produksi |

### B. Konfigurasi Cleartext Traffic di Android Studio
Karena backend lokal menggunakan protokol HTTP (`http://`), Android 9.0+ (API 28+) memblokir lalu lintas tanpa enkripsi secara default.

Tambahkan di `app/src/main/AndroidManifest.xml`:
```xml
<application
    android:name=".FieldTrackApp"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.FieldTrackPro"
    android:usesCleartextTraffic="true"
    android:networkSecurityConfig="@xml/network_security_config">
```

Dan buat berkas `app/src/main/res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">127.0.0.1</domain>
        <domain includeSubdomains="true">192.168.1.15</domain>
    </domain-config>
</network-security-config>
```

---

## 2. Akun Surveyor Default untuk Pengujian di APK

Database telah diisi dengan pasangan tim surveyor (*Surveyor Pair*) resmi:

| Peran | Username | Password | Cabang Distributor | Kode Harga (GROUPPRICE) |
| :--- | :--- | :--- | :--- | :--- |
| **Leader (Auditor)** | `leader01` | `password123` | ASW Foods Medan (`BR-MDN-01`) | `AHRTMDN` |
| **Member (Merchandiser)** | `member01` | `password123` | ASW Foods Medan (`BR-MDN-01`) | `AHRTMDN` |
| **Leader 02** | `leader02` | `password123` | ASW Foods Medan 02 | `AHRTMDN` |
| **Member 02** | `member02` | `password123` | ASW Foods Medan 02 | `AHRTMDN` |
| **Leader 03** | `leader03` | `password123` | ASW Foods Jakarta (`BR-JKT-01`) | `AHCOMDN` |
| **Member 03** | `member03` | `password123` | ASW Foods Jakarta (`BR-JKT-01`) | `AHCOMDN` |

---

## 3. Arsitektur Peran & Formula Penilaian Terpadu (100%)

FieldTrack Pro membagi penugasan surveyor lapangan menjadi dua peran spesifik per pasangan tim (*surveyor pair*):

```
                                [General Manager Area / HO Admin]
                                                │
                                    [Rencana Rute Harian]
                                (10 Toko Wajib + 5 Tambahan)
                                                │
                     ┌──────────────────────────┴──────────────────────────┐
                     ▼                                                     ▼
           Surveyor LEADER (Ketua)                               Surveyor MEMBER (Anggota)
           • Wawancara Pemilik Toko (L1 - L12)                   • Audit Fisik Terukur (M1 - M4)
           • SOP Pembacaan Pertanyaan Baku                       • Keakuratan Geotag GPS (25%)
           • Kepatuhan Jadwal Sales & Program                    • Kondisi Rak Display Sewa (20%)
           • Penilaian Net Promoter Score (NPS)                  • Price Tag Planogram Top 5 + 1 (20%)
           • Validasi Blind Test Foto SPV Cabang (15%)           • Kepatuhan Harga Jual GM Area (20%)
```

### A. Formula Total Survey Score (Skala 100%)
$$\text{Total Survey Score} = \underbrace{(W_{M1} + W_{M2} + W_{M3} + W_{M4})}_{85\%\text{ (Fisik Member)}} + \underbrace{W_{L12}}_{15\%\text{ (Pengenalan SPV Leader)}} = 100\%$$

| Kode | Aspek Penilaian | Peran | Bobot | Cara Penskoran | Keberlakuan / Catatan |
| :--- | :--- | :---: | :---: | :--- | :--- |
| **M1** | Keakuratan Geotag GPS | Member | **25%** | $\le 10\text{m}: 100\%$, $11-25\text{m}: 75\%$, $26-50\text{m}: 50\%$, $51-100\text{m}: 25\%$, $>100\text{m}: 0\%$ | Berlaku untuk semua jenis toko |
| **M2** | Rak Display Sewa Terpasang | Member | **20%** | Planogram rapi: 100%, 5 item: 75%, 3 item: 50%, 2 item: 25%, Kosong: 0% | **Kondisional**: Hanya jika outlet sewa rak. Trader otomatis **N/A** |
| **M3** | Price Tag Sesuai Planogram | Member | **20%** | Rasio kesesuaian SKU Top 5 + 1 NPL $\left(\frac{X}{6} \times 100\%\right)$ | Berlaku toko umum; Trader otomatis **N/A** |
| **M4** | Rentang Harga Jual GM Area | Member | **20%** | Rasio SKU dalam batas min/max Kode Harga toko $\left(\frac{X}{6} \times 100\%\right)$ | Berlaku semua toko berdasarkan **Kode Harga** |
| **L12** | Validasi Pengenalan SPV | Leader | **15%** | Benar = 100%, Salah = 0%, N/A = Ditunda (Revisit) | Wawancara blind test 3 foto SPV cabang |

### B. Aturan Redistribusi Bobot Proporsional (N/A Handling)
Jika suatu kriteria bernilai **N/A** (misal toko bukan peserta sewa display, atau tipe Trader):
$$\text{Final Score (\%)} = \frac{\sum (\text{Skor Kriteria Aktif} \times \text{Bobot Kriteria Aktif})}{\sum \text{Bobot Kriteria Aktif}} \times 100$$

### C. Aturan Penguncian Skor & Revisit
1. **Status Parsial:** Skor gabungan `final_combined_score` bernilai `null` sampai kedua surveyor menyelesaikan survei toko tersebut.
2. **Kalkulasi Final:** Saat kedua surveyor telah submit, backend otomatis menggabungkan skor fisik Member (85%) dan L12 Leader (15%).
3. **Peringatan Revisit:** Jika PIC toko tidak di tempat, toko ditandai `needs_revisit = true` dan muncul di rute berikutnya dengan label **`Revisit: Data Belum Lengkap`**.

---

## 4. Spesifikasi Kontrak REST API Android

Seluruh endpoint terproteksi memerlukan Header autentikasi:
```http
Authorization: Bearer <SANCTUM_API_TOKEN>
Accept: application/json
Content-Type: application/json
```

---

### A. Autentikasi Surveyor
* **Endpoint:** `POST /api/v1/auth/login`
* **Request:**
  ```json
  {
    "username": "leader01",
    "password": "password123",
    "device_id": "android_uuid_or_imei_optional"
  }
  ```
* **Response (200 OK):**
  ```json
  {
    "status": "success",
    "message": "Autentikasi surveyor berhasil.",
    "data": {
      "token": "1|qwert123456abcdef...",
      "user": {
        "id": 2,
        "name": "Budi Santoso (Leader)",
        "username": "leader01",
        "role": "leader",
        "branch_id": 1,
        "branch_code": "BR-MDN-01",
        "branch_name": "Distributor ASW Foods Medan",
        "price_code": "AHRTMDN"
      }
    }
  }
  ```

---

### B. Pengambilan Rute Kunjungan Harian (Daily Routes)
* **Endpoint:** `GET /api/v1/master/daily-routes`
* **Query Params (Opsional):** `?date=2026-11-20` (default hari ini)
* **Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": {
      "plan_id": 1,
      "plan_date": "2026-11-20",
      "branch_code": "BR-MDN-01",
      "price_code": "AHRTMDN",
      "total_outlets": 15,
      "partner_name": "Rian Hidayat (Member)",
      "outlets": [
        {
          "id": 101,
          "kode_toko": "TK-MDN-001",
          "nama_toko": "Toko Maju Bersama Gatot Subroto",
          "alamat": "Jl. Gatot Subroto No. 45, Medan",
          "latitude": 3.5852420,
          "longitude": 98.6651230,
          "class_outlet": "RWO",
          "price_code": "AHRTMDN",
          "is_sewa_display": true,
          "owner_name": "Koh Ahok",
          "contact_phone": "081234567890",
          "stop_order": 1,
          "outlet_type": "wajib",
          "is_visited_by_me": false,
          "is_visited_leader": false,
          "is_visited_member": false,
          "is_survey_complete": false,
          "final_combined_score": null,
          "needs_revisit": false,
          "revisit_reason": null
        }
      ]
    }
  }
  ```

---

### C. Pengambilan Master Katalog SKU & Batas Rentang Harga
* **Endpoint:** `GET /api/v1/master/products`
* **Catatan Penting:** Backend otomatis menyesuaikan `price_range` (`min_price` dan `max_price`) sesuai **Kode Harga (`price_code`)** yang berlaku di cabang surveyor login (misal `AHRTMDN`).
* **Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": [
      {
        "id": 1,
        "sku_code": "CC-SHP-36",
        "sku_name": "HATARI SEE HONG PUFF 6 PACK X 6 BKS X 245 GR",
        "category": "See Hong Puff",
        "is_top5": true,
        "is_npl": false,
        "planogram_image_url": "http://10.0.2.2:8080/images/planograms/cc-shp-36.jpg",
        "price_range": {
          "min_price": 5800.0,
          "max_price": 7300.0
        }
      },
      {
        "id": 2,
        "sku_code": "MB-ATB-02",
        "sku_name": "ATB MARIE COCONUT 6 PACK X 4 BKS X 180 GR",
        "category": "Marie",
        "is_top5" : false,
        "is_npl": true,
        "planogram_image_url": "http://10.0.2.2:8080/images/planograms/mb-atb-02.jpg",
        "price_range": {
          "min_price": 5200.0,
          "max_price": 6500.0
        }
      }
    ]
  }
  ```

---

### D. Pengambilan Master Supervisor untuk Blind Test L12
* **Endpoint:** `GET /api/v1/master/supervisors`
* **Kegunaan:** Menampilkan 3 kandidat foto supervisor cabang di layar Leader untuk diuji buta (*blind test*) ke pemilik toko.
* **Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": [
      {
        "id": 1,
        "nip": "SPV-MDN-001",
        "name": "Andi Wijaya, S.E.",
        "photo_url": "http://10.0.2.2:8080/images/supervisors/spv_andi.jpg",
        "is_target": true
      },
      {
        "id": 2,
        "nip": "SPV-MDN-002",
        "name": "Bambang Kusuma",
        "photo_url": "http://10.0.2.2:8080/images/supervisors/spv_bambang.jpg",
        "is_target": false
      },
      {
        "id": 3,
        "nip": "SPV-MDN-003",
        "name": "Hendro Pratama",
        "photo_url": "http://10.0.2.2:8080/images/supervisors/spv_hendro.jpg",
        "is_target": false
      }
    ]
  }
  ```

---

### E. Pengambilan Master Kuesioner Survei Dinamis
* **Endpoint:** `GET /api/v1/master/survey-questions?role=leader` (atau `?role=member`)
* **Response (200 OK):**
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
        "question_text": "Apakah Sales representative mengunjungi toko ini dalam 14 hari terakhir?",
        "answer_type": "yes_no_na",
        "scoring_guide": "Ya = 1, Tidak = 0, N/A tidak dihitung",
        "weight": 1.0,
        "is_active": true
      },
      {
        "id": 12,
        "question_code": "L12",
        "aspect": "Pengenalan SPV",
        "question_text": "Pemilik toko diminta menunjuk FOTO SPV cabang resmi dari 3 pilihan foto...",
        "answer_type": "photo_choice",
        "scoring_guide": "Benar = 1, Salah = 0, N/A = Revisit (Bobot 15% skor akhir)",
        "weight": 0.15,
        "is_active": true
      }
    ]
  }
  ```

---

### F. Pengunggahan Bukti Foto Survei (Multipart Media)
* **Endpoint:** `POST /api/v1/media/upload`
* **Content-Type:** `multipart/form-data`
* **Form Fields:**
  * `photo`: File gambar (`image/jpeg`, `image/png`, maksimal 10MB)
  * `category`: `'rak'` | `'sku'` | `'supervisor'`
* **Response (200 OK):**
  ```json
  {
    "status": "success",
    "message": "Berkas foto berhasil diunggah.",
    "data": {
      "file_path": "storage/uploads/surveys/rak/rak_20261117_112000_abc123xyz789.jpg",
      "full_url": "http://10.0.2.2:8080/storage/uploads/surveys/rak/rak_20261117_112000_abc123xyz789.jpg"
    }
  }
  ```

---

### G. Submit Hasil Wawancara Surveyor Leader
* **Endpoint:** `POST /api/v1/survey/leader/submit`
* **Request Payload (Normal):**
  ```json
  {
    "offline_sync_id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "outlet_id": 101,
    "visit_date": "2026-11-20",
    "checkin_latitude": 3.5852400,
    "checkin_longitude": 98.6651200,
    "checkin_at": "2026-11-20 09:15:00",
    "checkout_at": "2026-11-20 09:40:00",
    "notes": "Pemilik toko kooperatif",
    "responses": [
      { "question_code": "L1", "answer_value": "Ya", "notes": null },
      { "question_code": "L2", "answer_value": "Ya", "notes": null },
      { "question_code": "L3", "answer_value": "Ya", "notes": null },
      { "question_code": "L4", "answer_value": "Ya", "notes": null },
      { "question_code": "L5", "answer_value": "Ya", "notes": null },
      { "question_code": "L6", "answer_value": "Ya", "notes": null },
      { "question_code": "L7", "answer_value": "Ya", "notes": null },
      { "question_code": "L8", "answer_value": "Ya", "notes": null },
      { "question_code": "L9", "answer_value": "Ya", "notes": null },
      { "question_code": "L10", "answer_value": "Minta diskon promosi diperbanyak", "notes": null },
      { "question_code": "L11", "answer_value": "9", "rating_value": 9 },
      { "question_code": "L12", "answer_value": "Ya", "selected_supervisor_id": 1 }
    ]
  }
  ```
* **Response (200 OK):**
  ```json
  {
    "status": "success",
    "message": "Data survey Leader berhasil disimpan dan diskor.",
    "data": {
      "visit_id": 42,
      "offline_sync_id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
      "total_score": 100.0,
      "is_survey_complete": false,
      "final_combined_score": null
    }
  }
  ```

---

### H. Submit Hasil Audit Fisik Surveyor Member
* **Endpoint:** `POST /api/v1/survey/member/submit`
* **Request Payload (Normal):**
  ```json
  {
    "offline_sync_id": "7a3ceb1c-2c5d-4f1e-8bcc-1c0d5a2ebb4f",
    "outlet_id": 101,
    "visit_date": "2026-11-20",
    "actual_latitude": 3.5852410,
    "actual_longitude": 98.6651210,
    "checkin_at": "2026-11-20 09:15:00",
    "checkout_at": "2026-11-20 09:42:00",
    "rak_condition": "terpasang_rapih",
    "rak_photo_path": "storage/uploads/surveys/rak/rak_101.jpg",
    "notes": "Planogram display sesuai standar",
    "sku_audits": [
      {
        "product_id": 1,
        "has_price_tag": true,
        "is_price_tag_compliant": true,
        "actual_price": 6500,
        "sku_photo_path": "storage/uploads/surveys/sku/sku_1.jpg"
      },
      {
        "product_id": 2,
        "has_price_tag": true,
        "is_price_tag_compliant": true,
        "actual_price": 6000,
        "sku_photo_path": "storage/uploads/surveys/sku/sku_2.jpg"
      }
    ]
  }
  ```
* **Response (200 OK):**
  ```json
  {
    "status": "success",
    "message": "Audit fisik Member berhasil disinkronisasi.",
    "data": {
      "visit_id": 43,
      "offline_sync_id": "7a3ceb1c-2c5d-4f1e-8bcc-1c0d5a2ebb4f",
      "total_score": 95.0,
      "geotag_score_rate": 1.0,
      "rak_score_rate": 1.0,
      "price_tag_score_rate": 1.0,
      "price_range_score_rate": 1.0,
      "is_survey_complete": true,
      "final_combined_score": 95.75
    }
  }
  ```

---

### I. Check-Out Dini / Kendala Lapangan (Incomplete Survey Check-Out)
Digunakan ketika surveyor sudah check-in di toko, namun proses survei terhenti/terkendala (misal: PIC tidak di tempat, toko sangat ramai, toko tutup sementara):

* **Endpoint:** `POST /api/v1/survey/leader/submit` (atau `/member/submit`)
* **Request Payload:**
  ```json
  {
    "offline_sync_id": "8c4dea2b-4c5e-4f3a-9cba-3b1d8b4dcb7e",
    "outlet_id": 101,
    "visit_date": "2026-11-20",
    "checkin_latitude": 3.5852400,
    "checkin_longitude": 98.6651200,
    "checkin_at": "2026-11-20 09:15:00",
    "checkout_at": "2026-11-20 09:25:00",
    "is_incomplete": true,
    "incomplete_reason_type": "pic_absent",
    "incomplete_reason": "Pemilik sedang ke luar kota, toko dijaga kasir pengganti",
    "needs_revisit": true,
    "revisit_notes": "Lakukan survei susulan saat pemilik berada di lokasi",
    "responses": []
  }
  ```
* **Pilihan `incomplete_reason_type` Baku:**
  * `pic_absent`: Pemilik / PIC Toko Tidak Berada di Tempat
  * `busy_store`: Toko Sedang Sangat Ramai Dikunjungi Pembeli
  * `store_closed`: Toko Tutup / Sedang Renovasi / Tidak Beroperasi
  * `refused`: Pemilik / Toko Menolak Disurvei
  * `emergency`: Kondisi Darurat / Cuaca Buruk / Akses Terhalang
  * `other`: Lainnya (Alasan Khusus, wajib mengisi `incomplete_reason`)

---

### J. Batch Sync Offline-First (WorkManager)
Ketika surveyor selesai bekerja di area tanpa sinyal (*blank spot*), AndroidX `SyncWorker` mengirim antrean data kunjungan sekaligus:

* **Endpoint:** `POST /api/v1/survey/batch-sync`
* **Request Payload:**
  ```json
  {
    "leader_visits": [
      { /* objek submit leader 1 */ },
      { /* objek submit leader 2 */ }
    ],
    "member_visits": [
      { /* objek submit member 1 */ },
      { /* objek submit member 2 */ }
    ]
  }
  ```
* **Response (200 OK):**
  ```json
  {
    "status": "success",
    "message": "Batch sinkronisasi selesai diproses.",
    "data": {
      "synced_leader_sync_ids": [
        "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
        "8c4dea2b-4c5e-4f3a-9cba-3b1d8b4dcb7e"
      ],
      "synced_member_sync_ids": [
        "7a3ceb1c-2c5d-4f1e-8bcc-1c0d5a2ebb4f"
      ],
      "failed_items": []
    }
  }
  ```

---

## 5. Implementasi Retrofit & Room di Android Studio (Kotlin)

### A. Retrofit API Service Interface (`FieldTrackApiService.kt`)
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
    ): Response<BaseResponse<DailyRoutesResponseData>>

    @GET("master/products")
    suspend fun getProducts(): Response<BaseResponse<List<ProductDto>>>

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

### B. Auth Interceptor untuk Bearer Token (`AuthInterceptor.kt`)
```kotlin
package com.aswfoods.fieldtrack.data.remote

import com.aswfoods.fieldtrack.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = sessionManager.getAuthToken()

        val newRequest = originalRequest.newBuilder().apply {
            header("Accept", "application/json")
            if (!token.isNullOrEmpty()) {
                header("Authorization", "Bearer $token")
            }
        }.build()

        return chain.proceed(newRequest)
    }
}
```

### C. Room Entity Toko / Outlet (`OutletEntity.kt`)
```kotlin
package com.aswfoods.fieldtrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outlets")
data class OutletEntity(
    @PrimaryKey val id: Long,
    val kodeToko: String,
    val namaToko: String,
    val alamat: String,
    val latitude: Double,
    val longitude: Double,
    val classOutlet: String,
    val priceCode: String, // Kode Harga resmi (misal: AHRTMDN)
    val isSewaDisplay: Boolean,
    val ownerName: String?,
    val contactPhone: String?,
    val stopOrder: Int,
    val outletType: String, // 'wajib' atau 'tambahan'
    var isVisitedByMe: Boolean = false,
    var isSurveyComplete: Boolean = false,
    var needsRevisit: Boolean = false,
    var revisitReason: String? = null
)
```

---

## 6. Checklist Verifikasi Integrasi Android

1. [ ] Ganti `BASE_URL` di `BuildConfig` atau Retrofit instance ke IP lokal/emulator.
2. [ ] Jalankan login dengan kredensial surveyor `leader01` atau `member01` (`password123`).
3. [ ] Pastikan respons login mengembalikan `token` dan disimpan di EncryptedSharedPreferences.
4. [ ] Muat daftar rute harian via `GET /api/v1/master/daily-routes` dan simpan ke Room database lokal.
5. [ ] Sinkronkan katalog master SKU via `GET /api/v1/master/products` (verifikasi batas `min_price` dan `max_price` telah sesuai dengan `price_code` cabang).
6. [ ] Uji alur check-in GPS Haversine di pintu toko ($\le 25\text{ m}$).
7. [ ] Uji modal intersepsi check-out dini jika kuesioner belum terisi lengkap.
8. [ ] Matikan koneksi internet (Airplane mode), selesaikan survei di Room SQLite lokal, lalu nyalakan kembali untuk memverifikasi auto-sync WorkManager via `POST /api/v1/survey/batch-sync`.
