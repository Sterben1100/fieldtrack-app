# Handover & Project Memory: FieldTrack Pro System (ASW Foods)

Dokumen ini berfungsi sebagai **Project Memory & Handover Guide** resmi bagi tim pengembang yang melanjutkan pengembangan ekosistem **FieldTrack Pro** (`com.aswfoods.fieldtrack` untuk Android Mobile dan Web Portal Head Office berbasis Laravel 12 + Inertia.js + Vue 3).

---

## 1. Ringkasan Eksekutif & Tujuan Proyek

* **Nama Proyek:** FieldTrack Pro (ASW Foods Field Survey, Audit & Territory Management System)
* **Paket Aplikasi Mobile:** `com.aswfoods.fieldtrack`
* **Target Pengguna & Peran:**
  1. **Surveyor Leader (Auditor / Bisnis):** Melakukan wawancara langsung dengan pemilik/pengelola toko sesuai SOP baku (L1–L12), mengevaluasi kepatuhan sales representative, program promosi SHP & ATB, reward toko, NPS kepuasan pelanggan, dan validasi foto SPV cabang (blind test 3 foto acak).
  2. **Surveyor Member (Merchandiser / Fisik):** Melakukan audit fisik terukur di toko: keakuratan titik koordinat GPS Geotag (25%), kondisi rak display sewa (20%), kesesuaian price tag planogram Top 6 + NPL (20%), dan rentang harga jual GM Area (20%).
  3. **Head Office (HO Admin / Supervisor):** Memonitor dan mengelola wilayah bertingkat (Region, Entity, Branch), master toko, rute harian dinamis (10 Wajib + 5 Tambahan), kuesioner survei dinamis, katalog SKU & kode harga, master supervisor, serta rekapitulasi sesi kunjungan kartu toko terpadu.
* **Prinsip Utama:** **Offline-First**. Surveyor bekerja di area minim sinyal (*blank spot*), data tersimpan di SQLite Room lokal, dan disinkronkan ke server pusat saat koneksi internet pulih melalui `SyncWorker` WorkManager.

---

## 2. Tech Stack & Environment

| Layer / Komponen | Teknologi & Library | Keterangan |
| :--- | :--- | :--- |
| **Mobile Platform** | Android Native (Kotlin 2.0+) | Jetpack Compose (Material 3), Single Activity Architecture |
| **Mobile SDK** | `compileSdk = 35`, `targetSdk = 35`, `minSdk = 24` | Java 17, Coroutines & Flow |
| **Mobile Local DB** | Room Database (SQLite) | Idempotent sync via `offline_sync_id` (UUID v4) |
| **Mobile Sync** | AndroidX WorkManager (`SyncWorker`) | Periodic & Network Constraint Greedy Scheduler |
| **Backend Monolith** | Laravel 12 (PHP 8.2+) | REST API Sanctum Token + Web Portal Inertia.js |
| **Database Server** | PostgreSQL 18 (`fieldtrack_db`) | Port `5432`, user `postgres` |
| **Frontend Web Portal**| Inertia.js + Vue 3 (Composition API) | Tailwind CSS v4, Vite 6, Leaflet.js (Peta Geospasial) |
| **Spreadsheet Engine** | PhpSpreadsheet (`phpoffice/phpspreadsheet`) | Parser & Exporter bulk Excel (.xlsx, .xls) / CSV |
| **Design System** | Identitas Brand Resmi ASW Foods | Primary Red `#b2000c`, Dark Charcoal `#444444`, Surface `#f8f9fa` |

---

## 3. Struktur Modul & Direktori Proyek

