# Threat Model: [Nama Aplikasi / Sistem]

## 🎯 Batasan & Ruang Lingkup
*Dokumentasikan secara singkat arsitektur sistem, teknologi yang digunakan, serta batasan sistem yang sedang dianalisis.*

* **Repositori/Komponen:** [Contoh: Frontend React, Backend Go, AWS RDS]
* **Target Pengguna:** [Contoh: Publik, Admin Internal]
* **Tanggal Pembaruan:** 9 November 2026

---

## 🏗️ Aset Kritis
Daftar aset berharga yang harus dilindungi:
1. **Aset Data:** Data kredensial pengguna, token API, data transaksi database.
2. **Aset Sistem:** Server backend, infrastruktur cloud, pipeline CI/CD.

---

## 🛑 Batas Kepercayaan (Trust Boundaries)
Titik-titik di mana data berpindah dari tingkat kepercayaan rendah ke tingkat kepercayaan tinggi:
* **TB-01:** Antara Web Browser Pengguna (Tidak Terpercaya) dan API Gateway (Terpercaya).
* **TB-02:** Antara API Gateway Internal dan Database (Sangat Terpercaya).
* **TB-03:** Jalur integrasi dengan pihak ketiga (Webhook / Payment Gateway).

---

## 🛡️ Analisis Ancaman (Metode STRIDE)

### 1. Spoofing (Pemalsuan Identitas)
* **Ancaman:** Penyerang memalsukan session token untuk mengakses akun pengguna lain.
* **Batas Kepercayaan:** TB-01
* **Tingkat Risiko:** 🔴 Tinggi
* **Mitigasi:** Implementasi JWT bertanda tangan digital (HS256/RS256) dengan masa kedaluwarsa singkat (short-lived tokens) dan menggunakan atribut HttpOnly pada cookie.

### 2. Tampering (Modifikasi Data)
* **Ancaman:** Penyerang mengubah parameter harga produk di payload HTTP sebelum dikirim ke backend.
* **Batas Kepercayaan:** TB-01
* **Tingkat Risiko:** 🔴 Tinggi
* **Mitigasi:** Backend harus melakukan validasi ulang harga berdasarkan data master di database, bukan memercayai input langsung dari frontend.

### 3. Repudiation (Penyangkalan)
* **Ancaman:** Admin mengubah konfigurasi sistem yang sensitif tetapi menyangkal tindakan tersebut karena tidak ada jejak audit.
* **Batas Kepercayaan:** TB-02
* **Tingkat Risiko:** 🟡 Sedang
* **Mitigasi:** Mengaktifkan logging terpusat yang bersifat *append-only* (misalnya AWS CloudTrail atau ELK Stack) untuk mencatat semua tindakan administratif secara permanen.

### 4. Information Disclosure (Kebocoran Informasi)
* **Ancaman:** Pesan error database (SQL stack trace) bocor ke pengguna akhir saat terjadi kesalahan input, mengekspos struktur tabel.
* **Batas Kepercayaan:** TB-01
* **Tingkat Risiko:** 🟡 Sedang
* **Mitigasi:** Implementasi penanganan error global (*global error handler*) yang mengembalikan pesan error generik ke pengguna, sementara detail teknis disimpan di log internal.

### 5. Denial of Service (Kelumpuhan Sistem)
* **Ancaman:** Penyerang membanjiri endpoint `/api/login` dengan jutaan request per detik, membuat server kehabisan resource.
* **Batas Kepercayaan:** TB-01
* **Tingkat Risiko:** 🔴 Tinggi
* **Mitigasi:** Menerapkan *Rate Limiting* di level API Gateway dan menggunakan perlindungan DDoS berbasis Cloud (seperti Cloudflare atau AWS Shield).

### 6. Elevation of Privilege (Peningkatan Hak Akses)
* **Ancaman:** Pengguna biasa mengubah parameter `role` di payload profil mereka dari `user` menjadi `admin`.
* **Batas Kepercayaan:** TB-01 -> TB-02
* **Tingkat Risiko:** 🔴 Kritis
* **Mitigasi:** Menerapkan kontrol akses berbasis peran (*Role-Based Access Control* / RBAC) yang divalidasi secara ketat di sisi server (backend) pada setiap request endpoint.

---

## 📊 Matriks Risiko & Prioritas Kerja

| ID | Kategori STRIDE | Deskripsi Ancaman | Risiko | Status Mitigasi |
| :--- | :--- | :--- | :--- | :--- |
| T-01 | Spoofing | Pemalsuan session token | 🔴 Tinggi | ⏳ Direncanakan (Sprint Berikutnya) |
| T-02 | Tampering | Modifikasi parameter harga | 🔴 Tinggi | ✅ Selesai diuji |
| T-03 | DoS | Brute force / flooding endpoint | 🔴 Tinggi | 🛠️ Sedang Dikerjakan |
| T-04 | Info Disclosure | Bocornya stack trace error | 🟡 Sedang | ✅ Selesai diuji |

---

## 🔍 Catatan & Asumsi Terbuka
* *Asumsi:* Server diasumsikan berada di bawah jaringan privat (VPC) dan tidak dapat diakses langsung via IP publik tanpa melalui API Gateway.
* *Pertanyaan Terbuka:* Apakah integrasi dengan payment gateway pihak ketiga sudah menggunakan skema webhook bertanda tangan (HMAC)?
