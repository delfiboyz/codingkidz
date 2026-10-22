package com.codingkidz.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

private val ScreenColor = Color(0xFF1E1B4B)
private val EyeColor = Color(0xFF4ADE80)
private val AntennaColor = Color(0xFFFACC15)

/**
 * Menggambar Bito. Tubuhnya selalu tegak supaya ramah dilihat anak; arah hadap ditunjukkan
 * lewat mata yang melirik dan panah kecil yang mengorbit di sekeliling tubuh.
 *
 * @param lookDegrees 0 = atas, 90 = kanan, dan seterusnya.
 */
fun DrawScope.drawRobot(
    center: Offset,
    size: Float,
    color: Color,
    lookDegrees: Float?,
    blink: Float = 0f,
    showPointer: Boolean = lookDegrees != null,
    happy: Boolean = false,
    costume: String? = null,
) {
    val s = size
    val headW = s * 0.62f
    val headH = s * 0.52f
    val headTop = center.y - headH * 0.42f
    val headLeft = center.x - headW / 2

    if (showPointer && lookDegrees != null) {
        rotate(lookDegrees, pivot = center) {
            val r = s * 0.47f
            val tip = Offset(center.x, center.y - r)
            val path = Path().apply {
                moveTo(tip.x, tip.y - s * 0.03f)
                lineTo(tip.x - s * 0.11f, tip.y + s * 0.09f)
                lineTo(tip.x + s * 0.11f, tip.y + s * 0.09f)
                close()
            }
            drawPath(path, Color.White)
            drawPath(path, lerp(color, Color.Black, 0.15f), style = Stroke(width = s * 0.025f))
        }
    }

    // Bayangan
    drawOval(
        color = Color.Black.copy(alpha = 0.15f),
        topLeft = Offset(center.x - headW * 0.45f, headTop + headH * 1.05f),
        size = Size(headW * 0.9f, s * 0.08f),
    )
    // Antena (disembunyikan kalau Bito memakai topi)
    if (costume !in HatCostumes) {
        drawLine(
            color = lerp(color, Color.Black, 0.25f),
            start = Offset(center.x, headTop),
            end = Offset(center.x, headTop - s * 0.1f),
            strokeWidth = s * 0.035f,
            cap = StrokeCap.Round,
        )
        drawCircle(AntennaColor, radius = s * 0.055f, center = Offset(center.x, headTop - s * 0.12f))
    }
    // Telinga
    val earH = headH * 0.36f
    listOf(headLeft - s * 0.045f, headLeft + headW - s * 0.02f).forEach { x ->
        drawRoundRect(
            color = lerp(color, Color.Black, 0.2f),
            topLeft = Offset(x, headTop + headH * 0.32f),
            size = Size(s * 0.065f, earH),
            cornerRadius = CornerRadius(s * 0.03f),
        )
    }
    // Kepala
    drawRoundRect(
        color = color,
        topLeft = Offset(headLeft, headTop),
        size = Size(headW, headH),
        cornerRadius = CornerRadius(s * 0.14f),
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.25f),
        topLeft = Offset(headLeft + s * 0.04f, headTop + s * 0.025f),
        size = Size(headW * 0.5f, s * 0.05f),
        cornerRadius = CornerRadius(s * 0.03f),
    )
    // Layar wajah
    val pad = s * 0.07f
    val screenTop = headTop + pad
    val screenH = headH - pad * 1.7f
    drawRoundRect(
        color = ScreenColor,
        topLeft = Offset(headLeft + pad, screenTop),
        size = Size(headW - pad * 2, screenH),
        cornerRadius = CornerRadius(s * 0.09f),
    )
    // Mata
    val look = lookDegrees?.let { Math.toRadians(it.toDouble()) }
    val lookX = look?.let { sin(it).toFloat() } ?: 0f
    val lookY = look?.let { -cos(it).toFloat() } ?: 0f
    val eyeY = screenTop + screenH * 0.45f
    val eyeR = s * 0.065f
    listOf(center.x - headW * 0.2f, center.x + headW * 0.2f).forEach { ex ->
        val c = Offset(ex + lookX * eyeR * 0.45f, eyeY + lookY * eyeR * 0.45f)
        if (happy) {
            drawArc(
                color = EyeColor,
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(c.x - eyeR, c.y - eyeR * 0.6f),
                size = Size(eyeR * 2, eyeR * 2),
                style = Stroke(width = s * 0.03f, cap = StrokeCap.Round),
            )
        } else {
            val h = eyeR * 2 * (1f - blink.coerceIn(0f, 0.9f))
            drawRoundRect(
                color = EyeColor,
                topLeft = Offset(c.x - eyeR, c.y - h / 2),
                size = Size(eyeR * 2, h),
                cornerRadius = CornerRadius(eyeR),
            )
        }
    }
    // Senyum
    drawArc(
        color = EyeColor,
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(center.x - s * 0.07f, eyeY + s * 0.02f),
        size = Size(s * 0.14f, s * 0.08f),
        style = Stroke(width = s * 0.022f, cap = StrokeCap.Round),
    )
    if (costume != null) drawCostume(costume, center, s, headLeft, headTop, headW, headH, eyeY, eyeR)
}

