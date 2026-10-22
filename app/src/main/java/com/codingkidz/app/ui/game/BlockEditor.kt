package com.codingkidz.app.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.codingkidz.app.data.BlockMode
import com.codingkidz.app.engine.Block
import com.codingkidz.app.engine.BlockType
import com.codingkidz.app.engine.CallBlock
import com.codingkidz.app.engine.ChangeVarBlock
import com.codingkidz.app.engine.Condition
import com.codingkidz.app.engine.Cursor
import com.codingkidz.app.engine.IfBlock
import com.codingkidz.app.engine.ListRef
import com.codingkidz.app.engine.MoveVarBlock
import com.codingkidz.app.engine.PaintBlock
import com.codingkidz.app.engine.PaintColor
import com.codingkidz.app.engine.ProgramEditor
import com.codingkidz.app.engine.RepeatBlock
import com.codingkidz.app.engine.RepeatUntilBlock
import com.codingkidz.app.engine.SayBlock
import com.codingkidz.app.engine.SetVarBlock
import com.codingkidz.app.engine.WaitBlock
import com.codingkidz.app.engine.type
import com.codingkidz.app.ui.components.BlockCategory
import com.codingkidz.app.ui.components.SayPresets
import com.codingkidz.app.ui.components.styleOf
import kotlin.math.roundToInt

private val FailRed = Color(0xFFEF4444)
private val ActiveAmber = Color(0xFFF59E0B)

/** Mode tampilan blok yang sedang aktif (gambar / kata / kode). */
val LocalBlockMode = staticCompositionLocalOf { BlockMode.WORDS }

/** Callback penyuntingan yang diteruskan dari [ProgramController]. */
class EditorActions(
    val select: (Long) -> Unit,
    val setCursor: (Cursor) -> Unit,
    val delete: (Long) -> Unit,
    val shift: (Long, Int) -> Unit,
    val setTimes: (Long, Int) -> Unit,
    val setCondition: (Long, Condition) -> Unit,
    val setColor: (Long, PaintColor) -> Unit,
    val setVarValue: (Long, Int) -> Unit,
    val setDelta: (Long, Int) -> Unit,
    val setSay: (Long, String) -> Unit,
)

fun ProgramController.editorActions() = EditorActions(
    select = ::select,
    setCursor = ::setCursor,
    delete = ::delete,
    shift = ::shift,
    setTimes = ::setTimes,
    setCondition = ::setCondition,
    setColor = ::setColor,
    setVarValue = ::setVarValue,
    setDelta = ::setDelta,
    setSay = ::setSay,
)

private class EditorContext(
    val ui: ProgramUi,
    val conditions: List<Condition>,
    val colors: List<PaintColor>,
    val actions: EditorActions,
)

/** Daftar blok program untuk tab yang sedang aktif. */
@Composable
fun ProgramView(
    ui: ProgramUi,
    conditions: List<Condition>,
    colors: List<PaintColor>,
    actions: EditorActions,
    modifier: Modifier = Modifier,
) {
    val tab = ui.currentTab
    val blocks = ProgramEditor.listAt(ui.program, tab.ref).orEmpty()
    val ctx = EditorContext(ui, conditions, colors, actions)
    Column(modifier.animateContentSize()) {
        when (tab) {
            is EditorTab.Event -> HatBlock(
                icon = Icons.Rounded.Bolt,
                text = "${tab.kind.emoji} ${tab.kind.label}",
                color = BlockCategory.EVENT.color,
                active = ui.activeBlockId == tab.kind.hatId,
            )
            is EditorTab.Function -> HatBlock(
                icon = Icons.Rounded.Functions,
                text = "Fungsi ${if (tab.index == 0) "A" else "B"}",
                color = BlockCategory.FUNCTION.color,
                active = false,
            )
            EditorTab.Main -> Unit
        }
        BlockList(tab.ref, blocks, ctx, root = true)
    }
}

