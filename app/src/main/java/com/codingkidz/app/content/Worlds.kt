package com.codingkidz.app.content

import com.codingkidz.app.engine.BlockType.CALL_A
import com.codingkidz.app.engine.BlockType.CHANGE_VAR
import com.codingkidz.app.engine.BlockType.IF
import com.codingkidz.app.engine.BlockType.IF_ELSE
import com.codingkidz.app.engine.BlockType.MOVE
import com.codingkidz.app.engine.BlockType.MOVE_VAR
import com.codingkidz.app.engine.BlockType.PAINT
import com.codingkidz.app.engine.BlockType.PICK
import com.codingkidz.app.engine.BlockType.REPEAT
import com.codingkidz.app.engine.BlockType.REPEAT_UNTIL
import com.codingkidz.app.engine.BlockType.SET_VAR
import com.codingkidz.app.engine.BlockType.TURN_LEFT
import com.codingkidz.app.engine.BlockType.TURN_RIGHT
import com.codingkidz.app.engine.Animal
import com.codingkidz.app.engine.Concept
import com.codingkidz.app.engine.Condition.ON_BLUE
import com.codingkidz.app.engine.Condition.ON_GEM
import com.codingkidz.app.engine.Condition.ON_ORANGE
import com.codingkidz.app.engine.Condition.PATH_AHEAD
import com.codingkidz.app.engine.Condition.PATH_LEFT
import com.codingkidz.app.engine.EventKind
import com.codingkidz.app.engine.Level
import com.codingkidz.app.engine.PaintColor.GREEN
import com.codingkidz.app.engine.PaintColor.PURPLE
import com.codingkidz.app.engine.PaintColor.RED
import com.codingkidz.app.engine.PaintColor.BLUE
import com.codingkidz.app.engine.PaintColor.WHITE
import com.codingkidz.app.engine.PaintColor.YELLOW
import com.codingkidz.app.engine.World
import com.codingkidz.app.engine.program

private val BASIC = setOf(MOVE, TURN_LEFT, TURN_RIGHT)
private val BASIC_PICK = BASIC + PICK
private val LOOPS = BASIC_PICK + REPEAT
private val CONDITIONS = LOOPS + IF
private val CONDITIONS_ELSE = CONDITIONS + IF_ELSE
private val WHILE = CONDITIONS_ELSE + REPEAT_UNTIL
private val FUNCTIONS = LOOPS + CALL_A
private val ART = setOf(MOVE, TURN_LEFT, TURN_RIGHT, PAINT, REPEAT)
private val VARIABLES = LOOPS + setOf(SET_VAR, CHANGE_VAR, MOVE_VAR)

/** Seluruh kurikulum: 9 dunia, masing-masing melatih satu konsep berpikir komputasional. */
object Worlds {

    private val sequence = World(
        id = "w1",
        number = 1,
        title = "Hutan Langkah",
        concept = Concept.SEQUENCE,
        emoji = "👣",
        colorArgb = 0xFF3B82F6,
        intro = "Komputer mengikuti perintah satu per satu, persis sesuai urutannya. " +
            "Urutan perintah ini disebut algoritma. Yuk susun langkah untuk Bito!",
        animal = Animal("🐱", "Kiki si Kucing"),
        story = "Oh tidak! Kiki si anak kucing tersesat di Hutan Langkah. Bantu Bito menyusun langkah yang tepat untuk menjemput Kiki!",
        nudges = listOf(
            "Coba bayangkan kamu adalah Bito. Ke mana kamu harus berjalan dulu?",
            "Hitung kotaknya satu per satu dengan jarimu.",
            "Ingat: belok hanya memutar badan Bito, tidak memindahkannya.",
        ),
        levels = listOf(
            Level(
                id = "w1-1",
                title = "Halo, Bito!",
                instruction = "Bantu Bito sampai ke bendera! Ketuk blok Maju untuk menambahkannya, lalu tekan Jalankan.",
                map = listOf(">...G"),
                blocks = setOf(MOVE),
                solution = program { moves(4) },
                hints = listOf(
                    "Hitung ada berapa kotak dari Bito ke bendera.",
                    "Bito perlu Maju sebanyak 4 kali.",
                ),
                funFact = "Kamu baru saja menulis program pertamamu! Program adalah daftar perintah untuk komputer.",
            ),
            Level(
                id = "w1-2",
                title = "Belokan Pertama",
                instruction = "Jalannya berbelok! Pakai blok Belok Kanan supaya Bito berputar menghadap ke jalan.",
                map = listOf(
                    ">..#",
                    "##.#",
                    "##G#",
                ),
                blocks = BASIC,
                solution = program { moves(2); right(); moves(2) },
                hints = listOf(
                    "Maju dulu sampai ujung jalan, baru belok.",
                    "Maju, Maju, Belok Kanan, Maju, Maju.",
                ),
                funFact = "Belok tidak memindahkan Bito, hanya mengubah arah hadapnya.",
            ),
            Level(
                id = "w1-3",
                title = "Ke Kiri!",
                instruction = "Kali ini bendera ada di atas. Ke mana Bito harus belok?",
                map = listOf(
                    "##G#",
                    "##.#",
                    ">..#",
                ),
                blocks = BASIC,
                solution = program { moves(2); left(); moves(2) },
                hints = listOf(
                    "Bayangkan kamu adalah Bito yang menghadap ke kanan. Bendera ada di sebelah kirimu.",
                    "Maju 2 kali, Belok Kiri, lalu Maju 2 kali.",
                ),
            ),
            Level(
                id = "w1-4",
                title = "Permata Pertama",
                instruction = "Ada permata di jalan! Berhenti di atas permata lalu pakai blok Ambil sebelum ke bendera.",
                map = listOf(">.*.G"),
                blocks = BASIC_PICK,
                solution = program { moves(2); pick(); moves(2) },
                hints = listOf(
                    "Blok Ambil hanya berhasil kalau Bito tepat berdiri di atas permata.",
                    "Maju, Maju, Ambil, Maju, Maju.",
                ),
            ),
            Level(
                id = "w1-5",
                title = "Tangga Kecil",
                instruction = "Turuni tangga menuju bendera. Perhatikan arah Bito setiap kali berbelok!",
                map = listOf(
                    ">.##",
                    "#..#",
                    "##.G",
                ),
                blocks = BASIC,
                solution = program { move(); right(); move(); left(); move(); right(); move(); left(); move() },
                hints = listOf(
                    "Setiap anak tangga: Maju, Belok Kanan, Maju, Belok Kiri.",
                    "Apakah kamu melihat pola yang berulang? Nanti kita akan belajar cara menyingkatnya!",
                ),
                funFact = "Pola yang berulang adalah petunjuk bahwa kita bisa memakai perulangan.",
            ),
            Level(
                id = "w1-6",
                title = "Kumpulkan Semua",
                instruction = "Ambil kedua permata, lalu bawa Bito pulang ke bendera.",
                map = listOf(
                    ">.*.",
                    "###.",
                    "G.*.",
                ),
                blocks = BASIC_PICK,
                solution = program {
                    moves(2); pick(); move(); right(); moves(2); right(); move(); pick(); moves(2)
                },
                hints = listOf(
                    "Pecah masalahnya: ambil permata pertama, lalu pikirkan jalan ke permata kedua.",
                    "Di ujung kanan, Bito harus belok kanan dua kali (di dua tikungan).",
                ),
                funFact = "Memecah masalah besar menjadi bagian kecil disebut dekomposisi.",
            ),
        ),
    )

