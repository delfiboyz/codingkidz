package com.codingkidz.app.ui.game

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.codingkidz.app.engine.BlockType
import com.codingkidz.app.engine.Cursor
import com.codingkidz.app.engine.IfBlock
import com.codingkidz.app.engine.ListRef
import com.codingkidz.app.engine.PaintColor
import com.codingkidz.app.engine.Program
import com.codingkidz.app.engine.ProgramEditor
import com.codingkidz.app.engine.RepeatBlock
import com.codingkidz.app.engine.RepeatUntilBlock

/** Yang sedang diseret: blok baru dari palet, atau blok yang sudah ada di program. */
sealed interface DragPayload {
    val type: BlockType

    data class New(override val type: BlockType, val color: PaintColor) : DragPayload
    data class Existing(val blockId: Long, override val type: BlockType) : DragPayload
}

enum class ZoneKind { BLOCK, HEADER, FOOTER, ELSE_BAR, END }

/** Area di layar yang bisa menjadi tempat jatuh, beserta artinya di pohon program. */
data class DropZone(val rect: Rect, val kind: ZoneKind, val blockId: Long? = null, val ref: ListRef? = null)

/**
 * Drag & drop buatan sendiri (tanpa ClipData) supaya bekerja di dalam daftar bersarang:
 * setiap blok mendaftarkan posisinya, lalu tujuan jatuh dihitung dari zona terkecil di bawah jari.
 */
@Stable
class DragDropState {
    var payload by mutableStateOf<DragPayload?>(null)
        private set
    var pointer by mutableStateOf(Offset.Zero)
        private set
    var dropTarget by mutableStateOf<Cursor?>(null)
        private set
    var overTrash by mutableStateOf(false)
        private set

    val zones = HashMap<String, DropZone>()
    var programRect: Rect = Rect.Zero
    var trashRect: Rect = Rect.Zero
    var defaultRef: ListRef = ListRef.Main
    var onDrop: (DragPayload, Cursor?, Boolean) -> Unit = { _, _, _ -> }
    var onStart: () -> Unit = {}

    private var program = Program()
    private var excluded: Set<Long> = emptySet()

    val isDragging: Boolean get() = payload != null

    fun start(payload: DragPayload, at: Offset, program: Program) {
        this.program = program
        excluded = (payload as? DragPayload.Existing)?.let { ProgramEditor.subtreeIds(program, it.blockId) }.orEmpty()
        this.payload = payload
        pointer = at
        recompute()
        onStart()
    }

    fun moveBy(delta: Offset) {
        if (payload == null) return
        pointer += delta
        recompute()
    }

    /** Dipanggil saat daftar bergulir otomatis, karena posisi zona ikut bergeser. */
    fun refresh() {
        if (payload != null) recompute()
    }

    fun end() {
        val p = payload ?: return
        onDrop(p, dropTarget, overTrash)
        reset()
    }

    fun cancel() = reset()

    private fun reset() {
        payload = null
        dropTarget = null
        overTrash = false
        excluded = emptySet()
    }

    private fun recompute() {
        overTrash = payload is DragPayload.Existing && trashRect.contains(pointer)
        dropTarget = if (overTrash) null else computeTarget()
    }

    private fun computeTarget(): Cursor? {
        if (!programRect.contains(pointer)) return null
        val zone = zones.values
            .filter { it.rect.contains(pointer) && allowed(it) }
            .minByOrNull { it.rect.width * it.rect.height }
            ?: return ProgramEditor.listAt(program, defaultRef)?.let { Cursor(defaultRef, it.size) }
        val upper = pointer.y < zone.rect.center.y
        return when (zone.kind) {
            ZoneKind.END -> zone.ref?.let { ref -> ProgramEditor.listAt(program, ref)?.let { Cursor(ref, it.size) } }
            ZoneKind.ELSE_BAR -> zone.blockId?.let { Cursor(ListRef.Else(it), 0) }
            else -> {
                val id = zone.blockId ?: return null
                val loc = ProgramEditor.locate(program, id) ?: return null
                when (zone.kind) {
                    ZoneKind.BLOCK -> Cursor(loc.ref, if (upper) loc.index else loc.index + 1)
                    ZoneKind.FOOTER -> Cursor(loc.ref, loc.index + 1)
                    ZoneKind.HEADER -> if (upper) Cursor(loc.ref, loc.index) else firstChild(id) ?: Cursor(loc.ref, loc.index + 1)
                    else -> null
                }
            }
        }
    }

    private fun firstChild(id: Long): Cursor? = when (ProgramEditor.find(program, id)) {
        is RepeatBlock, is RepeatUntilBlock -> Cursor(ListRef.Body(id), 0)
        is IfBlock -> Cursor(ListRef.Then(id), 0)
        else -> null
    }

    private fun allowed(z: DropZone): Boolean {
        if (excluded.isEmpty()) return true
        if (z.blockId != null && z.blockId in excluded) return false
        val owner = z.ref?.let { ProgramEditor.ownerOf(it) }
        return owner == null || owner !in excluded
    }
}

val LocalDragDrop = staticCompositionLocalOf<DragDropState?> { null }

/** Mendaftarkan elemen ini sebagai zona jatuh. */
@Composable
fun Modifier.dropZone(key: String, factory: (Rect) -> DropZone): Modifier {
    val state = LocalDragDrop.current ?: return this
    DisposableEffect(state, key) { onDispose { state.zones.remove(key) } }
    val latest by rememberUpdatedState(factory)
    return onGloballyPositioned { state.zones[key] = latest(it.boundsInRoot()) }
}

/**
 * Menjadikan elemen ini bisa diseret. Di palet yang tidak bergulir seretan langsung aktif;
 * di daftar program (yang bisa digulir) seretan dimulai dengan tekan-lama.
 */
@Composable
fun Modifier.dragSource(
    enabled: Boolean,
    longPress: Boolean,
    program: Program,
    payload: () -> DragPayload,
): Modifier {
    val state = LocalDragDrop.current ?: return this
    val origin = remember { mutableStateOf(Offset.Zero) }
    val latestPayload by rememberUpdatedState(payload)
    val latestProgram by rememberUpdatedState(program)
    return this
        .onGloballyPositioned { origin.value = it.positionInRoot() }
        .pointerInput(enabled, longPress) {
            if (!enabled) return@pointerInput
            val onStart: (Offset) -> Unit = { offset -> state.start(latestPayload(), origin.value + offset, latestProgram) }
            if (longPress) {
                detectDragGesturesAfterLongPress(
                    onDragStart = onStart,
                    onDrag = { change, amount -> change.consume(); state.moveBy(amount) },
                    onDragEnd = { state.end() },
                    onDragCancel = { state.cancel() },
                )
            } else {
                detectDragGestures(
                    onDragStart = onStart,
                    onDrag = { change, amount -> change.consume(); state.moveBy(amount) },
                    onDragEnd = { state.end() },
                    onDragCancel = { state.cancel() },
                )
            }
        }
}
