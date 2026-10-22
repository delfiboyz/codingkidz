package com.codingkidz.app.ui.studio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codingkidz.app.content.DesignTool
import com.codingkidz.app.content.Studio
import com.codingkidz.app.data.RunSpeed
import com.codingkidz.app.data.UserRepository
import com.codingkidz.app.engine.BlockType
import com.codingkidz.app.engine.Condition
import com.codingkidz.app.engine.EventKind
import com.codingkidz.app.engine.Outcome
import com.codingkidz.app.engine.PaintColor
import com.codingkidz.app.engine.Pos
import com.codingkidz.app.engine.Program
import com.codingkidz.app.platform.Sfx
import com.codingkidz.app.ui.LocalAppContainer
import com.codingkidz.app.ui.LocalFeedback
import com.codingkidz.app.ui.LocalSettings
import com.codingkidz.app.ui.components.ConfettiOverlay
import com.codingkidz.app.ui.components.avatarColor
import com.codingkidz.app.ui.game.BoardView
import com.codingkidz.app.ui.game.EditorPane
import com.codingkidz.app.ui.game.EditorTab
import com.codingkidz.app.ui.game.ProgramController
import com.codingkidz.app.ui.game.ProgramEvent
import com.codingkidz.app.ui.game.RunControls
import com.codingkidz.app.ui.game.RunPhase
import com.codingkidz.app.ui.game.playStep
import com.codingkidz.app.engine.Board
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val StudioBlocks = listOf(
    BlockType.MOVE, BlockType.TURN_LEFT, BlockType.TURN_RIGHT, BlockType.PICK, BlockType.PAINT,
    BlockType.SAY, BlockType.WAIT, BlockType.REPEAT, BlockType.IF, BlockType.IF_ELSE,
    BlockType.SET_VAR, BlockType.CHANGE_VAR, BlockType.MOVE_VAR, BlockType.CALL_A, BlockType.CALL_B,
)
private val StudioConditions = Condition.entries
private val StudioEvents = listOf(
    EventKind.BUTTON_UP, EventKind.BUTTON_LEFT, EventKind.BUTTON_RIGHT, EventKind.TAP_ROBOT,
    EventKind.ON_GEM, EventKind.ON_BLUE, EventKind.ON_ORANGE, EventKind.WALL_AHEAD,
)
private val ButtonEvents = setOf(EventKind.BUTTON_UP, EventKind.BUTTON_LEFT, EventKind.BUTTON_RIGHT, EventKind.TAP_ROBOT)

