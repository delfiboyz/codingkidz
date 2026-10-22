package com.codingkidz.app.ui.game

import com.codingkidz.app.data.RunSpeed
import com.codingkidz.app.engine.Action
import com.codingkidz.app.engine.Block
import com.codingkidz.app.engine.BlockType
import com.codingkidz.app.engine.Board
import com.codingkidz.app.engine.CallBlock
import com.codingkidz.app.engine.ChangeVarBlock
import com.codingkidz.app.engine.Condition
import com.codingkidz.app.engine.Cursor
import com.codingkidz.app.engine.EventKind
import com.codingkidz.app.engine.IfBlock
import com.codingkidz.app.engine.Interpreter
import com.codingkidz.app.engine.ListRef
import com.codingkidz.app.engine.MoveBlock
import com.codingkidz.app.engine.MoveVarBlock
import com.codingkidz.app.engine.Outcome
import com.codingkidz.app.engine.PaintBlock
import com.codingkidz.app.engine.PaintColor
import com.codingkidz.app.engine.PickBlock
import com.codingkidz.app.engine.Program
import com.codingkidz.app.engine.ProgramEditor
import com.codingkidz.app.engine.RepeatBlock
import com.codingkidz.app.engine.RepeatUntilBlock
import com.codingkidz.app.engine.RobotState
import com.codingkidz.app.engine.RunResult
import com.codingkidz.app.engine.SayBlock
import com.codingkidz.app.engine.SetVarBlock
import com.codingkidz.app.engine.Step
import com.codingkidz.app.engine.TurnLeftBlock
import com.codingkidz.app.engine.TurnRightBlock
import com.codingkidz.app.engine.WaitBlock
import com.codingkidz.app.engine.isContainer
import com.codingkidz.app.ui.components.SayPresets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RunPhase { IDLE, RUNNING, STEPPING, DONE }

/** Tab editor: program utama, fungsi, atau event. */
sealed interface EditorTab {
    val ref: ListRef

    data object Main : EditorTab {
        override val ref: ListRef = ListRef.Main
    }

    data class Function(val index: Int) : EditorTab {
        override val ref: ListRef = ListRef.Function(index)
    }

    data class Event(val kind: EventKind) : EditorTab {
        override val ref: ListRef = ListRef.Event(kind)
    }
}

fun editorTabs(functionCount: Int, events: List<EventKind>): List<EditorTab> =
    listOf(EditorTab.Main) + (0 until functionCount).map { EditorTab.Function(it) } + events.map { EditorTab.Event(it) }

data class ProgramUi(
    val program: Program,
    val tabs: List<EditorTab>,
    val cursor: Cursor = Cursor(ListRef.Main, program.main.size),
    val selectedId: Long? = null,
    val tab: Int = 0,
    val canUndo: Boolean = false,
    val robot: RobotState,
    val rotation: Float,
    val phase: RunPhase = RunPhase.IDLE,
    val activeBlockId: Long? = null,
    /** Label putaran yang sedang berjalan per blok perulangan, misalnya "2/4". */
    val loopLabels: Map<Long, String> = emptyMap(),
    /** Hasil pemeriksaan terakhir per blok "jika". */
    val checks: Map<Long, Boolean> = emptyMap(),
    val effects: BoardEffects = BoardEffects(),
    val outcome: Outcome? = null,
    val failBlockId: Long? = null,
    val speed: RunSpeed = RunSpeed.NORMAL,
    val stepCount: Int = 0,
) {
    val isBusy: Boolean get() = phase == RunPhase.RUNNING || phase == RunPhase.STEPPING
    val blockCount: Int get() = program.blockCount()
    val currentTab: EditorTab get() = tabs.getOrElse(tab) { EditorTab.Main }
}

sealed interface ProgramEvent {
    data class Stepped(val action: Action) : ProgramEvent
    data class Finished(val result: RunResult) : ProgramEvent
    /** Event tombol/sentuh (Studio) selesai dijalankan. */
    data class EventDone(val kind: EventKind, val result: RunResult) : ProgramEvent
    data class Message(val text: String) : ProgramEvent
    data class Added(val type: BlockType) : ProgramEvent
}

/**
 * Mengelola penyuntingan program blok (sisip, pindah, hapus, undo) dan pemutaran animasinya.
 * Dipakai bersama oleh layar level dan Studio Kreasi.
 */
