# 🌍 Project
- **Architecture:** Monolith.
- **Stack:** Laravel 12, Vue 3 (Composition API), Inertia.js, Tailwind CSS, PostgreSQL.
 
# 🧠 Aturan Dokumentasi & Komentar (WAJIB DIPATUHI)
Saya bekerja sendiri (Solo Developer). Kode harus bisa menjelaskan dirinya sendiri.
 
1.  **Bahasa Komentar:**
    - SELURUH komentar dalam kode WAJIB menggunakan **Bahasa Indonesia**.
 
2.  **Kualitas Komentar:**
    - Jangan menjelaskan SYNTAX (Contoh salah: `// Ini loop`).
    - Jelaskan **TUJUAN BISNIS** (Contoh benar: `// Loop rekomendasi PO untuk memfilter stok distributor yang kosong`).
    - Setiap `Function` atau `Method` wajib memiliki komentar blok di atasnya yang menjelaskan:
        - Apa fungsinya?
        - Apa parameternya?
        - Apa yang dikembalikan (return)?
 
# 🛠 Standar Coding
1.  **Laravel (Backend):**
    - Gunakan **Strict Types** (`declare(strict_types=1);`) di baris pertama file PHP.
    - Hindari logic berat di Controller. Pindahkan kalkulasi rumit ke folder `app/Services`.
    - Gunakan **Enum** untuk status PO (misal: `Pending`, `Approved`, `InTransit`).
 
2.  **Vue.js (Frontend):**
    - **DILARANG** menggunakan Class-Based Component.
    - **WAJIB** menggunakan `<script setup lang="js">`.
    - Pecah UI yang rumit menjadi komponen kecil di folder `Components`.
 
3.  **Docker Workflow:**
    - Environment berjalan di Docker.
    - Jika diminta menjalankan perintah artisan/composer, gunakan prefix:
      `docker-compose exec app [command]`
 
# 🚫 Larangan Keras
- Jangan pernah menghapus komentar yang sudah ada kecuali kodenya dihapus.
- Jangan gunakan jQuery.
- Jangan gunakan `any` atau `mixed` type jika tipe datanya sudah jelas.
 