    private val loops = World(
        id = "w2",
        number = 2,
        title = "Sungai Berulang",
        concept = Concept.LOOP,
        emoji = "🔁",
        colorArgb = 0xFFF97316,
        intro = "Kalau ada perintah yang sama berulang kali, programmer memakai perulangan. " +
            "Cukup tulis sekali, lalu suruh komputer mengulanginya!",
        animal = Animal("🐰", "Bubu si Kelinci"),
        story = "Bubu si kelinci terjebak di seberang Sungai Berulang. Jalannya panjang sekali — Bito harus pintar memakai perulangan!",
        nudges = listOf(
            "Apakah ada perintah yang kamu tulis berulang-ulang?",
            "Cari pola yang sama, lalu masukkan ke dalam blok Ulangi.",
            "Hitung berapa kali polanya terjadi.",
        ),
        levels = listOf(
            Level(
                id = "w2-1",
                title = "Lari Jauh",
                instruction = "Jalannya panjang sekali, tapi kamu hanya boleh memakai 2 blok! Masukkan Maju ke dalam blok Ulangi.",
                map = listOf(">......G"),
                blocks = LOOPS,
                maxBlocks = 2,
                solution = program { loop(7) { move() } },
                hints = listOf(
                    "Tambahkan blok Ulangi dulu, lalu tambahkan Maju — Maju akan masuk ke dalamnya.",
                    "Ubah angka di blok Ulangi dengan tombol + dan − sampai 7.",
                ),
                funFact = "Satu blok Ulangi bisa menggantikan puluhan blok yang sama!",
            ),
            Level(
                id = "w2-2",
                title = "Belok Lagi",
                instruction = "Pakai dua perulangan: satu untuk ke kanan, satu untuk ke bawah.",
                map = listOf(
                    ">....",
                    "####.",
                    "####.",
                    "####.",
                    "####G",
                ),
                blocks = LOOPS,
                maxBlocks = 5,
                solution = program { loop(4) { move() }; right(); loop(4) { move() } },
                hints = listOf(
                    "Untuk menaruh blok setelah Ulangi, ketuk area kosong di bawah program.",
                    "Ulangi 4 × Maju, Belok Kanan, Ulangi 4 × Maju.",
                ),
            ),
            Level(
                id = "w2-3",
                title = "Tangga Panjang",
                instruction = "Ingat tangga sebelumnya? Sekarang lebih panjang. Temukan polanya dan ulangi!",
                map = listOf(
                    ">.###",
                    "#..##",
                    "##..#",
                    "###.G",
                ),
                blocks = LOOPS,
                maxBlocks = 6,
                solution = program { loop(4) { move(); right(); move(); left() } },
                hints = listOf(
                    "Satu anak tangga = Maju, Belok Kanan, Maju, Belok Kiri.",
                    "Bito langsung berhenti begitu sampai di bendera, jadi kamu boleh mengulang 4 kali.",
                ),
            ),
            Level(
                id = "w2-4",
                title = "Barisan Permata",
                instruction = "Permata berbaris rapi. Ambil semuanya hanya dengan 3 blok!",
                map = listOf(">******"),
                blocks = LOOPS,
                maxBlocks = 3,
                solution = program { loop(6) { move(); pick() } },
                hints = listOf(
                    "Di dalam Ulangi boleh ada lebih dari satu blok.",
                    "Ulangi 6 × (Maju, Ambil).",
                ),
            ),
            Level(
                id = "w2-5",
                title = "Lompat-Lompat",
                instruction = "Permatanya berselang-seling. Pola apa yang harus diulang?",
                map = listOf(">.*.*.*G"),
                blocks = LOOPS,
                maxBlocks = 5,
                solution = program { loop(3) { move(); move(); pick() }; move() },
                hints = listOf(
                    "Setiap permata berjarak 2 langkah.",
                    "Ulangi 3 × (Maju, Maju, Ambil), lalu Maju sekali lagi ke bendera.",
                ),
            ),
            Level(
                id = "w2-6",
                title = "Keliling Pulau",
                instruction = "Kelilingi pulau, ambil 3 permata, lalu kembali ke rumah (tempat Bito mulai).",
                map = listOf(
                    ">..*",
                    ".##.",
                    ".##.",
                    "*..*",
                ),
                goalAtStart = true,
                blocks = LOOPS,
                maxBlocks = 6,
                solution = program { loop(4) { loop(3) { move() }; pick(); right() } },
                hints = listOf(
                    "Setiap sisi pulau sama: Maju 3 kali, Ambil, Belok Kanan.",
                    "Kamu bisa menaruh Ulangi di dalam Ulangi! Ini disebut perulangan bersarang.",
                ),
                funFact = "Perulangan di dalam perulangan disebut nested loop — sering dipakai untuk menggambar dan membuat game.",
            ),
        ),
    )

