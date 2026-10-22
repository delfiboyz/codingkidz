package com.codingkidz.app.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.semantics
import com.codingkidz.app.engine.Board
import com.codingkidz.app.engine.Pos
import com.codingkidz.app.engine.RobotState
import com.codingkidz.app.engine.Tile
import com.codingkidz.app.ui.components.drawRobot
import com.codingkidz.app.ui.theme.GameColors
import com.codingkidz.app.ui.theme.LocalGameColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Efek sesaat yang diputar papan (menabrak, mengambil permata, dll.). */
data class BoardEffects(
    val bumpTick: Int = 0,
    val pickTick: Int = 0,
    val paintTick: Int = 0,
    val celebrate: Boolean = false,
)

@Composable
fun BoardView(
    board: Board,
    robot: RobotState,
    rotation: Float,
    robotColor: Color,
    animMillis: Int,
    effects: BoardEffects,
    modifier: Modifier = Modifier,
    /** Hewan yang menunggu di tujuan (cerita penyelamatan); null = bendera biasa. */
    goalEmoji: String? = null,
    costume: String? = null,
) {
    val colors = LocalGameColors.current
    val textMeasurer = rememberTextMeasurer()
    val bubbleColor = MaterialTheme.colorScheme.surface
    val bubbleText = MaterialTheme.colorScheme.onSurface
    val x by animateFloatAsState(robot.pos.x.toFloat(), tween(animMillis, easing = FastOutSlowInEasing), label = "x")
    val y by animateFloatAsState(robot.pos.y.toFloat(), tween(animMillis, easing = FastOutSlowInEasing), label = "y")
    val rot by animateFloatAsState(rotation, tween(animMillis, easing = FastOutSlowInEasing), label = "rot")

    val bump = remember { Animatable(0f) }
    LaunchedEffect(effects.bumpTick) {
        if (effects.bumpTick == 0) return@LaunchedEffect
        bump.snapTo(0f)
        bump.animateTo(0.3f, tween(110))
        bump.animateTo(-0.06f, tween(120))
        bump.animateTo(0f, tween(120))
    }
    val sparkle = remember { Animatable(1f) }
    LaunchedEffect(effects.pickTick, effects.paintTick) {
        if (effects.pickTick == 0 && effects.paintTick == 0) return@LaunchedEffect
        sparkle.snapTo(0f)
        sparkle.animateTo(1f, tween(520))
    }

    val infinite = rememberInfiniteTransition(label = "board")
    val wave by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(1200, easing = LinearEasing)), label = "wave")
    val hop by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "hop")

    val description = buildString {
        append("Papan ${board.width} kali ${board.height}. Bito di kolom ${robot.pos.x + 1}, baris ${robot.pos.y + 1}, menghadap ${robot.dir.label}.")
        if (robot.gemsLeft.isNotEmpty()) append(" Sisa ${robot.gemsLeft.size} permata.")
        robot.say?.let { append(" Bito berkata: $it.") }
    }

    Canvas(
        modifier
            .aspectRatio(board.width.toFloat() / board.height)
            .semantics { contentDescription = description },
    ) {
        val tile = size.width / board.width
        val gap = tile * 0.05f
        drawRoundRect(colors.boardBackground, cornerRadius = CornerRadius(tile * 0.25f))

        for (ty in 0 until board.height) for (tx in 0 until board.width) {
            val p = Pos(tx, ty)
            val topLeft = Offset(tx * tile + gap, ty * tile + gap)
            val cell = Size(tile - gap * 2, tile - gap * 2)
            when (board.tile(p)) {
                Tile.WALL -> drawWall(topLeft, cell, tile, colors)
                Tile.BLUE -> drawFloor(topLeft, cell, tile, colors.blueTile, marker = true)
                Tile.ORANGE -> drawFloor(topLeft, cell, tile, colors.orangeTile, marker = true)
                Tile.FLOOR -> drawFloor(topLeft, cell, tile, if ((tx + ty) % 2 == 0) colors.floorA else colors.floorB, marker = false)
            }
            board.target?.get(p)?.let { target ->
                drawRoundRect(target.toColor().copy(alpha = 0.28f), topLeft, cell, CornerRadius(tile * 0.14f))
                drawRoundRect(
                    target.toColor(),
                    topLeft + Offset(tile * 0.06f, tile * 0.06f),
                    Size(cell.width - tile * 0.12f, cell.height - tile * 0.12f),
                    CornerRadius(tile * 0.1f),
                    style = Stroke(width = tile * 0.035f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(tile * 0.1f, tile * 0.07f))),
                )
            }
            robot.painted[p]?.let { color ->
                drawRoundRect(color.toColor(), topLeft, cell, CornerRadius(tile * 0.14f))
                drawRoundRect(
                    Color.White.copy(alpha = 0.25f),
                    topLeft + Offset(tile * 0.08f, tile * 0.06f),
                    Size(cell.width * 0.45f, tile * 0.08f),
                    CornerRadius(tile * 0.04f),
                )
            }
        }

        board.goal?.let { goal ->
            if (goalEmoji != null) drawCagedAnimal(goal, tile, goalEmoji, textMeasurer, freed = effects.celebrate, wave = wave)
            else drawFlag(goal, tile, colors, wave)
        }
        robot.gemsLeft.forEach { drawGem(it, tile, colors, wave) }

        val bumpDx = robot.dir.dx * bump.value
        val bumpDy = robot.dir.dy * bump.value
        val lift = if (effects.celebrate) hop * tile * 0.18f else 0f
        val center = Offset((x + 0.5f + bumpDx) * tile, (y + 0.52f + bumpDy) * tile - lift)

        if (sparkle.value < 1f) drawSparkle(center, tile, sparkle.value)
        drawRobot(center, tile * 0.92f, robotColor, rot, happy = effects.celebrate, costume = costume)
        robot.say?.let { drawSpeech(it, center, tile, textMeasurer, bubbleColor, bubbleText) }
    }
}