class StudioViewModel(
    private val users: UserRepository,
    creationId: Long?,
    exampleIndex: Int?,
    speed: RunSpeed,
) : ViewModel() {
    private val _map: MutableStateFlow<List<String>> = MutableStateFlow(Studio.emptyMap())
    val map: StateFlow<List<String>> = _map.asStateFlow()
    private val _board: MutableStateFlow<Board> = MutableStateFlow(Studio.board(_map.value))
    val board: StateFlow<Board> = _board.asStateFlow()

    val controller = ProgramController(
        board = _board.value,
        scope = viewModelScope,
        tabs = listOf(EditorTab.Main, EditorTab.Function(0), EditorTab.Function(1)) + StudioEvents.map { EditorTab.Event(it) },
        freePlay = true,
        speed = speed,
    )
    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()
    var savedId: Long? = creationId
        private set

    init {
        viewModelScope.launch {
            when {
                creationId != null -> users.current().creations.firstOrNull { it.id == creationId }?.let {
                    _title.value = it.title
                    load(it.program, it.map ?: Studio.emptyMap())
                }
                exampleIndex != null -> Studio.examples.getOrNull(exampleIndex)?.let {
                    _title.value = it.title
                    load(it.program, it.map ?: Studio.emptyMap())
                }
            }
        }
    }

    private fun load(program: Program, map: List<String>) {
        _map.value = map
        _board.value = Studio.board(map)
        controller.load(program, _board.value)
    }

    fun applyTool(pos: Pos, tool: DesignTool) {
        val next = Studio.applyTool(_map.value, pos, tool)
        if (next == _map.value) return
        _map.value = next
        _board.value = Studio.board(next)
        controller.setBoard(_board.value)
    }

    fun clearMap() {
        _map.value = Studio.emptyMap()
        _board.value = Studio.board(_map.value)
        controller.setBoard(_board.value)
    }

    suspend fun save(title: String) {
        _title.value = title
        savedId = users.saveCreation(title, controller.state.value.program, _map.value, savedId)
    }

    suspend fun delete() {
        savedId?.let { users.deleteCreation(it) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(creationId: Long?, exampleIndex: Int?, avatar: Int, costume: String?, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val settings = LocalSettings.current
    val vm: StudioViewModel = viewModel(key = "studio-$creationId-$exampleIndex") {
        StudioViewModel(container.userRepository, creationId, exampleIndex, settings.speed)
    }
    val ui by vm.controller.state.collectAsStateWithLifecycle()
    val title by vm.title.collectAsStateWithLifecycle()
    val board by vm.board.collectAsStateWithLifecycle()
    val feedback = LocalFeedback.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSave by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var showCode by rememberSaveable { mutableStateOf(false) }
    var designMode by rememberSaveable { mutableStateOf(false) }
    var tool by rememberSaveable { mutableStateOf(DesignTool.WALL) }
    var wins by remember { mutableIntStateOf(0) }

    LaunchedEffect(vm) {
        vm.controller.events.collect { e ->
            when (e) {
                is ProgramEvent.Stepped -> playStep(e.action, feedback)
                is ProgramEvent.Finished -> handleOutcome(e.result.outcome, snackbar, feedback) { wins++ }
                is ProgramEvent.EventDone -> handleOutcome(e.result.outcome, snackbar, feedback, quietFinish = true) { wins++ }
                is ProgramEvent.Message -> snackbar.showSnackbar(e.text)
                is ProgramEvent.Added -> feedback.tap()
            }
        }
    }

    val hasButtons = ui.program.events.any { it.kind in ButtonEvents && it.body.isNotEmpty() }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Studio Kreasi", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(title.ifBlank { "Karya baru" }, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                        }
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali") } },
                    actions = {
                        IconButton(onClick = { designMode = !designMode; if (designMode) vm.controller.stop() }) {
                            Icon(
                                Icons.Rounded.GridOn,
                                contentDescription = if (designMode) "Selesai mendesain peta" else "Desain peta",
                                tint = if (designMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = { showCode = !showCode }) {
                            Icon(Icons.Rounded.Code, contentDescription = "Lihat kode", tint = if (showCode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (vm.savedId != null) {
                            IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Rounded.Delete, contentDescription = "Hapus karya") }
                        }
                        IconButton(onClick = { showSave = true }) { Icon(Icons.Rounded.Save, contentDescription = "Simpan karya") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                )
            },
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                if (!designMode) {
                    RunControls(
                        ui = ui,
                        onRun = vm.controller::run,
                        onStep = vm.controller::step,
                        onReset = vm.controller::stop,
                        onUndo = vm.controller::undo,
                        onClear = vm.controller::clear,
                    )
                }
            },
        ) { padding ->
            BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
                val wide = maxWidth >= 720.dp
                val boardMax = if (wide) maxHeight * 0.7f else maxHeight * 0.36f
                val boardArea: @Composable (Modifier) -> Unit = { mod ->
                    Column(mod.padding(horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().heightIn(max = boardMax).padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                            Box(
                                Modifier.pointerInput(designMode, tool, board, ui.robot.pos) {
                                    detectTapGestures { offset ->
                                        val tile = size.width.toFloat() / board.width
                                        val pos = Pos((offset.x / tile).toInt(), (offset.y / tile).toInt())
                                        if (!board.inBounds(pos)) return@detectTapGestures
                                        if (designMode) {
                                            vm.applyTool(pos, tool)
                                            feedback.tap()
                                        } else if (pos == ui.robot.pos) {
                                            vm.controller.fireEvent(EventKind.TAP_ROBOT)
                                        }
                                    }
                                },
                            ) {
                                BoardView(
                                    board = board,
                                    robot = ui.robot,
                                    rotation = ui.rotation,
                                    robotColor = avatarColor(avatar),
                                    animMillis = (ui.speed.stepMillis * 0.85f).toInt(),
                                    effects = ui.effects,
                                    costume = costume,
                                )
                            }
                        }
                        AnimatedVisibility(designMode) { DesignToolbar(tool, onTool = { tool = it }, onClear = vm::clearMap) }
                        AnimatedVisibility(!designMode && hasButtons) {
                            PlayPad(
                                enabled = ui.phase != RunPhase.RUNNING && ui.phase != RunPhase.STEPPING,
                                gems = board.gems.size - ui.robot.gemsLeft.size,
                                totalGems = board.gems.size,
                                onEvent = vm.controller::fireEvent,
                            )
                        }
                    }
                }
                val editor: @Composable (Modifier) -> Unit = { mod ->
                    EditorPane(
                        ui = ui,
                        controller = vm.controller,
                        blockTypes = StudioBlocks,
                        conditions = StudioConditions,
                        colors = PaintColor.entries,
                        mode = settings.blockMode,
                        showCode = showCode,
                        language = settings.codeLanguage,
                        onLanguage = { lang -> scope.launch { container.settingsRepository.update { it.copy(codeLanguage = lang) } } },
                        modifier = mod,
                    )
                }
                if (wide) {
                    Row(Modifier.fillMaxSize()) {
                        boardArea(Modifier.weight(1f))
                        if (!designMode) editor(Modifier.weight(1.2f))
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        boardArea(Modifier.fillMaxWidth())
                        if (!designMode) editor(Modifier.weight(1f))
                        else DesignHelp()
                    }
                }
            }
        }
        ConfettiOverlay(wins)
    }

    if (showSave) {
        var name by remember { mutableStateOf(title) }
        AlertDialog(
            onDismissRequest = { showSave = false },
            title = { Text("Simpan karya") },
            text = {
                OutlinedTextField(value = name, onValueChange = { if (it.length <= 30) name = it }, label = { Text("Judul karya") }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    showSave = false
                    scope.launch {
                        vm.save(name)
                        feedback.play(Sfx.STAR)
                        snackbar.showSnackbar("Karya tersimpan di galeri! 🎨")
                    }
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showSave = false }) { Text("Batal") } },
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Hapus karya ini?") },
            text = { Text("Karya akan dihapus dari galeri dan tidak bisa dikembalikan.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    scope.launch {
                        vm.delete()
                        onBack()
                    }
                }) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Batal") } },
        )
    }
}