@Composable
private fun HatBlock(icon: ImageVector, text: String, color: Color, active: Boolean) {
    Surface(
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 6.dp, bottomEnd = 6.dp),
        color = if (active) lerp(color, Color.White, 0.2f) else color,
        border = if (active) BorderStroke(4.dp, ActiveAmber) else null,
        modifier = Modifier.padding(bottom = 6.dp),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, color = Color.White, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun BlockList(ref: ListRef, blocks: List<Block>, ctx: EditorContext, root: Boolean = false) {
    val dnd = LocalDragDrop.current
    val dragging = dnd?.isDragging == true
    val marker = if (dragging) dnd?.dropTarget else ctx.ui.cursor.takeIf { !ctx.ui.isBusy }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        blocks.forEachIndexed { i, block ->
            if (marker == Cursor(ref, i)) CursorLine(strong = dragging)
            BlockItem(block, ctx)
        }
        EndSlot(
            ref = ref,
            active = marker == Cursor(ref, blocks.size),
            empty = blocks.isEmpty(),
            root = root,
            onClick = { ctx.actions.setCursor(Cursor(ref, blocks.size)) },
        )
    }
}

@Composable
private fun CursorLine(strong: Boolean) {
    val pulse by rememberInfiniteTransition(label = "cursor").animateFloat(
        0.4f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "pulse",
    )
    Box(
        Modifier
            .fillMaxWidth()
            .height(if (strong) 8.dp else 4.dp)
            .clip(CircleShape)
            .background(if (strong) ActiveAmber else MaterialTheme.colorScheme.primary.copy(alpha = pulse)),
    )
}