```
d:\AndroidStudioProjects\fieldtrack-pro\
├── app/
│   ├── Enums/                     # UserRole, OutletClass, VisitStatus, RakDisplayCondition
│   ├── Http/
│   │   ├── Controllers/
│   │   │   ├── Api/               # AuthController, SurveySyncController, RouteApiController, SurveyQuestionController, MasterDataController, MediaUploadController
│   │   │   └── Web/               # DashboardController, VisitController, RoutePlanController, TerritoryController, UserController, OutletController, ProductController, SupervisorController, QuestionController
│   │   └── Requests/Api/          # LeaderSyncRequest, MemberSyncRequest (Dukungan Incomplete Check-Out)
│   ├── Models/                    # Region, Entity, Branch, Outlet, Visit, SurveyQuestion, SurveyorPair, RoutePlan, RoutePlanOutlet, Product, PriceRange, Supervisor, User
│   └── Services/                  # SurveyScoringService, VisitSyncService, RoutePlanningService, RouteExcelImportService, ProductCatalogService
├── database/
│   ├── migrations/                # Skema PostgreSQL (Territory, Routes, Questions, Revisit, Price Codes, Supervisors)
│   └── seeders/                   # EskalinkHierarchySeeder, SurveyQuestionSeeder, MasterProductAndPriceSeeder, SupervisorDataSeeder, DatabaseSeeder
├── resources/js/
│   ├── Components/                # TerritoryFilter, Modal, Card, Badge, Button, RouteMap (Leaflet), DatePicker, StatCard
│   ├── Layouts/                   # AuthenticatedLayout (Side Navigation Menu ASW Foods)
│   └── Pages/
│       ├── Visits/                # Index.vue (Rekapitulasi Card Terpadu Toko), Show.vue (Detail Audit)
│       ├── Routes/                # Index.vue, Create.vue (Perencanaan Rute & Import Excel), Edit.vue (Edit Rute & Tambah Toko), Show.vue
│       ├── Master/                # Regions, Entities, Branches, Users, Outlets (Full CRUD & Territory Filter)
│       ├── Products/              # Index.vue (Katalog SKU, GROUPPRICE, Bulk Upload & Download Template)
│       ├── Supervisors/           # Index.vue (Full CRUD SPV, Salescode, Coveran Distributor, Foto Wajah)
│       └── Questions/             # Index.vue (Master Kuesioner Survei Leader L1-L12 & Member M1-M4)
├── memory_mobile.md               # Spesifikasi lengkap REST API Mobile, JSON Contracts, & Panduan Tim Android
├── Architecture.md                # Standar arsitektur & coding conventions (100% Bahasa Indonesia)
└── memory.md                      # Dokumen Handover & Project Memory (File ini)
```

---

## 4. Metodologi Penilaian & Formula Scoring Terpadu

### A. Komposisi Total Skor Survei (100%)
Berdasarkan dokumen resmi **`2026.11.05-Pertanyaan Survey Leader & Member.xlsx`**, skor akhir toko per sesi survei terdiri dari **4 Kriteria Fisik Member (85%)** dan **Indikator L12 Pengenalan SPV Leader (15%)**:

$$\text{Total Survey Score} = \underbrace{(W_{M1} + W_{M2} + W_{M3} + W_{M4})}_{85\%} + \underbrace{W_{L12\text{ (Pengenalan SPV)}}}_{15\%} = 100\%$$

1. **M1 — Keakuratan Geotag GPS (25%):**
   * Dihitung via Haversine Formula (jarak koordinat database Eskalink vs lokasi surveyor di pintu toko):
     * $\le 10\text{ m} \rightarrow 100\%$, $11-25\text{ m} \rightarrow 75\%$, $26-50\text{ m} \rightarrow 50\%$, $51-100\text{ m} \rightarrow 25\%$, $>100\text{ m} \rightarrow 0\%$.
2. **M2 — Rak Display Sewa Terpasang (20%):**
   * **Kondisional:** Hanya untuk toko yang di database berstatus ikut kontrak sewa rak (`is_sewa_display = true`).
   * Planogram & isi rapi: 100%, 5 item: 75%, 3 item: 50%, 2 item: 25%, Kosong: 0%.
   * Jika tidak sewa rak atau toko tipe **Trader**, kriteria berstatus **N/A**.
3. **M3 — Price Tag Sesuai Planogram (20%):**
   * Kepatuhan label harga per SKU fokus (Top 6 + NPL). Rasio $\frac{X}{N} \times 100\%$.
   * Toko tipe **Trader** (tanpa rak/etalase fisik) otomatis berstatus **N/A**.
4. **M4 — Rentang Harga Jual GM Area (20%):**
   * Harga aktual produk dicocokkan dengan rentang harga batas bawah dan atas GM area berdasarkan **Kode Harga (GROUPPRICE)** toko. Rasio $\frac{X}{N} \times 100\%$. Berlaku semua toko.
