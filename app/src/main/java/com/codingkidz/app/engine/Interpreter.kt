package com.codingkidz.app.engine

data class RobotState(
    val pos: Pos,
    val dir: Dir,
    val gemsLeft: Set<Pos>,
    val painted: Map<Pos, PaintColor> = emptyMap(),
    /** Nilai variabel "angka". */
    val number: Int = 0,
    /** Kalimat yang sedang diucapkan Bito (balon kata). */
    val say: String? = null,
)

enum class Action {
    MOVE, TURN_LEFT, TURN_RIGHT, PICK, PAINT,
    /** Gagal maju karena ada tembok atau tepi peta. */
    BUMP,
    /** Mencoba mengambil padahal tidak ada permata. */
    PICK_EMPTY,
    /** Memeriksa kondisi pada blok "jika". */
    CHECK,
    /** Memulai satu putaran perulangan. */
    LOOP,
    /** Memanggil fungsi. */
    CALL,
    /** Mengubah nilai variabel. */
    VAR,
    /** Sebuah event terpicu. */
    EVENT,
    SAY,
    WAIT;

    /** Langkah yang benar-benar menggerakkan/mengubah robot (bukan sekadar sorotan blok). */
    val isPhysical: Boolean get() = this != CHECK && this != LOOP && this != CALL && this != VAR && this != EVENT

    /** Langkah yang ditampilkan lebih lama supaya sempat dibaca/dilihat. */
    val isSlow: Boolean get() = this == SAY || this == WAIT
}

data class Step(
    val blockId: Long,
    val action: Action,
    val state: RobotState,
    /** Hasil pemeriksaan kondisi untuk [Action.CHECK]. */
    val check: Boolean? = null,
    /** Putaran ke berapa untuk [Action.LOOP], dimulai dari 1. */
    val iteration: Int = 0,
    /** Jumlah total putaran, atau -1 untuk "ulangi sampai". */
    val total: Int = 0,
)

enum class Outcome {
    SUCCESS,
    HIT_WALL,
    NO_GEM_HERE,
    NOT_AT_GOAL,
    GEMS_LEFT,
    PATTERN_MISMATCH,
    INFINITE_LOOP,
    EMPTY_PROGRAM,
    /** Mode bebas (Studio) selesai tanpa target. */
    FINISHED,
}

data class RunResult(
    val steps: List<Step>,
    val outcome: Outcome,
    /** Blok yang menyebabkan kegagalan, jika ada. */
    val failBlockId: Long? = null,
) {
    val physicalSteps: Int get() = steps.count { it.action.isPhysical }
    val finalState: RobotState? get() = steps.lastOrNull()?.state
}

/**
 * Menjalankan program sampai selesai dan merekam setiap langkah, sehingga UI bisa
 * memutar animasinya (termasuk mode langkah demi langkah) tanpa logika tambahan.
 *
 * Level dianggap selesai seketika saat syaratnya terpenuhi (semua permata terambil dan
 * robot di bendera, atau pola lukisan sudah sama) — lebih ramah untuk anak-anak.
 * Di mode bebas (Studio), "menang" hanya berlaku jika kanvas punya permata atau bendera.
 */