    private val conditions = World(
        id = "w3",
        number = 3,
        title = "Gua Persimpangan",
        concept = Concept.CONDITION,
        emoji = "🤔",
        colorArgb = 0xFF8B5CF6,
        intro = "Program yang pintar bisa mengambil keputusan: JIKA sesuatu benar, MAKA lakukan ini. " +
            "Kita menyebutnya kondisi atau percabangan.",
        animal = Animal("🦉", "Ollie si Burung Hantu"),
        story = "Di dalam Gua Persimpangan yang gelap, Ollie si burung hantu menunggu. Bito harus memutuskan sendiri jalannya dengan JIKA… MAKA…",
        nudges = listOf(
            "Apa yang harus Bito lakukan JIKA keadaannya berbeda?",
            "Pikirkan dua kemungkinan: benar atau salah.",
            "Setelah melangkah, apa yang perlu Bito periksa?",
        ),
        levels = listOf(
            Level(
                id = "w3-1",
                title = "Permata Tersembunyi",
                instruction = "Tidak setiap petak punya permata. Pakai blok Jika: jika ada permata, ambil!",
                map = listOf(">*..*.**"),
                blocks = CONDITIONS,
                conditions = listOf(ON_GEM),
                maxBlocks = 4,
                solution = program { loop(7) { move(); ifThen(ON_GEM) { pick() } } },
                hints = listOf(
                    "Di setiap langkah: Maju, lalu periksa apakah ada permata.",
                    "Ulangi 7 × (Maju, Jika ada permata → Ambil).",
                ),
                funFact = "Blok Jika membuat program bisa bekerja walau keadaannya berubah-ubah.",
            ),
            Level(
                id = "w3-2",
                title = "Petak Biru",
                instruction = "Petak biru adalah tanda belok kiri. Ajari Bito: jika di petak biru, belok kiri.",
                map = listOf(
                    "G..b",
                    "###.",
                    ">..b",
                ),
                blocks = CONDITIONS,
                conditions = listOf(ON_BLUE),
                maxBlocks = 5,
                solution = program { loop(8) { move(); ifThen(ON_BLUE) { left() } } },
                hints = listOf(
                    "Bito perlu maju 8 kali. Setelah setiap langkah, periksa warnanya.",
                    "Ulangi 8 × (Maju, Jika di petak biru → Belok Kiri).",
                ),
            ),
            Level(
                id = "w3-3",
                title = "Biru dan Oranye",
                instruction = "Biru = belok kiri, oranye = belok kanan. Satu perulangan dengan dua blok Jika!",
                map = listOf(
                    ">..o###",
                    "###.###",
                    "###b.o#",
                    "#####.#",
                    "#####bG",
                ),
                blocks = CONDITIONS,
                conditions = listOf(ON_BLUE, ON_ORANGE),
                maxBlocks = 7,
                solution = program {
                    loop(10) {
                        move()
                        ifThen(ON_ORANGE) { right() }
                        ifThen(ON_BLUE) { left() }
                    }
                },
                hints = listOf(
                    "Hitung semua langkah Maju dari awal sampai bendera: ada 10.",
                    "Di dalam Ulangi: Maju, Jika oranye → Belok Kanan, Jika biru → Belok Kiri.",
                ),
            ),
            Level(
                id = "w3-4",
                title = "Jalan Buntu",
                instruction = "Pakai Jika–Lainnya: jika jalan di depan terbuka, maju. Kalau tidak, belok kanan.",
                map = listOf(
                    ">...",
                    "###.",
                    "G...",
                ),
                blocks = CONDITIONS_ELSE,
                conditions = listOf(PATH_AHEAD),
                maxBlocks = 5,
                solution = program { loop(10) { ifElse(PATH_AHEAD, { move() }, { right() }) } },
                hints = listOf(
                    "Blok Jika–Lainnya punya dua cabang: satu untuk BENAR, satu untuk SALAH.",
                    "Ulangi 10 × (Jika jalan depan terbuka → Maju, Lainnya → Belok Kanan).",
                ),
                funFact = "Lainnya (else) dijalankan hanya kalau kondisinya salah.",
            ),
            Level(
                id = "w3-5",
                title = "Permata di Tikungan",
                instruction = "Gabungkan semuanya: ambil permata dan belok di petak biru.",
                map = listOf(
                    "G.*.b",
                    "####*",
                    ">*..b",
                ),
                blocks = CONDITIONS,
                conditions = listOf(ON_GEM, ON_BLUE),
                maxBlocks = 7,
                solution = program {
                    loop(10) {
                        move()
                        ifThen(ON_GEM) { pick() }
                        ifThen(ON_BLUE) { left() }
                    }
                },
                hints = listOf(
                    "Setiap langkah, periksa dua hal: ada permata? di petak biru?",
                    "Ulangi 10 × (Maju, Jika ada permata → Ambil, Jika biru → Belok Kiri).",
                ),
            ),
            Level(
                id = "w3-6",
                title = "Robot Pintar",
                instruction = "Jalan berliku ke kiri dan ke kanan. Buat Bito memutuskan sendiri arah beloknya!",
                map = listOf(
                    ">.#..G",
                    "#.#.##",
                    "#...##",
                ),
                blocks = CONDITIONS_ELSE,
                conditions = listOf(PATH_AHEAD, PATH_LEFT),
                maxBlocks = 7,
                solution = program {
                    loop(13) {
                        ifElse(PATH_AHEAD, { move() }, { ifElse(PATH_LEFT, { left() }, { right() }) })
                    }
                },
                hints = listOf(
                    "Jika depan terbuka → Maju. Kalau tidak, periksa lagi: kiri terbuka?",
                    "Taruh Jika–Lainnya kedua di dalam cabang Lainnya yang pertama.",
                    "Angka Ulangi boleh lebih besar — Bito berhenti otomatis di bendera.",
                ),
                funFact = "Kondisi di dalam kondisi disebut percabangan bersarang.",
            ),
        ),
    )

