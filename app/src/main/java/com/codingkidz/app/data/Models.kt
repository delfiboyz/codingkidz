package com.codingkidz.app.data

import com.codingkidz.app.engine.Program
import kotlinx.serialization.Serializable

@Serializable
enum class AgeGroup(val label: String, val range: String) {
    YOUNG("Kecil", "4–6 tahun"),
    MIDDLE("Tengah", "7–9 tahun"),
    OLDER("Besar", "10+ tahun");

    /** Tampilan blok yang disarankan: transisi bertahap dari gambar → kata → kode. */
    val suggestedBlockMode: BlockMode
        get() = when (this) {
            YOUNG -> BlockMode.PICTURE
            MIDDLE -> BlockMode.WORDS
            OLDER -> BlockMode.CODE
        }
}

/** Cara blok ditampilkan, mengikuti tahap perkembangan anak. */
@Serializable
enum class BlockMode(val label: String, val description: String) {
    PICTURE("Gambar", "Ikon besar tanpa tulisan, cocok untuk 4–6 tahun"),
    WORDS("Kata", "Blok berlabel kata, cocok untuk 7–9 tahun"),
    CODE("Kode", "Blok dan kode teks berdampingan, cocok untuk 10+ tahun"),
}

@Serializable
data class Profile(
    val name: String,
    val avatar: Int,
    val ageGroup: AgeGroup,
)

@Serializable
data class LevelRecord(
    val stars: Int,
    val bestBlocks: Int,
    val usedHint: Boolean,
    val attempts: Int,
    val completedAt: Long,
    val usedSolution: Boolean = false,
)

@Serializable
data class Creation(
    val id: Long,
    val title: String,
    val program: Program,
    val createdAt: Long,
    /** Peta buatan anak (format sama dengan level); null = kanvas kosong. */
    val map: List<String>? = null,
)

@Serializable
data class UserData(
    val profile: Profile? = null,
    val xp: Int = 0,
    val levels: Map<String, LevelRecord> = emptyMap(),
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val lastActiveDay: Long = -1,
    val dailyDone: List<Long> = emptyList(),
    val quizSessions: Int = 0,
    val quizAnswered: Int = 0,
    val quizCorrect: Int = 0,
    val quizPerfect: Int = 0,
    val achievements: Map<String, Long> = emptyMap(),
    val creations: List<Creation> = emptyList(),
    val minutesByDay: Map<Long, Int> = emptyMap(),
    val totalMinutes: Int = 0,
    val programRuns: Int = 0,
    /** Jumlah percobaan menjalankan program per level (termasuk yang gagal). */
    val attempts: Map<String, Int> = emptyMap(),
    val hintsUsed: Int = 0,
    val solutionsViewed: Int = 0,
    val unlockAll: Boolean = false,
    /** Kostum Bito yang sedang dipakai. */
    val costume: String? = null,
    /** Dunia yang cerita pembukanya sudah ditampilkan. */
    val seenStories: List<String> = emptyList(),
    /** Untuk membatasi XP kuis per hari agar anak tidak sekadar mengejar poin. */
    val quizDay: Long = -1,
    val quizToday: Int = 0,
) {
    val totalStars: Int get() = levels.values.sumOf { it.stars }

    fun currentStreak(today: Long): Int = if (lastActiveDay >= today - 1) streak else 0

    fun minutesOn(day: Long): Int = minutesByDay[day] ?: 0

    val playerLevel: Int get() = PlayerLevels.levelFor(xp)
}

@Serializable
enum class ThemeMode(val label: String) { SYSTEM("Ikuti sistem"), LIGHT("Terang"), DARK("Gelap") }

@Serializable
enum class RunSpeed(val label: String, val stepMillis: Long) {
    SLOW("Pelan", 750),
    NORMAL("Sedang", 450),
    FAST("Cepat", 200),
}

@Serializable
data class AppSettings(
    val sound: Boolean = true,
    val haptics: Boolean = true,
    val readAloud: Boolean = false,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val speed: RunSpeed = RunSpeed.NORMAL,
    val blockMode: BlockMode = BlockMode.WORDS,
    val codeLanguage: com.codingkidz.app.engine.CodeLanguage = com.codingkidz.app.engine.CodeLanguage.PYTHON,
    /** 0 = tanpa batas. */
    val dailyLimitMinutes: Int = 0,
    val reminder: Boolean = false,
    val reminderHour: Int = 17,
)

object PlayerLevels {
    private val titles = listOf(
        "Pemula Kode",
        "Penjelajah",
        "Pemrogram Cilik",
        "Pemecah Teka-teki",
        "Insinyur Muda",
        "Arsitek Algoritma",
        "Master Kode",
        "Legenda Coding",
    )

    /** XP yang dibutuhkan untuk mencapai level [level] (level 1 = 0 XP). */
    fun xpForLevel(level: Int): Int = (level - 1) * (level - 1) * 40 + (level - 1) * 60

    fun levelFor(xp: Int): Int {
        var level = 1
        while (xpForLevel(level + 1) <= xp) level++
        return level
    }

    fun title(level: Int): String = titles[((level - 1) / 2).coerceIn(0, titles.lastIndex)]

    /** Kemajuan 0..1 menuju level berikutnya. */
    fun progress(xp: Int): Float {
        val level = levelFor(xp)
        val from = xpForLevel(level)
        val to = xpForLevel(level + 1)
        return (xp - from).toFloat() / (to - from)
    }
}
