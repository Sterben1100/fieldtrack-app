# FieldTrack Pro (ASW Foods) — Dokumentasi Integrasi & Status Aplikasi Mobile (v2.1.0)

**Terakhir Diperbarui:** 20 November 2026  
**Aplikasi Android:** `com.aswfoods.fieldtrack` (Android Native — Kotlin & Jetpack Compose Material 3)  
**Target Integrasi Backend:** `fieldtrack-pro` (Laravel 12 + PostgreSQL 18 + Laravel Sanctum)  
**Perangkat Uji Fisik:** Xiaomi Redmi Note 10S (`Xiaomi M2101K7BNY`)

> **Catatan untuk Tim Pengembang Web Backend:** Dokumen ini berisi spesifikasi teknis terkini aplikasi mobile Android beserta matriks kontrak API yang sudah diimplementasikan dan disesuaikan dengan `memory (1).md`, `memory_mobile 5.md` (v2.1.0), serta skema database `fieldtrack_db.sql`.

---

## 1. Ringkasan Eksekutif & Pembagian Tanggung Jawab

| Komponen | Cakupan Sistem |
| :--- | :--- |
| **Aplikasi Mobile Android** (`fieldtrack-app`) | Antarmuka lapangan untuk **Surveyor Leader** (L1–L12 + pertanyaan CMS dinamis seperti L99) dan **Surveyor Member** (M1 Geotag 15m + Kalibrasi Titik Toko, M2 Rak Sewa, M3 Price Tag, M4 Audit Harga 6 SKU Fokus), arsitektur *Offline-First* (Room DB v6 + WorkManager), kompresi foto lokal, Mode Demo lengkap, dan navigasi Google Maps Directions. |
| **Web Backend & CMS Admin** (`fieldtrack-pro`) | Otentikasi Laravel Sanctum, manajemen master rute harian (10 Wajib + 5 Tambahan), manajemen 5 Pilar outlet & harga GM per `price_code`, CMS master pertanyaan survei, upload foto bukti (`/api/v1/media/upload`), penghitungan skor akhir gabungan (`85% Member + 15% Leader L12`), serta endpoint idempotent `/batch-sync`. |

Seluruh kontrak REST API v2.1.0 dari `memory_mobile 5.md` dan struktur data relasional dari `fieldtrack_db.sql` telah diintegrasikan ke dalam kode produksi Android.

---

## 2. Konfigurasi Jaringan, Server (Port 8000) & Keamanan Transport

Aplikasi Android mendukung pergantian IP server secara dinamis langsung dari layar Login tanpa perlu kompilasi ulang APK.

### 2.1 Preset & Normalisasi Base URL (`SessionManager.kt`)
* **WiFi Hotspot Server (Default):** `http://192.168.89.178:8000/` (`WIFI_BASE_URL`)
* **LAN Server:** `http://192.168.88.64:8000/` (`LAN_BASE_URL`)
* **Android Emulator:** `http://10.0.2.2:8000/` (`EMULATOR_BASE_URL`)
* **Custom IP/URL:** Pengguna dapat mengetik IP/domain server apa saja pada pengaturan server di `LoginScreen.kt`. Fungsi `SessionManager.normalizeBaseUrl()` otomatis menambahkan skema `http://` jika belum ada, memastikan trailing slash `/`, dan mencegah duplikasi sufiks `/api/v1`.

### 2.2 Cleartext HTTP & Rewrite URL Media (`network_security_config.xml` & `RetrofitClient.kt`)
* **`res/xml/network_security_config.xml`**: Sudah mengaktifkan `<base-config cleartextTrafficPermitted="true" />` beserta domain eksplisit (`10.0.2.2`, `localhost`, `127.0.0.1`, `192.168.89.178`, `192.168.88.64`) dan atribut `android:usesCleartextTraffic="true"` di `AndroidManifest.xml`, sehingga komunikasi HTTP port `8000` di IP lokal mana pun berjalan lancar di Android 9+.
* **Auto-Rewrite URL Media Lokal (`RetrofitClient.rewriteLocalhostUrl`)**: Apabila server Laravel mengembalikan URL media (foto supervisor untuk L12 atau foto planogram SKU) dengan host `localhost`, `127.0.0.1`, atau `10.0.2.2`, aplikasi Android otomatis menulis ulang host dan port-nya mengikuti IP server aktif yang sedang dipakai perangkat fisik.

