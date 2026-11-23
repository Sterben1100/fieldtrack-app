# Security Architecture Documentation

This document defines the current security posture, architectural boundaries, and defense-in-depth controls implemented across the system. It serves as a living blueprint aligned with modern industry benchmarks as of November 2026.

---

## 1. Executive Summary & Posture (2026)
*   **Security Paradigm:** Zero-Trust Architecture (ZTA) – *Never Trust, Always Verify*.
*   **Primary Frameworks:** Mapped directly to **NIST CSF 2.0** and **ISO/IEC 27001:2022**.
*   **Key Focus Areas:** Post-Quantum Cryptography (PQC) readiness, AI Asset Governance, API micro-segmentation, and Runtime Exploit Mitigation.

---

## 2. Security Control Mapping
Berikut adalah status implementasi kontrol keamanan saat ini yang dikonstruksikan berdasarkan domain kontrol global:

| Control Domain | Implementation Status (Nov 2026) | Target Standard / Framework |
| :--- | :--- | :--- |
| **Access Control** | 🟢 Full (MFA, Passkeys, Least Privilege RBAC) | ISO 27001 A.9 / NIST PR.AA |
| **Cryptography** | 🟡 Hybrid (Classical TLS 1.3 + PQC Migration) | ISO 27001 A.10 / NIST PR.DS |
| **Network Security** | 🟢 Full (Micro-segmentation, WAF, Zero-Trust) | ISO 27001 A.13 / NIST PR.IR |
| **AI & LLM Security** | 🟡 Partial (Guardrails, Data Masking enabled) | ISO 42001 / NIST AI RMF |
| **Supply Chain & Runtime**| 🟢 Full (Automated SBOM, RASP, Drastic Egress Filter) | ISO 27001 A.15 / NIST GV.SC |

---

## 3. Defense-in-Depth Architecture

### 3.1 Network & Boundary Security
*   **Edge Protection:** Web Application Firewall (WAF) aktif dengan aturan perlindungan DDoS otomatis, *rate limiting*, dan geo-blocking pada perimeter luar.
*   **Segmentation:** Jaringan dibagi menjadi zona-zona ketat (*Security Zones*) menggunakan VPC/Subnet privat:
    *   *Public Zone / DMZ:* Hanya untuk CDN dan Load Balancer.
    *   *Application Zone:* Berjalan di subnet privat tanpa IP publik langsung.
    *   *Data Zone:* Terisolasi penuh; hanya menerima koneksi dari Application Tier via port spesifik.

### 3.2 Authentication & Authorization (Identity Layer)
*   **Authentication (OIDC):** Autentikasi pengguna dikelola via OpenID Connect (OIDC) terpusat dengan kewajiban Phishing-Resistant MFA (FIDO2/Passkeys).
*   **Authorization (OAUTH 2.0 / RBAC):** Otorisasi berbasis *Role-Based Access Control* (RBAC) menggunakan *short-lived* Access Tokens (JWT) dengan validasi *signature* yang ketat di tingkat API Gateway.

### 3.3 Data Protection & Cryptography
*   **Data-in-Transit:** Seluruh komunikasi wajib menggunakan **TLS 1.3** dengan mematikan *cipher suites* lama yang rentan.
*   **Data-at-Rest:** Enkripsi database dan storage menggunakan algoritma **AES-256-GCM**. Kunci enkripsi dikelola secara otomatis melalui Cloud KMS dengan rotasi berkala.
*   **PQC Roadmap:** Enkripsi data sensitif jangka panjang mulai mengadopsi algoritma hibrida tahan-kuantum (seperti ML-KEM).

---

## 4. Advanced Threat Mitigation (2026 Vulnerability Response)

Bab ini merinci kontrol arsitektur specifik untuk memitigasi serangan kritikal terbaru seperti eksploitasi *Remote Code Execution* (RCE) ke *Reverse Shell* (PHP2Shell/serupa) dan kerentanan *Zero-Day* pada komponen internal.

