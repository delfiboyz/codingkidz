package com.codingkidz.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

val StarGold = Color(0xFFFBBF24)

@Composable
fun StarRow(stars: Int, modifier: Modifier = Modifier, size: Dp = 18.dp, max: Int = 3, animate: Boolean = false) {
    Row(
        modifier.semantics { contentDescription = "$stars dari $max bintang" },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        repeat(max) { i ->
            val filled = i < stars
            val scale = remember { Animatable(if (animate) 0f else 1f) }
            if (animate) {
                LaunchedEffect(Unit) {
                    delay(250L + i * 280L)
                    scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                }
            }
            Icon(
                imageVector = if (filled) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = null,
                tint = if (filled) StarGold else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(size).scale(scale.value),
            )
        }
    }
}

@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    height: Dp = 12.dp,
    trackColor: Color = color.copy(alpha = 0.18f),
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(700), label = "progress")
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(trackColor),
    ) {
        Box(
            Modifier
                .fillMaxWidth(animated)
                .height(height)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.85f), color))),
        )
    }
}

@Composable
fun StatChip(icon: String, text: String, modifier: Modifier = Modifier, container: Color = MaterialTheme.colorScheme.surfaceContainerHigh) {
    Surface(modifier, shape = CircleShape, color = container) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon)
            Text(" $text", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun IconBadge(icon: ImageVector, color: Color, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3))
            .background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(size * 0.55f))
    }
}

@Composable
fun EmojiBadge(emoji: String, color: Color, modifier: Modifier = Modifier, size: Dp = 52.dp, locked: Boolean = false) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3))
            .background(if (locked) MaterialTheme.colorScheme.surfaceContainerHighest else color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (locked) "🔒" else emoji, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        action?.invoke()
    }
}

private class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    val spin: Float,
    val color: Color,
    val size: Float,
    val round: Boolean,
)

private val ConfettiColors = listOf(
    Color(0xFFEF4444), Color(0xFFF97316), Color(0xFFFACC15), Color(0xFF22C55E),
    Color(0xFF3B82F6), Color(0xFFA855F7), Color(0xFFEC4899),
)

/** Hujan konfeti setiap kali [trigger] berubah menjadi nilai positif baru. */
@Composable
fun ConfettiOverlay(trigger: Int, modifier: Modifier = Modifier) {
    var particles by remember { mutableStateOf(emptyList<Particle>()) }
    var frame by remember { mutableLongStateOf(0L) }
    LaunchedEffect(trigger) {
        if (trigger <= 0) return@LaunchedEffect
        val rnd = Random(trigger)
        particles = List(140) {
            Particle(
                x = rnd.nextFloat(),
                y = -rnd.nextFloat() * 0.3f,
                vx = (rnd.nextFloat() - 0.5f) * 0.35f,
                vy = 0.25f + rnd.nextFloat() * 0.45f,
                rotation = rnd.nextFloat() * 360f,
                spin = (rnd.nextFloat() - 0.5f) * 720f,
                color = ConfettiColors[rnd.nextInt(ConfettiColors.size)],
                size = 6f + rnd.nextFloat() * 8f,
                round = rnd.nextInt(4) == 0,
            )
        }
        var last = withFrameNanos { it }
        val start = last
        while (last - start < 3_200_000_000L) {
            val now = withFrameNanos { it }
            val dt = (now - last) / 1_000_000_000f
            last = now
            particles.forEach { p ->
                p.vy += 0.35f * dt
                p.x += p.vx * dt
                p.y += p.vy * dt
                p.rotation += p.spin * dt
            }
            frame = now
        }
        particles = emptyList()
    }
    if (particles.isEmpty()) return
    Canvas(modifier.fillMaxSize()) {
        frame // baca state agar Canvas digambar ulang tiap frame
        particles.forEach { p ->
            val pos = Offset(p.x * size.width, p.y * size.height)
            val px = p.size.dp.toPx()
            if (p.round) {
                drawCircle(p.color, radius = px / 2, center = pos)
            } else {
                rotate(p.rotation, pivot = pos) {
                    drawRect(p.color, topLeft = Offset(pos.x - px / 2, pos.y - px / 4), size = Size(px, px / 2))
                }
            }
        }
    }
}
