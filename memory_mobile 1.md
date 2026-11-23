# Memory Dokumen: Arsitektur Backend, Skema Database, & Integrasi API Mobile
**Aplikasi:** FieldTrack Pro (ASW Foods)  
**Versi Sistem:** 1.0.0 (Field Survey & Audit Lapangan)  
**Terakhir Diperbarui:** 16 November 2026  

---

## 1. Arsitektur & Peran Surveyor Lapangan

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

---

## 2. Struktur Rubrik Penilaian & Formula Scoring

### A. Komposisi Total Survey Score (Skala 100%)
Total skor survei toko menggabungkan **4 Kriteria Fisik Member (85%)** dan **Indikator L12 Pengenalan SPV Leader (15%)**:

$$\text{Total Survey Score} = \underbrace{(W_{M1} + W_{M2} + W_{M3} + W_{M4})}_{85\%} + \underbrace{W_{L12\text{ (Pengenalan SPV)}}}_{15\%} = 100\%$$

| Kode | Aspek Penilaian | Peran | Bobot | Cara Penskoran | Keberlakuan / Catatan |
| :--- | :--- | :---: | :---: | :--- | :--- |
| **M1** | Keakuratan Geotag GPS | Member | **25%** | $\le 10\text{m}: 100\%$, $11-25\text{m}: 75\%$, $26-50\text{m}: 50\%$, $51-100\text{m}: 25\%$, $>100\text{m}: 0\%$ | Berlaku untuk semua jenis toko (termasuk Trader) |
| **M2** | Rak Display Sewa Terpasang | Member | **20%** | Planogram: 100%, 5 item: 75%, 3 item: 50%, 2 item: 25%, Kosong: 0% | **Kondisional**: Hanya jika outlet berstatus sewa rak. Trader otomatis **N/A** |
| **M3** | Price Tag Sesuai Planogram | Member | **20%** | Rasio kesesuaian SKU Top 5 + 1 NPL $\left(\frac{X}{6} \times 100\%\right)$ | Berlaku toko umum; Trader otomatis **N/A** |
| **M4** | Rentang Harga Jual GM Area | Member | **20%** | Rasio SKU dengan harga dalam batas min/max GM $\left(\frac{X}{6} \times 100\%\right)$ | Berlaku semua jenis toko (termasuk Trader) |
| **L12** | Validasi Pengenalan SPV | Leader | **15%** | Benar = 100%, Salah = 0%, N/A = Ditunda (Revisit) | Wawancara uji buta 3 foto SPV cabang oleh Leader |

---

### B. Aturan Redistribusi Bobot Proporsional (N/A Handling)
Jika suatu kriteria bernilai **N/A** (misal toko bukan peserta sewa display, atau tipe Trader yang tidak memiliki rak/price tag fisik), bobot kriteria tersebut **tidak dianggap nilai 0**, melainkan otomatis diredistribusikan secara proporsional ke kriteria yang berlaku:

$$\text{Final Score (\%)} = \frac{\sum (\text{Skor Kriteria Aktif} \times \text{Bobot Kriteria Aktif})}{\sum \text{Bobot Kriteria Aktif}} \times 100$$

---

### C. Aturan Penguncian Skor & Revisit
1. **Skor Belum Lengkap (Parsial):**  
   Skor gabungan `final_combined_score` **tidak dihitung ulang (tetap NULL)** jika salah satu surveyor (Member atau Leader) belum menyelesaikan submit survei. Status kunjungan ditandai `Parsial` (`is_survey_complete = false`).
2. **Kalkulasi Final:**  
   Ketika kedua surveyor (`is_visited_leader = true` dan `is_visited_member = true`) telah menyelesaikan survei, backend otomatis menghitung dan mengunci `final_combined_score` pada kedua record kunjungan.
