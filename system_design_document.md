# System Design Document (SDD): Enterprise Outlet Survey & Monitoring System

## 1. Arsitektur Tingkat Tinggi (High-Level Architecture)

Sistem ini dirancang menggunakan arsitektur **Monolith** berbasis Laravel 12, Inertia.js, Vue 3 (Composition API), Tailwind CSS, dan PostgreSQL, dengan integrasi eksternal melalui REST API ke sistem Eskalink (untuk Master Data Customer) serta komunikasi dengan klien Android menggunakan REST API terstandarisasi.

```
┌────────────────────────┐         ┌────────────────────────┐
│     Eskalink API       │         │    Android App Client  │
│  (Customer Master Data)│         │  (Leader & Member)     │
└───────────┬────────────┘         └───────────┬────────────┘
            │ (Cron / Scheduled Sync)          │ (Online/Offline REST API)
            ▼                                  ▼
┌───────────────────────────────────────────────────────────┐
│              Laravel 12 Monolithic Backend                │
│  - API Controller & Form Requests                         │
│  - Service Layer (app/Services)                           │
│  - PostgreSQL Database                                    │
└───────────────────────────┬───────────────────────────────┘
                            │ (Inertia.js Server-Side Rendering)
                            ▼
               ┌─────────────────────────┐
               │   Web Portal (HO User)  │
               │   Vue 3 + Tailwind CSS  │
               └─────────────────────────┘
```

---

## 2. Rincian Komponen & Alur Data

### A. Integrasi Master Data Customer (Eskalink API)
* **Sumber Data:** Sistem Eskalink (Menu Customer Master).
* **Atribut Utama:** `branch_id`, `branch_name`, `kode_toko`, `nama_toko`, `alamat`, `LA` (Latitude), `LG` (Longitude), dan `class_outlet` (mencakup: `RWO`, `Pareto Non RWO`, `NGVO`, `Grooming`, `Pareto`, dll).
* **Mekanisme Sinkronisasi:** Laravel Scheduler / Artisan Command (`docker-compose exec app php artisan esk:sync-customers`) melakukan *polling* atau *webhook* berkala ke API Eskalink untuk memperbarui tabel `outlets` di PostgreSQL.

### B. Aplikasi Android (Field Survey App)
Aplikasi Android mendukung mode **Online** dan **Offline** dengan *local persistence* (SQLite/Room).

1. **Manajemen Rute & Penugasan Harian:**
   * Surveyor mengunduh daftar toko dari seluruh distributor yang menjadi jadwal kunjungan harian (*daily route plan*).
2. **Pembagian Role Surveyor:**
   * **Leader:** Fokus melakukan *interview* mendalam dengan pemilik/pengelola toko (kuesioner bisnis, komplain, feedback program).
   * **Member:** Fokus melakukan survey fisik lokasi toko, ketersediaan stok, tata letak (*merchandising*), hingga penilaian kondisi produk yang terpajang.
3. **Mekanisme Sinkronisasi (Offline-First):**
   * Data hasil survey disimpan secara lokal jika perangkat tidak memiliki koneksi internet.
   * Tombol *Push Data Pending* memicu *batch upload* via REST API begitu koneksi pulih.

### C. Web Portal (Head Office)
* Dibangun menggunakan **Laravel 12 + Inertia.js + Vue 3 (Composition API)**.
* Menyajikan dashboard analitik, rekapitulasi hasil survey *Leader* dan *Member*, peta persebaran outlet berdasarkan koordinat `LA` & `LG`, serta pengelolaan parameter pertanyaan.

---

## 3. Desain Skema Database Utama (PostgreSQL)

Berikut adalah rancangan relasi tabel inti di PostgreSQL:

```sql
-- Tabel Master Customer / Outlet (Disinkronkan dari Eskalink)
CREATE TABLE outlets (
    id SERIAL PRIMARY KEY,
    branch_id VARCHAR(50) NOT NULL,
    branch_name VARCHAR(100) NOT NULL,
    kode_toko VARCHAR(50) UNIQUE NOT NULL,
    nama_toko VARCHAR(150) NOT NULL,
    alamat TEXT NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    class_outlet VARCHAR(50) NOT NULL, -- RWO, Pareto, NGVO, Grooming, dll.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabel Sesi Kunjungan Harian Surveyor
CREATE TABLE visits (
    id SERIAL PRIMARY KEY,
    outlet_id INT REFERENCES outlets(id),
    user_id INT NOT NULL, -- ID Surveyor (Leader / Member)
    role_type VARCHAR(20) NOT NULL, -- 'leader' atau 'member'
    visit_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL, -- 'pending', 'submitted', 'synced'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabel Hasil Survey Detail
CREATE TABLE survey_results (
    id SERIAL PRIMARY KEY,
    visit_id INT REFERENCES visits(id),
    question_code VARCHAR(50) NOT NULL,
    answer_value TEXT NOT NULL,
    metadata JSONB, -- Menyimpan data tambahan seperti foto path, kondisi produk, dll.
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 4. Standar Implementasi & Kepatuhan Arsitektur

* **Strict Types:** Setiap file PHP wajib mendeklarasikan `declare(strict_types=1);`.
* **Service Pattern:** Logika kompleks seperti sinkronisasi Eskalink API dan kalkulasi rekapitulasi survey harus diletakkan di `app/Services/`.
* **Bahasa Komentar:** Seluruh komentar kode menggunakan **Bahasa Indonesia** dan berfokus pada **tujuan bisnis**.
* **Vue 3 SFC:** Menggunakan `<script setup lang="js">` tanpa Class-Based Component.