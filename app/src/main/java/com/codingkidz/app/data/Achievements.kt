package com.codingkidz.app.data

import com.codingkidz.app.content.Worlds

data class Achievement(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val isUnlocked: (UserData) -> Boolean,
)

object Achievements {
    private fun worldDone(data: UserData, worldId: String): Boolean =
        Worlds.world(worldId)?.levels?.all { it.id in data.levels } == true

    val all = listOf(
        Achievement("first_step", "👣", "Langkah Pertama", "Selesaikan level pertamamu.") { it.levels.isNotEmpty() },
        Achievement("stars_10", "⭐", "Bintang Terang", "Kumpulkan 10 bintang.") { it.totalStars >= 10 },
        Achievement("stars_50", "🌟", "Kolektor Bintang", "Kumpulkan 50 bintang.") { it.totalStars >= 50 },
        Achievement("stars_all", "🏆", "Bintang Sempurna", "Raih 3 bintang di semua level.") { data ->
            Worlds.allLevels.all { (data.levels[it.id]?.stars ?: 0) == 3 }
        },
        Achievement("perfect_5", "💎", "Kode Rapi", "Raih 3 bintang di 5 level.") { d -> d.levels.values.count { it.stars == 3 } >= 5 },
        Achievement("loop_master", "🔁", "Ahli Perulangan", "Tamatkan Sungai Berulang.") { worldDone(it, "w2") },
        Achievement("logic_master", "🤔", "Pemikir Logis", "Tamatkan Gua Persimpangan.") { worldDone(it, "w3") },
        Achievement("while_master", "🏁", "Pantang Menyerah", "Tamatkan Gurun Tanpa Ujung.") { worldDone(it, "w4") },
        Achievement("var_master", "📦", "Penjaga Kotak", "Tamatkan Pulau Kotak Ajaib (variabel).") { worldDone(it, "w8") },
        Achievement("function_master", "✨", "Penyihir Fungsi", "Tamatkan Kastil Fungsi.") { worldDone(it, "w5") },
        Achievement("event_master", "⚡", "Raja Event", "Tamatkan Kota Sibuk (event).") { worldDone(it, "w9") },
        Achievement("bug_hunter", "🐞", "Pemburu Bug", "Tamatkan Lab Bug.") { worldDone(it, "w6") },
        Achievement("pixel_artist", "🎨", "Seniman Piksel", "Tamatkan Galeri Piksel.") { worldDone(it, "w7") },
        Achievement("rescuer", "🐾", "Sahabat Hewan", "Selamatkan semua hewan di setiap dunia.") { data ->
            Worlds.all.all { w -> w.levels.all { it.id in data.levels } }
        },
        Achievement("optimizer", "✂️", "Lebih Ringkas!", "Selesaikan level dengan blok lebih sedikit dari solusi kami.") { data ->
            data.levels.any { (id, r) -> Worlds.level(id)?.let { r.bestBlocks < it.par } == true }
        },
        Achievement("creator", "🖌️", "Kreator", "Simpan karya pertamamu di Studio.") { it.creations.isNotEmpty() },
        Achievement("gallery_5", "🖼️", "Galeri Mini", "Simpan 5 karya di Studio.") { it.creations.size >= 5 },
        Achievement("daily_1", "📅", "Tantangan Diterima", "Selesaikan tantangan harian.") { it.dailyDone.isNotEmpty() },
        Achievement("daily_7", "🗓️", "Penjelajah Mingguan", "Selesaikan 7 tantangan harian.") { it.dailyDone.size >= 7 },
        Achievement("streak_3", "🔥", "Rajin", "Belajar 3 hari berturut-turut.") { it.bestStreak >= 3 },
        Achievement("streak_7", "☄️", "Super Rajin", "Belajar 7 hari berturut-turut.") { it.bestStreak >= 7 },
        Achievement("quiz_perfect", "🧠", "Jenius Kuis", "Jawab benar semua soal dalam satu kuis.") { it.quizPerfect >= 1 },
        Achievement("quiz_50", "📚", "Kutu Buku", "Jawab benar 50 soal kuis.") { it.quizCorrect >= 50 },
        Achievement("no_hint_10", "🦉", "Mandiri", "Selesaikan 10 level tanpa petunjuk.") { d -> d.levels.values.count { !it.usedHint } >= 10 },
        Achievement("runs_100", "🚀", "Uji Coba", "Jalankan program 100 kali.") { it.programRuns >= 100 },
        Achievement("level_5", "🎖️", "Naik Pangkat", "Capai level pemain 5.") { it.playerLevel >= 5 },
        Achievement("game_maker", "🕹️", "Pembuat Game", "Simpan karya Studio yang memakai event tombol.") { data ->
            data.creations.any { c -> c.program.events.any { !it.kind.isTrigger && it.body.isNotEmpty() } }
        },
    )

    fun byId(id: String): Achievement? = all.firstOrNull { it.id == id }

    /** Lencana yang terpenuhi oleh [data] tetapi belum tercatat. */
    fun newlyUnlocked(data: UserData): List<Achievement> =
        all.filter { it.id !in data.achievements && it.isUnlocked(data) }
}