---

## 3. Arsitektur & Struktur Proyek Android

* **Language & UI:** Kotlin + Jetpack Compose (Material 3)
* **Networking:** Retrofit 2.11 + OkHttp 4.12 + Gson (`AuthInterceptor`, dynamic Base URL `Interceptor`, `MockApiInterceptor`)
* **Local Persistence:** Room Database v6 (`fieldtrack_asw_db`) + `SessionManager` terenkripsi Android Keystore (`AES/GCM/NoPadding`)
* **Background Sync:** WorkManager (`SyncWorker` + `SyncManager`) dengan constraint jaringan aktif
* **Lokasi & Media:** Google Play Services Location (`FusedLocationProviderClient`), Android `Geocoder` reverse-geocoding, Camera FileProvider + Coil (`AsyncPhotoPreview`)

```text
app/src/main/java/com/aswfoods/fieldtrack/
├── FieldTrackApplication.kt
├── MainActivity.kt                              # Root navigasi Compose + NetworkStatusBar di top bar
├── data/
│   ├── local/
│   │   ├── SessionManager.kt                    # AES-256-GCM session storage, preset IP :8000, Demo flag
│   │   ├── dao/
│   │   │   ├── OutletDao.kt                     # Query rute harian & simpan kalibrasi titik koordinat toko
│   │   │   ├── PendingVisitDao.kt               # Antrean sinkronisasi offline & status sync per toko/hari
│   │   │   ├── ProductDao.kt
│   │   │   ├── SupervisorDao.kt
│   │   │   └── SurveyQuestionDao.kt
│   │   ├── database/
│   │   │   └── AppDatabase.kt                   # Room Database version = 6 (fallbackToDestructiveMigration)
│   │   └── entity/
│   │       ├── OutletEntity.kt                  # Outlet 5 Pilar, partner, revisit, kalibrasi GPS & riwayat survei
│   │       ├── PendingVisitEntity.kt            # Antrean survei (`PENDING`, `SYNCED`, `FAILED`)
│   │       ├── ProductEntity.kt                 # 6 SKU Fokus + minPrice/maxPrice GM
│   │       ├── SupervisorEntity.kt              # Data SPV & target L12
│   │       └── SurveyQuestionEntity.kt          # Master pertanyaan dinamis dari CMS Web
│   ├── remote/
│   │   ├── RetrofitClient.kt                    # OkHttp + dynamic BaseUrl + rewriteLocalhostUrl
│   │   ├── api/FieldTrackApiService.kt          # Interface seluruh endpoint `/api/v1/...`
│   │   ├── dto/
│   │   │   ├── AuthDto.kt
│   │   │   ├── MasterDto.kt                     # Dual Array/Object DailyRoutesResponse & 5 Pilar fields
│   │   │   └── SurveyDto.kt                     # Leader, Member, PhotoUpload, Dual BatchSync DTO
│   │   └── interceptor/
│   │       ├── AuthInterceptor.kt               # Header Authorization Bearer Sanctum & Accept: application/json
│   │       └── MockApiInterceptor.kt            # Mode Demo offline lengkap (data riil Batam/Bengkulu + 3 toko uji)
│   └── repository/
│       ├── MasterDataRepository.kt              # Sync master + selectFocus6Products + preservasi kalibrasi GPS
│       └── SurveyRepository.kt                  # Upload foto bertahap + batch-sync + fallback 4xx per kunjungan
├── ui/
│   ├── components/
│   │   ├── IncompleteCheckoutDialog.kt          # Modal Check-Out Dini / Toko Terkendala
│   │   └── NetworkStatusBar.kt                  # Indikator ping (ms) & kualitas sinyal di bagian atas layar
│   ├── screens/
│   │   ├── dashboard/
│   │   │   ├── DashboardScreen.kt               # Rute harian, 5 Pilar badge, OutletProfileDialog + Open Direction
│   │   │   └── DashboardViewModel.kt
│   │   ├── leader/
│   │   │   ├── LeaderSurveyScreen.kt            # Form L1-L12 + L10 text_notes + L11 rating + L12 Blind Test 3 foto
│   │   │   └── LeaderSurveyViewModel.kt
│   │   ├── login/
│   │   │   ├── LoginScreen.kt                   # Form login, Mode Demo dinamis, pengaturan IP server
│   │   │   └── LoginViewModel.kt
│   │   └── member/
│   │       ├── MemberSurveyScreen.kt            # Audit M1-M4, tombol "Perbarui Titik Toko" & pop-up peringatan
│   │       └── MemberSurveyViewModel.kt
│   └── theme/ (Color.kt, Theme.kt, Type.kt)
├── util/
│   ├── ImageHelper.kt                           # Kompresi JPEG & FileProvider URI
│   ├── LocationHelper.kt                        # FusedLocation GPS + MAX_GEOTAG_RADIUS_METERS (15m) + Geocoder
│   └── NetworkMonitor.kt                        # Pemantauan TCP ping (Good/Medium/Bad/Offline) setiap 8 detik
└── worker/
    ├── SyncManager.kt
    └── SyncWorker.kt                            # Worker sinkronisasi otomatis antrean kunjungan & foto
```