    private val whileLoops = World(
        id = "w4",
        number = 4,
        title = "Gurun Tanpa Ujung",
        concept = Concept.WHILE,
        emoji = "🏁",
        colorArgb = 0xFF14B8A6,
        intro = "Kadang kita tidak tahu harus mengulang berapa kali. Blok \"Ulangi sampai finis\" " +
            "terus mengulang sampai Bito tiba di bendera.",
        animal = Animal("🐢", "Tito si Kura-kura"),
        story = "Tito si kura-kura kepanasan di Gurun Tanpa Ujung. Bito tidak tahu jauhnya, jadi ia harus terus mengulang sampai tiba!",
        nudges = listOf(
            "Kapan Bito harus berhenti mengulang?",
            "Pola apa yang harus diulang sampai finis?",
            "Program yang sama bisa dipakai di jalan yang berbeda-beda.",
        ),
        levels = listOf(
            Level(
                id = "w4-1",
                title = "Sampai Bendera",
                instruction = "Tidak perlu menghitung! Ulangi Maju sampai Bito tiba di bendera.",
                map = listOf(">.......G"),
                blocks = setOf(MOVE, TURN_LEFT, TURN_RIGHT, REPEAT_UNTIL),
                maxBlocks = 2,
                solution = program { untilGoal { move() } },
                hints = listOf("Pakai blok \"Ulangi sampai finis\" lalu masukkan Maju."),
                funFact = "Perulangan ini disebut while loop: ulangi selama syaratnya belum terpenuhi.",
            ),
            Level(
                id = "w4-2",
                title = "Tangga Raksasa",
                instruction = "Tangga terpanjang! Ulangi satu anak tangga sampai finis.",
                map = listOf(
                    ">.####",
                    "#..###",
                    "##..##",
                    "###..#",
                    "####.G",
                ),
                blocks = WHILE,
                maxBlocks = 5,
                solution = program { untilGoal { move(); right(); move(); left() } },
                hints = listOf("Satu anak tangga = Maju, Belok Kanan, Maju, Belok Kiri."),
            ),
            Level(
                id = "w4-3",
                title = "Ikuti Jalan",
                instruction = "Buat program yang bisa mengikuti jalan apa pun, sampai ke bendera.",
                map = listOf(
                    ">....#",
                    "####.#",
                    "#....#",
                    "#.####",
                    "#....G",
                ),
                blocks = WHILE,
                conditions = listOf(PATH_AHEAD, PATH_LEFT),
                maxBlocks = 6,
                solution = program {
                    untilGoal { ifElse(PATH_AHEAD, { move() }, { ifElse(PATH_LEFT, { left() }, { right() }) }) }
                },
                hints = listOf(
                    "Mirip level Robot Pintar, tapi pakai Ulangi sampai finis.",
                    "Jika depan terbuka → Maju. Lainnya: Jika kiri terbuka → Belok Kiri, Lainnya → Belok Kanan.",
                ),
                funFact = "Program yang sama bisa menyelesaikan banyak labirin. Itulah kekuatan algoritma umum!",
            ),
            Level(
                id = "w4-4",
                title = "Permata Tersebar",
                instruction = "Maju sampai finis dan ambil setiap permata yang ditemui.",
                map = listOf(">*..*.*.G"),
                blocks = WHILE,
                conditions = listOf(ON_GEM),
                maxBlocks = 4,
                solution = program { untilGoal { move(); ifThen(ON_GEM) { pick() } } },
                hints = listOf("Ulangi sampai finis: Maju, Jika ada permata → Ambil."),
            ),
            Level(
                id = "w4-5",
                title = "Labirin Warna",
                instruction = "Tantangan besar! Permata, petak biru, dan petak oranye — sampai finis.",
                map = listOf(
                    ">.*.o#",
                    "####*#",
                    "#b*.o#",
                    "#.####",
                    "#b..*G",
                ),
                blocks = WHILE,
                conditions = listOf(ON_GEM, ON_BLUE, ON_ORANGE),
                maxBlocks = 9,
                solution = program {
                    untilGoal {
                        move()
                        ifThen(ON_GEM) { pick() }
                        ifThen(ON_BLUE) { left() }
                        ifThen(ON_ORANGE) { right() }
                    }
                },
                hints = listOf(
                    "Biru = belok kiri, oranye = belok kanan.",
                    "Di dalam perulangan: Maju lalu tiga blok Jika.",
                ),
            ),
        ),
    )

