package com.codingkidz.app.data

import androidx.datastore.core.DataStore
import com.codingkidz.app.content.Worlds
import com.codingkidz.app.engine.Level
import com.codingkidz.app.engine.Program
import com.codingkidz.app.engine.World
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

data class Reward(
    val xpGained: Int,
    val stars: Int = 0,
    val previousStars: Int = 0,
    val firstClear: Boolean = false,
    val newAchievements: List<Achievement> = emptyList(),
    val leveledUpTo: Int? = null,
)

fun today(): Long = LocalDate.now().toEpochDay()

class UserRepository(private val store: DataStore<UserData>) {
    val data: Flow<UserData> = store.data

    private val _achievementEvents = MutableSharedFlow<Achievement>(extraBufferCapacity = 16)

    /** Dipancarkan setiap kali lencana baru terbuka, untuk banner perayaan global. */
    val achievementEvents: SharedFlow<Achievement> = _achievementEvents.asSharedFlow()

    suspend fun current(): UserData = store.data.first()

    private suspend fun mutate(transform: (UserData) -> UserData): Pair<UserData, List<Achievement>> {
        var unlocked = emptyList<Achievement>()
        val result = store.updateData { current ->
            val next = transform(current)
            unlocked = Achievements.newlyUnlocked(next)
            if (unlocked.isEmpty()) next
            else next.copy(achievements = next.achievements + unlocked.associate { it.id to System.currentTimeMillis() })
        }
        unlocked.forEach { _achievementEvents.tryEmit(it) }
        return result to unlocked
    }

    private fun UserData.touch(day: Long): UserData {
        if (lastActiveDay == day) return this
        val newStreak = if (lastActiveDay == day - 1) streak + 1 else 1
        return copy(streak = newStreak, bestStreak = maxOf(bestStreak, newStreak), lastActiveDay = day)
    }

    suspend fun saveProfile(profile: Profile) {
        mutate { it.copy(profile = profile) }
    }

    suspend fun recordRun(levelId: String?) {
        mutate { d ->
            d.copy(
                programRuns = d.programRuns + 1,
                attempts = if (levelId == null) d.attempts else d.attempts + (levelId to (d.attempts[levelId] ?: 0) + 1),
            )
        }
    }

    suspend fun recordHint() {
        mutate { it.copy(hintsUsed = it.hintsUsed + 1) }
    }

    suspend fun recordSolutionViewed() {
        mutate { it.copy(solutionsViewed = it.solutionsViewed + 1) }
    }

    suspend fun markStorySeen(worldId: String) {
        mutate { d -> if (worldId in d.seenStories) d else d.copy(seenStories = d.seenStories + worldId) }
    }

    suspend fun setCostume(id: String?) {
        mutate { it.copy(costume = id) }
    }

    suspend fun completeLevel(level: Level, stars: Int, blocks: Int, usedHint: Boolean, usedSolution: Boolean = false): Reward {
        val before = current()
        val previous = before.levels[level.id]
        val firstClear = previous == null
        val xp = when {
            firstClear -> 20 + stars * 5
            stars > previous.stars -> (stars - previous.stars) * 5
            else -> 0
        }
        val (after, unlocked) = mutate { d ->
            val old = d.levels[level.id]
            val record = LevelRecord(
                stars = maxOf(stars, old?.stars ?: 0),
                bestBlocks = minOf(blocks, old?.bestBlocks ?: Int.MAX_VALUE),
                usedHint = (old?.usedHint ?: true) && usedHint,
                attempts = old?.attempts ?: (d.attempts[level.id] ?: 1),
                completedAt = old?.completedAt ?: System.currentTimeMillis(),
                usedSolution = (old?.usedSolution ?: true) && usedSolution,
            )
            d.copy(levels = d.levels + (level.id to record), xp = d.xp + xp).touch(today())
        }
        return Reward(
            xpGained = xp,
            stars = stars,
            previousStars = previous?.stars ?: 0,
            firstClear = firstClear,
            newAchievements = unlocked,
            leveledUpTo = after.playerLevel.takeIf { it > before.playerLevel },
        )
    }