private fun DrawScope.drawCagedAnimal(p: Pos, tile: Float, emoji: String, tm: TextMeasurer, freed: Boolean, wave: Float) {
    val left = p.x * tile
    val top = p.y * tile
    val bounce = if (freed) sin(wave * 2 * PI).toFloat() * tile * 0.06f else 0f
    val style = TextStyle(fontSize = (tile * 0.42f).toSp())
    val layout = tm.measure(emoji, style)
    drawText(
        layout,
        topLeft = Offset(left + (tile - layout.size.width) / 2, top + (tile - layout.size.height) / 2 + bounce),
    )
    if (freed) {
        val heart = tm.measure("💖", TextStyle(fontSize = (tile * 0.2f).toSp()))
        drawText(heart, topLeft = Offset(left + tile * 0.62f, top + tile * 0.02f - bounce))
        return
    }
    val pad = tile * 0.14f
    val barColor = Color(0xFF78716C)
    drawRoundRect(barColor, Offset(left + pad, top + pad), Size(tile - pad * 2, tile - pad * 2), CornerRadius(tile * 0.08f), style = Stroke(width = tile * 0.04f))
    repeat(3) { i ->
        val x = left + pad + (tile - pad * 2) * (i + 1) / 4f
        drawLine(barColor, Offset(x, top + pad), Offset(x, top + tile - pad), strokeWidth = tile * 0.03f)
    }
}

private fun DrawScope.drawSpeech(text: String, robotCenter: Offset, tile: Float, tm: TextMeasurer, bg: Color, fg: Color) {
    val style = TextStyle(fontSize = maxOf(12f, tile * 0.22f / density).sp, color = fg, fontWeight = FontWeight.Bold)
    val layout = tm.measure(text, style, constraints = Constraints(maxWidth = (size.width * 0.8f).toInt()))
    val padH = 10.dp.toPx()
    val padV = 6.dp.toPx()
    val w = layout.size.width + padH * 2
    val h = layout.size.height + padV * 2
    val x = (robotCenter.x - w / 2).coerceIn(0f, maxOf(0f, size.width - w))
    var y = robotCenter.y - tile * 0.55f - h
    if (y < 0f) y = robotCenter.y + tile * 0.5f
    drawRoundRect(Color.Black.copy(alpha = 0.18f), Offset(x + 2f, y + 3f), Size(w, h), CornerRadius(h / 2))
    drawRoundRect(bg, Offset(x, y), Size(w, h), CornerRadius(h / 2))
    drawText(layout, topLeft = Offset(x + padH, y + padV))
}

private fun com.codingkidz.app.engine.PaintColor.toColor() = Color(argb)