    private val functions = World(
        id = "w5",
        number = 6,
        title = "Kastil Fungsi",
        concept = Concept.FUNCTION,
        emoji = "✨",
        colorArgb = 0xFFEC4899,
        intro = "Fungsi adalah kumpulan blok yang diberi nama. Susun sekali di tab Fungsi A, " +
            "lalu panggil kapan saja dengan satu blok!",
        animal = Animal("🐉", "Drako si Naga Kecil"),
        story = "Drako si naga kecil terkunci di Kastil Fungsi. Mantranya panjang, jadi Bito menyimpannya dalam fungsi agar bisa dipanggil berkali-kali.",
        nudges = listOf(
            "Bagian mana dari jalannya yang sama persis?",
            "Simpan bagian yang sama di Fungsi A.",
            "Panggil fungsi setiap kali bagian itu dibutuhkan.",
        ),
        levels = listOf(
            Level(
                id = "w5-1",
                title = "Fungsi Pertama",
                instruction = "Buka tab Fungsi A dan isi: Maju, Maju, Ambil. Lalu di tab Utama, panggil Fungsi A setiap kali dibutuhkan.",
                map = listOf(
                    ">.*##",
                    "##.##",
                    "##*.*",
                ),
                blocks = FUNCTIONS,
                maxBlocks = 9,
                solution = program(fnA = { moves(2); pick() }) { callA(); right(); callA(); left(); callA() },
                hints = listOf(
                    "Fungsi A: Maju, Maju, Ambil.",
                    "Utama: Fungsi A, Belok Kanan, Fungsi A, Belok Kiri, Fungsi A.",
                ),
                funFact = "Fungsi membuat program lebih pendek dan mudah dibaca.",
            ),
            Level(
                id = "w5-2",
                title = "Lorong Permata",
                instruction = "Setiap lorong berisi 3 permata. Buat fungsi untuk menyapu satu lorong!",
                map = listOf(
                    ">***###",
                    "###*###",
                    "###*###",
                    "###****",
                ),
                blocks = FUNCTIONS,
                maxBlocks = 9,
                solution = program(fnA = { loop(3) { move(); pick() } }) { callA(); right(); callA(); left(); callA() },
                hints = listOf(
                    "Di dalam fungsi juga boleh ada perulangan.",
                    "Fungsi A: Ulangi 3 × (Maju, Ambil).",
                ),
            ),
            Level(
                id = "w5-3",
                title = "Kantong Permata",
                instruction = "Permata tersimpan di kantong atas. Buat fungsi untuk masuk kantong, ambil, lalu kembali.",
                map = listOf(
                    "##**#*#",
                    ">.....G",
                ),
                blocks = FUNCTIONS,
                maxBlocks = 18,
                solution = program(fnA = { left(); move(); pick(); right(); right(); move(); left() }) {
                    moves(2); callA(); move(); callA(); moves(2); callA(); move()
                },
                hints = listOf(
                    "Masuk kantong: Belok Kiri, Maju, Ambil.",
                    "Kembali: Belok Kanan 2 kali (putar balik), Maju, Belok Kiri.",
                ),
            ),
            Level(
                id = "w5-4",
                title = "Kebun Zig-Zag",
                instruction = "Panen permata di setiap baris kebun. Fungsi + belokan zig-zag!",
                map = listOf(
                    ">***",
                    "***.",
                    ".***",
                ),
                blocks = FUNCTIONS,
                maxBlocks = 13,
                solution = program(fnA = { loop(3) { move(); pick() } }) {
                    callA(); right(); move(); right(); callA(); left(); move(); left(); callA()
                },
                hints = listOf(
                    "Fungsi A memanen satu baris: Ulangi 3 × (Maju, Ambil).",
                    "Di ujung kanan belok kanan-maju-belok kanan; di ujung kiri belok kiri-maju-belok kiri.",
                ),
            ),
            Level(
                id = "w5-5",
                title = "Fungsi Pintar",
                instruction = "Gabungkan fungsi dengan kondisi: buat fungsi yang memeriksa permata di sepanjang lorong.",
                map = listOf(
                    ">.*.*####",
                    "####.####",
                    "####*####",
                    "####*####",
                    "####.*.*.",
                ),
                blocks = FUNCTIONS + IF,
                conditions = listOf(ON_GEM),
                maxBlocks = 10,
                solution = program(fnA = { loop(4) { move(); ifThen(ON_GEM) { pick() } } }) {
                    callA(); right(); callA(); left(); callA()
                },
                hints = listOf(
                    "Setiap lorong panjangnya 4 langkah.",
                    "Fungsi A: Ulangi 4 × (Maju, Jika ada permata → Ambil).",
                ),
                funFact = "Programmer sungguhan menggabungkan fungsi, perulangan, dan kondisi setiap hari!",
            ),
        ),
    )

    private val debugging = World(
        id = "w6",
        number = 8,
        title = "Lab Bug",
        concept = Concept.DEBUG,
        emoji = "🐞",
        colorArgb = 0xFFEF4444,
        intro = "Bug adalah kesalahan dalam program. Setiap programmer pasti menemui bug! " +
            "Jalankan program, amati di mana salahnya, lalu perbaiki.",
        animal = Animal("🐼", "Popo si Panda"),
        story = "Popo si panda terkurung di Lab Bug! Program-program di sini rusak. Jadilah detektif: temukan kesalahannya dan perbaiki.",
        nudges = listOf(
            "Jalankan programnya dulu. Di mana Bito mulai salah?",
            "Pakai tombol Langkah untuk melihat satu blok setiap kali.",
            "Biasanya hanya ada satu atau dua blok yang salah.",
        ),
        levels = listOf(
            Level(
                id = "w6-1",
                title = "Langkah Kurang",
                instruction = "Program Bito sudah ada, tapi Bito berhenti sebelum bendera. Temukan bug-nya!",
                map = listOf(">....G"),
                blocks = setOf(MOVE, TURN_LEFT, TURN_RIGHT, REPEAT),
                starter = program { loop(4) { move() } },
                solution = program { loop(5) { move() } },
                hints = listOf(
                    "Jalankan dulu dan perhatikan di mana Bito berhenti.",
                    "Hitung kotaknya lagi. Angka di blok Ulangi kurang satu.",
                ),
                funFact = "Kesalahan \"kurang satu\" (off-by-one) adalah bug paling terkenal di dunia pemrograman!",
            ),
            Level(
                id = "w6-2",
                title = "Salah Belok",
                instruction = "Bito menabrak! Ada satu blok yang salah. Bisakah kamu menemukannya?",
                map = listOf(
                    ">...",
                    "###.",
                    "###G",
                ),
                blocks = BASIC + REPEAT,
                starter = program { moves(3); left(); moves(2) },
                solution = program { moves(3); right(); moves(2) },
                hints = listOf(
                    "Gunakan tombol Langkah untuk menjalankan program satu blok setiap kali.",
                    "Bito seharusnya belok ke kanan, bukan ke kiri.",
                ),
            ),
            Level(
                id = "w6-3",
                title = "Lupa Ambil",
                instruction = "Bito sampai di bendera, tapi permatanya tertinggal. Perbaiki programnya!",
                map = listOf(">.*.*.G"),
                blocks = LOOPS,
                starter = program { loop(3) { move(); move() } },
                solution = program { loop(3) { move(); move(); pick() } },
                hints = listOf(
                    "Program ini lupa satu perintah.",
                    "Tambahkan Ambil di dalam perulangan, setelah dua kali Maju.",
                ),
            ),
            Level(
                id = "w6-4",
                title = "Kondisi Keliru",
                instruction = "Bito mencoba mengambil permata yang tidak ada. Kondisinya salah!",
                map = listOf(">.*.**.*"),
                blocks = CONDITIONS,
                conditions = listOf(ON_GEM, PATH_AHEAD),
                starter = program { loop(7) { move(); ifThen(PATH_AHEAD) { pick() } } },
                solution = program { loop(7) { move(); ifThen(ON_GEM) { pick() } } },
                hints = listOf(
                    "Ketuk kondisi di blok Jika untuk menggantinya.",
                    "Bito seharusnya mengambil hanya jika ada permata.",
                ),
            ),
            Level(
                id = "w6-5",
                title = "Berputar Terus",
                instruction = "Program ini tidak pernah selesai! Bito berputar-putar tanpa henti. Perbaiki!",
                map = listOf(
                    ">...",
                    "###.",
                    "G...",
                ),
                blocks = WHILE,
                conditions = listOf(PATH_AHEAD),
                starter = program { untilGoal { ifElse(PATH_AHEAD, { move() }, { left() }) } },
                solution = program { untilGoal { ifElse(PATH_AHEAD, { move() }, { right() }) } },
                hints = listOf(
                    "Perhatikan ke mana Bito belok saat jalan buntu.",
                    "Ganti Belok Kiri di cabang Lainnya menjadi Belok Kanan.",
                ),
                funFact = "Program yang tidak pernah berhenti disebut infinite loop (perulangan tak terbatas).",
            ),
            Level(
                id = "w6-6",
                title = "Fungsi Rusak",
                instruction = "Bug-nya bersembunyi di dalam fungsi. Buka tab Fungsi A dan selidiki!",
                map = listOf(
                    ">.*##",
                    "##.##",
                    "##*.*",
                ),
                blocks = FUNCTIONS,
                starter = program(fnA = { move(); pick(); move() }) { callA(); right(); callA(); left(); callA() },
                solution = program(fnA = { moves(2); pick() }) { callA(); right(); callA(); left(); callA() },
                hints = listOf(
                    "Program utamanya sudah benar. Lihat isi Fungsi A.",
                    "Urutan di Fungsi A tertukar: seharusnya Maju, Maju, Ambil.",
                ),
            ),
        ),
    )