5. **L12 — Validasi Pengenalan SPV Cabang (15%):**
   * Wawancara uji buta 3 foto SPV oleh Leader kepada penanggung jawab toko: Benar = 100%, Salah = 0%, N/A = Ditunda (Revisit).

### B. Redistribusi Bobot Proporsional (N/A Handling)
Kriteria yang bernilai **N/A** otomatis dikeluarkan dari penyebut pembagi, dan bobotnya didistribusikan secara proporsional ke kriteria yang berlaku:

$$\text{Final Score (\%)} = \frac{\sum (\text{Skor Kriteria Aktif} \times \text{Bobot Kriteria Aktif})}{\sum \text{Bobot Kriteria Aktif}} \times 100$$

### C. Penguncian Kalkulasi Skor (Belum Selesai = Belum Di-recalculate)
* Skor gabungan `final_combined_score` **tidak akan dihitung (tetap NULL)** jika salah satu peran (Member atau Leader) belum menyelesaikan surveinya.
* Kunjungan yang baru diselesaikan satu peran ditandai **`Parsial (Menunggu Partner)`** dengan `is_survey_complete = false`.
* Ketika kedua surveyor telah mengirimkan hasil (`submitted`), backend otomatis menggabungkan skor fisik Member (85%) dan L12 Leader (15%) menjadi skor terpadu 100%.

---

## 5. Penanganan Kendala Lapangan & Check-Out Dini (Incomplete Survey)

Jika survei terhambat di lapangan (misal: pemilik tidak ada di tempat, toko sangat ramai pembeli, toko tutup sementara):

1. **Modal Intersepsi Check-Out Dini di Android:**
   * Jika surveyor menekan tombol **Check-Out Toko** dalam kondisi pertanyaan kuesioner/audit masih kosong, aplikasi memunculkan modal peringatan wajib memilih alasan kendala.
   * **Pilihan Alasan Standar (`incomplete_reason_type`):**
     * `pic_absent`: Pemilik / PIC Toko Tidak Berada di Tempat.
     * `busy_store`: Toko Sedang Sangat Ramai Dikunjungi Pembeli.
     * `store_closed`: Toko Tutup / Sedang Renovasi / Tidak Beroperasi Sementara.
     * `refused`: Pemilik / Penanggung Jawab Toko Menolak Disurvei.
     * `emergency`: Kondisi Darurat / Cuaca Buruk / Akses Terhalang.
     * `other`: **Lainnya (Alasan Khusus):** Memunculkan Textbox manual wajib diisi alasan spesifik.
2. **Penyimpanan Status Revisit:**
   * Backend menerima submit dengan flag `is_incomplete = true`, `needs_revisit = true`, `incomplete_reason`.
   * Pada rute kunjungan berikutnya, toko memiliki label **`Revisit: Data Belum Lengkap`** sehingga surveyor dapat melakukan penilaian susulan di lokasi.

---

## 6. Master Katalog SKU, TOP 6, NPL & Kode Harga (GROUPPRICE)

### A. Klasifikasi SKU Fokus Audit (`PRODUCT.xlsx`)
Katalog master produk telah disinkronkan langsung dari `MASTER PRODUK DIST ALL 1.xlsx` (334 SKU aktif) dan dikelompokkan berdasarkan aturan bisnis resmi:
* **TOP 6 (113 SKU Terpilih):** Mencakup seluruh varian produk dari 5 grup utama:
  1. `SHP ALL 36` (Hatari See Hong Puff & Cream Crackers 36s)
  2. `HC-21` (Hatari Cream 21s)
  3. `HAT-40` (Hatari Biskuit 40s)
  4. `ATB 24` (ATB Marie Susu 24s)
  5. `HAT-12` (Hatari Jam 12s)
  *(Sesuai aturan bisnis, grup `DCC 20` dan produk kategori `NPL` dikecualikan dari Top 6).*
* **NPL / New Product Launch (20 SKU Terpilih):** Seluruh produk dengan penanda NPL aktif (`NPL MALKIST`, `NPL ATB COCONUT`, `NPL NUNO`, `NPL RING COOKIES`, `NPL LEMONTHINS`).