private fun DrawScope.drawFloor(topLeft: Offset, cell: Size, tile: Float, color: Color, marker: Boolean) {
    drawRoundRect(color, topLeft, cell, CornerRadius(tile * 0.14f))
    if (marker) {
        drawCircle(
            Color.White.copy(alpha = 0.55f),
            radius = tile * 0.1f,
            center = Offset(topLeft.x + cell.width * 0.82f, topLeft.y + cell.height * 0.18f),
        )
    }
}

private fun DrawScope.drawWall(topLeft: Offset, cell: Size, tile: Float, colors: GameColors) {
    drawRoundRect(colors.wall, topLeft, cell, CornerRadius(tile * 0.18f))
    drawRoundRect(
        colors.wallTop,
        topLeft,
        Size(cell.width, cell.height * 0.78f),
        CornerRadius(tile * 0.18f),
    )
    drawCircle(Color.White.copy(alpha = 0.18f), radius = tile * 0.08f, center = topLeft + Offset(cell.width * 0.28f, cell.height * 0.25f))
}

private fun DrawScope.drawFlag(p: Pos, tile: Float, colors: GameColors, wave: Float) {
    val baseX = p.x * tile + tile * 0.36f
    val top = p.y * tile + tile * 0.16f
    val bottom = p.y * tile + tile * 0.86f
    drawOval(Color.Black.copy(alpha = 0.15f), Offset(baseX - tile * 0.12f, bottom - tile * 0.04f), Size(tile * 0.3f, tile * 0.08f))
    drawLine(colors.pole, Offset(baseX, top), Offset(baseX, bottom), strokeWidth = tile * 0.06f, cap = StrokeCap.Round)
    val w = tile * 0.42f
    val h = tile * 0.28f
    fun waveAt(t: Float) = sin((t * 2f + wave * 2f) * PI).toFloat() * tile * 0.035f * t
    val steps = 8
    val path = Path().apply {
        moveTo(baseX, top)
        for (i in 1..steps) {
            val t = i / steps.toFloat()
            lineTo(baseX + w * t, top + waveAt(t))
        }
        for (i in steps downTo 0) {
            val t = i / steps.toFloat()
            lineTo(baseX + w * t, top + h + waveAt(t))
        }
        close()
    }
    drawPath(path, colors.flag)
}

private fun DrawScope.drawGem(p: Pos, tile: Float, colors: GameColors, wave: Float) {
    val bob = sin(wave * 2 * PI + p.x + p.y).toFloat() * tile * 0.04f
    val cx = (p.x + 0.5f) * tile
    val cy = (p.y + 0.5f) * tile + bob
    val r = tile * 0.22f
    drawOval(Color.Black.copy(alpha = 0.12f), Offset(cx - r * 0.8f, (p.y + 0.78f) * tile), Size(r * 1.6f, tile * 0.07f))
    val path = Path().apply {
        moveTo(cx, cy - r)
        lineTo(cx + r * 0.85f, cy - r * 0.2f)
        lineTo(cx, cy + r)
        lineTo(cx - r * 0.85f, cy - r * 0.2f)
        close()
    }
    drawPath(path, colors.gem)
    val shine = Path().apply {
        moveTo(cx, cy - r)
        lineTo(cx + r * 0.85f, cy - r * 0.2f)
        lineTo(cx, cy - r * 0.05f)
        lineTo(cx - r * 0.85f, cy - r * 0.2f)
        close()
    }
    drawPath(shine, colors.gemShine.copy(alpha = 0.7f))
    drawCircle(Color.White.copy(alpha = 0.8f), radius = r * 0.12f, center = Offset(cx - r * 0.25f, cy - r * 0.45f))
}

private fun DrawScope.drawSparkle(center: Offset, tile: Float, t: Float) {
    val alpha = 1f - t
    drawCircle(
        lerp(Color(0xFFFDE68A), Color.White, t),
        radius = tile * (0.2f + 0.45f * t),
        center = center,
        alpha = alpha * 0.8f,
        style = Stroke(width = tile * 0.05f),
    )
    repeat(6) { i ->
        val angle = i / 6.0 * 2 * PI
        val d = tile * (0.25f + 0.4f * t)
        drawCircle(
            Color(0xFFFACC15),
            radius = tile * 0.05f * (1 - t * 0.5f),
            center = center + Offset((cos(angle) * d).toFloat(), (sin(angle) * d).toFloat()),
            alpha = alpha,
        )
    }
}