---

## 4. Matriks Kompatibilitas Kontrak REST API (`/api/v1`)

Seluruh komunikasi memakai header `Accept: application/json` dan `Authorization: Bearer {token}` (kecuali login).

### 4.1 Otentikasi (`AuthDto.kt`)
* **`POST api/v1/auth/login`**
  * **Request Body:** `{ "username": String, "password": String, "device_id": String? }`
  * **Response Yang Diharapkan:** `{ "status": "success", "message": "...", "data": { "token": "...", "user": { "id": Long, "name": String, "username": String, "role": "leader" | "member", "branch_id": Long?, "branch_code": String?, "branch_name": String?, "price_code": String? } } }`
* **`POST api/v1/auth/logout`**: Menghapus sesi token di server sebelum token lokal dihapus oleh `SessionManager.clearSession()`.

### 4.2 Rute Harian & Profil Toko (`GET api/v1/master/daily-routes?date=YYYY-MM-DD`)
* **Fleksibilitas Parser `DailyRoutesResponse.extractRoutePlan()`**:
  * Aplikasi Android mendukung `data` berupa **JSON Array langsung** (sesuai contoh `memory_mobile 5.md`) **maupun JSON Object** (`{ "plan_id", "plan_date", "branch_code", "price_code", "total_outlets", "partner_name", "outlets": [...] }`).
* **Field per Outlet (`OutletDto`) yang Sudah Dipetakan ke `OutletEntity`**:
  * Identitas & lokasi: `id`, `branch_id`, `kode_toko`, `nama_toko`, `alamat`, `latitude`, `longitude`, `owner_name`, `contact_phone`.
  * Segmentasi & Harga: `class_outlet` (`RWO`, `Pareto`, `NGVO`, `Grooming`, `NOO`, `Trader`, `GT Biasa`), `is_pareto`, `price_code`, `effective_price_code` (diprioritaskan Android jika tersedia), `is_sewa_display`.
  * Status Rute & Kolaborasi Pasangan: `route_plan_id`, `stop_order`, `outlet_type` (`"wajib"` / `"tambahan"`), `is_visited_by_me`, `is_visited_leader`, `is_visited_member`, `is_survey_complete`, `final_combined_score`, `needs_revisit`, `revisit_reason`, `partner_name`.
  * Riwayat Survei Sebelumnya: `has_surveyed_before`, `last_survey_date`, `last_survey_date_formatted`, `last_survey_score`.