    private val art = World(
        id = "w7",
        number = 9,
        title = "Galeri Piksel",
        concept = Concept.ART,
        emoji = "🎨",
        colorArgb = 0xFFEAB308,
        intro = "Kode juga bisa dipakai untuk menggambar! Bito membawa kuas: blok Cat mewarnai " +
            "petak tempat Bito berdiri. Tiru pola yang samar di papan.",
        animal = Animal("🦜", "Lala si Burung Beo"),
        story = "Lala si burung beo ingin dinding galerinya penuh warna. Bantu Bito melukis pola dengan kode!",
        nudges = listOf(
            "Lihat pola samar di papan. Dari mana sebaiknya mulai?",
            "Cat dulu, baru maju.",
            "Bagian mana dari pola yang berulang?",
        ),
        levels = listOf(
            Level(
                id = "w7-1",
                title = "Garis Merah",
                instruction = "Warnai satu baris dengan cat merah. Cat dulu, baru maju!",
                map = listOf(
                    ".....",
                    ">....",
                    ".....",
                ),
                target = listOf(
                    ".....",
                    "rrrrr",
                    ".....",
                ),
                blocks = ART,
                colors = listOf(RED),
                solution = program { loop(5) { paint(RED); move() } },
                hints = listOf("Ulangi 5 × (Cat Merah, Maju). Bito berhenti saat pola sudah sama."),
            ),
            Level(
                id = "w7-2",
                title = "Kotak Biru",
                instruction = "Gambar bingkai kotak biru. Setiap sisi punya pola yang sama!",
                map = listOf(
                    ".....",
                    ".>...",
                    ".....",
                    ".....",
                    ".....",
                ),
                target = listOf(
                    ".....",
                    ".bbb.",
                    ".b.b.",
                    ".bbb.",
                    ".....",
                ),
                blocks = ART,
                colors = listOf(BLUE),
                solution = program { loop(4) { loop(2) { paint(BLUE); move() }; right() } },
                hints = listOf(
                    "Satu sisi: Cat, Maju, Cat, Maju, lalu Belok Kanan.",
                    "Ulangi sisi itu 4 kali. Kamu bisa memakai Ulangi di dalam Ulangi.",
                ),
            ),
            Level(
                id = "w7-3",
                title = "Merah Putih",
                instruction = "Lukis bendera Merah Putih! Baris atas merah, baris bawah putih.",
                map = listOf(
                    "......",
                    ">.....",
                    "......",
                    "......",
                ),
                target = listOf(
                    "......",
                    ".rrrr.",
                    ".wwww.",
                    "......",
                ),
                blocks = ART,
                colors = listOf(RED, WHITE),
                solution = program {
                    move()
                    loop(4) { paint(RED); move() }
                    right(); move(); right(); move()
                    loop(4) { paint(WHITE); move() }
                },
                hints = listOf(
                    "Lukis baris merah dari kiri ke kanan.",
                    "Di ujung kanan, putar ke bawah (Belok Kanan, Maju, Belok Kanan, Maju), lalu lukis putih ke kiri.",
                ),
                funFact = "Gambar di layar komputer tersusun dari titik-titik kecil bernama piksel.",
            ),
            Level(
                id = "w7-4",
                title = "Huruf L",
                instruction = "Lukis huruf L berwarna ungu.",
                map = listOf(
                    ".v...",
                    ".....",
                    ".....",
                    ".....",
                    ".....",
                ),
                target = listOf(
                    ".p...",
                    ".p...",
                    ".p...",
                    ".ppp.",
                    ".....",
                ),
                blocks = ART,
                colors = listOf(PURPLE),
                solution = program {
                    loop(3) { paint(PURPLE); move() }
                    left()
                    loop(3) { paint(PURPLE); move() }
                },
                hints = listOf(
                    "Bito menghadap ke bawah. Lukis 3 petak ke bawah dulu.",
                    "Lalu Belok Kiri (dari arah bawah, kiri Bito adalah kanan layar).",
                ),
            ),
            Level(
                id = "w7-5",
                title = "Tanda Tambah",
                instruction = "Lukis tanda tambah hijau. Rencanakan rutenya dulu!",
                map = listOf(
                    "..v..",
                    ".....",
                    ".....",
                    ".....",
                    ".....",
                ),
                target = listOf(
                    "..g..",
                    "..g..",
                    "ggggg",
                    "..g..",
                    "..g..",
                ),
                blocks = ART,
                colors = listOf(GREEN),
                solution = program {
                    loop(4) { paint(GREEN); move() }
                    paint(GREEN)
                    right(); moves(2); right(); moves(2); right()
                    loop(5) { paint(GREEN); move() }
                },
                hints = listOf(
                    "Lukis garis tegak dulu dari atas ke bawah.",
                    "Lalu jalan ke ujung kiri baris tengah dan lukis garis mendatar.",
                ),
            ),
            Level(
                id = "w7-6",
                title = "Tiga Kotak",
                instruction = "Gambar tiga kotak kuning. Buat Fungsi A yang menggambar satu kotak, lalu panggil 3 kali.",
                map = listOf(
                    ">......",
                    ".......",
                    ".......",
                    ".......",
                    ".......",
                    ".......",
                ),
                target = listOf(
                    "yy.yy..",
                    "yy.yy..",
                    ".......",
                    "...yy..",
                    "...yy..",
                    ".......",
                ),
                blocks = ART + CALL_A,
                colors = listOf(YELLOW),
                maxBlocks = 15,
                solution = program(fnA = { loop(4) { paint(YELLOW); move(); right() } }) {
                    callA(); loop(3) { move() }; callA()
                    right(); loop(3) { move() }; left(); callA()
                },
                hints = listOf(
                    "Fungsi A: Ulangi 4 × (Cat Kuning, Maju, Belok Kanan) — Bito kembali ke tempat semula.",
                    "Di antara kotak, gerakkan Bito 3 langkah.",
                ),
                funFact = "Seniman digital memakai kode untuk membuat pola rumit yang disebut generative art.",
            ),
        ),
    )