@Composable
private fun EndSlot(ref: ListRef, active: Boolean, empty: Boolean, root: Boolean, onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline
    val mode = LocalBlockMode.current
    val pulse by rememberInfiniteTransition(label = "slot").animateFloat(
        0.5f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "pulse",
    )
    val text = when {
        mode == BlockMode.PICTURE -> ""
        root && empty -> "Seret atau ketuk blok di bawah untuk mulai menyusun program"
        active -> "Blok baru masuk di sini"
        empty -> "Ketuk atau seret ke sini"
        else -> "Tambah di sini"
    }
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = if (root && empty) 72.dp else 36.dp)
            .dropZone("end-$ref") { DropZone(it, ZoneKind.END, ref = ref) }
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) color.copy(alpha = 0.10f * pulse + 0.04f) else Color.Transparent)
            .drawBehind {
                drawRoundRect(
                    color = if (active) color.copy(alpha = pulse) else outline.copy(alpha = 0.5f),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                )
            }
            .clickable(onClick = onClick)
            .semantics { contentDescription = text.ifBlank { "Tempat blok baru" } }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = if (mode == BlockMode.PICTURE) Alignment.Center else Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Add, null, tint = if (active) color else outline, modifier = Modifier.size(if (text.isEmpty()) 26.dp else 18.dp))
            if (text.isNotEmpty()) {
                Spacer(Modifier.width(6.dp))
                Text(text, style = MaterialTheme.typography.labelMedium, color = if (active) color else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BlockItem(block: Block, ctx: EditorContext) {
    val ui = ctx.ui
    val active = ui.activeBlockId == block.id
    val failed = ui.failBlockId == block.id && ui.outcome != null
    val selected = ui.selectedId == block.id && !ui.isBusy
    val dnd = LocalDragDrop.current
    val beingDragged = (dnd?.payload as? DragPayload.Existing)?.blockId == block.id

    val shake = remember { Animatable(0f) }
    LaunchedEffect(failed) {
        if (failed) {
            repeat(3) {
                shake.animateTo(8f, tween(50))
                shake.animateTo(-8f, tween(50))
            }
            shake.animateTo(0f, tween(50))
        }
    }
    val scale by animateFloatAsState(if (active && !failed) 1.03f else 1f, tween(150), label = "scale")
    val borderColor by animateColorAsState(
        when {
            failed -> FailRed
            active -> ActiveAmber
            selected -> MaterialTheme.colorScheme.onSurface
            else -> Color.Transparent
        },
        label = "border",
    )
    val baseModifier = Modifier
        .offset { IntOffset(shake.value.roundToInt(), 0) }
        .scale(scale)
        .alpha(if (beingDragged) 0.35f else 1f)

    if (block is RepeatBlock || block is RepeatUntilBlock || block is IfBlock) {
        ContainerBlock(block, borderColor, selected, active, ctx, baseModifier)
    } else {
        SimpleBlock(block, borderColor, selected, active, failed, ctx, baseModifier)
    }
}

@Composable
private fun SimpleBlock(
    block: Block,
    borderColor: Color,
    selected: Boolean,
    active: Boolean,
    failed: Boolean,
    ctx: EditorContext,
    modifier: Modifier,
) {
    val style = styleOf(block.type)
    val mode = LocalBlockMode.current
    val picture = mode == BlockMode.PICTURE
    val ui = ctx.ui
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .dropZone("block-${block.id}") { DropZone(it, ZoneKind.BLOCK, blockId = block.id) }
            .dragSource(enabled = !ui.isBusy, longPress = true, program = ui.program) { DragPayload.Existing(block.id, block.type) }
            .semantics { contentDescription = describe(block) },
        shape = RoundedCornerShape(14.dp),
        color = if (active) lerp(style.color, Color.White, 0.18f) else style.color,
        border = BorderStroke(if (active || failed) 4.dp else 2.dp, borderColor),
        shadowElevation = if (active || selected) 6.dp else 1.dp,
        onClick = { ctx.actions.select(block.id) },
    ) {
        Row(
            Modifier.heightIn(min = if (picture) 56.dp else 46.dp).padding(start = 6.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.DragIndicator, null, tint = Color.White.copy(alpha = 0.55f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Icon(style.icon, null, tint = Color.White, modifier = Modifier.size(if (picture) 32.dp else 22.dp))
            Spacer(Modifier.width(8.dp))
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                when (block) {
                    is CallBlock -> BlockText(if (picture) (if (block.fn == 0) "A" else "B") else "Panggil Fungsi ${if (block.fn == 0) "A" else "B"}")
                    is SetVarBlock -> {
                        VarBadge(picture)
                        BlockText(" = ")
                        Stepper(block.value, min = 0, max = 20, enabled = !ui.isBusy) { ctx.actions.setVarValue(block.id, it) }
                    }
                    is ChangeVarBlock -> {
                        VarBadge(picture)
                        Spacer(Modifier.width(6.dp))
                        Stepper(block.delta, min = -5, max = 5, enabled = !ui.isBusy, signed = true) { ctx.actions.setDelta(block.id, it) }
                    }
                    is MoveVarBlock -> {
                        if (!picture) BlockText("Maju sebanyak ")
                        VarBadge(picture)
                    }
                    is SayBlock -> {
                        if (!picture) BlockText("Katakan ")
                        SayChip(block, enabled = !ui.isBusy) { ctx.actions.setSay(block.id, it) }
                    }
                    is WaitBlock -> BlockText(if (picture) "1" else "Tunggu 1 detik")
                    is PaintBlock -> {
                        if (!picture) BlockText(style.label)
                        Spacer(Modifier.weight(1f))
                        ColorPicker(block, ctx.colors, enabled = !ui.isBusy, showLabel = !picture) { ctx.actions.setColor(block.id, it) }
                    }
                    else -> if (!picture) BlockText(style.label)
                }
            }
            if (selected) BlockActions(block.id, ctx.actions)
        }
    }
}

@Composable
private fun BlockText(text: String) {
    Text(text, color = Color.White, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun VarBadge(compact: Boolean) {
    Surface(shape = RoundedCornerShape(8.dp), color = Color.White.copy(alpha = 0.92f)) {
        Row(Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Inventory2, null, tint = BlockCategory.VARIABLE.color, modifier = Modifier.size(16.dp))
            if (!compact) {
                Spacer(Modifier.width(3.dp))
                Text("angka", style = MaterialTheme.typography.labelLarge, color = BlockCategory.VARIABLE.color)
            }
        }
    }
}

private fun describe(block: Block): String = when (block) {
    is RepeatBlock -> "Ulangi ${block.times} kali"
    is IfBlock -> "Jika ${block.condition.label}"
    is SetVarBlock -> "angka sama dengan ${block.value}"
    is ChangeVarBlock -> "angka ${if (block.delta >= 0) "tambah" else "kurang"} ${kotlin.math.abs(block.delta)}"
    is SayBlock -> "Katakan ${block.text}"
    is PaintBlock -> "Cat ${block.color.label}"
    is CallBlock -> "Panggil Fungsi ${if (block.fn == 0) "A" else "B"}"
    else -> styleOf(block.type).label
}

@Composable
private fun ContainerBlock(
    block: Block,
    borderColor: Color,
    selected: Boolean,
    active: Boolean,
    ctx: EditorContext,
    modifier: Modifier,
) {
    val style = styleOf(block.type)
    val color = style.color
    val ui = ctx.ui
    val picture = LocalBlockMode.current == BlockMode.PICTURE
    val headerColor = if (active) lerp(color, Color.White, 0.18f) else color
    Column(
        modifier
            .fillMaxWidth()
            .border(BorderStroke(if (active) 4.dp else 2.dp, borderColor), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .semantics { contentDescription = describe(block) },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .dropZone("head-${block.id}") { DropZone(it, ZoneKind.HEADER, blockId = block.id) }
                .dragSource(enabled = !ui.isBusy, longPress = true, program = ui.program) { DragPayload.Existing(block.id, block.type) }
                .background(headerColor)
                .clickable { ctx.actions.select(block.id) }
                .heightIn(min = if (picture) 56.dp else 48.dp)
                .padding(start = 6.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.DragIndicator, null, tint = Color.White.copy(alpha = 0.55f), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Icon(style.icon, null, tint = Color.White, modifier = Modifier.size(if (picture) 30.dp else 22.dp))
            Spacer(Modifier.width(8.dp))
            when (block) {
                is RepeatBlock -> {
                    if (!picture) BlockText("Ulangi ")
                    Stepper(block.times, min = 1, max = 20, enabled = !ui.isBusy) { ctx.actions.setTimes(block.id, it) }
                    BlockText(if (picture) " ×" else " kali")
                }
                is RepeatUntilBlock -> BlockText(if (picture) "🏁" else "Ulangi sampai finis")
                is IfBlock -> {
                    BlockText(if (picture) "❓" else "Jika ")
                    ConditionChip(block, ctx.conditions, enabled = !ui.isBusy, compact = picture) { ctx.actions.setCondition(block.id, it) }
                    ui.checks[block.id]?.let { ok ->
                        Spacer(Modifier.width(4.dp))
                        Text(if (ok) "✅" else "❌")
                    }
                }
                else -> Unit
            }
            Spacer(Modifier.weight(1f))
            ui.loopLabels[block.id]?.let { label ->
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.25f)) {
                    Text(label, color = Color.White, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }
            if (selected) BlockActions(block.id, ctx.actions)
        }
        when (block) {
            is RepeatBlock -> ContainerBody(ListRef.Body(block.id), block.body, color, ctx)
            is RepeatUntilBlock -> ContainerBody(ListRef.Body(block.id), block.body, color, ctx)
            is IfBlock -> {
                ContainerBody(ListRef.Then(block.id), block.then, color, ctx)
                block.otherwise?.let { otherwise ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .dropZone("else-${block.id}") { DropZone(it, ZoneKind.ELSE_BAR, blockId = block.id) }
                            .background(headerColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(if (picture) "↪️" else "Lainnya (jika tidak)", color = Color.White, style = MaterialTheme.typography.labelLarge)
                    }
                    ContainerBody(ListRef.Else(block.id), otherwise, color, ctx)
                }
            }
            else -> Unit
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(14.dp)
                .dropZone("foot-${block.id}") { DropZone(it, ZoneKind.FOOTER, blockId = block.id) }
                .background(headerColor),
        )
    }
}

@Composable
private fun ContainerBody(ref: ListRef, blocks: List<Block>, color: Color, ctx: EditorContext) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(Modifier.width(14.dp).fillMaxHeight().background(color))
        Box(
            Modifier
                .weight(1f)
                .background(color.copy(alpha = 0.08f))
                .padding(6.dp),
        ) {
            BlockList(ref, blocks, ctx)
        }
    }
}

@Composable
private fun Stepper(value: Int, min: Int, max: Int, enabled: Boolean, signed: Boolean = false, onChange: (Int) -> Unit) {
    Row(
        Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.22f)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallRoundButton(Icons.Rounded.Remove, "Kurangi", enabled && value > min) { onChange(value - 1) }
        Text(
            if (signed && value > 0) "+$value" else if (signed) "−${-value}" else "$value",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        SmallRoundButton(Icons.Rounded.Add, "Tambah", enabled && value < max) { onChange(value + 1) }
    }
}

@Composable
private fun SmallRoundButton(icon: ImageVector, label: String, enabled: Boolean, size: Dp = 34.dp, onClick: () -> Unit) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = Color.White.copy(alpha = if (enabled) 1f else 0.4f), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun BlockActions(id: Long, actions: EditorActions) {
    Row {
        SmallRoundButton(Icons.Rounded.KeyboardArrowUp, "Geser ke atas", true) { actions.shift(id, -1) }
        SmallRoundButton(Icons.Rounded.KeyboardArrowDown, "Geser ke bawah", true) { actions.shift(id, 1) }
        SmallRoundButton(Icons.Rounded.Close, "Hapus blok", true) { actions.delete(id) }
    }
}

@Composable
private fun ConditionChip(block: IfBlock, conditions: List<Condition>, enabled: Boolean, compact: Boolean, onPick: (Condition) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Surface(
            shape = CircleShape,
            color = Color.White,
            onClick = { if (enabled && conditions.size > 1) open = true },
        ) {
            Row(Modifier.padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (compact) block.condition.emoji else block.condition.emoji + " " + block.condition.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF4C1D95),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 170.dp),
                )
                if (conditions.size > 1) Icon(Icons.Rounded.KeyboardArrowDown, null, tint = Color(0xFF4C1D95), modifier = Modifier.size(18.dp))
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            conditions.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.emoji + "  " + c.label) },
                    onClick = {
                        open = false
                        onPick(c)
                    },
                )
            }
        }
    }
}