private suspend fun handleOutcome(
    outcome: Outcome,
    snackbar: SnackbarHostState,
    feedback: com.codingkidz.app.ui.Feedback,
    quietFinish: Boolean = false,
    onWin: () -> Unit,
) {
    when (outcome) {
        Outcome.SUCCESS -> {
            feedback.success()
            onWin()
            snackbar.showSnackbar("🏆 Kamu menang! Semua permata terkumpul.")
        }
        Outcome.HIT_WALL -> {
            feedback.bump()
            if (!quietFinish) snackbar.showSnackbar("Bito menabrak tembok atau tepi peta dan berhenti.")
        }
        Outcome.INFINITE_LOOP -> snackbar.showSnackbar("Programnya terlalu panjang atau tidak pernah berhenti.")
        Outcome.NO_GEM_HERE -> snackbar.showSnackbar("Tidak ada permata di sini.")
        else -> if (!quietFinish) feedback.play(Sfx.CORRECT)
    }
}

@Composable
private fun DesignToolbar(tool: DesignTool, onTool: (DesignTool) -> Unit, onClear: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DesignTool.entries.forEach { t ->
                FilterChip(selected = tool == t, onClick = { onTool(t) }, label = { Text("${t.emoji} ${t.label}") })
            }
            TextButton(onClick = onClear) { Text("Kosongkan peta") }
        }
    }
}

@Composable
private fun DesignHelp() {
    Text(
        "Mode desain: pilih alat lalu ketuk petak di papan. Ketuk Bito dengan alat 🤖 untuk memutar arahnya. " +
            "Tambahkan permata dan bendera untuk membuat game — kamu menang saat semua permata terkumpul!",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(16.dp),
    )
}

/** Tombol kendali untuk karya yang memakai event tombol (mode main). */
@Composable
private fun PlayPad(enabled: Boolean, gems: Int, totalGems: Int, onEvent: (EventKind) -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.padding(vertical = 4.dp)) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (totalGems > 0) Text("💎 $gems/$totalGems", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 6.dp))
            PadButton("⬅️", "Tombol kiri", enabled) { onEvent(EventKind.BUTTON_LEFT) }
            PadButton("⬆️", "Tombol atas", enabled) { onEvent(EventKind.BUTTON_UP) }
            PadButton("➡️", "Tombol kanan", enabled) { onEvent(EventKind.BUTTON_RIGHT) }
            PadButton("👆", "Sentuh Bito", enabled) { onEvent(EventKind.TAP_ROBOT) }
        }
    }
}

@Composable
private fun PadButton(emoji: String, label: String, enabled: Boolean, onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.size(52.dp).semantics { contentDescription = label },
    ) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
    }
}