private val HatCostumes = setOf("party_hat", "wizard_hat", "pirate_hat", "beret", "crown", "headphones")

private fun DrawScope.drawCostume(
    id: String,
    center: Offset,
    s: Float,
    headLeft: Float,
    headTop: Float,
    headW: Float,
    headH: Float,
    eyeY: Float,
    eyeR: Float,
) {
    val cx = center.x
    when (id) {
        "party_hat" -> {
            val path = Path().apply {
                moveTo(cx - s * 0.16f, headTop + s * 0.02f)
                lineTo(cx + s * 0.02f, headTop - s * 0.28f)
                lineTo(cx + s * 0.18f, headTop + s * 0.02f)
                close()
            }
            drawPath(path, Color(0xFFEC4899))
            drawLine(Color(0xFFFDE047), Offset(cx - s * 0.08f, headTop - s * 0.06f), Offset(cx + s * 0.1f, headTop - s * 0.06f), strokeWidth = s * 0.03f)
            drawLine(Color(0xFF22D3EE), Offset(cx - s * 0.03f, headTop - s * 0.15f), Offset(cx + s * 0.07f, headTop - s * 0.15f), strokeWidth = s * 0.03f)
            drawCircle(Color(0xFFFDE047), radius = s * 0.045f, center = Offset(cx + s * 0.02f, headTop - s * 0.29f))
        }
        "wizard_hat" -> {
            drawOval(Color(0xFF4C1D95), Offset(cx - s * 0.3f, headTop - s * 0.04f), Size(s * 0.6f, s * 0.1f))
            val path = Path().apply {
                moveTo(cx - s * 0.18f, headTop)
                lineTo(cx + s * 0.08f, headTop - s * 0.38f)
                lineTo(cx + s * 0.18f, headTop)
                close()
            }
            drawPath(path, Color(0xFF6D28D9))
            drawCircle(Color(0xFFFDE047), radius = s * 0.035f, center = Offset(cx, headTop - s * 0.12f))
        }
        "pirate_hat" -> {
            val path = Path().apply {
                moveTo(cx - s * 0.34f, headTop + s * 0.04f)
                quadraticTo(cx, headTop - s * 0.36f, cx + s * 0.34f, headTop + s * 0.04f)
                close()
            }
            drawPath(path, Color(0xFF111827))
            drawCircle(Color.White, radius = s * 0.045f, center = Offset(cx, headTop - s * 0.08f))
            drawLine(Color(0xFFFACC15), Offset(cx - s * 0.3f, headTop + s * 0.02f), Offset(cx + s * 0.3f, headTop + s * 0.02f), strokeWidth = s * 0.02f)
        }
        "beret" -> {
            drawOval(Color(0xFFDC2626), Offset(cx - s * 0.3f, headTop - s * 0.1f), Size(s * 0.52f, s * 0.16f))
            drawLine(Color(0xFFDC2626), Offset(cx - s * 0.05f, headTop - s * 0.1f), Offset(cx - s * 0.02f, headTop - s * 0.16f), strokeWidth = s * 0.03f, cap = StrokeCap.Round)
        }
        "crown" -> {
            val w = s * 0.4f
            val left = cx - w / 2
            val base = headTop + s * 0.02f
            val path = Path().apply {
                moveTo(left, base)
                lineTo(left, base - s * 0.14f)
                lineTo(left + w * 0.25f, base - s * 0.06f)
                lineTo(left + w * 0.5f, base - s * 0.2f)
                lineTo(left + w * 0.75f, base - s * 0.06f)
                lineTo(left + w, base - s * 0.14f)
                lineTo(left + w, base)
                close()
            }
            drawPath(path, Color(0xFFFACC15))
            drawCircle(Color(0xFFEF4444), radius = s * 0.025f, center = Offset(cx, base - s * 0.06f))
        }
        "headphones" -> {
            drawArc(
                Color(0xFF111827), 190f, 160f, false,
                topLeft = Offset(headLeft - s * 0.02f, headTop - s * 0.14f),
                size = Size(headW + s * 0.04f, headH * 0.9f),
                style = Stroke(width = s * 0.05f, cap = StrokeCap.Round),
            )
            listOf(headLeft - s * 0.07f, headLeft + headW - s * 0.03f).forEach { x ->
                drawRoundRect(Color(0xFFEF4444), Offset(x, headTop + headH * 0.25f), Size(s * 0.1f, headH * 0.45f), CornerRadius(s * 0.04f))
            }
        }
        "cat_ears" -> {
            listOf(-1f, 1f).forEach { side ->
                val bx = cx + side * headW * 0.3f
                val path = Path().apply {
                    moveTo(bx - s * 0.08f, headTop + s * 0.03f)
                    lineTo(bx + side * s * 0.02f, headTop - s * 0.14f)
                    lineTo(bx + s * 0.08f, headTop + s * 0.03f)
                    close()
                }
                drawPath(path, Color(0xFF1F2937))
                drawCircle(Color(0xFFF9A8D4), radius = s * 0.025f, center = Offset(bx + side * s * 0.005f, headTop - s * 0.03f))
            }
        }
        "sunglasses" -> {
            listOf(cx - headW * 0.2f, cx + headW * 0.2f).forEach { ex ->
                drawRoundRect(Color(0xFF0F172A), Offset(ex - eyeR * 1.4f, eyeY - eyeR * 1.05f), Size(eyeR * 2.8f, eyeR * 2.1f), CornerRadius(eyeR * 0.7f))
                drawLine(Color.White.copy(alpha = 0.5f), Offset(ex - eyeR * 0.8f, eyeY - eyeR * 0.4f), Offset(ex - eyeR * 0.2f, eyeY - eyeR * 0.8f), strokeWidth = s * 0.012f)
            }
            drawLine(Color(0xFF0F172A), Offset(cx - headW * 0.07f, eyeY - eyeR * 0.5f), Offset(cx + headW * 0.07f, eyeY - eyeR * 0.5f), strokeWidth = s * 0.025f)
        }
        "bow_tie" -> {
            val y = headTop + headH + s * 0.02f
            val left = Path().apply { moveTo(cx, y); lineTo(cx - s * 0.12f, y - s * 0.06f); lineTo(cx - s * 0.12f, y + s * 0.06f); close() }
            val right = Path().apply { moveTo(cx, y); lineTo(cx + s * 0.12f, y - s * 0.06f); lineTo(cx + s * 0.12f, y + s * 0.06f); close() }
            drawPath(left, Color(0xFFEF4444))
            drawPath(right, Color(0xFFEF4444))
            drawCircle(Color(0xFFB91C1C), radius = s * 0.03f, center = Offset(cx, y))
        }
        "flower" -> {
            val fc = Offset(headLeft + headW - s * 0.04f, headTop + s * 0.02f)
            repeat(5) { i ->
                val a = Math.toRadians(i * 72.0)
                drawCircle(Color(0xFFFDE047), radius = s * 0.045f, center = fc + Offset((cos(a) * s * 0.05f).toFloat(), (sin(a) * s * 0.05f).toFloat()))
            }
            drawCircle(Color(0xFFF97316), radius = s * 0.035f, center = fc)
        }
    }
}

/** Avatar Bito dengan animasi berkedip dan sedikit melayang. */
@Composable
fun RobotAvatar(
    colorIndex: Int,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    animated: Boolean = true,
    happy: Boolean = false,
    costume: String? = null,
) {
    val transition = rememberInfiniteTransition(label = "avatar")
    val blink by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 3600
                0f at 0
                0f at 3300
                1f at 3420
                0f at 3560
            },
        ),
        label = "blink",
    )
    val bob by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label = "bob",
    )
    val color = avatarColor(colorIndex)
    Canvas(modifier.size(size).semantics { contentDescription = "Robot Bito" }) {
        val dy = if (animated) bob * this.size.minDimension * 0.03f else 0f
        val hat = costume in HatCostumes
        drawRobot(
            center = Offset(this.size.width / 2, this.size.height / 2 + this.size.height * (if (hat) 0.13f else 0.06f) + dy),
            size = this.size.minDimension * (if (hat) 0.84f else 1f),
            color = color,
            lookDegrees = null,
            blink = if (animated) blink else 0f,
            happy = happy,
            costume = costume,
        )
    }
}