    suspend fun completeDaily(day: Long, stars: Int): Reward {
        val before = current()
        val already = day in before.dailyDone
        val xp = if (already) 0 else 30 + stars * 5
        val (after, unlocked) = mutate { d ->
            if (day in d.dailyDone) d
            else d.copy(dailyDone = (d.dailyDone + day).takeLast(400), xp = d.xp + xp).touch(today())
        }
        return Reward(
            xpGained = xp,
            stars = stars,
            firstClear = !already,
            newAchievements = unlocked,
            leveledUpTo = after.playerLevel.takeIf { it > before.playerLevel },
        )
    }

    suspend fun recordQuiz(correct: Int, total: Int): Reward {
        val before = current()
        val perfect = correct == total && total > 0
        val day = today()
        val sessionsToday = if (before.quizDay == day) before.quizToday else 0
        // XP kuis hanya untuk 3 sesi pertama per hari; selebihnya tetap boleh berlatih.
        val xp = if (sessionsToday < 3) correct * 5 + if (perfect) 10 else 0 else 0
        val (after, unlocked) = mutate { d ->
            d.copy(
                quizDay = day,
                quizToday = sessionsToday + 1,
                quizSessions = d.quizSessions + 1,
                quizAnswered = d.quizAnswered + total,
                quizCorrect = d.quizCorrect + correct,
                quizPerfect = d.quizPerfect + if (perfect) 1 else 0,
                xp = d.xp + xp,
            ).touch(today())
        }
        return Reward(
            xpGained = xp,
            newAchievements = unlocked,
            leveledUpTo = after.playerLevel.takeIf { it > before.playerLevel },
        )
    }

    suspend fun saveCreation(title: String, program: Program, map: List<String>?, existingId: Long? = null): Long {
        val id = existingId ?: System.currentTimeMillis()
        mutate { d ->
            val creation = Creation(id, title.ifBlank { "Karya tanpa judul" }, program, System.currentTimeMillis(), map)
            val others = d.creations.filterNot { it.id == id }
            d.copy(creations = listOf(creation) + others).touch(today())
        }
        return id
    }

    suspend fun deleteCreation(id: Long) {
        mutate { d -> d.copy(creations = d.creations.filterNot { it.id == id }) }
    }

    suspend fun addMinute(day: Long) {
        store.updateData { d ->
            val minutes = (d.minutesByDay + (day to d.minutesOn(day) + 1)).filterKeys { it > day - 60 }
            d.copy(minutesByDay = minutes, totalMinutes = d.totalMinutes + 1)
        }
    }

    suspend fun setUnlockAll(enabled: Boolean) {
        mutate { it.copy(unlockAll = enabled) }
    }

    /** Menghapus progres belajar tetapi tetap menyimpan profil anak. */
    suspend fun resetProgress() {
        store.updateData { d -> UserData(profile = d.profile) }
    }
}

/** Aturan buka-kunci dunia dan level. */
object Progression {
    fun isWorldUnlocked(world: World, data: UserData): Boolean {
        if (data.unlockAll) return true
        val pre = Worlds.prerequisite(world) ?: return true
        return pre.levels.all { it.id in data.levels }
    }

    fun isLevelUnlocked(world: World, index: Int, data: UserData): Boolean {
        if (data.unlockAll) return true
        if (!isWorldUnlocked(world, data)) return false
        return index == 0 || world.levels[index - 1].id in data.levels
    }

    /** Level berikutnya yang belum selesai dan sudah terbuka, untuk kartu "Lanjutkan". */
    fun nextUp(data: UserData): Pair<World, Level>? {
        for (world in Worlds.all) {
            if (!isWorldUnlocked(world, data)) continue
            world.levels.forEachIndexed { i, level ->
                if (level.id !in data.levels && isLevelUnlocked(world, i, data)) return world to level
            }
        }
        return null
    }

    fun worldStars(world: World, data: UserData): Int = world.levels.sumOf { data.levels[it.id]?.stars ?: 0 }

    fun worldCompleted(world: World, data: UserData): Int = world.levels.count { it.id in data.levels }
}