    private val variables = World(
        id = "w8",
        number = 5,
        title = "Pulau Kotak Ajaib",
        concept = Concept.VARIABLE,
        emoji = "📦",
        colorArgb = 0xFF0EA5E9,
        intro = "Variabel adalah kotak bernama yang menyimpan nilai. Kotak \"angka\" milik Bito " +
            "bisa diisi, ditambah, atau dikurangi — dan nilainya dipakai oleh blok lain.",
        animal = Animal("🐧", "Pipi si Penguin"),
        story = "Pipi si penguin terdampar di Pulau Kotak Ajaib. Di sini Bito membawa kotak bernama angka yang bisa menyimpan dan mengubah bilangan!",
        nudges = listOf(
            "Berapa nilai kotak angka sekarang?",
            "Apakah langkahnya bertambah atau berkurang setiap kali?",
            "Ubah angka sedikit demi sedikit di dalam perulangan.",
        ),
        levels = listOf(
            Level(
                id = "w8-1",
                title = "Kotak Angka",
                instruction = "Isi kotak angka dengan 5, lalu suruh Bito maju sebanyak angka. Cukup 2 blok!",
                map = listOf(">....G"),
                blocks = setOf(MOVE, TURN_LEFT, TURN_RIGHT, SET_VAR, MOVE_VAR),
                maxBlocks = 2,
                solution = program { setVar(5); moveVar() },
                hints = listOf(
                    "Tambahkan blok \"angka =\", lalu ubah nilainya dengan tombol + sampai 5.",
                    "Setelah itu tambahkan \"Maju sebanyak angka\".",
                ),
                funFact = "Variabel seperti kotak berlabel: isinya bisa dilihat dan diganti kapan saja.",
            ),
            Level(
                id = "w8-2",
                title = "Spiral Keluar",
                instruction = "Jalannya makin panjang: 1, 2, 3, 4, 5 langkah. Pakai angka yang bertambah di setiap putaran!",
                map = listOf(
                    ".....G",
                    ".#####",
                    ".#>.##",
                    ".##.##",
                    "....##",
                ),
                blocks = VARIABLES,
                maxBlocks = 6,
                solution = program { setVar(1); loop(5) { moveVar(); right(); changeVar(1) } },
                hints = listOf(
                    "Mulai dengan angka = 1.",
                    "Di dalam Ulangi: Maju sebanyak angka, Belok Kanan, lalu angka + 1.",
                ),
                funFact = "Dengan variabel, satu perulangan bisa menghasilkan langkah yang berbeda-beda.",
            ),
            Level(
                id = "w8-3",
                title = "Spiral Masuk",
                instruction = "Sekarang kebalikannya: 4, 3, 2, 1 langkah menuju pusat. Kurangi angka di setiap putaran.",
                map = listOf(
                    ">....",
                    "####.",
                    "##G#.",
                    "##...",
                ),
                blocks = VARIABLES,
                maxBlocks = 6,
                solution = program { setVar(4); loop(4) { moveVar(); right(); changeVar(-1) } },
                hints = listOf(
                    "Mulai dengan angka = 4.",
                    "Pakai \"angka − 1\" (tekan tombol − sampai negatif) di akhir putaran.",
                ),
            ),
            Level(
                id = "w8-4",
                title = "Permata Berjarak",
                instruction = "Jarak antar permata makin jauh: 1, 2, lalu 3 langkah. Ambil semuanya!",
                map = listOf(">*.*..*"),
                blocks = VARIABLES,
                maxBlocks = 6,
                solution = program { setVar(1); loop(3) { moveVar(); pick(); changeVar(1) } },
                hints = listOf(
                    "Polanya: maju sebanyak angka, ambil, lalu tambah angka.",
                    "angka = 1, lalu Ulangi 3 × (Maju sebanyak angka, Ambil, angka + 1).",
                ),
            ),
            Level(
                id = "w8-5",
                title = "Kotak Kosong",
                instruction = "Program spiral ini punya bug! Bito malah menabrak. Periksa nilai awal kotak angka.",
                map = listOf(
                    ".....G",
                    ".#####",
                    ".#>.##",
                    ".##.##",
                    "....##",
                ),
                blocks = VARIABLES,
                starter = program { setVar(0); loop(5) { moveVar(); right(); changeVar(1) } },
                solution = program { setVar(1); loop(5) { moveVar(); right(); changeVar(1) } },
                hints = listOf(
                    "Jalankan dengan tombol Langkah dan perhatikan nilai angka di atas papan.",
                    "Maju sebanyak 0 berarti tidak bergerak. Nilai awalnya harus 1.",
                ),
                funFact = "Lupa memberi nilai awal yang benar adalah bug yang sering dialami programmer.",
            ),
        ),
    )

