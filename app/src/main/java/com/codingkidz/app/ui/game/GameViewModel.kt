package com.codingkidz.app.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingkidz.app.content.DailyChallenge
import com.codingkidz.app.content.Worlds
import com.codingkidz.app.data.Costume
import com.codingkidz.app.data.Reward
import com.codingkidz.app.data.Rewards
import com.codingkidz.app.data.RunSpeed
import com.codingkidz.app.data.UserRepository
import com.codingkidz.app.engine.Animal
import com.codingkidz.app.engine.Condition
import com.codingkidz.app.engine.Level
import com.codingkidz.app.engine.Outcome
import com.codingkidz.app.engine.Program
import com.codingkidz.app.engine.RunResult
import com.codingkidz.app.engine.World
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LevelComplete(
    val stars: Int,
    val blocks: Int,
    val reward: Reward,
    val nextLevelId: String?,
    /** Hewan yang baru saja diselamatkan karena dunianya tamat. */
    val rescued: Animal? = null,
    val newCostumes: List<Costume> = emptyList(),
)

data class GameUi(
    val nudgesShown: Int = 0,
    val hintsShown: Int = 0,
    val solutionShown: Boolean = false,
    val failedRuns: Int = 0,
    val showHints: Boolean = false,
    val showCode: Boolean = false,
    val complete: LevelComplete? = null,
    val failMessage: String? = null,
    val bestStars: Int = 0,
)

private val GenericNudges = listOf(
    "Coba bayangkan kamu adalah Bito. Langkah apa yang pertama?",
    "Jalankan programmu dan perhatikan di mana Bito mulai salah.",
    "Pecah masalahnya menjadi bagian-bagian kecil.",
)