@Composable
private fun ColorPicker(block: PaintBlock, colors: List<PaintColor>, enabled: Boolean, showLabel: Boolean, onPick: (PaintColor) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Surface(
            shape = CircleShape,
            color = Color.White,
            onClick = { if (enabled && colors.size > 1) open = true },
            modifier = Modifier.padding(end = 4.dp),
        ) {
            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(20.dp).clip(CircleShape).background(Color(block.color.argb)).border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape))
                if (showLabel) {
                    Spacer(Modifier.width(6.dp))
                    Text(block.color.label, style = MaterialTheme.typography.labelLarge, color = Color(0xFF134E4A))
                }
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            colors.forEach { c ->
                DropdownMenuItem(
                    leadingIcon = { Box(Modifier.size(20.dp).clip(CircleShape).background(Color(c.argb)).border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)) },
                    text = { Text(c.label) },
                    onClick = {
                        open = false
                        onPick(c)
                    },
                )
            }
        }
    }
}

@Composable
private fun SayChip(block: SayBlock, enabled: Boolean, onChange: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    Box {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White, onClick = { if (enabled) open = true }) {
            Text(
                "“${block.text}”",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF701A75),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp).widthIn(max = 160.dp),
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            SayPresets.forEach { text ->
                DropdownMenuItem(text = { Text(text) }, onClick = { open = false; onChange(text) })
            }
            HorizontalDivider()
            DropdownMenuItem(text = { Text("✏️  Tulis sendiri…") }, onClick = { open = false; editing = true })
        }
    }
    if (editing) {
        var text by remember { mutableStateOf(block.text) }
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text("Bito berkata…") },
            text = {
                OutlinedTextField(value = text, onValueChange = { if (it.length <= 40) text = it }, singleLine = true)
            },
            confirmButton = { TextButton(onClick = { editing = false; onChange(text) }) { Text("Simpan") } },
            dismissButton = { TextButton(onClick = { editing = false }) { Text("Batal") } },
        )
    }
}