### B. Standardisasi Kode Harga (`Master Price.xlsx`)
* Kolom `Kode Cabang` telah diganti menjadi **`Kode Harga`** (`price_code`).
* Terdapat **8 Kode Harga (GROUPPRICE)** resmi ASW Foods yang terdaftar:
  * `AHRTMDN` (Medan Retail), `AHCOMDN` (Medan Company/MT)
  * `AHRTKEP` (Kepri Retail), `AHCOKEP` (Kepri Company/MT)
  * `AHRTLPG` (Lampung Retail), `AHCOLPG` (Lampung Company/MT)
  * `AHRTBBL` (Bangka Belitung Retail), `AHCOBBL` (Bangka Belitung Company/MT)
* **Hubungan Distributor & Toko:**
  * Setiap Cabang Distributor (`branches.price_code`) memiliki kode harga bawaan wilayahnya.
  * Setiap Toko/Outlet (`outlets.price_code`) menyimpan kode harga spesifik hasil sinkronisasi Master Eskalink (`ASW Sum 3 Baru.xlsx`) dan mewarisi kode cabang sebagai fallback jika null.
  * Endpoint `GET /api/v1/master/products?outlet_id={id}` secara dinamis mengembalikan batas harga jual wajar GM Area (`min_price` & `max_price`) berdasarkan `price_code` efektif toko tersebut.

---

## 7. Master Supervisor Sales Distributor (Validasi Blind Test L12)

* **Penggantian NIP Menjadi Salescode:** Kolom `nip` dimigrasikan menjadi `sales_code` (misal: `SPVKEPRI1`, `SPVLAMBABEL3`, `SPVSUMSEL2`, `SPVBENGKULU3`, `SPVJAMBI1`, `SPVRIAU1A`, `SPVSUMBAR2`). Dukungan *backward compatibility* tetap dipertahankan pada Mobile API (`/api/v1/master/supervisors`) dan model database.
* **Fitur CRUD & Upload Foto Wajah:** Foto diunggah ke storage publik (`storage/uploads/supervisors/`) dan disajikan dengan URL absolut yang siap diakses aplikasi Android untuk uji blind test L12 (1 foto target + 2 foto pengecoh acak).
* **Integrasi Data Seluruh Wilayah (`DATA_HOASWSUM.xlsx`):** Mencakup **12 Area Wilayah** dan **40 Master Supervisor** terdaftar di database.

---

## 8. Master Toko, Hirarki 5 Pilar, Rute & Peta Geospasial

### A. Hirarki 5 Pilar Resmi Toko (Prioritas Kunjungan Wajib)
1. **RWO** (*Reward Outlet*) &mdash; Prioritas 1
2. **Pareto** (*Top Volume Contributor*) &mdash; Prioritas 2
3. **NGVO** (*New Grooming Value Outlet*) &mdash; Prioritas 3
4. **Grooming** &mdash; Prioritas 4
5. **NOO** (*New Open Outlet*) &mdash; Prioritas 5

### B. Master Data Outlet Region Sumatera 3 (18.022 Toko)
* Seluruh data toko dari file Excel `ASW Sum 3 Baru.xlsx` telah diimpor lengkap ke database:
  * **Distribusi 5 Pilar:** Grooming: 13.040 toko, NOO: 4.027 toko, RWO: 720 toko, Pareto: 140 toko, NGVO: 107 toko (GT Biasa: 3, Trader: 2).
  * **Status Pareto (`is_pareto`):** Ya (849 toko) atau Tidak (17.191 toko).
  * **Grup Harga (`price_code`):** Terdistribusi pada 6 kode harga aktif.

