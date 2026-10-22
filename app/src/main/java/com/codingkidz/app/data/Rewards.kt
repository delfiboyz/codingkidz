package com.codingkidz.app.data

import com.codingkidz.app.content.Worlds
import com.codingkidz.app.engine.World

/**
 * Kostum Bito. Sengaja dibuka lewat pencapaian belajar (menamatkan dunia / menguasai konsep),
 * bukan dibeli dengan poin, supaya anak mengejar pemahaman — bukan angka.
 */
data class Costume(
    val id: String,
    val emoji: String,
    val name: String,
    val requirement: String,
    val isUnlocked: (UserData) -> Boolean,
)

object Rewards {
    fun worldDone(world: World, data: UserData): Boolean = world.levels.all { it.id in data.levels }

    private fun worldDone(id: String, data: UserData): Boolean = Worlds.world(id)?.let { worldDone(it, data) } == true

    private fun worldName(id: String) = Worlds.world(id)?.title ?: id

    private fun forWorld(id: String, costumeId: String, emoji: String, name: String) =
        Costume(costumeId, emoji, name, "Tamatkan ${worldName(id)}") { worldDone(id, it) }

    val costumes: List<Costume> by lazy {
        listOf(
            forWorld("w1", "party_hat", "🎉", "Topi Pesta"),
            forWorld("w2", "sunglasses", "🕶️", "Kacamata Keren"),
            forWorld("w3", "bow_tie", "🎀", "Dasi Kupu-kupu"),
            forWorld("w4", "flower", "🌼", "Bunga Gurun"),
            forWorld("w8", "headphones", "🎧", "Headphone"),
            forWorld("w5", "wizard_hat", "🧙", "Topi Penyihir"),
            forWorld("w9", "cat_ears", "🐱", "Bando Kucing"),
            forWorld("w6", "pirate_hat", "🏴‍☠️", "Topi Bajak Laut"),
            forWorld("w7", "beret", "🎨", "Baret Seniman"),
            Costume("crown", "👑", "Mahkota", "Raih 3 bintang di semua level satu dunia") { data ->
                Worlds.all.any { w -> w.levels.all { (data.levels[it.id]?.stars ?: 0) == 3 } }
            },
        )
    }

    fun costume(id: String?): Costume? = costumes.firstOrNull { it.id == id }

    /** Hewan yang sudah diselamatkan (dunianya tamat), sesuai urutan dunia. */
    fun rescued(data: UserData): List<World> = Worlds.all.filter { worldDone(it, data) }
}