/**
 * Palet blok. Ketuk untuk menambah di posisi kursor, atau seret ke tempat yang diinginkan.
 * Kalau blok sedikit, semuanya ditampilkan membungkus ke baris berikutnya supaya tidak ada yang
 * tersembunyi; kalau banyak (Studio), palet bisa digeser dan seret dimulai dengan tekan-lama.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlockPalette(
    types: List<BlockType>,
    colors: List<PaintColor>,
    enabled: Boolean,
    program: com.codingkidz.app.engine.Program,
    onAdd: (BlockType, PaintColor) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items: List<Pair<BlockType, PaintColor?>> = types.flatMap { type ->
        if (type == BlockType.PAINT) colors.map { type to it } else listOf(type to null)
    }
    val wrap = items.size <= 9
    val chip: @Composable (BlockType, PaintColor?) -> Unit = { type, color ->
        PaletteChip(
            type = type,
            color = color,
            enabled = enabled,
            longPressDrag = !wrap,
            program = program,
            onClick = { onAdd(type, color ?: PaintColor.RED) },
        )
    }
    if (wrap) {
        FlowRow(
            modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) { items.forEach { (t, c) -> chip(t, c) } }
    } else {
        Row(
            modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) { items.forEach { (t, c) -> chip(t, c) } }
    }
}

@Composable
private fun PaletteChip(
    type: BlockType,
    color: PaintColor?,
    enabled: Boolean,
    longPressDrag: Boolean,
    program: com.codingkidz.app.engine.Program,
    onClick: () -> Unit,
) {
    val style = styleOf(type)
    val picture = LocalBlockMode.current == BlockMode.PICTURE
    val label = if (color != null) "Cat ${color.label}" else style.label
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        color = style.color.copy(alpha = if (enabled) 1f else 0.45f),
        shadowElevation = 2.dp,
        modifier = Modifier
            .dragSource(enabled = enabled, longPress = longPressDrag, program = program) {
                DragPayload.New(type, color ?: PaintColor.RED)
            }
            .semantics { contentDescription = "Tambah blok $label" },
    ) {
        Row(
            Modifier
                .height(if (picture) 56.dp else 44.dp)
                .padding(horizontal = if (picture) 14.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (color != null) {
                Box(Modifier.size(22.dp).clip(CircleShape).background(Color(color.argb)).border(2.dp, Color.White, CircleShape))
            } else {
                Icon(style.icon, null, tint = Color.White, modifier = Modifier.size(if (picture) 30.dp else 22.dp))
            }
            if (!picture) {
                Spacer(Modifier.width(8.dp))
                Text(label, color = Color.White, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Bayangan blok yang mengikuti jari saat diseret. */