3. **PIC Toko Tidak di Tempat:**  
   Jika penanggung jawab toko tidak berada di tempat saat sesi wawancara Leader:
   * Butir **L12** diberi jawaban **N/A**.
   * Surveyor Leader **wajib melampirkan alasan** pada saat check-out akhir toko (`pic_absence_reason`).
   * Toko otomatis ditandai `needs_revisit = true`.
   * Pada rute kunjungan berikutnya, aplikasi mobile menandai toko tersebut dengan badge **`Revisit L12`** agar surveyor dapat melakukan penilaian susulan langsung di lokasi.

---

## 3. Skema Database PostgreSQL

```
                              ┌───────────────────────────────────┐
                              │              regions              │
                              └─────────────────┬─────────────────┘
                                                │ 1:N
                              ┌─────────────────┴─────────────────┐
                              │             entities              │
                              └─────────────────┬─────────────────┘
                                                │ 1:N
                              ┌─────────────────┴─────────────────┐
                              │             branches              │
                              └───────┬───────────────────┬───────┘
                                      │ 1:N               │ 1:N
                    ┌─────────────────┴─────┐   ┌─────────┴────────────────┐
                    │        outlets        │   │          users           │
                    └───────────┬───────────┘   │  (HO, SPV, Leader, Mbr)  │
                                │               └─────────┬────────────────┘
                                │ 1:N                     │
                                ├─────────────────────────┤
                                ▼                         ▼
                    ┌──────────────────────────────────────────────┐
                    │                    visits                    │
                    │ ──────────────────────────────────────────── │
                    │ id (PK)                                      │
                    │ offline_sync_id (UUID, Unique)               │
                    │ outlet_id (FK -> outlets)                    │
                    │ user_id (FK -> users)                        │
                    │ role_type ('leader' | 'member')              │
                    │ visit_date (date)                            │
                    │ status ('draft'|'submitted'|'verified')      │
                    │ checkin_latitude / checkin_longitude         │
                    │ checkin_at / checkout_at                     │
                    │ total_score (numeric 5,2)                    │
                    │ final_combined_score (numeric 5,2, nullable) │
                    │ is_survey_complete (boolean)                 │
                    │ pic_absence_reason (text, nullable)          │
                    │ needs_revisit (boolean)                      │
                    │ revisit_notes (text, nullable)               │
                    │ notes (text, nullable)                       │
                    └───────────────┬──────────────────────────────┘
                                    │
            ┌───────────────────────┴───────────────────────┐
            ▼                                               ▼
┌───────────────────────────────┐               ┌───────────────────────────────┐
│    survey_leader_responses   │               │     survey_member_audits      │
│ ───────────────────────────── │               │ ───────────────────────────── │
│ id (PK)                       │               │ id (PK)                       │
│ visit_id (FK -> visits)       │               │ visit_id (FK -> visits)       │
│ question_code ('L1'..'L12')   │               │ actual_latitude / longitude   │
│ answer_value ('Ya','Tidak','NA')              │ distance_meters               │
│ is_scoreable (boolean)        │               │ geotag_score_rate (0.0..1.0)  │
│ score_value (1, 0, null)      │               │ is_rak_applicable (boolean)   │
│ selected_supervisor_id (FK)   │               │ rak_condition (string)        │
│ is_correct_supervisor (bool)  │               │ rak_score_rate (0.0..1.0)     │
│ rating_value (int 0..10, L11) │               │ price_tag_score_rate (0..1.0) │
│ notes (text)                  │               │ price_range_score_rate(0..1.0)│
└───────────────────────────────┘               │ final_member_score            │
                                                └───────────────┬───────────────┘
                                                                │ 1:N
                                                ┌───────────────┴───────────────┐
                                                │   survey_member_sku_audits    │
                                                │ ───────────────────────────── │
                                                │ id (PK)                       │
                                                │ survey_member_audit_id (FK)   │
                                                │ product_id (FK -> products)   │
                                                │ has_price_tag (boolean)       │
                                                │ is_price_tag_compliant (bool)│
                                                │ actual_price (numeric 12,2)   │
                                                │ min_allowed_price             │
                                                │ max_allowed_price             │
                                                │ is_price_in_range (boolean)   │
                                                └───────────────────────────────┘
```

