package com.codingkidz.app.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.codingkidz.app.data.BlockMode
import com.codingkidz.app.engine.BlockType
import com.codingkidz.app.engine.CodeLanguage
import com.codingkidz.app.engine.Condition
import com.codingkidz.app.engine.PaintColor
import com.codingkidz.app.ui.LocalFeedback
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private val RunGreen = Color(0xFF16A34A)

/**
 * Area penyusun program: tab (utama/fungsi/event), daftar blok dengan drag & drop, kode teks,
 * dan palet. Di mode Kode, blok dan kode ditampilkan berdampingan.
 */
@Composable
fun EditorPane(
    ui: ProgramUi,
    controller: ProgramController,
    blockTypes: List<BlockType>,
    conditions: List<Condition>,
    colors: List<PaintColor>,
    mode: BlockMode,
    showCode: Boolean,
    language: CodeLanguage,
    onLanguage: (CodeLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions = remember(controller) { controller.editorActions() }
    val feedback = LocalFeedback.current
    val dnd = remember { DragDropState() }
    dnd.defaultRef = ui.currentTab.ref
    dnd.onStart = { feedback.tap() }
    dnd.onDrop = { payload, target, trash ->
        when {
            trash && payload is DragPayload.Existing -> controller.delete(payload.blockId)
            target == null -> Unit
            payload is DragPayload.New -> controller.addAt(payload.type, payload.color, target)
            payload is DragPayload.Existing -> controller.moveTo(payload.blockId, target)
        }
    }
    val programScroll = rememberScrollState()
    var origin by remember { mutableStateOf(Offset.Zero) }

    // Gulir otomatis saat blok diseret ke tepi atas/bawah daftar.
    LaunchedEffect(dnd.isDragging) {
        while (dnd.isDragging) {
            val r = dnd.programRect
            val edge = 120f
            val dy = when {
                dnd.pointer.y < r.top + edge && dnd.pointer.y > r.top - 40f -> -18f
                dnd.pointer.y > r.bottom - edge && dnd.pointer.y < r.bottom + 40f -> 18f
                else -> 0f
            }
            if (dy != 0f) {
                programScroll.scrollBy(dy)
                dnd.refresh()
            }
            delay(16)
        }
    }

    CompositionLocalProvider(LocalDragDrop provides dnd, LocalBlockMode provides mode) {
        Box(modifier.onGloballyPositioned { origin = it.positionInRoot() }) {
            Column(Modifier.fillMaxSize()) {
                if (ui.tabs.size > 1) TabsRow(ui, controller)
                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                    val sideBySide = mode == BlockMode.CODE
                    if (sideBySide && maxWidth >= 560.dp) {
                        Row(Modifier.fillMaxSize()) {
                            ProgramArea(ui, conditions, colors, actions, dnd, programScroll, Modifier.weight(1f).fillMaxHeight())
                            VerticalDivider()
                            CodeArea(ui, language, onLanguage, Modifier.weight(1f).fillMaxHeight())
                        }
                    } else if (sideBySide) {
                        Column(Modifier.fillMaxSize()) {
                            ProgramArea(ui, conditions, colors, actions, dnd, programScroll, Modifier.weight(1.2f).fillMaxWidth())
                            HorizontalDivider()
                            CodeArea(ui, language, onLanguage, Modifier.weight(1f).fillMaxWidth())
                        }
                    } else {
                        Column(Modifier.fillMaxSize()) {
                            ProgramArea(ui, conditions, colors, actions, dnd, programScroll, Modifier.weight(1f).fillMaxWidth()) {
                                AnimatedVisibility(showCode) {
                                    CodeArea(ui, language, onLanguage, Modifier.padding(top = 12.dp))
                                }
                            }
                        }
                    }
                }
                HorizontalDivider()
                val draggingExisting = dnd.payload is DragPayload.Existing
                if (draggingExisting) {
                    TrashZone(active = dnd.overTrash)
                } else {
                    val paletteTypes = blockTypes.filter { type ->
                        when (type) {
                            BlockType.CALL_A -> ui.tabs.any { it is EditorTab.Function && it.index == 0 }
                            BlockType.CALL_B -> ui.tabs.any { it is EditorTab.Function && it.index == 1 }
                            else -> true
                        }
                    }
                    BlockPalette(paletteTypes, colors, enabled = ui.phase != RunPhase.RUNNING, program = ui.program, onAdd = controller::add)
                }
            }
            dnd.payload?.let { payload ->
                val p = dnd.pointer - origin
                DragGhost(
                    payload,
                    Modifier.offset { IntOffset((p.x - 60.dp.toPx()).roundToInt(), (p.y - 64.dp.toPx()).roundToInt()) },
                )
            }
        }
    }
}