    private val events = World(
        id = "w9",
        number = 7,
        title = "Kota Sibuk",
        concept = Concept.EVENT,
        emoji = "⚡",
        colorArgb = 0xFF6366F1,
        intro = "Event adalah kejadian. Blok di bawah \"Ketika…\" dijalankan otomatis setiap kali " +
            "kejadian itu terjadi — seperti lampu yang menyala ketika saklar ditekan.",
        animal = Animal("🐶", "Doni si Anak Anjing"),
        story = "Doni si anak anjing bermain di Kota Sibuk. Di kota ini, Bito belajar bereaksi otomatis KETIKA sesuatu terjadi.",
        nudges = listOf(
            "Apa yang harus terjadi setiap kali Bito menginjak sesuatu?",
            "Program utama cukup membuat Bito berjalan; biarkan event yang bereaksi.",
            "Buka tab \"Ketika…\" dan isi dengan apa yang harus dilakukan Bito.",
        ),
        levels = listOf(
            Level(
                id = "w9-1",
                title = "Ketika Ada Permata",
                instruction = "Buka tab ⚡ Ketika menginjak permata dan isi dengan Ambil. Program utama cukup membuat Bito berjalan.",
                map = listOf(">*..*.**"),
                blocks = setOf(MOVE, TURN_LEFT, TURN_RIGHT, PICK, REPEAT),
                events = listOf(EventKind.ON_GEM),
                maxBlocks = 4,
                solution = program(events = mapOf(EventKind.ON_GEM to { pick() })) { loop(7) { move() } },
                hints = listOf(
                    "Utama: Ulangi 7 × Maju.",
                    "Tab Ketika menginjak permata: Ambil.",
                ),
                funFact = "Event membuat program bisa bereaksi tanpa terus-menerus memeriksa dengan blok Jika.",
            ),
            Level(
                id = "w9-2",
                title = "Petak Ajaib",
                instruction = "Setiap kali Bito menginjak petak biru, ia harus belok kiri. Pakai event, bukan blok Jika!",
                map = listOf(
                    "G..b",
                    "###.",
                    ">..b",
                ),
                blocks = setOf(MOVE, TURN_LEFT, TURN_RIGHT, REPEAT),
                events = listOf(EventKind.ON_BLUE),
                maxBlocks = 4,
                solution = program(events = mapOf(EventKind.ON_BLUE to { left() })) { loop(8) { move() } },
                hints = listOf(
                    "Utama: Ulangi 8 × Maju.",
                    "Tab Ketika menginjak petak biru: Belok Kiri.",
                ),
            ),
            Level(
                id = "w9-3",
                title = "Dua Pemicu",
                instruction = "Biru = belok kiri, oranye = belok kanan. Buat dua event!",
                map = listOf(
                    ">..o###",
                    "###.###",
                    "###b.o#",
                    "#####.#",
                    "#####bG",
                ),
                blocks = setOf(MOVE, TURN_LEFT, TURN_RIGHT, REPEAT),
                events = listOf(EventKind.ON_BLUE, EventKind.ON_ORANGE),
                maxBlocks = 7,
                solution = program(
                    events = mapOf(EventKind.ON_ORANGE to { right() }, EventKind.ON_BLUE to { left() }),
                ) { loop(10) { move() } },
                hints = listOf(
                    "Bito perlu maju 10 kali.",
                    "Isi kedua tab Ketika: oranye → Belok Kanan, biru → Belok Kiri.",
                ),
            ),
            Level(
                id = "w9-4",
                title = "Robot Refleks",
                instruction = "Ketika jalan di depan buntu, Bito harus otomatis belok kanan. Lalu terus maju sampai finis!",
                map = listOf(
                    ">...",
                    "###.",
                    "G...",
                ),
                blocks = setOf(MOVE, TURN_LEFT, TURN_RIGHT, REPEAT_UNTIL),
                events = listOf(EventKind.WALL_AHEAD),
                maxBlocks = 5,
                solution = program(events = mapOf(EventKind.WALL_AHEAD to { right() })) { untilGoal { move() } },
                hints = listOf(
                    "Utama: Ulangi sampai finis → Maju.",
                    "Tab Ketika jalan di depan buntu: Belok Kanan.",
                ),
            ),
            Level(
                id = "w9-5",
                title = "Kota Warna-warni",
                instruction = "Tantangan event! Ambil permata, belok di petak biru dan oranye — semuanya dengan event.",
                map = listOf(
                    ">.*.o#",
                    "####*#",
                    "#b*.o#",
                    "#.####",
                    "#b..*G",
                ),
                blocks = setOf(MOVE, TURN_LEFT, TURN_RIGHT, PICK, REPEAT_UNTIL),
                events = listOf(EventKind.ON_GEM, EventKind.ON_BLUE, EventKind.ON_ORANGE),
                maxBlocks = 9,
                solution = program(
                    events = mapOf(
                        EventKind.ON_GEM to { pick() },
                        EventKind.ON_BLUE to { left() },
                        EventKind.ON_ORANGE to { right() },
                    ),
                ) { untilGoal { move() } },
                hints = listOf(
                    "Utama cukup: Ulangi sampai finis → Maju.",
                    "Tiga event: permata → Ambil, biru → Belok Kiri, oranye → Belok Kanan.",
                ),
                funFact = "Game dan aplikasi di HP-mu penuh event: ketika tombol disentuh, ketika pesan masuk, dan banyak lagi.",
            ),
        ),
    )

    /** Urutan kurikulum: urutan → perulangan → percabangan → ulangi-sampai → variabel → fungsi → event → debugging → seni. */
    val all: List<World> = listOf(sequence, loops, conditions, whileLoops, variables, functions, events, debugging, art)

    /** Dunia yang harus diselesaikan dulu sebelum dunia ini terbuka. */
    fun prerequisite(world: World): World? = when (world.id) {
        "w1" -> null
        "w7" -> loops
        else -> all.getOrNull(all.indexOf(world) - 1)
    }

    val allLevels: List<Level> = all.flatMap { it.levels }

    fun level(id: String): Level? = allLevels.firstOrNull { it.id == id }

    fun world(id: String): World? = all.firstOrNull { it.id == id }

    fun worldOf(levelId: String): World? = all.firstOrNull { w -> w.levels.any { it.id == levelId } }

    fun nextLevel(levelId: String): Level? {
        val world = worldOf(levelId) ?: return null
        val i = world.levels.indexOfFirst { it.id == levelId }
        return world.levels.getOrNull(i + 1)
    }

}