---

## 4. Alur Integrasi API Mobile (Android Kotlin Native)

Semua endpoint mobile menggunakan autentikasi Bearer Token via **Laravel Sanctum**.  
Header wajib:
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
    "password": "password123"
  }
  ```
* **Response (200 OK):**
  ```json
  {
    "status": "success",
    "data": {
      "token": "1|abcdef123456...",
      "user": {
        "id": 5,
        "name": "Budi Santoso",
        "username": "leader01",
        "role": "leader",
        "branch_id": 12
      }
    }
  }
  ```

---

### B. Pengambilan Rute Kunjungan Harian (Route Planning)
* **Endpoint:** `GET /api/v1/routes/daily`
* **Query Params (Opsional):** `?date=2026-11-20`
* **Response:** Mengembalikan daftar 10 toko wajib + 5 toko tambahan terurut sejalur koordinat GPS.
* **Struktur Objek Toko:**
  ```json
  {
    "id": 105,
    "branch_id": 12,
    "kode_toko": "DABIR002",
    "nama_toko": "UD. AD RAHMAT BIREUEN",
    "alamat": "Jl. Merdeka No. 45",
    "latitude": 5.1845642,
    "longitude": 97.1498704,
    "class_outlet": "RWO",
    "is_sewa_display": true,
    "stop_order": 1,
    "outlet_type": "wajib",
    "is_visited_by_me": false,
    "is_visited_leader": false,
    "is_visited_member": false,
    "is_survey_complete": false,
    "final_combined_score": null,
    "needs_revisit": true,
    "revisit_reason": "PIC toko sedang keluar kota, lakukan blind test SPV susulan",
    "partner_name": "Ahmad Yani"
  }
  ```

---

### C. Pengambilan Master Kuesioner Survei
* **Endpoint:** `GET /api/v1/master/survey-questions?role=leader` (atau `?role=member`)
* **Response:**
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
        "question_text": "Apakah Sales mengunjungi toko ini dalam 14 hari terakhir?",
        "answer_type": "yes_no_na",
        "scoring_guide": "Ya = 1, Tidak = 0, N/A tidak dihitung",
        "weight": 1.0,
        "is_active": true
      },
      {
        "id": 12,
        "question_code": "L12",
        "aspect": "Pengenalan SPV",
        "question_text": "Toko diminta menunjuk FOTO SPV dari 3 foto...",
        "answer_type": "photo_choice",
        "scoring_guide": "Benar = 1, Salah = 0, N/A = Revisit (Bobot 15% skor akhir)",
        "weight": 0.15,
        "is_active": true
      }
    ]
  }
  ```

---

### D. Submit Hasil Wawancara Surveyor Leader
* **Endpoint:** `POST /api/v1/survey/leader/submit`
* **Request Payload:**
  ```json
  {
    "offline_sync_id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "outlet_id": 105,
    "visit_date": "2026-11-20",
    "checkin_latitude": 5.1845600,
    "checkin_longitude": 97.1498700,
    "checkin_at": "2026-11-20 09:15:00",
    "checkout_at": "2026-11-20 09:40:00",
    "notes": "Kunjungan berjalan tertib",
    "pic_absence_reason": "Pemilik sedang keluar kota, toko dijaga kasir pengganti",
    "needs_revisit": true,
    "revisit_notes": "Lakukan penilaian susulan L12 pada jadwal berikutnya",
    "responses": [
      { "question_code": "L1", "answer_value": "Ya", "notes": null },
      { "question_code": "L2", "answer_value": "Ya", "notes": null },
      { "question_code": "L10", "answer_value": "Minta diskon ditambah", "notes": "Saran promo" },
      { "question_code": "L11", "answer_value": "9", "rating_value": 9 },
      { "question_code": "L12", "answer_value": "N/A", "selected_supervisor_id": null }
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
      "status": "submitted"
    }
  }
  ```