### 4.3 Katalog 6 SKU Fokus & Harga GM (`GET api/v1/master/products?outlet_id={outlet_id}`)
* Pada `MemberSurveyViewModel.initSurvey()`, `refreshOutletProductsPriceRange(outletId)` memanggil `getProducts(outlet_id = outletId)` di latar belakang untuk menyegarkan `price_range` (`price_code`, `min_price`, `max_price`) per outlet sesuai `effective_price_code` toko (atau fallback ke data Room lokal apabila sedang offline).
* **Proteksi 6 SKU Fokus (`MasterDataRepository.selectFocus6Products`)**: Idealnya backend langsung mengembalikan maksimal 6 SKU Fokus (`5 Top SKU + 1 NPL`). Namun jika backend mengirim lebih dari 6 item (misalnya seluruh master produk), Android otomatis menyeleksi 5 SKU dengan `is_top5 = true && is_npl = false` dan 1 SKU dengan `is_npl = true` (dengan prioritas yang memiliki `price_range.max_price > 0`).

### 4.4 Master Supervisor untuk Blind Test L12 (`GET api/v1/master/supervisors`)
* Mengambil daftar `SupervisorDto` (`id`, `sales_code`, `nip`, `name`, `area_name`, `coverage_distributors`, `photo_url`, `is_target`).
* Pada form Leader L12, Android menampilkan 3 pilihan foto SPV secara acak (1 SPV target cabang asli + 2 SPV cabang lain sebagai pengecoh). URL foto yang mengandung `localhost`/`127.0.0.1`/`10.0.2.2` otomatis ditulis ulang oleh `RetrofitClient.rewriteLocalhostUrl()`.

### 4.5 Master Kuesioner Dinamis (`GET api/v1/master/survey-questions?role=leader`)
* Mengambil daftar `SurveyQuestionDto` (`id`, `question_code`, `aspect`, `question_text`, `answer_type`, `scoring_guide`, `weight`, `is_active`).
* **Dukungan Rendering Dinamis di `LeaderSurveyScreen.kt`**:
  * `yes_no_na` (L1–L9 & pertanyaan dinamis baru seperti `L99`): Tombol pilihan `Ya`, `Tidak`, dan `N/A` (`L1` tidak mengizinkan `N/A`). Jika `L9 = "Ya"`, kolom catatan wajib diisi.
  * `text_notes` (`L10`): Merender tombol pilihan `Ya` / `Tidak` / `N/A` sekaligus kolom input catatan teks (*"Catatan / Masukan Toko"*).
  * `rating_0_10` / `rating` (`L11`): Pilihan angka skala `0..10`.
  * `photo_choice` (`L12`): Pilihan 3 kartu foto SPV atau opsi `N/A` (PIC tidak ada di tempat — wajib mengisi `pic_absence_reason` minimal 5 karakter dan otomatis menandai `needs_revisit = true`).

### 4.6 Upload Foto Bukti (`POST api/v1/media/upload`)
* **Format Request:** `Multipart/form-data`
  * Part file: key `"photo"`, MIME `"image/jpeg"` (sudah dikompresi oleh `ImageHelper`)
  * Part teks: key `"category"`, bernilai `"rak"` atau `"sku"`
* **Response Yang Diharapkan:** `{ "status": "success", "message": "...", "data": { "file_path": "...", "full_url": "..." } }`
* Pada saat `SurveyRepository.pushPendingVisits()` berjalan, foto lokal pada audit Member diunggah terlebih dahulu. Path file hasil response (`data.file_path`) langsung diperbarui ke `payloadJson` di database lokal Room agar tidak diunggah berulang apabila terjadi retry sinkronisasi.