class GameViewModel(
    val levelId: String,
    private val users: UserRepository,
    speed: RunSpeed,
) : ViewModel() {
    val isDaily = DailyChallenge.isDaily(levelId)
    val level: Level = if (isDaily) DailyChallenge.forDay(DailyChallenge.dayOf(levelId) ?: 0) else requireNotNull(Worlds.level(levelId))
    val world: World? = Worlds.worldOf(levelId)
    val levelNumber: Int = world?.levels?.indexOfFirst { it.id == levelId }?.plus(1) ?: 1
    val nudges: List<String> = world?.nudges ?: GenericNudges

    /** Hewan yang menunggu di tujuan; tantangan harian memilih hewan berbeda setiap hari. */
    val animal: Animal = world?.animal
        ?: Worlds.all[((DailyChallenge.dayOf(levelId) ?: 0) % Worlds.all.size).toInt()].animal

    val usesVariable: Boolean = level.blocks.any { it.name.endsWith("_VAR") }

    /** Di papan, tujuan digambar sebagai hewan dalam kandang; teks level ikut menyesuaikan. */
    fun storyText(text: String): String =
        if (level.board.goal == null) text else text.replace("bendera", "kandang ${animal.name.substringBefore(" si")}")

    val controller = ProgramController(
        board = level.board,
        scope = viewModelScope,
        initial = level.starter ?: Program(),
        tabs = editorTabs(level.functionCount, level.events),
        maxBlocks = level.maxBlocks,
        defaultCondition = level.conditions.firstOrNull() ?: Condition.PATH_AHEAD,
        speed = speed,
    )

    private val _ui = MutableStateFlow(GameUi())
    val ui: StateFlow<GameUi> = _ui.asStateFlow()

    /** Contoh solusi baru boleh dibuka setelah anak benar-benar mencoba (dan membaca petunjuk). */
    val solutionUnlocked: Boolean
        get() = _ui.value.let { it.hintsShown >= level.hints.size && it.failedRuns >= 2 }

    init {
        viewModelScope.launch {
            controller.events.collect { if (it is ProgramEvent.Finished) onFinished(it.result) }
        }
        viewModelScope.launch {
            users.data.collect { data -> _ui.update { it.copy(bestStars = data.levels[levelId]?.stars ?: 0) } }
        }
    }

    fun run() {
        _ui.update { it.copy(failMessage = null) }
        if (controller.state.value.phase != RunPhase.STEPPING) recordRun()
        controller.run()
    }

    fun step() {
        _ui.update { it.copy(failMessage = null) }
        if (controller.state.value.phase != RunPhase.STEPPING) recordRun()
        controller.step()
    }

    fun reset() {
        _ui.update { it.copy(failMessage = null) }
        controller.stop()
    }

    private fun recordRun() {
        viewModelScope.launch { users.recordRun(if (isDaily) null else levelId) }
    }

    fun openHints() {
        _ui.update { it.copy(showHints = true, nudgesShown = maxOf(it.nudgesShown, 1)) }
    }

    fun nextNudge() = _ui.update { it.copy(nudgesShown = (it.nudgesShown + 1).coerceAtMost(nudges.size)) }

    fun revealHint() {
        if (_ui.value.hintsShown >= level.hints.size) return
        _ui.update { it.copy(hintsShown = it.hintsShown + 1) }
        viewModelScope.launch { users.recordHint() }
    }

    fun revealSolution() {
        if (!solutionUnlocked || _ui.value.solutionShown) return
        _ui.update { it.copy(solutionShown = true) }
        viewModelScope.launch { users.recordSolutionViewed() }
    }

    fun closeHints() = _ui.update { it.copy(showHints = false) }

    fun toggleCode() = _ui.update { it.copy(showCode = !it.showCode) }

    fun dismissComplete() = _ui.update { it.copy(complete = null) }

    private suspend fun onFinished(result: RunResult) {
        if (result.outcome != Outcome.SUCCESS) {
            _ui.update { it.copy(failMessage = failMessage(result), failedRuns = it.failedRuns + 1) }
            return
        }
        val before = users.current()
        val blocks = controller.state.value.blockCount
        val stars = level.starsFor(blocks)
        val reward = if (isDaily) users.completeDaily(DailyChallenge.dayOf(levelId) ?: 0, stars)
        else users.completeLevel(level, stars, blocks, usedHint = _ui.value.hintsShown > 0, usedSolution = _ui.value.solutionShown)
        val after = users.current()
        val rescued = world?.takeIf { !Rewards.worldDone(it, before) && Rewards.worldDone(it, after) }?.animal
        val costumes = Rewards.costumes.filter { !it.isUnlocked(before) && it.isUnlocked(after) }
        _ui.update {
            it.copy(
                complete = LevelComplete(
                    stars = stars,
                    blocks = blocks,
                    reward = reward,
                    nextLevelId = if (isDaily) null else Worlds.nextLevel(levelId)?.id,
                    rescued = rescued,
                    newCostumes = costumes,
                ),
                failMessage = null,
            )
        }
    }

    private fun failMessage(result: RunResult): String {
        val state = controller.state.value.robot
        return when (result.outcome) {
            Outcome.HIT_WALL -> "Aduh! Bito menabrak. Blok yang bermasalah ditandai merah."
            Outcome.NO_GEM_HERE -> "Tidak ada permata di sini. Bito harus berdiri tepat di atas permata sebelum Ambil."
            Outcome.NOT_AT_GOAL -> "Programnya selesai, tapi Bito belum sampai ke ${animal.name}."
            Outcome.GEMS_LEFT -> "Masih ada ${state.gemsLeft.size} permata yang belum diambil!"
            Outcome.PATTERN_MISMATCH -> "Lukisannya belum sama dengan pola. Bandingkan dengan petak bergaris putus-putus."
            Outcome.INFINITE_LOOP -> "Programnya berputar terus tanpa henti! Ini disebut perulangan tak terbatas."
            Outcome.EMPTY_PROGRAM -> "Programnya masih kosong. Seret atau ketuk blok di bawah untuk mulai."
            Outcome.SUCCESS, Outcome.FINISHED -> ""
        }
    }
}