### C. Perencanaan Rute Kunjungan (10 Wajib + 5 Tambahan)
* **Slider Batas Radius & Hop Distance:** Slider batas radius kluster (3–30 km) dan **Maks. Jarak Antar-Toko** (0.5–10 km, default 3 km) mencegah algoritma melompat melintasi selat/pulau terpisah.
* **Generator Rute Mingguan:** Kluster 5 hari kerja (Senin–Jumat) tanpa duplikasi toko antar-hari.
* **Import Template Excel Resmi (`Template_Rute_Kunjungan_ASW_Foods.xls`):**
  * Format bersih: Sheet `RUTE 1`, kolom `CUSTNO`, matriks `H1–H7` bernilai `'Y'` dan `'T'`.
  * Deteksi hari kalender riil otomatis terkunci ke hari **Senin (H1)**.
  * **Modal Error Khusus:** Jika terdapat kode toko yang belum terdaftar di master database, sistem memunculkan pop-up modal error berisi daftar kode toko yang hilang beserta tombol *Salin Kode Toko* 1-klik.
  * **Auto-Close:** Modal upload otomatis tertutup seketika pemrosesan impor berhasil.
* **Dinamika Tombol Hari Terakhir:** Jika 4 dari 5 hari telah dipublikasikan, tombol pada hari ke-5 otomatis berubah menjadi **"Publikasikan & Selesaikan Rute Kunjungan"** dengan penanda visual khusus dan memunculkan notifikasi perayaan sukses bahwa seluruh pekan telah tuntas.
* **Deteksi Toko Terjadwal & Alert Riwayat Survei:**
  * `⚠️ Catatan: Sudah masuk di hari sebelumnya` (berwarna amber).
  * `📋 Alert: Toko sudah pernah disurvei pada [Tanggal] (Skor: [Score]%)` (berwarna oranye).