class ProgramController(
    private val board: Board,
    private val scope: CoroutineScope,
    initial: Program = Program(),
    tabs: List<EditorTab> = listOf(EditorTab.Main),
    private val freePlay: Boolean = false,
    private val maxBlocks: Int? = null,
    private val defaultCondition: Condition = Condition.PATH_AHEAD,
    speed: RunSpeed = RunSpeed.NORMAL,
) {
    private var interpreter = Interpreter(board, freePlay)
    private var initialRobot = interpreter.initialState()
    private var startDegrees = board.startDir.degrees
    private val undoStack = ArrayDeque<Program>()
    private var nextId = initial.maxId() + 1
    private var job: Job? = null
    private var pendingResult: RunResult? = null
    private var stepIndex = 0

    private val _state = MutableStateFlow(
        ProgramUi(program = initial, tabs = tabs, robot = initialRobot, rotation = startDegrees, speed = speed),
    )
    val state: StateFlow<ProgramUi> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProgramEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<ProgramEvent> = _events.asSharedFlow()

    private val current get() = _state.value

    /** Selama animasi berjalan program tidak boleh diubah; saat mode langkah, mengedit akan mengulang. */
    private val locked get() = current.phase == RunPhase.RUNNING

    private fun tabRef(ui: ProgramUi = current): ListRef = ui.currentTab.ref

    private fun endOf(program: Program, ref: ListRef) = Cursor(ref, ProgramEditor.listAt(program, ref)?.size ?: 0)

    // ---------- Penyuntingan ----------

    private fun edit(transform: (ProgramUi) -> ProgramUi) {
        if (locked) return
        val before = current.program
        val next = transform(current)
        if (next.program == before && next == current) return
        undoStack.addLast(before)
        if (undoStack.size > 60) undoStack.removeFirst()
        _state.value = resetRun(next).copy(canUndo = true)
    }

    private fun resetRun(ui: ProgramUi) = ui.copy(
        robot = initialRobot,
        rotation = startDegrees,
        phase = RunPhase.IDLE,
        activeBlockId = null,
        loopLabels = emptyMap(),
        checks = emptyMap(),
        effects = BoardEffects(),
        outcome = null,
        failBlockId = null,
        stepCount = 0,
    )

    private fun newBlock(type: BlockType, color: PaintColor): Block {
        val id = nextId++
        return when (type) {
            BlockType.MOVE -> MoveBlock(id)
            BlockType.TURN_LEFT -> TurnLeftBlock(id)
            BlockType.TURN_RIGHT -> TurnRightBlock(id)
            BlockType.PICK -> PickBlock(id)
            BlockType.PAINT -> PaintBlock(id, color)
            BlockType.REPEAT -> RepeatBlock(id, 3)
            BlockType.REPEAT_UNTIL -> RepeatUntilBlock(id)
            BlockType.IF -> IfBlock(id, defaultCondition)
            BlockType.IF_ELSE -> IfBlock(id, defaultCondition, otherwise = emptyList())
            BlockType.CALL_A -> CallBlock(id, 0)
            BlockType.CALL_B -> CallBlock(id, 1)
            BlockType.SET_VAR -> SetVarBlock(id, 1)
            BlockType.CHANGE_VAR -> ChangeVarBlock(id, 1)
            BlockType.MOVE_VAR -> MoveVarBlock(id)
            BlockType.SAY -> SayBlock(id, SayPresets.first())
            BlockType.WAIT -> WaitBlock(id)
        }
    }

    private fun canAdd(): Boolean {
        val max = maxBlocks ?: return true
        if (current.blockCount < max) return true
        _events.tryEmit(ProgramEvent.Message("Batas $max blok tercapai! Coba pakai perulangan, variabel, atau fungsi."))
        return false
    }

    /** Ketuk palet: blok masuk di posisi kursor. */
    fun add(type: BlockType, color: PaintColor = PaintColor.RED) {
        if (locked) return
        val cursor = current.cursor.takeIf { ProgramEditor.exists(current.program, it.ref) } ?: endOf(current.program, tabRef())
        addAt(type, color, cursor)
    }

    /** Seret dari palet: blok masuk tepat di posisi jatuhnya. */
    fun addAt(type: BlockType, color: PaintColor, cursor: Cursor) {
        if (locked || !canAdd()) return
        val block = newBlock(type, color)
        edit { ui ->
            val program = ProgramEditor.insert(ui.program, cursor, block)
            val nextCursor = when (block) {
                is RepeatBlock, is RepeatUntilBlock -> Cursor(ListRef.Body(block.id), 0)
                is IfBlock -> Cursor(ListRef.Then(block.id), 0)
                else -> Cursor(cursor.ref, cursor.index + 1)
            }
            ui.copy(program = program, cursor = nextCursor, selectedId = block.id)
        }
        _events.tryEmit(ProgramEvent.Added(type))
    }

    /** Seret blok yang sudah ada ke posisi baru (beserta isinya). */
    fun moveTo(id: Long, target: Cursor) {
        edit { ui ->
            val program = ProgramEditor.move(ui.program, id, target)
            val loc = ProgramEditor.locate(program, id)
            ui.copy(program = program, selectedId = id, cursor = loc?.let { Cursor(it.ref, it.index + 1) } ?: ui.cursor)
        }
    }

    fun select(id: Long) {
        if (locked) return
        val loc = ProgramEditor.locate(current.program, id) ?: return
        _state.update {
            if (it.selectedId == id) it.copy(selectedId = null)
            else it.copy(selectedId = id, cursor = Cursor(loc.ref, loc.index + 1))
        }
    }

    fun setCursor(cursor: Cursor) {
        if (locked) return
        _state.update { it.copy(cursor = cursor, selectedId = null) }
    }

    fun delete(id: Long) {
        val loc = ProgramEditor.locate(current.program, id) ?: return
        edit { ui ->
            val program = ProgramEditor.remove(ui.program, id)
            val cursor = if (ProgramEditor.exists(program, loc.ref)) loc else endOf(program, tabRef(ui))
            ui.copy(program = program, cursor = cursor, selectedId = null)
        }
    }

    fun shift(id: Long, delta: Int) {
        edit { ui ->
            val program = ProgramEditor.shift(ui.program, id, delta)
            val loc = ProgramEditor.locate(program, id)
            ui.copy(program = program, cursor = loc?.let { Cursor(it.ref, it.index + 1) } ?: ui.cursor)
        }
    }

    private fun update(id: Long, transform: (Block) -> Block) {
        edit { ui -> ui.copy(program = ProgramEditor.replace(ui.program, id, transform)) }
    }

    fun setTimes(id: Long, times: Int) = update(id) { (it as? RepeatBlock)?.copy(times = times.coerceIn(1, 20)) ?: it }
    fun setCondition(id: Long, condition: Condition) = update(id) { (it as? IfBlock)?.copy(condition = condition) ?: it }
    fun setColor(id: Long, color: PaintColor) = update(id) { (it as? PaintBlock)?.copy(color = color) ?: it }
    fun setVarValue(id: Long, value: Int) = update(id) { (it as? SetVarBlock)?.copy(value = value.coerceIn(0, 20)) ?: it }
    /** Perubahan 0 tidak berguna, jadi stepper melompatinya (+1 → −1 dan sebaliknya). */
    fun setDelta(id: Long, delta: Int) = update(id) { b ->
        if (b !is ChangeVarBlock) b
        else b.copy(delta = if (delta == 0) (if (b.delta > 0) -1 else 1) else delta.coerceIn(-5, 5))
    }
    fun setSay(id: Long, text: String) = update(id) { (it as? SayBlock)?.copy(text = text.take(40).ifBlank { "…" }) ?: it }

    fun setTab(tab: Int) {
        if (locked) return
        _state.update {
            val ref = it.tabs.getOrElse(tab) { EditorTab.Main }.ref
            it.copy(tab = tab, selectedId = null, cursor = endOf(it.program, ref))
        }
    }

    fun undo() {
        if (locked) return
        val previous = undoStack.removeLastOrNull() ?: return
        _state.update {
            resetRun(it).copy(
                program = previous,
                selectedId = null,
                cursor = endOf(previous, tabRef(it)),
                canUndo = undoStack.isNotEmpty(),
            )
        }
    }

    fun clear() {
        edit { ui -> ui.copy(program = Program(), cursor = Cursor(tabRef(ui), 0), selectedId = null) }
    }

    fun load(program: Program, newBoard: Board? = null) {
        stop()
        newBoard?.let { setBoard(it) }
        undoStack.clear()
        nextId = program.maxId() + 1
        _state.update {
            resetRun(it).copy(program = program, tab = 0, selectedId = null, cursor = Cursor(ListRef.Main, program.main.size), canUndo = false)
        }
    }

    /** Mengganti peta (Studio: mode desain). */
    fun setBoard(newBoard: Board) {
        job?.cancel()
        interpreter = Interpreter(newBoard, freePlay)
        initialRobot = interpreter.initialState()
        startDegrees = newBoard.startDir.degrees
        pendingResult = null
        _state.update { resetRun(it) }
    }

    fun setSpeed(speed: RunSpeed) = _state.update { it.copy(speed = speed) }

    fun isContainer(id: Long): Boolean = ProgramEditor.find(current.program, id)?.isContainer == true

    // ---------- Menjalankan ----------

    fun run() {
        if (current.phase == RunPhase.RUNNING) return
        val continuing = current.phase == RunPhase.STEPPING
        val result = if (continuing) pendingResult!! else interpreter.run(current.program)
        if (!continuing) {
            pendingResult = result
            stepIndex = 0
            _state.update { resetRun(it).copy(selectedId = null) }
        }
        _state.update { it.copy(phase = RunPhase.RUNNING) }
        job = scope.launch {
            if (!continuing) delay(150)
            while (stepIndex < result.steps.size) {
                val step = result.steps[stepIndex++]
                apply(step)
                delay(delayFor(step))
            }
            finish(result)
        }
    }

    fun step() {
        if (current.phase == RunPhase.RUNNING) return
        if (current.phase != RunPhase.STEPPING) {
            pendingResult = interpreter.run(current.program)
            stepIndex = 0
            _state.update { resetRun(it).copy(selectedId = null, phase = RunPhase.STEPPING) }
        }
        val result = pendingResult ?: return
        if (stepIndex < result.steps.size) {
            apply(result.steps[stepIndex++])
            if (stepIndex >= result.steps.size) finish(result)
        } else {
            finish(result)
        }
    }

    /** Menjalankan event tombol/sentuh dari posisi Bito saat ini (mode main di Studio). */
    fun fireEvent(kind: EventKind) {
        if (current.phase == RunPhase.RUNNING || current.phase == RunPhase.STEPPING) return
        if (current.program.event(kind).isEmpty()) {
            _events.tryEmit(ProgramEvent.Message("Tab \"${kind.label}\" masih kosong."))
            return
        }
        val result = interpreter.runEvent(current.program, current.robot, kind)
        _state.update { it.copy(phase = RunPhase.RUNNING, outcome = null, failBlockId = null, selectedId = null) }
        job = scope.launch {
            result.steps.forEach { step ->
                apply(step)
                delay(delayFor(step))
            }
            job = null
            _state.update {
                it.copy(
                    phase = RunPhase.DONE,
                    outcome = result.outcome,
                    failBlockId = result.failBlockId,
                    activeBlockId = null,
                    effects = it.effects.copy(celebrate = result.outcome == Outcome.SUCCESS),
                )
            }
            _events.tryEmit(ProgramEvent.EventDone(kind, result))
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        pendingResult = null
        _state.update { resetRun(it) }
    }

    private fun delayFor(step: Step): Long {
        val base = current.speed.stepMillis
        return when {
            step.action.isSlow -> maxOf(base * 2, 900)
            step.action.isPhysical -> base
            else -> (base * 0.45f).toLong()
        }
    }

    private fun apply(step: Step) {
        _state.update { ui ->
            val rotation = when (step.action) {
                Action.TURN_LEFT -> ui.rotation - 90f
                Action.TURN_RIGHT -> ui.rotation + 90f
                else -> ui.rotation
            }
            val effects = when (step.action) {
                Action.BUMP, Action.PICK_EMPTY -> ui.effects.copy(bumpTick = ui.effects.bumpTick + 1)
                Action.PICK -> ui.effects.copy(pickTick = ui.effects.pickTick + 1)
                Action.PAINT -> ui.effects.copy(paintTick = ui.effects.paintTick + 1)
                else -> ui.effects
            }
            val labels = if (step.action == Action.LOOP) {
                ui.loopLabels + (step.blockId to if (step.total > 0) "${step.iteration}/${step.total}" else "ke-${step.iteration}")
            } else ui.loopLabels
            val checks = if (step.action == Action.CHECK && step.check != null) ui.checks + (step.blockId to step.check) else ui.checks
            ui.copy(
                robot = step.state,
                rotation = rotation,
                activeBlockId = step.blockId,
                effects = effects,
                loopLabels = labels,
                checks = checks,
                stepCount = ui.stepCount + if (step.action.isPhysical) 1 else 0,
            )
        }
        _events.tryEmit(ProgramEvent.Stepped(step.action))
    }

    private fun finish(result: RunResult) {
        job = null
        _state.update {
            it.copy(
                phase = RunPhase.DONE,
                outcome = result.outcome,
                failBlockId = result.failBlockId,
                activeBlockId = result.failBlockId,
                effects = it.effects.copy(celebrate = result.outcome == Outcome.SUCCESS),
            )
        }
        _events.tryEmit(ProgramEvent.Finished(result))
    }
}