### 4.1 PHP2Shell & RCE Hardening
Untuk memutus rantai serangan jika penyerang berhasil mengeksploitasi celah kode (RCE) dan mencoba menyuntikkan *malicious shell*:
*   **Drastic Egress Filtering:** Seluruh *Application Container/Server* dilarang keras melakukan inisiasi koneksi keluar (*outbound/egress*) ke internet publik, kecuali ke IP *whitelist* resmi (misal: API payment gateway). Hal ini mencegah *Reverse Shell* menghubungi server *Command & Control* (C2) milik penyerang.
*   **Read-Only File System:** *Runtime environment* aplikasi dijalankan dengan mode **Read-Only Root Filesystem**. Penyerang tidak dapat menulis file `.php` baru atau mengubah skrip yang ada di direktori server.
*   **Disabled Functions:** Pada tingkat konfigurasi runtime (misal `php.ini`), fungsi-fungsi eksekusi sistem wajib dimatikan total:
    ```ini
    disable_functions = exec, passthru, shell_exec, system, proc_open, popen, curl_exec, socket_create
    ```
*   **Isolated Upload Directory:** Direktori untuk *user upload* dipisahkan ke Object Storage eksternal (misal: AWS S3) dengan eksekusi skrip non-aktif (`No-Exec`).

### 4.2 Runtime Application Self-Protection (RASP)
*   Mengintegrasikan agen **RASP** di tingkat runtime aplikasi untuk menganalisis perilaku kode secara *real-time*. Jika ada panggilan fungsi tidak wajar akibat eksploitasi *Zero-Day*, RASP akan langsung menghentikan proses (*kill request*) sebelum menyentuh sistem operasi.

### 4.3 Container Hardening & Minimal Images
*   **Distroless Images:** Menggunakan *base image* kontainer tanpa *shell* (`/bin/sh` atau `/bin/bash`) dan tanpa *package manager* (seperti `apt` atau `apk`). Tanpa adanya utilitas *shell* di dalam kontainer, skrip *exploit* otomatis gagal dieksekusi.
*   **Non-Root Execution:** Kontainer dilarang keras berjalan sebagai user `root`. Setiap proses dijalankan menggunakan user dengan hak akses terbatas (`UID 10001`).

---

## 5. Application & Software Supply Chain Security
*   **Secure Development:** Pemindaian kode statis (SAST) dan dinamis (DAST) diintegrasikan langsung ke dalam pipeline CI/CD (misal: CodeQL, SonarQube).
*   **SBOM (Software Bill of Materials):** Kompilasi otomatis manifes dependensi (CycloneDX/SPDX) di setiap *build* untuk mendeteksi *dependency substitution* dan kerentanan pihak ketiga via Dependabot/Snyk.
*   **Security Headers:** Setiap respons HTTP wajib menyertakan:
    ```http
    Content-Security-Policy: default-src 'self'; ...
    Strict-Transport-Security: max-age=63072000; includeSubDomains; preload
    X-Frame-Options: DENY
    X-Content-Type-Options: nosniff
    ```

---

## 6. Monitoring, Logging, & Incident Response
*   **Centralized Logging:** Log audit aplikasi, infrastruktur, dan akses dikirimkan secara *real-time* ke SIEM terpusat dengan proteksi *write-once-read-many* (WORM) untuk mencegah manipulasi log.
*   **Threat Detection:** Deteksi anomali berbasis kecerdasan buatan untuk mengidentifikasi pola serangan siber tak dikenal secara instan.
*   **Incident Response:** Jika terjadi insiden keamanan, tim mengikuti prosedur *Incident Response Plan* yang terdokumentasi dengan target MTTR (Mean Time to Respond) di bawah 30 menit melalui otomatisasi *isolation script*.

---

## 7. Compliance & References
Dokumen arsitektur ini divalidasi dan diuji kepatuhannya terhadap regulasi berikut:
*   **ISO/IEC 27001:2022** (Information Security Management System)
*   **NIST Cybersecurity Framework 2.0**
*   **DORA** / **NIS2** *(jika beroperasi atau melayani wilayah Uni Eropa)*
*   **UU Perlindungan Data Pribadi (UU PDP)** *(Kepatuhan enkripsi data subjek lokal)*

---
*Terakhir Diperbarui: November 2026 oleh Tim Arsitektur Keamanan Siber.*
