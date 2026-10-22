package com.codingkidz.app.content

import com.codingkidz.app.engine.Concept
import kotlin.random.Random

data class Question(
    val id: String,
    val prompt: String,
    val options: List<String>,
    val answer: Int,
    val explanation: String,
    val concept: Concept,
    /** Potongan kode/gambar opsional yang ditampilkan dengan huruf monospace. */
    val code: String? = null,
)

/** Soal pilihan ganda tentang pola, urutan, perulangan, logika, dan debugging. */
object QuizBank {
    val all = listOf(
        Question(
            "q1", "Lanjutkan polanya!", listOf("🔵", "🔴", "🟢", "🟡"), 0,
            "Warnanya bergantian merah lalu biru. Setelah merah selalu biru.", Concept.ART,
            code = "🔴 🔵 🔴 🔵 🔴 ❓",
        ),
        Question(
            "q2", "Gambar apa yang berikutnya?", listOf("🟢", "🟡", "🔴", "🔵"), 1,
            "Polanya: hijau, hijau, kuning — lalu berulang.", Concept.ART,
            code = "🟢 🟢 🟡 🟢 🟢 🟡 🟢 🟢 ❓",
        ),
        Question(
            "q3", "Angka berapa selanjutnya?", listOf("8", "9", "10", "11"), 1,
            "Setiap angka bertambah 2: 1, 3, 5, 7, 9.", Concept.ART,
            code = "1, 3, 5, 7, ❓",
        ),
        Question(
            "q4", "Angka berapa selanjutnya?", listOf("18", "24", "32", "20"), 2,
            "Setiap angka dikali 2: 16 × 2 = 32.", Concept.ART,
            code = "2, 4, 8, 16, ❓",
        ),
        Question(
            "q5", "Bito menghadap ke atas ⬆️, lalu belok kanan 1 kali. Sekarang Bito menghadap ke mana?",
            listOf("➡️ Kanan", "⬅️ Kiri", "⬇️ Bawah", "⬆️ Atas"), 0,
            "Belok kanan dari atas membuat Bito menghadap ke kanan.", Concept.SEQUENCE,
        ),
        Question(
            "q6", "Bito menghadap ke kanan ➡️, lalu belok kiri 2 kali. Sekarang menghadap ke mana?",
            listOf("⬆️ Atas", "⬇️ Bawah", "⬅️ Kiri", "➡️ Kanan"), 2,
            "Dua kali belok kiri sama dengan putar balik.", Concept.SEQUENCE,
        ),
        Question(
            "q7", "Bito menghadap ke atas ⬆️ dan belok kanan 4 kali. Sekarang menghadap ke mana?",
            listOf("⬆️ Atas", "➡️ Kanan", "⬇️ Bawah", "⬅️ Kiri"), 0,
            "Empat kali belok kanan = satu putaran penuh, kembali ke arah semula.", Concept.SEQUENCE,
        ),
        Question(
            "q8", "Berapa kali Bito maju?", listOf("1", "3", "4", "6"), 1,
            "Perulangan menjalankan maju() sebanyak 3 kali.", Concept.LOOP,
            code = "for i in range(3):\n    maju()",
        ),
        Question(
            "q9", "Berapa kali Bito maju?", listOf("5", "6", "3", "2"), 1,
            "Perulangan luar 2 kali, perulangan dalam 3 kali: 2 × 3 = 6.", Concept.LOOP,
            code = "for i in range(2):\n    for j in range(3):\n        maju()",
        ),
        Question(
            "q10", "Berapa kali Bito maju?", listOf("4", "6", "8", "2"), 2,
            "Setiap putaran ada 2 maju(), diulang 4 kali: 4 × 2 = 8.", Concept.LOOP,
            code = "for i in range(4):\n    maju()\n    maju()",
        ),
        Question(
            "q11", "Aturannya: JIKA hujan, MAKA bawa payung. Hari ini cerah. Apakah kamu membawa payung?",
            listOf("Ya", "Tidak"), 1,
            "Kondisinya (hujan) salah, jadi perintah bawa payung tidak dijalankan.", Concept.CONDITION,
        ),
        Question(
            "q12", "Benar atau salah?", listOf("Benar", "Salah"), 1,
            "DAN hanya benar jika kedua bagian benar. 2 > 4 itu salah, jadi semuanya salah.", Concept.CONDITION,
            code = "(5 > 3) DAN (2 > 4)",
        ),
        Question(
            "q13", "Benar atau salah?", listOf("Benar", "Salah"), 0,
            "ATAU sudah benar jika salah satu bagian benar. 5 > 3 itu benar.", Concept.CONDITION,
            code = "(5 > 3) ATAU (2 > 4)",
        ),
        Question(
            "q14", "Mana urutan yang benar untuk menyikat gigi?",
            listOf(
                "Ambil sikat → Beri pasta → Sikat gigi → Kumur",
                "Sikat gigi → Ambil sikat → Kumur → Beri pasta",
                "Kumur → Sikat gigi → Beri pasta → Ambil sikat",
            ),
            0, "Algoritma harus berurutan dengan benar supaya berhasil.", Concept.SEQUENCE,
        ),
        Question(
            "q15", "Bito harus maju 3 kotak lalu mengambil permata. Apa yang salah dari program ini?",
            listOf("Kurang satu maju()", "ambil() harus di awal", "Tidak ada yang salah", "Kelebihan maju()"), 0,
            "Programnya hanya maju 2 kali, padahal permata ada 3 kotak di depan.", Concept.DEBUG,
            code = "maju()\nmaju()\nambil()",
        ),
        Question(
            "q16", "Fungsi lompat() berisi maju() dua kali. Jika lompat() dipanggil 3 kali, berapa kali Bito maju?",
            listOf("3", "5", "6", "2"), 2,
            "Setiap panggilan menjalankan 2 maju(): 3 × 2 = 6.", Concept.FUNCTION,
            code = "def lompat():\n    maju()\n    maju()\n\nlompat()\nlompat()\nlompat()",
        ),
        Question(
            "q17", "Berapa nilai x sekarang?", listOf("2", "3", "5", "23"), 2,
            "x mula-mula 2, lalu ditambah 3 sehingga menjadi 5.", Concept.SEQUENCE,
            code = "x = 2\nx = x + 3",
        ),
        Question(
            "q18", "\"Selama gelas belum penuh, tuang air.\" Kapan kamu berhenti menuang?",
            listOf("Setelah 3 kali tuang", "Saat gelas penuh", "Tidak pernah berhenti", "Sebelum mulai"), 1,
            "Perulangan \"selama\" berhenti saat syaratnya tidak terpenuhi lagi.", Concept.WHILE,
        ),
        Question(
            "q19", "Apa itu algoritma?",
            listOf("Langkah-langkah untuk menyelesaikan masalah", "Nama sebuah robot", "Jenis komputer", "Tombol di keyboard"), 0,
            "Algoritma adalah urutan langkah yang jelas untuk menyelesaikan suatu tugas.", Concept.SEQUENCE,
        ),
        Question(
            "q20", "Apa arti \"bug\" dalam pemrograman?",
            listOf("Serangga di komputer", "Kesalahan dalam program", "Program yang cepat", "Nama bahasa pemrograman"), 1,
            "Bug adalah kesalahan dalam program. Memperbaikinya disebut debugging.", Concept.DEBUG,
        ),
        Question(
            "q21", "Mana yang merupakan contoh perulangan?",
            listOf("Lompat 10 kali", "Makan siang", "Membuka pintu sekali", "Tidur"), 0,
            "Melakukan hal yang sama berulang kali adalah perulangan.", Concept.LOOP,
        ),
        Question(
            "q22", "Bito di kotak (2, 3). Bito maju 2 kotak ke kanan. Sekarang Bito di kotak mana?",
            listOf("(4, 3)", "(2, 5)", "(3, 4)", "(0, 3)"), 0,
            "Ke kanan menambah angka pertama (x): 2 + 2 = 4.", Concept.SEQUENCE,
        ),
        Question(
            "q23", "Bentuk apa yang berikutnya?", listOf("🔺", "🟦", "⭐", "🟢"), 1,
            "Polanya: segitiga, segitiga, kotak.", Concept.ART,
            code = "🔺 🔺 🟦 🔺 🔺 🟦 🔺 🔺 ❓",
        ),
        Question(
            "q24", "Semua robot bisa bergerak. Bito adalah robot. Maka…",
            listOf("Bito bisa bergerak", "Bito tidak bisa bergerak", "Bito bukan robot", "Tidak bisa ditentukan"), 0,
            "Jika semua robot bisa bergerak dan Bito robot, pasti Bito bisa bergerak.", Concept.CONDITION,
        ),
        Question(
            "q25", "Jika angkanya genap, warnai biru. Lainnya, warnai merah. Angka 7 diwarnai apa?",
            listOf("Biru", "Merah", "Hijau", "Tidak diwarnai"), 1,
            "7 adalah angka ganjil, jadi cabang \"lainnya\" yang dijalankan: merah.", Concept.CONDITION,
        ),
        Question(
            "q26", "Urutkan dari yang terkecil: 7, 2, 9, 4. Angka di urutan kedua adalah…",
            listOf("2", "4", "7", "9"), 1,
            "Urutannya 2, 4, 7, 9. Angka kedua adalah 4.", Concept.SEQUENCE,
        ),
        Question(
            "q27", "Program ini seharusnya berhenti di bendera, tapi tidak pernah berhenti. Bug apa ini?",
            listOf("Perulangan tak terbatas", "Kurang permata", "Salah warna", "Program terlalu pendek"), 0,
            "Perulangan yang syarat berhentinya tidak pernah terpenuhi disebut infinite loop.", Concept.DEBUG,
            code = "while not di_finis():\n    belok_kanan()",
        ),
        Question(
            "q28", "Bito mulai di kotak 0 dan maju 5 kali. Lalu mundur dengan putar balik dan maju 2 kali. Bito di kotak berapa?",
            listOf("2", "3", "5", "7"), 1,
            "Maju ke kotak 5, lalu kembali 2 kotak: 5 − 2 = 3.", Concept.SEQUENCE,
        ),
    )

    fun session(size: Int = 5, seed: Long = System.currentTimeMillis()): List<Question> =
        all.shuffled(Random(seed)).take(size)
}