class Interpreter(
    private val board: Board,
    private val freePlay: Boolean = false,
    private val maxTicks: Int = 3000,
) {
    private class Halt(val outcome: Outcome, val blockId: Long?) : RuntimeException(null, null, false, false)

    private lateinit var state: RobotState
    private lateinit var program: Program
    private val steps = mutableListOf<Step>()
    private var ticks = 0
    private var depth = 0
    private var inEvent = false

    fun initialState() = RobotState(board.start, board.startDir, board.gems)

    fun run(program: Program): RunResult {
        if (program.main.isEmpty() && program.events.none { it.body.isNotEmpty() && it.kind.isTrigger }) {
            return RunResult(emptyList(), Outcome.EMPTY_PROGRAM)
        }
        return execute(program, initialState()) { execList(program.main) }
    }

    /** Menjalankan satu event (misalnya tombol di Studio) mulai dari keadaan [from]. */
    fun runEvent(program: Program, from: RobotState, kind: EventKind): RunResult =
        execute(program, from.copy(say = null)) { fireEvent(kind) }

    private fun execute(program: Program, from: RobotState, body: () -> Unit): RunResult {
        this.program = program
        state = from
        steps.clear()
        ticks = 0
        depth = 0
        inEvent = false
        return try {
            body()
            RunResult(steps.toList(), finalOutcome())
        } catch (h: Halt) {
            RunResult(steps.toList(), h.outcome, h.blockId)
        }
    }

    private val hasWinCondition: Boolean get() = board.gems.isNotEmpty() || board.goal != null

    private fun finalOutcome(): Outcome = when {
        freePlay -> Outcome.FINISHED
        isComplete() -> Outcome.SUCCESS
        board.isPaintLevel -> Outcome.PATTERN_MISMATCH
        state.gemsLeft.isNotEmpty() -> Outcome.GEMS_LEFT
        board.goal != null && state.pos != board.goal -> Outcome.NOT_AT_GOAL
        else -> Outcome.SUCCESS
    }

    private fun isComplete(): Boolean {
        if (freePlay) return hasWinCondition && state.gemsLeft.isEmpty() && (board.goal == null || state.pos == board.goal)
        board.target?.let { return state.painted == it }
        return state.gemsLeft.isEmpty() && (board.goal == null || state.pos == board.goal)
    }

    private fun tick(blockId: Long) {
        if (++ticks > maxTicks) throw Halt(Outcome.INFINITE_LOOP, blockId)
    }

    private fun record(blockId: Long, action: Action, check: Boolean? = null, iteration: Int = 0, total: Int = 0) {
        steps += Step(blockId, action, state, check, iteration, total)
    }

    private fun afterAction() {
        if (isComplete()) throw Halt(Outcome.SUCCESS, null)
    }

    private fun execList(blocks: List<Block>) {
        for (block in blocks) exec(block)
    }

    private fun moveOnce(blockId: Long) {
        val next = state.pos + state.dir
        if (!board.isOpen(next)) {
            record(blockId, Action.BUMP)
            throw Halt(Outcome.HIT_WALL, blockId)
        }
        state = state.copy(pos = next)
        record(blockId, Action.MOVE)
        afterAction()
        fireTriggers()
    }

    /** Event pemicu diperiksa setiap kali Bito tiba di petak baru (tidak bersarang). */
    private fun fireTriggers() {
        if (inEvent) return
        for (kind in TRIGGER_ORDER) {
            val happened = when (kind) {
                EventKind.ON_GEM -> state.pos in state.gemsLeft
                EventKind.ON_BLUE -> board.tile(state.pos) == Tile.BLUE
                EventKind.ON_ORANGE -> board.tile(state.pos) == Tile.ORANGE
                EventKind.WALL_AHEAD -> !board.isOpen(state.pos + state.dir)
                else -> false
            }
            if (happened && program.event(kind).isNotEmpty()) fireEvent(kind)
        }
    }

    private fun fireEvent(kind: EventKind) {
        val body = program.event(kind)
        if (body.isEmpty()) return
        tick(kind.hatId)
        record(kind.hatId, Action.EVENT)
        inEvent = true
        try {
            execList(body)
        } finally {
            inEvent = false
        }
    }

    private fun exec(block: Block) {
        tick(block.id)
        when (block) {
            is MoveBlock -> moveOnce(block.id)

            is TurnLeftBlock -> {
                state = state.copy(dir = state.dir.left())
                record(block.id, Action.TURN_LEFT)
                afterAction()
            }

            is TurnRightBlock -> {
                state = state.copy(dir = state.dir.right())
                record(block.id, Action.TURN_RIGHT)
                afterAction()
            }

            is PickBlock -> {
                if (state.pos !in state.gemsLeft) {
                    record(block.id, Action.PICK_EMPTY)
                    throw Halt(Outcome.NO_GEM_HERE, block.id)
                }
                state = state.copy(gemsLeft = state.gemsLeft - state.pos)
                record(block.id, Action.PICK)
                afterAction()
            }

            is PaintBlock -> {
                state = state.copy(painted = state.painted + (state.pos to block.color))
                record(block.id, Action.PAINT)
                afterAction()
            }

            is RepeatBlock -> {
                for (i in 1..block.times) {
                    tick(block.id)
                    record(block.id, Action.LOOP, iteration = i, total = block.times)
                    execList(block.body)
                }
            }

            is RepeatUntilBlock -> {
                var i = 0
                while (!atGoal()) {
                    tick(block.id)
                    record(block.id, Action.LOOP, iteration = ++i, total = -1)
                    execList(block.body)
                }
            }

            is IfBlock -> {
                val result = evaluate(block.condition)
                record(block.id, Action.CHECK, check = result)
                if (result) execList(block.then) else block.otherwise?.let { execList(it) }
            }

            is CallBlock -> {
                if (depth >= MAX_DEPTH) throw Halt(Outcome.INFINITE_LOOP, block.id)
                record(block.id, Action.CALL)
                depth++
                execList(program.function(block.fn))
                depth--
            }

            is SetVarBlock -> {
                state = state.copy(number = block.value)
                record(block.id, Action.VAR)
            }

            is ChangeVarBlock -> {
                state = state.copy(number = (state.number + block.delta).coerceIn(-99, 99))
                record(block.id, Action.VAR)
            }

            is MoveVarBlock -> {
                if (state.number <= 0) record(block.id, Action.VAR)
                repeat(state.number.coerceAtMost(MAX_MOVE)) {
                    tick(block.id)
                    moveOnce(block.id)
                }
            }

            is SayBlock -> {
                state = state.copy(say = block.text)
                record(block.id, Action.SAY)
            }

            is WaitBlock -> record(block.id, Action.WAIT)
        }
    }

    private fun atGoal() = board.goal != null && state.pos == board.goal

    private fun evaluate(c: Condition): Boolean = when (c) {
        Condition.PATH_AHEAD -> board.isOpen(state.pos + state.dir)
        Condition.PATH_LEFT -> board.isOpen(state.pos + state.dir.left())
        Condition.PATH_RIGHT -> board.isOpen(state.pos + state.dir.right())
        Condition.ON_GEM -> state.pos in state.gemsLeft
        Condition.ON_BLUE -> board.tile(state.pos) == Tile.BLUE
        Condition.ON_ORANGE -> board.tile(state.pos) == Tile.ORANGE
    }

    private companion object {
        const val MAX_DEPTH = 40
        const val MAX_MOVE = 30
        val TRIGGER_ORDER = listOf(EventKind.ON_GEM, EventKind.ON_BLUE, EventKind.ON_ORANGE, EventKind.WALL_AHEAD)
    }
}