* **Halaman Edit Rute (`Routes/Edit.vue`):**
  * Perbaikan CSS Stacking Context (`:overflow-visible="true"` & `z-30`) agar dropdown kalender `DatePicker` tidak terpotong (tenggelam).
  * **Seksi 4: Rekomendasi Toko Belum Terpilih:** Menampilkan daftar toko cabang yang belum masuk rute, diurutkan otomatis dari jarak terdekat ke stop terakhir (Stop #9 -> Stop #10).
  * Tombol `+ Toko Wajib` dan `+ Toko Tambahan` untuk melengkapi kuota rute menjadi 10 toko.
  * Tombol `Urutkan Sejalur Otomatis` (Nearest Neighbor) untuk merapikan kembali lintasan rute.

---

## 9. Skema Tabel Database Utama (PostgreSQL)

| Nama Tabel | Kolom Penting | Keterangan & Relasi |
| :--- | :--- | :--- |
| `regions` | `id`, `region_code`, `region_name`, `is_active` | Tingkat 1 wilayah geografis |
| `entities` | `id`, `region_id`, `entity_code`, `entity_name` | Tingkat 2 entitas operasional/badan usaha |
| `branches` | `id`, `entity_id`, `branch_code`, `branch_name`, `price_code` | Tingkat 3 cabang distributor (96 distributor + 4 cabang regional) |
| `outlets` | `id`, `branch_id`, `kode_toko`, `nama_toko`, `price_code`, `class_outlet`, `is_pareto`, `latitude`, `longitude`, `is_sewa_display` | Master 18.022 toko aktif (5 Pilar, Grup Harga, Status Pareto) |
| `products` | `id`, `sku_code`, `product_name`, `category`, `is_top_5`, `is_npl`, `is_active` | Master 334 SKU produk biskuit & crackers ASW Foods |
| `price_ranges`| `id`, `product_id`, `price_code`, `min_price`, `max_price`, `is_active` | Rentang harga jual rekomendasi GM Area berdasarkan 8 GROUPPRICE |
| `supervisors` | `id`, `sales_code`, `nip`, `name`, `area_name`, `coverage_distributors`, `branch_id`, `photo_path`, `is_active` | Master SPV lapangan untuk blind test L12 toko |
| `survey_questions`| `id`, `question_code`, `category`, `question_text`, `sop_guideline`, `weight`, `is_active`, `sort_order` | Master kuesioner dinamis (Leader L1–L12 dan Member M1–M4) |
| `surveyor_pairs` | `id`, `branch_id`, `leader_id`, `member_id`, `is_active` | Penugasan pasangan surveyor harian di cabang |
| `route_plans` | `id`, `branch_id`, `leader_id`, `member_id`, `name`, `plan_date`, `status` | Rencana rute harian |
| `route_plan_outlets`| `id`, `route_plan_id`, `outlet_id`, `stop_order`, `outlet_type`, `is_visited_leader`, `is_visited_member` | Urutan kunjungan toko yang dioptimasi geometris |
| `visits` | `id`, `outlet_id`, `route_plan_id`, `leader_id`, `member_id`, `visit_date`, `final_combined_score`, `is_survey_complete`, `needs_revisit`, `incomplete_reason` | Sesi kunjungan kartu toko terpadu |
| `survey_leader_responses`| `id`, `visit_id`, `question_code`, `answer_value`, `selected_supervisor_id`, `is_correct_supervisor`, `score_value` | Jawaban wawancara Leader & hasil uji buta L12 |
| `survey_member_audits`| `id`, `visit_id`, `gps_distance_meters`, `rak_display_condition`, `price_tag_compliance_ratio`, `price_range_compliance_ratio`, `subtotal_score` | Hasil pengukuran audit fisik Member (M1–M4) |
| `survey_member_sku_audits`| `id`, `survey_member_audit_id`, `product_id`, `has_price_tag`, `is_price_tag_compliant`, `actual_price`, `price_range_compliant` | Audit detail per item SKU produk |

---

## 10. Status Pengujian Otomatis (Test Suite) & SOP Kebersihan Database

* **SOP Kebersihan Data Pengujian (Penting):**
  1. **Isolasi Akun Riil:** Pengujian dilarang keras mengubah atribut atau mengikat cabang (`branch_id`) pada akun surveyor riil operasional seperti `leader_batam` (`Muhammad Fachrozy`) dan `member_batam` (`Salomo Gusto Leonard`). Pengujian wajib menggunakan akun khusus testing (`leader01`, `member01`) atau membuat instance dummy sementara.
  2. **Zero Dummy Leftover via DatabaseTransactions:** Seluruh test case mewarisi trait `Illuminate\Foundation\Testing\DatabaseTransactions` pada `tests/TestCase.php`. Setiap mutasi database selama automated test secara otomatis di-*rollback*, sehingga database `fieldtrack_db` tetap 100% bersih tanpa residu.
* **Hasil Uji PHPUnit Terakhir:** **52 Passed (3.448 assertions)**
  * `SupervisorManagementTest`: 6 tests (View index, Filter area & search, Create with photo upload, Update details, Delete protection, Mobile API sales_code & nip response).
  * `ProductCatalogManagementTest`: 5 tests (View catalog, Add SKU manual, Update price range by price code, Download Excel template, Bulk upload Excel/CSV).
  * `BackendAndWebPortalTest`: 13 tests (Login, API, Scoring, Incomplete Checkout, Locking Score).
  * `RoutePlanningTest`: 14 tests (Path Optimization, Single Route, Daily API, Weekly Clusters Preview, Batch Store Weekly, Import Excel Template, Warning & Alert Metadata, View Detail Show, View Edit Page).
  * `SurveyQuestionCrudTest`: 7 tests (CRUD Questions, Reset Template, API fetch).
  * `TerritoryAndMasterDataTest`: 7 tests (CRUD Region, Entity, Branch, User Scope, Outlet protection).

---

## 11. Panduan Menjalankan Sistem (Local Dev Run) & Repositori GitHub

### A. Repositori GitHub Resmi
* **URL Repositori:** [`https://github.com/jagerboy/fieldtrack-pro`](https://github.com/jagerboy/fieldtrack-pro)
* **Remote Git:** `https://github.com/jagerboy/fieldtrack-pro.git`
* **Branch Utama:** `main`
* **Status Sinkronisasi:** Up to date (`origin/main`). Telah di-push oleh **@jagerboy** pada 20 November 2026.
* **Perlindungan Berkas (.gitignore):** Seluruh berkas rahasia (`.env`), direktori modul (`vendor/`, `node_modules/`), berkas cache, dan build production telah diproteksi secara ketat dari repositori publik.

### B. Setup Cepat untuk Developer Baru / Tim Mobdev:
```bash
# 1. Clone Repositori
git clone https://github.com/jagerboy/fieldtrack-pro.git
cd fieldtrack-pro

# 2. Instalasi Dependensi PHP & Node.js
composer install
npm install

# 3. Konfigurasi Lingkungan
cp .env.example .env
php artisan key:generate

# 4. Database Setup (Pilih Salah Satu):
# Opsi A: Migrasi & Seeder Lengkap (Otomatis)
php artisan migrate
php artisan db:seed

# Opsi B: Restore Dump Bersih (Termasuk 18.022 outlet, master produk & harga)
psql -U postgres -d fieldtrack_db -f database/dumps/fieldtrack_db_clean.sql

# 5. Jalankan Server Pengembangan
php artisan serve --port=8000
npm run dev
```

### C. Menjalankan Automated Tests:
```bash
vendor/bin/phpunit
```

### D. Koneksi Emulator Android:
* Host IP dari emulator: `http://10.0.2.2:8000/api/v1/`
* Panduan lengkap integrasi aplikasi mobile dapat dilihat di [`memory_mobile.md`](file:///d:/AndroidStudioProjects/fieldtrack-pro/memory_mobile.md).

---

## 12. Catatan Pengerjaan & Handover (20 November 2026)

### A. Fitur & Optimalisasi yang Telah Selesai:
1. **Publikasi ke Repositori GitHub (@jagerboy):**
   - Repositori Git diinisialisasi pada branch `main` dan sukses di-push ke [https://github.com/jagerboy/fieldtrack-pro](https://github.com/jagerboy/fieldtrack-pro).
   - Menyertakan dump database bersih [fieldtrack_db_clean.sql](file:///d:/AndroidStudioProjects/fieldtrack-pro/database/dumps/fieldtrack_db_clean.sql) (3.4 MB) untuk memudahkan tim mobile/backend menjalankan lingkungan lokal.
2. **Pembersihan Data Menu Visits:**
   - Dijalankan truncate cascade bersih pada `visits`, `survey_leader_responses`, `survey_member_audits`, `survey_member_sku_audits` (0 data dummy tersisa).
3. **Penyempurnaan Upload Template Rute Excel:**
   - Modal upload otomatis tertutup seketika pemrosesan file Excel sukses (`closeUploadModal(true)`).
   - Pemetaan template resmi `Template_Rute_Kunjungan_ASW_Foods.xls` dengan nilai 'Y' dan 'T' pada kolom H1–H7.
4. **Tombol Rute Mingguan Terpadu:**
   - Deteksi hari terakhir rute belum terpublikasi (`isLastUnpublishedDay`).
   - Tombol berubah otomatis menjadi **"Publikasikan & Selesaikan Rute Kunjungan"** dan memicu alert perayaan sukses seluruh pekan.
5. **Perbaikan Halaman Edit Rute (`Routes/Edit.vue`):**
   - Memperbaiki bug `Undefined column "is_active"` pada query `User` di `RoutePlanController::edit`.
   - Mengatasi isu kalender tanggal terpotong/tenggelam dengan `:overflow-visible="true"` & `z-30` pada `<Card>` Seksi 1.
   - Menambahkan **Seksi 4: Rekomendasi Toko Belum Terpilih**, memungkinkan admin melengkapi kuota rute (misal 9 toko menjadi 10 toko) dengan memilih toko terdekat dari stop terakhir.
   - Tombol `Urutkan Sejalur Otomatis` (Nearest Neighbor) ditambahkan di atas tabel stop.
6. **Jaminan Integritas Akun & Testing Rollback:**
   - Akun riil `@leader_batam` dan `@member_batam` tetap terkunci di cabang Batam (`DABTM001`, ID: 91).
   - `DatabaseTransactions` aktif di `TestCase.php`, menjamin database tidak akan pernah terkontaminasi hasil pengujian unit test.

### B. Agenda & Target Pengerjaan Berikutnya:
1. **Handover ke Tim Mobile Developer:**
   - Mengirimkan link repositori GitHub dan [`memory_mobile.md`](file:///d:/AndroidStudioProjects/fieldtrack-pro/memory_mobile.md) ke tim Android untuk pembangunan `fieldtrack-app`.
2. **Uji Coba Integrasi End-to-End Mobile:**
   - Melakukan uji coba login surveyor, penarikan rute harian yang telah dipublikasikan dari Head Office, submit survei berpasangan, dan validasi rekalkulasi skor otomatis di Web Portal.