---

### E. Submit Hasil Audit Fisik Surveyor Member
* **Endpoint:** `POST /api/v1/survey/member/submit`
* **Request Payload:**
  ```json
  {
    "offline_sync_id": "7a3ceb1c-2c5d-4f1e-8bcc-1c0d5a2ebb4f",
    "outlet_id": 105,
    "visit_date": "2026-11-20",
    "actual_latitude": 5.1845640,
    "actual_longitude": 97.1498710,
    "checkin_at": "2026-11-20 09:15:00",
    "checkout_at": "2026-11-20 09:42:00",
    "rak_condition": "terpasang_rapih",
    "rak_photo_path": "uploads/surveys/rak_105.jpg",
    "notes": "Display rapi sesuai SOP",
    "sku_audits": [
      {
        "product_id": 1,
        "has_price_tag": true,
        "is_price_tag_compliant": true,
        "actual_price": 12500,
        "sku_photo_path": "uploads/surveys/sku_1.jpg"
      },
      {
        "product_id": 2,
        "has_price_tag": true,
        "is_price_tag_compliant": true,
        "actual_price": 8500,
        "sku_photo_path": "uploads/surveys/sku_2.jpg"
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
      "total_score": 93.33,
      "geotag_score_rate": 1.0,
      "rak_score_rate": 1.0
    }
  }
  ```

---

### F. Submit Kunjungan Belum Selesai / Terkendala (Incomplete Survey Check-Out)
Digunakan ketika surveyor sudah check-in toko, namun survei terhambat (karena PIC tidak di tempat, toko ramai, dsb.) dan surveyor melakukan check-out dengan kondisi form survei masih kosong / belum lengkap.

* **Endpoint:** `POST /api/v1/survey/leader/submit` (atau `POST /api/v1/survey/member/submit`)
* **Request Payload:**
  ```json
  {
    "offline_sync_id": "8c4dea2b-4c5e-4f3a-9cba-3b1d8b4dcb7e",
    "outlet_id": 105,
    "visit_date": "2026-11-20",
    "checkin_latitude": 5.1845600,
    "checkin_longitude": 97.1498700,
    "checkin_at": "2026-11-20 09:15:00",
    "checkout_at": "2026-11-20 09:25:00",
    "is_incomplete": true,
    "incomplete_reason_type": "busy_store",
    "incomplete_reason": "Toko sedang sangat ramai dikunjungi pembeli, pemilik tidak bersedia diwawancarai saat jam sibuk",
    "needs_revisit": true,
    "revisit_notes": "Jadwalkan kunjungan susulan pada jam sepi toko",
    "responses": []
  }
  ```
* **Response (200 OK):**
  ```json
  {
    "status": "success",
    "message": "Data survey Leader berhasil disimpan dan diskor.",
    "data": {
      "visit_id": 44,
      "offline_sync_id": "8c4dea2b-4c5e-4f3a-9cba-3b1d8b4dcb7e",
      "total_score": 0.0,
      "status": "submitted"
    }
  }
  ```

---

## 5. Spesifikasi Desain Modal Intersepsi Check-Out Dini di Android