### 4.7 Submit Survei Leader (`POST api/v1/survey/leader/submit`)
```json
{
  "offline_sync_id": "uuid-v4",
  "outlet_id": 4780,
  "visit_date": "2026-11-20",
  "checkin_latitude": 1.154412,
  "checkin_longitude": 104.128378,
  "checkin_at": "2026-11-20 09:30:00",
  "checkout_at": "2026-11-20 09:45:00",
  "notes": "Survei wawancara Leader selesai",
  "pic_absence_reason": null,
  "is_incomplete": false,
  "incomplete_reason_type": null,
  "incomplete_reason": null,
  "needs_revisit": false,
  "revisit_notes": null,
  "responses": [
    {
      "question_code": "L1",
      "answer_value": "Ya",
      "notes": "",
      "rating_value": null,
      "selected_supervisor_id": null
    },
    {
      "question_code": "L11",
      "answer_value": "9",
      "notes": "",
      "rating_value": 9,
      "selected_supervisor_id": null
    },
    {
      "question_code": "L12",
      "answer_value": "Selected",
      "notes": null,
      "rating_value": null,
      "selected_supervisor_id": 8
    }
  ]
}
```
> Jika `L12` dipilih `N/A`, maka `answer_value` bernilai `"N/A"`, `selected_supervisor_id = null`, `pic_absence_reason` terisi teks, dan `needs_revisit = true`.

### 4.8 Submit Audit Fisik Member (`POST api/v1/survey/member/submit`)
```json
{
  "offline_sync_id": "uuid-v4",
  "outlet_id": 4780,
  "visit_date": "2026-11-20",
  "actual_latitude": 1.154412,
  "actual_longitude": 104.128378,
  "checkin_at": "2026-11-20 09:30:00",
  "checkout_at": "2026-11-20 09:50:00",
  "notes": "Audit fisik Member selesai dicatat",
  "has_sewa_display": true,
  "rak_condition": "compliant",
  "rak_photo_path": "uploads/rak/xxx.jpg",
  "is_incomplete": false,
  "incomplete_reason_type": null,
  "incomplete_reason": null,
  "needs_revisit": false,
  "revisit_notes": null,
  "sku_audits": [
    {
      "product_id": 2,
      "has_price_tag": true,
      "is_price_tag_compliant": true,
      "actual_price": 8500.0,
      "sku_photo_path": "uploads/sku/xxx.jpg"
    }
  ]
}
```
* **Daftar Nilai `rak_condition` dari UI Mobile:**
  * `"compliant"` → Terpasang & Sesuai Planogram (100%)
  * `"items_5_non_compliant"` → 5 Item Terpajang, Tidak Sesuai (75%)
  * `"items_3_non_compliant"` → 3 Item Terpajang, Tidak Sesuai (50%)
  * `"items_2_non_compliant"` → 2 Item Terpajang, Tidak Sesuai (25%)
  * `"no_items"` → Tidak Ada Produk Terpajang (0%)
  *(Kalkulator skor lokal juga tetap mengenali nilai lama `"terpasang_rapih"` = 100%).*

### 4.9 Batch Sync Offline (`POST api/v1/survey/batch-sync`)
* **Request Body (`BatchSyncRequest`)**: Mengirimkan daftar kunjungan Leader dan Member sekaligus menggunakan pasangan key `leader_visits` / `member_visits` maupun alias `leader_surveys` / `member_surveys` secara bersamaan sehingga kompatibel dengan implementasi backend mana pun.
* **Response Handling (`BatchSyncResponse`)**: Android mengenali konfirmasi sukses baik dari format `data.synced_leader_sync_ids` + `data.synced_member_sync_ids` maupun array `results` (`[{ "offline_sync_id": "...", "status": "synced" }]`).
* **Isolasi Error 4xx**: Jika `/batch-sync` mengembalikan HTTP `400..499`, Android otomatis memanggil fallback `pushPendingIndividually()` untuk mengirim satu per satu lewat `/survey/leader/submit` dan `/survey/member/submit` agar antrean valid tetap tersimpan dan item bermasalah ditandai `FAILED` beserta pesan errornya.

---

## 5. Fitur Lapangan, Logika Bisnis & Pembaruan UI Terkini

