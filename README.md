# CodingKidz

Aplikasi Android untuk melatih logika coding anak (4–12 tahun) lewat petualangan robot **Bito** yang
menyelamatkan hewan di 9 dunia. Sepenuhnya offline, tanpa iklan, tanpa akun.

## Fitur

**Inti pembelajaran**
- Pemrograman blok dengan **drag & drop**: seret blok dari palet ke posisi mana pun, tekan-lama untuk
  memindahkan blok (termasuk ke dalam/luar perulangan), seret ke tempat sampah untuk membuang. Ketuk juga
  tetap bisa dipakai.
- **Transisi bertahap ke teks** sesuai usia: mode *Gambar* (ikon besar, nama blok dibacakan — 4–6 th),
  *Kata* (7–9 th), dan *Kode* (blok ↔ Python/JavaScript berdampingan — 10+ th). Bisa diubah di Pengaturan.
- **Kurikulum berjenjang**, 50 level: urutan → perulangan → percabangan → ulangi-sampai → **variabel** →
  fungsi → **event** → debugging → seni piksel. Plus tantangan harian (labirin baru setiap hari).
- **Jalankan langkah demi langkah**: blok yang sedang berjalan disorot, putaran perulangan dan hasil kondisi
  ditampilkan, nilai variabel terlihat di atas papan.
- **Tantangan debugging**: level berisi program rusak untuk diperbaiki.

**Game & motivasi**
- Cerita petualangan: setiap dunia punya hewan yang terkurung di tujuan; menamatkan dunia menyelamatkannya
  dan membuka dunia berikutnya.
- Banyak solusi; bintang dijelaskan per kriteria — selesai, kode rapi, dan **paling efisien**.
- Hadiah secukupnya: lencana, **kostum Bito** (dibuka oleh penguasaan konsep, bukan dibeli dengan poin),
  koleksi hewan. XP kuis dibatasi per hari.
- **Petunjuk 3 tingkat**: dorongan ringan → petunjuk spesifik → contoh solusi (baru terbuka setelah
  mencoba).
- **Studio Kreasi**: desain peta sendiri, event tombol ⬅⬆➡ dan sentuh, blok Katakan/Tunggu — untuk membuat
  game, animasi, dan cerita interaktif, lalu simpan ke galeri.

Juga: kuis logika, area orang tua (laporan, batas waktu layar, pengingat), tema gelap, Material You.

## Teknologi

Kotlin 2.4 · Jetpack Compose (BOM 2026.09) + Material 3 · Navigation 3 · DataStore + kotlinx.serialization ·
WorkManager · AGP 9.4 / Gradle 9.8 · compileSdk/targetSdk 37, minSdk 26.

## Struktur

- `engine/` — model blok, interpreter (merekam setiap langkah untuk animasi), editor pohon program, ekspor
  kode Python/JavaScript.
- `content/` — dunia & level, tantangan harian, kuis, contoh Studio.
- `data/` — progres, lencana, kostum (DataStore JSON).
- `ui/` — layar Compose; `ui/game/DragDrop.kt` berisi drag & drop untuk daftar blok bersarang.

## Build & tes

```bash
./gradlew :app:testDebugUnitTest
```

```bash
./gradlew :app:installDebug
```

Unit test memastikan semua level bisa diselesaikan dengan kunci jawabannya, program awal level debug
memang rusak, dan tantangan harian setahun penuh selalu bisa diselesaikan.

Catatan rilis: build `release` saat ini ditandatangani dengan kunci debug; ganti dengan kunci rilis sendiri
sebelum publikasi.