```
┌────────────────────────────────────────────────────────────────────────┐
│           ⚠️  Survei Toko Belum Lengkap / Data Masih Kosong            │
│ ────────────────────────────────────────────────────────────────────── │
│ Anda mencoba melakukan Check-Out namun pertanyaan survei belum diisi.   │
│ Mohon pilih alasan kendala operasional lapangan yang terjadi:          │
│                                                                        │
│ ( ) 1. Pemilik / PIC Toko Tidak Berada di Tempat                       │
│        (Keluar kota, toko dijaga kasir pengganti / tidak berwenang)    │
│ ( ) 2. Toko Sedang Sangat Ramai Dikunjungi Pembeli                     │
│        (Pemilik / kasir sedang sibuk melayani transaksi pelanggan)     │
│ ( ) 3. Toko Tutup / Sedang Renovasi / Tidak Beroperasi Sementara       │
│ ( ) 4. Pemilik / Penanggung Jawab Toko Menolak Disurvei                │
│ ( ) 5. Kondisi Darurat / Cuaca Buruk / Akses Jalan Terhalang           │
│ (●) 6. Lainnya (Alasan Khusus Lainnya)                                 │
│                                                                        │
│ ┌─ Tuliskan Alasan Spesifik (Wajib jika memilih 'Lainnya') ──────────┐ │
│ │ Pemilik meminta surveyor datang kembali besok sore pukul 16:00 WIB │ │
│ └────────────────────────────────────────────────────────────────────┘ │
│                                                                        │
│ ℹ️ Toko akan otomatis ditandai 'Perlu Kunjungan Ulang (Revisit)'       │
│    pada rute jadwal berikutnya untuk melengkapi data yang tertunda.    │
│                                                                        │
│   [ Batal (Lanjutkan Isi Survei) ]   [ Simpan & Check-Out Toko ]       │
└────────────────────────────────────────────────────────────────────────┘
```

### Aturan Logika Modal di Aplikasi Android:
1. **Pemicu (Trigger):**
   * Surveyor menekan tombol **"Check-Out"** di layar toko.
   * Sistem mendeteksi `responses` (Leader) atau `sku_audits` (Member) masih kosong atau belum lengkap terisi.
2. **Pilihan Alasan Standar (`incomplete_reason_type`):**
   * `pic_absent`: Pemilik / PIC Toko Tidak Berada di Tempat
   * `busy_store`: Toko Sedang Sangat Ramai Dikunjungi Pembeli
   * `store_closed`: Toko Tutup / Sedang Renovasi / Tidak Beroperasi Sementara
   * `refused`: Pemilik / Penanggung Jawab Toko Menolak Disurvei
   * `emergency`: Kondisi Darurat / Cuaca Buruk / Akses Terhalang
   * `other`: Lainnya (Alasan Khusus Lainnya)
3. **Validasi Pilihan 'Lainnya':**
   * Jika memilih opsi `other`, Textarea input manual wajib diisi minimal 5 karakter.
   * Teks yang diketik dimasukkan ke dalam properti `incomplete_reason`.
4. **Hasil Submit:**
   * Aplikasi mengirim flag `is_incomplete: true`, `needs_revisit: true`, dan `incomplete_reason`.
   * Pada rute harian berikutnya, toko ini memiliki badge **`Revisit: Data Belum Lengkap`**.

---

## 6. Ringkasan Siklus Status Data

```
[Offline SQLite Room]
        │ (Koneksi Pulih)
        ▼
[HTTP POST /api/v1/survey/{role}/submit]
        │
        ├─► [Kondisi Check-Out Dini / Incomplete Survey]
        │   ├─► is_incomplete = true
        │   ├─► needs_revisit = true
        │   ├─► incomplete_reason = "...alasan kendala..."
        │   ├─► total_score = 0.0
        │   └─► final_combined_score = null (TIDAK digabung)
        │
        └─► [Kondisi Survei Normal & Lengkap]
            ├─► Simpan data visit (idempotent via offline_sync_id)
            ├─► Hitung skor mandiri role
            ▼
[Cek Apakah Partner Surveyor Sudah Submitted & Lengkap?]
        │
        ├─► [BELUM] ──► Status: is_survey_complete = false
        │               Skor final_combined_score = null (TIDAK di-recalculate)
        │
        └─► [SUDAH] ──► Status: is_survey_complete = true
                        Ambil L12 dari sesi Leader (Bobot 15%)
                        Kalkulasi final_combined_score (100%)
                        Kunci nilai di tabel visits dan route_plan_outlets
```

