# Kebijakan Keamanan (SECURITY.md)

Dokumen ini mendefinisikan kebijakan keamanan dan panduan pelaporan kerentanan (vulnerability) untuk proyek ini. Kami sangat menghargai kontribusi dari para peneliti keamanan dan komunitas untuk menjaga sistem tetap aman.

## 🛡️ Versi yang Didukung (Supported Versions)

Kami secara aktif memberikan pembaruan keamanan (security patches) hanya untuk versi-versi berikut:

| Versi | Didukung | Catatan / Batas Waktu |
| :--- | :---: | :--- |
| **v3.x** | Keamanan & Fitur | Versi stabil utama saat ini. |
| **v2.5.x** | Hanya Perbaikan Keamanan | Didukung hingga 31 Desember 2026. |
| **< v2.5** | ❌ Tidak Didukung | Silakan tingkatkan ke versi terbaru. |

---

## 🛑 Melaporkan Kerentanan (Reporting a Vulnerability)

**JANGAN membuat Issue, Pull Request, atau Discussion publik untuk melaporkan masalah keamanan.** Laporan publik sebelum adanya perbaikan dapat dimanfaatkan oleh pihak yang tidak bertanggung jawab.

Silakan laporkan celah keamanan secara privat melalui salah satu jalur berikut:

1. **GitHub Private Vulnerability Reporting:**
   * Buka tab **Security** di repositori ini.
   * Pilih **Vulnerability reporting** lalu klik **Report a vulnerability**.
2. **Email Tim Keamanan:**
   * Kirim email ke: `security@example.com`
   * *Opsional:* Anda dapat mengenkripsi email Anda menggunakan kunci PGP kami (Link Kunci PGP / Fingerprint).

### Informasi yang Dibutuhkan dalam Laporan:
Untuk membantu kami menganalisis laporan dengan cepat, mohon sertakan informasi berikut:
* **Jenis Kerentanan:** (Misal: SQL Injection, XSS, Remote Code Execution, Authentication Bypass).
* **Versi Terdampak:** Versi aplikasi, komponen, atau jalur file yang spesifik.
* **Langkah Reproduksi:** Panduan langkah demi langkah atau kode *Proof of Concept* (PoC) untuk memicu celah tersebut.
* **Dampak Keamanan:** Bagaimana celah ini dapat dieksploitasi dan apa risikonya terhadap sistem/data.

---

## ⏳ Waktu Respon & Target Kami (What to Expect)

Setelah Anda mengirimkan laporan, tim kami akan bekerja dengan target waktu berikut (hari kerja):

* **Konfirmasi Penerimaan Laporan:** Maksimal dalam **2 hari kerja**.
* **Penilaian Awal (Validasi):** Maksimal dalam **5 hari kerja** (kami akan menginfokan apakah laporan diterima atau ditolak beserta alasannya).
* **Pembaruan Status Berkala:** Kami akan memberikan *update* perkembangan perbaikan setiap **7 hari sekali** hingga masalah selesai.

---

## 📢 Kebijakan Pengungkapan (Disclosure Policy)

Kami menerapkan prinsip **Coordinated Vulnerability Disclosure (CVD)**:
1. Tim internal akan memvalidasi celah dan membuat perbaikan (patch) secara privat.
2. Perbaikan akan diuji dan dirilis pada seluruh versi yang didukung.
3. Setelah *patch* resmi dirilis, kami akan mempublikasikan **Security Advisory** untuk mengedukasi pengguna agar segera melakukan pembaruan.
4. Nama Anda akan dicantumkan dalam *Credit/Acknowledgement* sebagai bentuk apresiasi kami (kecuali jika Anda memilih untuk anonim).

---
*Terakhir diperbarui: November 2026*