@Composable
fun DragGhost(payload: DragPayload, modifier: Modifier = Modifier) {
    val style = styleOf(payload.type)
    val color = (payload as? DragPayload.New)?.color?.takeIf { payload.type == BlockType.PAINT }
    Surface(
        modifier = modifier.alpha(0.92f),
        shape = RoundedCornerShape(14.dp),
        color = style.color,
        shadowElevation = 12.dp,
        border = BorderStroke(2.dp, Color.White),
    ) {
        Row(Modifier.height(48.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (color != null) {
                Box(Modifier.size(20.dp).clip(CircleShape).background(Color(color.argb)).border(2.dp, Color.White, CircleShape))
            } else {
                Icon(style.icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            if (LocalBlockMode.current != BlockMode.PICTURE) {
                Spacer(Modifier.width(8.dp))
                Text(style.label, color = Color.White, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

/** Tempat sampah yang muncul saat blok program diseret. */
@Composable
fun TrashZone(active: Boolean, modifier: Modifier = Modifier) {
    val dnd = LocalDragDrop.current
    Box(
        modifier
            .fillMaxWidth()
            .height(76.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .onGloballyPositioned { dnd?.trashRect = it.boundsInRoot() }
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) FailRed else FailRed.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.DeleteOutline, null, tint = if (active) Color.White else FailRed)
            Spacer(Modifier.width(8.dp))
            Text(
                "Lepas di sini untuk membuang",
                color = if (active) Color.White else FailRed,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
