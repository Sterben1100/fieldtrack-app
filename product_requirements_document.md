# Product Requirements Document (PRD): Enterprise Outlet Survey & Monitoring System

## 1. Executive Summary & Overview
Sistem Survey Outlet adalah platform terpadu yang terdiri dari Aplikasi Mobile (Android) untuk surveyor lapangan dan Web Portal (Head Office) untuk manajemen data serta analitik. Sistem ini dirancang untuk mendata, memantau, dan mengevaluasi kondisi outlet secara <i>real-time</i> maupun periodik menggunakan dukungan sinkronisasi data *offline-first*.

Prioritas pengembangan dimulai dari **Aplikasi Android (Field Surveyor)**, dilanjutkan dengan **Web Portal (Head Office)** di atas arsitektur monolitik modern menggunakan stack teknologi terbaru.

---

## 2. Tujuan Bisnis & Sasaran (Objectives)
*   **Akurasi Data Lapangan:** Memastikan data audit/survey dari dua role surveyor berbeda tercatat akurat dan objektif.
*   **Efisiensi Operasional Lapangan:** Memungkinkan surveyor bekerja tanpa kendala di area minim sinyal melalui mekanisme penyimpanan lokal (*Local Storage/SQLite*) dan sinkronisasi tertunda (*pending push action*).
*   **Mitigasi Risiko Keamanan & Framework Vulnerabilities:** Memanfaatkan versi framework terbaru (Laravel 12, Node.js versi patch aman terbaru, PostgreSQL) untuk mengatasi potensi eksploitasi keamanan terkini (seperti isu CVSS/patch patch level terbaru).
*   **Kolaborasi Tim yang Efisien:** Menerapkan standar dokumentasi kode yang ketat menggunakan Bahasa Indonesia dan prinsip *Clean Code* untuk mendukung solo developer maupun ekspansi tim via GitHub.

---

## 3. User Roles & Hak Akses
1.  **Surveyor Role A (Auditor / Merchandiser):** Melakukan pengecekan parameter visual, ketersediaan produk, dan display di outlet.
2.  **Surveyor Role B (Promotor / Sales Auditor):** Melakukan pengecekan aspek penjualan, harga, promosi, dan feedback kompetitor di outlet.
3.  **Head Office (HO) User:** Mengakses web portal untuk memonitor hasil survey, melihat laporan komparasi antar-role, mengelola parameter pertanyaan, serta manajemen user.

---

## 4. Alur Bisnis (Business Process)

```
[Surveyor Lapangan] 
       │
       ├─► (Online Mode) ──► Kirim langsung via REST API ──┐
       │                                                   │
       └─► (Offline Mode) ──► Simpan di SQLite Lokal ──────┼──► [PostgreSQL Database]
                                                           │
             [Manual / Auto Trigger "Push Data"] ──────────┘
                                                              │
                                                              ▼
                                                   [Web Portal Head Office]
```

1.  **Inisialisasi & Pengambilan Data Master:** Surveyor mengunduh list pertanyaan dan data outlet saat terhubung internet.
2.  **Pelaksanaan Survey:** Surveyor mengunjungi outlet dan mengisi parameter sesuai role masing-masing.
3.  **Mekanisme Koneksi (Online / Offline):**
    *   Jika ada internet, data langsung dikirim ke server via REST API.
    *   Jika tidak ada jaringan, data disimpan sementara di database lokal Android. Tombol *Push Data* akan aktif untuk mengirimkan antrean data (*pending queue*) begitu koneksi tersedia.
4.  **Agregasi & Pelaporan HO:** Data dari kedua role masuk ke PostgreSQL, lalu diolah dan disajikan melalui dashboard web portal menggunakan Inertia.js dan Vue 3.

---

## 5. Spesifikasi Teknis & Arsitektur

*   **Architecture:** Monolith
*   **Backend:** Laravel 12 (Strict Types `declare(strict_types=1);`, Service Pattern di `app/Services`, Enum untuk status).
*   **Frontend:** Vue 3 (Composition API, `<script setup lang="js">`, komponen modular di folder `Components`), Inertia.js, Tailwind CSS.
*   **Database:** PostgreSQL.
*   **Containerization:** Docker workflow (`docker-compose exec app [command]`).
*   **Security & Patch Management:** Menjaga dependensi PHP, Node.js, Nginx, dan PostgreSQL pada versi stabil terbaru guna menutup celah keamanan (*security patches*).

---

## 6. Aturan Dokumentasi & Coding Standards (WAJIB)
*   **Komentar Kode:** Seluruh komentar wajib menggunakan **Bahasa Indonesia**, berfokus pada **tujuan bisnis** (bukan menjelaskan syntax), dan setiap fungsi/method wajib memiliki blok dokumentasi parameter serta *return type*.
*   **Larangan Keras:** Dilarang menggunakan jQuery, dilarang menghapus komentar eksisting tanpa menghapus kodenya, dan dilarang menggunakan tipe data `any` atau `mixed` jika tipe sudah jelas.

---

## 7. Tahapan Pengembangan (Milestones)
*   **Fase 1 (Prioritas Utama):** Pengembangan Aplikasi Android (Local Database, UI Vue/Flutter-equivalent sesuai stack, REST API Integration, Offline Sync Engine).
*   **Fase 2:** Pengembangan Web Portal Head Office (Dashboard Analitik, Manajemen Parameter Survey, User Management, Laravel 12 + Inertia + Vue 3).
*   **Fase 3:** Testing, Bug Fixing, Security Hardening, & Deployment ke GitHub Repository.