@Composable
private fun TabsRow(ui: ProgramUi, controller: ProgramController) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ui.tabs.forEachIndexed { i, tab ->
            val filled = when (tab) {
                EditorTab.Main -> ui.program.main.isNotEmpty()
                is EditorTab.Function -> ui.program.function(tab.index).isNotEmpty()
                is EditorTab.Event -> ui.program.event(tab.kind).isNotEmpty()
            }
            FilterChip(
                selected = ui.tab == i,
                onClick = { controller.setTab(i) },
                label = {
                    Text(
                        when (tab) {
                            EditorTab.Main -> "Utama"
                            is EditorTab.Function -> "Fungsi ${if (tab.index == 0) "A" else "B"}"
                            is EditorTab.Event -> "Ketika ${tab.kind.emoji}"
                        } + if (filled && ui.tab != i) " •" else "",
                    )
                },
                leadingIcon = when (tab) {
                    EditorTab.Main -> null
                    is EditorTab.Function -> ({ Text("✨") })
                    is EditorTab.Event -> ({ Text("⚡") })
                },
            )
        }
    }
}

@Composable
private fun ProgramArea(
    ui: ProgramUi,
    conditions: List<Condition>,
    colors: List<PaintColor>,
    actions: EditorActions,
    dnd: DragDropState,
    scroll: ScrollState,
    modifier: Modifier,
    footer: @Composable () -> Unit = {},
) {
    Column(
        modifier
            .onGloballyPositioned { dnd.programRect = it.boundsInRoot() }
            .verticalScroll(scroll)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        when (val tab = ui.currentTab) {
            is EditorTab.Function -> Hint("Blok di sini dijalankan setiap kali Bito memanggil Fungsi ${if (tab.index == 0) "A" else "B"}.")
            is EditorTab.Event -> Hint("Blok di sini dijalankan otomatis setiap kali: ${tab.kind.label.lowercase()}.")
            EditorTab.Main -> if (LocalBlockMode.current != BlockMode.PICTURE && ui.program.isEmpty()) {
                Hint("Tip: tekan lama sebuah blok untuk memindahkannya, atau seret ke tempat sampah untuk membuang.")
            }
        }
        ProgramView(ui, conditions, colors, actions)
        footer()
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Hint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
private fun CodeArea(ui: ProgramUi, language: CodeLanguage, onLanguage: (CodeLanguage) -> Unit, modifier: Modifier) {
    Column(modifier.verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("</> Kode", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            CodeLanguage.entries.forEach { lang ->
                FilterChip(selected = language == lang, onClick = { onLanguage(lang) }, label = { Text(lang.label) })
            }
        }
        CodePanel(ui.program, ui.activeBlockId, language, Modifier.padding(top = 6.dp))
    }
}

/** Tombol Jalankan / Langkah / Ulang / Undo / Hapus semua. */
@Composable
fun RunControls(
    ui: ProgramUi,
    onRun: () -> Unit,
    onStep: () -> Unit,
    onReset: () -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmClear by remember { mutableStateOf(false) }
    Surface(modifier, tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onUndo, enabled = ui.canUndo && ui.phase != RunPhase.RUNNING) {
                Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = "Batalkan")
            }
            IconButton(onClick = { confirmClear = true }, enabled = !ui.program.isEmpty() && ui.phase != RunPhase.RUNNING) {
                Icon(Icons.Rounded.DeleteSweep, contentDescription = "Hapus semua blok")
            }
            Spacer(Modifier.weight(1f))
            if (ui.phase == RunPhase.STEPPING) {
                IconButton(onClick = onReset) { Icon(Icons.Rounded.RestartAlt, contentDescription = "Ulang dari awal") }
            }
            FilledTonalButton(onClick = onStep, enabled = ui.phase != RunPhase.RUNNING) {
                Icon(Icons.Rounded.SkipNext, null, Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text("Langkah", maxLines = 1)
            }
            Spacer(Modifier.width(8.dp))
            val running = ui.phase == RunPhase.RUNNING
            Button(
                onClick = if (running || ui.phase == RunPhase.DONE) onReset else onRun,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (running) MaterialTheme.colorScheme.error else RunGreen,
                    contentColor = Color.White,
                ),
                modifier = Modifier.height(52.dp),
            ) {
                Icon(
                    when {
                        running -> Icons.Rounded.Stop
                        ui.phase == RunPhase.DONE -> Icons.Rounded.RestartAlt
                        else -> Icons.Rounded.PlayArrow
                    },
                    null,
                    Modifier.size(26.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    when {
                        running -> "Berhenti"
                        ui.phase == RunPhase.DONE -> "Ulang"
                        ui.phase == RunPhase.STEPPING -> "Lanjutkan"
                        else -> "Jalankan"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Hapus semua blok?") },
            text = { Text("Program akan dikosongkan. Kamu masih bisa membatalkannya dengan tombol undo.") },
            confirmButton = {
                TextButton(onClick = { confirmClear = false; onClear() }) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Batal") } },
        )
    }
}
