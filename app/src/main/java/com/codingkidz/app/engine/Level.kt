package com.codingkidz.app.engine

/** Konsep pemrograman yang dilatih oleh sebuah dunia. */
enum class Concept(val label: String) {
    SEQUENCE("Urutan"),
    LOOP("Perulangan"),
    CONDITION("Percabangan"),
    WHILE("Ulangi Sampai"),
    VARIABLE("Variabel"),
    FUNCTION("Fungsi"),
    EVENT("Event"),
    DEBUG("Debugging"),
    ART("Pola & Seni"),
}

data class Level(
    val id: String,
    val title: String,
    val instruction: String,
    val map: List<String>,
    val blocks: Set<BlockType>,
    val solution: Program,
    /** Petunjuk spesifik (tingkat 2). Dorongan umum (tingkat 1) diambil dari dunianya. */
    val hints: List<String>,
    val conditions: List<Condition> = emptyList(),
    val colors: List<PaintColor> = emptyList(),
    val events: List<EventKind> = emptyList(),
    val goalAtStart: Boolean = false,
    val target: List<String>? = null,
    val maxBlocks: Int? = null,
    val starter: Program? = null,
    /** Penjelasan singkat konsep yang muncul setelah level selesai. */
    val funFact: String? = null,
) {
    val board: Board by lazy { Board.parse(map, goalAtStart, target) }

    /** Jumlah blok dari kunci jawaban; memakai blok sebanyak ini atau kurang = bintang efisiensi. */
    val par: Int get() = solution.blockCount()

    /** Batas blok untuk bintang kedua ("kode rapi"). */
    val tidyLimit: Int get() = par + maxOf(2, par / 3)

    val functionCount: Int
        get() = when {
            BlockType.CALL_B in blocks -> 2
            BlockType.CALL_A in blocks -> 1
            else -> 0
        }

    fun starsFor(blocksUsed: Int): Int = when {
        blocksUsed <= par -> 3
        blocksUsed <= tidyLimit -> 2
        else -> 1
    }
}

/** Hewan yang diselamatkan Bito di setiap dunia — benang merah cerita petualangan. */
data class Animal(val emoji: String, val name: String)

data class World(
    val id: String,
    val number: Int,
    val title: String,
    val concept: Concept,
    val emoji: String,
    val colorArgb: Long,
    val intro: String,
    val animal: Animal,
    /** Cerita pembuka saat dunia pertama kali dibuka. */
    val story: String,
    /** Dorongan ringan (petunjuk tingkat 1) yang berlaku di seluruh dunia. */
    val nudges: List<String>,
    val levels: List<Level>,
)