1. **Kalibrasi Titik Toko ("Perbarui Titik Toko") — M1 Geotagging:**
   * Jika koordinat master toko belum tersedia (`null`) atau selisih jarak GPS surveyor melebihi `15.0 meter`, tombol **"Perbarui Titik Toko"** muncul di kartu M1 maupun di dialog peringatan radius > 15m.
   * Menekan tombol tersebut memunculkan pop-up peringatan yang meminta surveyor berdiri tepat di titik lokasi toko berada sebelum mengambil GPS aktual HP.
   * Titik koordinat hasil pembaruan disimpan secara persisten di `OutletEntity` (`isCoordinateCalibrated = true`), dipertahankan meski `syncMasterData()` berjalan ulang, dan dikirim melalui field `actual_latitude` & `actual_longitude` pada saat submit survei Member.
   * **Catatan Koordinasi Tim Web:** Saat ini hasil kalibrasi titik baru dikirim via `actual_latitude`/`actual_longitude` pada `MemberSurveyRequest` serta disimpan di lokal Room. Jika Web Dashboard Admin ingin koordinat master `outlets` di PostgreSQL otomatis ter-update dari field tersebut atau membutuhkan endpoint `PATCH` khusus kalibrasi titik, mohon kabari tim Mobile agar disinkronkan.
2. **Kalkulasi Skor & Redistribusi Bobot Member (`MemberSurveyViewModel.recalculateScores`):**
   * **M1 Geotagging:** Bobot `25%` (`≤15m` = 100%, `≤25m` = 75%, `≤50m` = 50%, `≤100m` = 25%, `>100m` = 0%).
   * **M2 Rak Sewa Display:** Bobot `20%` (aktif jika `is_sewa_display == true` dan `class_outlet != "Trader"`).
   * **M3 Price Tag Planogram:** Bobot `20%` (aktif jika `class_outlet != "Trader"`).
   * **M4 Kesesuaian Harga GM Area (6 SKU Fokus):** Bobot `20%`.
   * Total bobot aktif Member (`0.85` / 85% bobot tim) otomatis diredistribusi secara proporsional apabila M2 atau M3 berstatus N/A. Label persentase bobot di judul kartu penilaian pada UI telah dihilangkan sesuai permintaan produk.
3. **Profil Toko, 5 Pilar & Navigasi Google Maps (`DashboardScreen.kt`):**
   * Ketuk kartu toko di Dashboard untuk membuka `OutletProfileDialog` (detail alamat, PIC/telepon, koordinat, kode harga, riwayat survei `has_surveyed_before`, status kunjungan pasangan, serta tombol **"Open Direction"** langsung ke mode Direction Google Maps dan tombol mulai/edit/revisit survei).
   * Pewarnaan badge 5 Pilar (`class_outlet`): `RWO` (`#B2000C`), `Pareto` (`#8B5CF6`), `NGVO` (`#059669`), `Grooming` (`#D97706`), `NOO` (`#64748B`).
4. **Indikator Jaringan Live, Performa Scroll 60fps & Mode Demo:**
   * `NetworkStatusBar` di bagian teratas layar mengukur TCP ping setiap 8 detik (`Good ≤ 100ms`, `Medium ≤ 250ms`, `Bad > 250ms`, `Offline`) dalam komponen terisolasi agar tidak memicu rekomposisi layar lain.
   * Daftar rute Dashboard dan kartu form survei dioptimalkan tanpa jank (pre-composed `Column` + `key`, kontainer datar ringan, serta anotasi `@Stable` / `@Immutable`).
   * **Mode Demo (`MockApiInterceptor`):** Tombol quick-fill leader/member di halaman Login sudah dihilangkan sehingga peran ditentukan dari username yang diinput (kata sandi demo: `123456` atau `password123`; username dengan kata `leader`/`ketua` masuk sebagai Leader, lainnya sebagai Member; tersedia profil spesifik `leader_batam`, `member_batam`, `leader_bengkulu`, `member_bengkulu`, `leader01`, `member01`). Data rute demo memuat outlet riil Batam & Bengkulu dari `fieldtrack_db.sql` ditambah 3 toko uji koordinat lapangan, 6 SKU fokus riil, dan 4 SPV riil.
