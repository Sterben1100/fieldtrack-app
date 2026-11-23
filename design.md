version: alpha
name: Survey ASW
description: Design system for Field Survey ASWFOODS, optimized for a bright, editorial marketing homepage with strong hierarchy, rounded CTAs, and a clean survey-analytics product aesthetic, powered by ASW Foods brand color palette.
colors:
  primary: "#b2000c"
  secondary: "#444444"
  tertiary: "#eb1c23"
  neutral:
    0: "#ffffff"
    50: "#f8f9fa"
    100: "#f3f5f4"
    200: "#e5e7eb"
    600: "#7a7f7d"
    900: "#444444"
  surface:
    default: "#f8f9fa"
    raised: "#ffffff"
    muted: "#e5e7eb"
  on-surface:
    default: "#444444"
    muted: "#7a7f7d"
    inverse: "#ffffff"
  error: "#eb1c23"
typography:
  headline-display:
    fontFamily: "National 2"
    fontSize: "92px"
    fontWeight: 700
    lineHeight: "92px"
    letterSpacing: "-1.84px"
  headline-lg:
    fontFamily: "National 2"
    fontSize: "66px"
    fontWeight: 700
    lineHeight: "67.8px"
    letterSpacing: "-1.2px"
  headline-md:
    fontFamily: "National 2"
    fontSize: "47px"
    fontWeight: 700
    lineHeight: "56px"
    letterSpacing: "-0.72px"
  body-lg:
    fontFamily: "National 2"
    fontSize: "24px"
    fontWeight: 400
    lineHeight: "38px"
    letterSpacing: "0px"
  body-md:
    fontFamily: "National 2"
    fontSize: "16px"
    fontWeight: 400
    lineHeight: "26px"
    letterSpacing: "0px"
  body-sm:
    fontFamily: "National 2"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: "22px"
    letterSpacing: "0px"
  label-lg:
    fontFamily: "National 2"
    fontSize: "16px"
    fontWeight: 600
    lineHeight: "22px"
    letterSpacing: "0px"
  label-md:
    fontFamily: "National 2"
    fontSize: "14px"
    fontWeight: 600
    lineHeight: "20px"
    letterSpacing: "0px"
  label-sm:
    fontFamily: "Inter"
    fontSize: "14px"
    fontWeight: 600
    lineHeight: "20px"
    letterSpacing: "0px"
rounded:
  none: "0px"
  sm: "4px"
  md: "8px"
  lg: "16px"
  xl: "32px"
  full: "100px"
spacing:
  xs: "4px"
  sm: "12px"
  md: "20px"
  lg: "32px"
  xl: "96px"
components:
  button:
    primary:
      backgroundColor: "{colors.primary}"
      color: "{colors.neutral.0}"
      borderColor: "transparent"
      borderRadius: "{rounded.full}"
      borderWidth: "1px"
      borderStyle: "solid"
      padding: "8px 16px"
      fontSize: "{typography.label-sm.fontSize}"
      fontWeight: "{typography.label-sm.fontWeight}"
      minWidth: "160px"
      minHeight: "37px"
      textDecoration: "none"
      boxShadow: "none"
      fontFamily: "Inter"
    secondary:
      backgroundColor: "transparent"
      color: "{colors.secondary}"
      borderColor: "{colors.secondary}"
      borderRadius: "{rounded.full}"
      borderWidth: "1px"
      borderStyle: "solid"
      padding: "8px 16px"
      fontSize: "{typography.label-sm.fontSize}"
      fontWeight: "{typography.label-sm.fontWeight}"
      minWidth: "160px"
      minHeight: "37px"
      textDecoration: "none"
      boxShadow: "none"
      fontFamily: "National 2"
    link:
      backgroundColor: "transparent"
      color: "{colors.secondary}"
      borderColor: "transparent"
      borderRadius: "{rounded.none}"
      borderWidth: "0px"
      borderStyle: "none"
      padding: "0px"
      fontSize: "{typography.body-sm.fontSize}"
      fontWeight: 500
      minWidth: "0px"
      minHeight: "0px"
      textDecoration: "none"
      boxShadow: "none"
      fontFamily: "National 2"
  card:
    backgroundColor: "{colors.neutral.50}"
    borderColor: "{colors.neutral.200}"
    borderRadius: "{rounded.md}"
    borderWidth: "1px"
    borderStyle: "solid"
    padding: "16px"
    boxShadow: "none"
    textColor: "{colors.on-surface.default}"
---

# Overview

Survey ASW dengan nuansa palet **ASW Foods** memadukan estetika minimalis modern berstandar korporat dengan identitas brand merah khas perusahaan. Antarmuka mempertahankan fokus pada hierarki yang bersih, tombol CTA berbentuk *pill* yang ramah pengguna, serta estetika analitik survei yang profesional.

Gunakan sistem ini untuk mengomunikasikan kepercayaan, kejelasan operasional lapangan, dan tindakan yang tegas. Utamakan *whitespace* yang longgar, navigasi yang ringkas, serta penggunaan warna merah utama (`#b2000c`) secara konsisten untuk tindakan penting dan penekanan brand.

# Colors

Warna primer menggunakan merah khas ASW Foods (`#b2000c`). Permukaan latar belakang menggunakan nuansa putih bersih dan abu-abu terang korporat (`#f8f9fa`), dengan teks abu-abu gelap/arang (`#444444`) untuk salinan bodi dan tampilan agar terasa lembut dibaca.

