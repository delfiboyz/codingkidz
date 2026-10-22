package com.codingkidz.app.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codingkidz.app.data.BlockMode
import com.codingkidz.app.data.RunSpeed
import com.codingkidz.app.engine.Action
import com.codingkidz.app.engine.Level
import com.codingkidz.app.engine.Outcome
import com.codingkidz.app.platform.Sfx
import com.codingkidz.app.ui.LocalAppContainer
import com.codingkidz.app.ui.LocalFeedback
import com.codingkidz.app.ui.LocalSettings
import com.codingkidz.app.ui.components.ConfettiOverlay
import com.codingkidz.app.ui.components.RobotAvatar
import com.codingkidz.app.ui.components.StarGold
import com.codingkidz.app.ui.components.StarRow
import com.codingkidz.app.ui.components.StatChip
import com.codingkidz.app.ui.components.avatarColor
import com.codingkidz.app.ui.components.styleOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    levelId: String,
    avatar: Int,
    costume: String?,
    onBack: () -> Unit,
    onNext: (String) -> Unit,
) {
    val container = LocalAppContainer.current
    val settings = LocalSettings.current
    val vm: GameViewModel = viewModel(key = "game-$levelId") {
        GameViewModel(levelId, container.userRepository, settings.speed)
    }
    val ui by vm.controller.state.collectAsStateWithLifecycle()
    val game by vm.ui.collectAsStateWithLifecycle()
    val feedback = LocalFeedback.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val level = vm.level
    val mode = settings.blockMode

    LaunchedEffect(vm) {
        vm.controller.events.collect { event ->
            when (event) {
                is ProgramEvent.Stepped -> playStep(event.action, feedback)
                is ProgramEvent.Finished -> if (event.result.outcome == Outcome.SUCCESS) feedback.success() else feedback.fail()
                is ProgramEvent.Message -> scope.launch { snackbar.showSnackbar(event.text) }
                is ProgramEvent.Added -> {
                    feedback.tap()
                    // Untuk anak yang belum lancar membaca: nama blok dibacakan.
                    if (mode == BlockMode.PICTURE) container.speaker.speak(styleOf(event.type).label.removeSuffix(" ="))
                }
                is ProgramEvent.EventDone -> Unit
            }
        }
    }
    LaunchedEffect(Unit) {
        if (settings.readAloud) container.speaker.speak(vm.storyText(level.instruction))
    }

    val subtitle = if (vm.isDaily) "Tantangan Harian" else "${vm.world?.title} • Level ${vm.levelNumber}"
    val worldColor = Color(vm.world?.colorArgb ?: 0xFF4F46E5)

    Scaffold(
        topBar = {
            GameTopBar(
                title = level.title,
                subtitle = subtitle,
                speed = ui.speed,
                showCode = game.showCode,
                codeToggle = mode != BlockMode.CODE,
                onBack = onBack,
                onHint = vm::openHints,
                onToggleCode = vm::toggleCode,
                onSpeed = {
                    val next = RunSpeed.entries[(ui.speed.ordinal + 1) % RunSpeed.entries.size]
                    vm.controller.setSpeed(next)
                    scope.launch { snackbar.showSnackbar("Kecepatan: ${next.label}") }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            RunControls(
                ui = ui,
                onRun = vm::run,
                onStep = vm::step,
                onReset = vm::reset,
                onUndo = vm.controller::undo,
                onClear = vm.controller::clear,
            )
        },
    ) { padding ->
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
            val wide = maxWidth >= 720.dp
            val boardMax = if (wide) maxHeight * 0.6f else maxHeight * 0.32f
            val boardSection: @Composable (Modifier) -> Unit = { mod ->
                Column(mod.padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InstructionCard(vm.storyText(level.instruction), worldColor, game.bestStars, onSpeak = { container.speaker.speak(vm.storyText(level.instruction)) })
                    Box(Modifier.fillMaxWidth().heightIn(max = boardMax), contentAlignment = Alignment.Center) {
                        BoardView(
                            board = level.board,
                            robot = ui.robot,
                            rotation = ui.rotation,
                            robotColor = avatarColor(avatar),
                            animMillis = (ui.speed.stepMillis * 0.85f).toInt(),
                            effects = ui.effects,
                            goalEmoji = vm.animal.emoji,
                            costume = costume,
                        )
                    }
                    StatusRow(level, ui, vm.usesVariable)
                    AnimatedVisibility(
                        visible = game.failMessage != null,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        FailCard(message = game.failMessage.orEmpty(), onRetry = vm::reset, onHint = vm::openHints)
                    }
                }
            }
            val editor: @Composable (Modifier) -> Unit = { mod ->
                EditorPane(
                    ui = ui,
                    controller = vm.controller,
                    blockTypes = level.blocks.sortedBy { it.ordinal },
                    conditions = level.conditions,
                    colors = level.colors,
                    mode = mode,
                    showCode = game.showCode,
                    language = settings.codeLanguage,
                    onLanguage = { lang -> scope.launch { container.settingsRepository.update { it.copy(codeLanguage = lang) } } },
                    modifier = mod,
                )
            }
            if (wide) {
                Row(Modifier.fillMaxSize().padding(top = 8.dp)) {
                    boardSection(Modifier.weight(1f).verticalScroll(rememberScrollState()))
                    editor(Modifier.weight(1.2f))
                }
            } else {
                Column(Modifier.fillMaxSize().padding(top = 4.dp)) {
                    boardSection(Modifier)
                    editor(Modifier.weight(1f))
                }
            }
        }
    }

    if (game.showHints) {
        HintSheet(vm, game, mode, onDismiss = vm::closeHints)
    }
    game.complete?.let { complete ->
        CompleteDialog(
            level = level,
            complete = complete,
            avatar = avatar,
            costume = costume,
            isDaily = vm.isDaily,
            onReplay = {
                vm.dismissComplete()
                vm.reset()
            },
            onNext = {
                vm.dismissComplete()
                complete.nextLevelId?.let(onNext) ?: onBack()
            },
        )
    }
}

fun playStep(action: Action, feedback: com.codingkidz.app.ui.Feedback) {
    when (action) {
        Action.MOVE -> feedback.play(Sfx.MOVE)
        Action.TURN_LEFT, Action.TURN_RIGHT -> feedback.play(Sfx.TURN)
        Action.PICK -> feedback.play(Sfx.PICK)
        Action.PAINT -> feedback.play(Sfx.PAINT)
        Action.BUMP, Action.PICK_EMPTY -> feedback.bump()
        Action.SAY, Action.EVENT -> feedback.play(Sfx.TAP)
        else -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameTopBar(
    title: String,
    subtitle: String,
    speed: RunSpeed,
    showCode: Boolean,
    codeToggle: Boolean,
    onBack: () -> Unit,
    onHint: () -> Unit,
    onToggleCode: () -> Unit,
    onSpeed: () -> Unit,
) {
    TopAppBar(
        title = {
            Column {
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1)
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali") }
        },
        actions = {
            IconButton(onClick = onSpeed) {
                BadgedBox(badge = { Badge { Text(speedBadge(speed)) } }) {
                    Icon(Icons.Rounded.Speed, contentDescription = "Ubah kecepatan, sekarang ${speed.label}")
                }
            }
            if (codeToggle) {
                IconButton(onClick = onToggleCode) {
                    Icon(
                        Icons.Rounded.Code,
                        contentDescription = if (showCode) "Sembunyikan kode" else "Lihat kode",
                        tint = if (showCode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onHint) {
                Icon(Icons.Rounded.Lightbulb, contentDescription = "Petunjuk", tint = Color(0xFFF59E0B))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

private fun speedBadge(speed: RunSpeed) = when (speed) {
    RunSpeed.SLOW -> "½×"
    RunSpeed.NORMAL -> "1×"
    RunSpeed.FAST -> "2×"
}

@Composable
private fun InstructionCard(instruction: String, color: Color, bestStars: Int, onSpeak: () -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(true) }
    Card(
        onClick = { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(Modifier.padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(instruction, style = MaterialTheme.typography.bodyMedium, maxLines = if (expanded) 6 else 1)
                if (bestStars > 0 && expanded) {
                    Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Rekor: ", style = MaterialTheme.typography.labelMedium)
                        StarRow(bestStars, size = 14.dp)
                    }
                }
            }
            IconButton(onClick = onSpeak) {
                Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Bacakan instruksi", tint = color)
            }
        }
    }
}

@Composable
private fun StatusRow(level: Level, ui: ProgramUi, usesVariable: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        val max = level.maxBlocks
        val over = max != null && ui.blockCount > max
        StatChip(
            "🧱",
            if (max != null) "${ui.blockCount}/$max" else "${ui.blockCount}",
            container = if (over) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        )
        StatChip("⭐", "≤ ${level.par}")
        if (usesVariable) StatChip("📦", "angka = ${ui.robot.number}", container = Color(0xFFFEF3C7))
        val target = level.board.target
        if (target != null) {
            val correct = target.count { (p, c) -> ui.robot.painted[p] == c }
            StatChip("🎨", "$correct/${target.size}")
        } else if (level.board.gems.isNotEmpty()) {
            StatChip("💎", "${level.board.gems.size - ui.robot.gemsLeft.size}/${level.board.gems.size}")
        }
    }
}

@Composable
private fun FailCard(message: String, onRetry: () -> Unit, onHint: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🤔", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(10.dp))
                Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            }
            Row(Modifier.align(Alignment.End).padding(top = 6.dp)) {
                TextButton(onClick = onHint) { Text("Butuh bantuan?") }
                Button(onClick = onRetry) { Text("Coba lagi") }
            }
        }
    }
}

/**
 * Petunjuk bertingkat: (1) dorongan ringan, (2) petunjuk spesifik, (3) contoh solusi —
 * yang terakhir baru terbuka setelah semua petunjuk dibaca dan anak sudah mencoba dua kali.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HintSheet(vm: GameViewModel, game: GameUi, mode: BlockMode, onDismiss: () -> Unit) {
    val level = vm.level
    var confirmSolution by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Bantuan untukmu", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Coba pikirkan sendiri dulu, ya. Buka bantuan satu per satu hanya kalau perlu.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            TierHeader("1", "Dorongan", "💭", unlocked = true)
            vm.nudges.take(game.nudgesShown).forEach { HintBubble(it, Color(0xFFE0F2FE), Color(0xFF0C4A6E)) }
            if (game.nudgesShown < vm.nudges.size) {
                TextButton(onClick = vm::nextNudge) { Text("Dorongan lain") }
            }

            TierHeader("2", "Petunjuk", "💡", unlocked = true)
            level.hints.take(game.hintsShown).forEachIndexed { i, hint ->
                HintBubble("${i + 1}. ${vm.storyText(hint)}", Color(0xFFFEF3C7), Color(0xFF451A03))
            }
            if (game.hintsShown < level.hints.size) {
                OutlinedButton(onClick = vm::revealHint, modifier = Modifier.fillMaxWidth()) {
                    Text("Lihat petunjuk ${game.hintsShown + 1} dari ${level.hints.size}")
                }
            }

            val unlocked = game.hintsShown >= level.hints.size && game.failedRuns >= 2
            TierHeader("3", "Contoh solusi", "🔑", unlocked = unlocked || game.solutionShown)
            when {
                game.solutionShown -> SolutionView(vm, mode)
                unlocked -> OutlinedButton(onClick = { confirmSolution = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Tunjukkan contoh solusi")
                }
                else -> Text(
                    "Terbuka setelah kamu membaca semua petunjuk dan sudah mencoba minimal 2 kali " +
                        "(percobaan yang belum berhasil: ${game.failedRuns}).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    if (confirmSolution) {
        AlertDialog(
            onDismissRequest = { confirmSolution = false },
            title = { Text("Lihat contoh solusi?") },
            text = { Text("Setelah melihatnya, coba susun ulang sendiri tanpa menyalin — atau temukan cara yang berbeda!") },
            confirmButton = { TextButton(onClick = { confirmSolution = false; vm.revealSolution() }) { Text("Lihat") } },
            dismissButton = { TextButton(onClick = { confirmSolution = false }) { Text("Nanti saja") } },
        )
    }
}

@Composable
private fun TierHeader(number: String, title: String, emoji: String, unlocked: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
        Surface(shape = CircleShape, color = if (unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest) {
            Text(
                number,
                color = if (unlocked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.width(10.dp))
        Text("$emoji $title", style = MaterialTheme.typography.titleMedium)
        if (!unlocked) Text("  🔒")
    }
}

@Composable
private fun HintBubble(text: String, bg: Color, fg: Color) {
    Surface(shape = RoundedCornerShape(16.dp), color = bg, modifier = Modifier.fillMaxWidth()) {
        Text(text, color = fg, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(14.dp))
    }
}

/** Menampilkan kunci jawaban sebagai blok hanya-baca untuk setiap tab. */
@Composable
private fun SolutionView(vm: GameViewModel, mode: BlockMode) {
    val level = vm.level
    val tabs = editorTabs(level.functionCount, level.events)
    val noop = remember {
        EditorActions({}, {}, {}, { _, _ -> }, { _, _ -> }, { _, _ -> }, { _, _ -> }, { _, _ -> }, { _, _ -> }, { _, _ -> })
    }
    CompositionLocalProvider(LocalBlockMode provides mode) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tabs.forEachIndexed { i, tab ->
                val list = com.codingkidz.app.engine.ProgramEditor.listAt(level.solution, tab.ref).orEmpty()
                if (list.isEmpty()) return@forEachIndexed
                if (tabs.size > 1 && tab == EditorTab.Main) Text("Utama", style = MaterialTheme.typography.labelLarge)
                ProgramView(
                    ui = ProgramUi(
                        program = level.solution,
                        tabs = tabs,
                        tab = i,
                        robot = com.codingkidz.app.engine.Interpreter(level.board).initialState(),
                        rotation = 0f,
                        phase = RunPhase.RUNNING,
                    ),
                    conditions = level.conditions,
                    colors = level.colors,
                    actions = noop,
                )
            }
            Text(
                "Solusi ini memakai ${level.par} blok. Ada banyak cara lain — bisakah kamu menemukan cara sendiri?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CompleteDialog(
    level: Level,
    complete: LevelComplete,
    avatar: Int,
    costume: String?,
    isDaily: Boolean,
    onReplay: () -> Unit,
    onNext: () -> Unit,
) {
    val feedback = LocalFeedback.current
    LaunchedEffect(complete) {
        repeat(complete.stars) {
            delay(300)
            feedback.play(Sfx.STAR)
        }
    }
    Dialog(onDismissRequest = onReplay, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(20.dp).widthIn(max = 460.dp),
            ) {
                Column(
                    Modifier.padding(22.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RobotAvatar(avatar, size = 92.dp, happy = true, costume = costume)
                    Text(
                        when (complete.stars) {
                            3 -> "Sempurna!"
                            2 -> "Hebat!"
                            else -> "Berhasil!"
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    StarRow(complete.stars, size = 46.dp, animate = true)
                    StarCriteria(level, complete)
                    complete.rescued?.let { animal ->
                        Highlight("${animal.emoji} Kamu menyelamatkan ${animal.name}! Dunia berikutnya kini terbuka.", MaterialTheme.colorScheme.secondaryContainer)
                    }
                    complete.newCostumes.forEach { c ->
                        Highlight("${c.emoji} Kostum baru: ${c.name}! Pakai di menu Profil.", MaterialTheme.colorScheme.tertiaryContainer)
                    }
                    complete.reward.newAchievements.forEach { a ->
                        Highlight("${a.emoji} Lencana baru: ${a.title}", MaterialTheme.colorScheme.tertiaryContainer)
                    }
                    if (complete.reward.xpGained > 0) {
                        Text("+${complete.reward.xpGained} XP", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    level.funFact?.let { fact ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(12.dp),
                        ) {
                            Text("💡 $fact", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onReplay, modifier = Modifier.weight(1f)) { Text(if (complete.stars < 3) "Coba lebih ringkas" else "Ulangi", textAlign = TextAlign.Center) }
                        Button(onClick = onNext, modifier = Modifier.weight(1f)) {
                            Text(if (complete.nextLevelId != null) "Lanjut ▶" else if (isDaily) "Selesai" else "Ke peta")
                        }
                    }
                }
            }
            ConfettiOverlay(1)
        }
    }
}

/** Rincian tiap bintang: selesai, kode rapi, dan solusi paling efisien. */
@Composable
private fun StarCriteria(level: Level, complete: LevelComplete) {
    val rows = listOf(
        "Berhasil menyelesaikan level" to true,
        "Kode rapi (≤ ${level.tidyLimit} blok)" to (complete.blocks <= level.tidyLimit),
        "Solusi paling efisien (≤ ${level.par} blok)" to (complete.blocks <= level.par),
    )
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        rows.forEach { (text, ok) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (ok) "★" else "☆", color = if (ok) StarGold else MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = if (ok) FontWeight.Bold else null)
            }
        }
        Text(
            when {
                complete.blocks < level.par -> "Wow! Programmu (${complete.blocks} blok) lebih pendek dari solusi kami!"
                complete.blocks == level.par -> "Kamu memakai ${complete.blocks} blok — seefisien mungkin!"
                else -> "Kamu memakai ${complete.blocks} blok. Ada banyak cara menyelesaikan level ini — bisakah lebih ringkas?"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Highlight(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(16.dp), color = color, modifier = Modifier.fillMaxWidth()) {
        Text(text, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(12.dp))
    }
}