Gunakan:
- **Primary (`#b2000c`)** untuk CTA utama, status aktif, penekanan data positif, dan indikator pilihan.
- **Secondary (`#444444`)** untuk teks bodi, tautan, garis batas, dan penekanan halus.
- **Tertiary (`#eb1c23`)** untuk aksen merah terang pendukung elemen promosi atau sorotan khusus.
- **Neutral / Surface** untuk kanvas halaman, kartu, dan pembatas modular.

Hindari memasukkan corak sekunder yang jenuh kecuali diperlukan untuk visualisasi data status atau grafik analitik. Jaga hubungan warna tetap sederhana dan kontras tinggi.

# Typography

Tipografi dipimpin oleh *National 2* untuk salinan brand dan produk, dengan *Inter* pada tombol utama. Judul tampilan dibuat tebal, tegas, dan berkarakter kuat.

Panduan token:
- `headline-display` untuk judul hero utama dan pernyataan kampanye besar.
- `headline-lg` dan `headline-md` untuk pengantar bagian dan judul fitur produk.
- `body-lg` untuk teks pendukung hero dan paragraf utama.
- `body-md` untuk salinan penjelasan standar.
- `label-md` dan `label-sm` untuk tombol, *chip*, dan UI utilitas.

Karakteristik:
- Teks tampilan harus dicetak tebal dengan *tracking* negatif yang rapat.
- Salinan bodi harus tetap mudah dibaca dengan tinggi baris yang nyaman.
- Pertahankan *sentence case* dan hindari gaya teknis yang berlebihan pada permukaan pemasaran.

# Layout

Komposisi halaman berpusat secara vertikal dengan jarak yang lapang. Konten hero dibatasi pada lebar baca yang nyaman dengan perataan tengah yang kuat, diikuti oleh klaster CTA terpusat dan modul analitik produk.

Gunakan isyarat tata letak berikut:
- Pusatkan konten hero pada halaman pemasaran kecuali halaman tersebut bersifat utilitas operasional.
- Tumpuk konten dengan celah besar antara hero, CTA, dan media pendukung.
- Gunakan wadah melengkung yang lebar untuk tangkapan layar produk dan panel analitik survei.
- Jaga navigasi tetap ramping dan horizontal dengan tindakan utilitas di sebelah kanan.

Token spasi dirancang sederhana: gunakan `xs` untuk celah internal yang rapat, `sm` untuk pengelompokan kecil, `md` untuk ritme konten kartu, `lg` untuk pemisahan bagian, dan `xl` untuk jeda vertikal utama.

# Elevation & Depth

Kedalaman dibatasi secara ketat. Sebagian besar permukaan datar, mengandalkan batas garis tipis dan kontras permukaan daripada bayangan tebal.

Aturan:
- Lebih mengutamakan batas garis tipis dan kontras permukaan daripada elevasi berat.
- Jangan menambahkan tumpukan bayangan berlapis atau efek buram (*blur*) yang dramatis.

# Shapes

Bentuk geometris melengkung diterapkan secara konsisten di seluruh sistem.

Gunakan:
- `full` untuk tombol *pill*, *tag*, dan kontrol UI kecil.
- `md` untuk kartu dan modul produk.
- `sm` hanya jika kontrol memerlukan kesan yang sedikit lebih rapat.

Pertahankan kelembutan kurva ini terutama pada elemen interaktif untuk mencerminkan kesan korporat yang modern dan mudah diakses.

# Components

## Button
Tombol utama berbentuk *pill* merah solid dengan teks putih dan *affordance* yang kuat. Tombol sekunder berbentuk *pill* bergaris tepi abu-abu gelap. Tombol tautan berbasis teks polos tanpa bingkai.

Catatan Implementasi:
- Pertahankan lebar dan tinggi minimum untuk kenyamanan sentuhan sentuh pada perangkat *mobile* maupun desktop.
- Gunakan teks 14px dengan bobot *semibold*.
- Pertahankan radius bentuk *pill*.
- Jangan mengubah tombol CTA menjadi bentuk persegi atau kontrol dengan elevasi tinggi.

## Card
Kartu bersifat netral, berbatas garis tipis, dan tenang. Mendukung pratinjau dasbor, modul ringkasan survei, dan ubin konten tanpa bersaing dengan judul utama.

Catatan Implementasi:
- Gunakan latar belakang putih atau abu-abu sangat muda (`#f8f9fa`).
- Pertahankan batas garis tipis tanpa bayangan kecuali diperlukan perlakuan khusus mengambang.
- Pertahankan padding yang sederhana dan konsisten.
- Utamakan hierarki internal yang jelas dan baris metadata yang ringkas.

# Do's and Don'ts

Do:
- Gunakan area hero berpusat dengan *whitespace* luas untuk halaman web portal / informasi.
- Pimpin dengan satu CTA utama yang kuat dan satu CTA sekunder.
- Jaga agar warna merah brand (`#b2000c`) dicadangkan untuk tindakan interaktif, pilihan, dan metrik kunci.
- Pertahankan kartu agar tetap datar, berbatas garis, dan melengkung lembut.
- Selaraskan dengan gaya korporat ASW Foods yang bersih, terang, dan profesional.

Don't:
- Jangan memasukkan gaya *dark mode*; brand didefinisikan untuk permukaan terang.
- Jangan menggunakan gradien mencolok, efek kaca, atau warna aksen sembarangan yang melenceng dari palet ASW Foods.
- Jangan membuat tombol menjadi persegi atau terlalu tinggi.
- Jangan menggunakan teks bodi berwarna hitam pekat, pertahankan abu-abu gelap korporat (`#444444`).
- Jangan mendesak bagian hero dengan terlalu banyak tautan atau pesan sekunder yang padat.