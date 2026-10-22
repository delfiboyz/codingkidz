package com.codingkidz.app.ui.worlds

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codingkidz.app.content.Worlds
import com.codingkidz.app.data.Progression
import com.codingkidz.app.data.UserData
import com.codingkidz.app.ui.LocalAppContainer
import com.codingkidz.app.ui.LocalFeedback
import com.codingkidz.app.data.Rewards
import com.codingkidz.app.engine.Animal
import com.codingkidz.app.ui.components.RobotAvatar
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import com.codingkidz.app.ui.components.StarRow

private val NodeSpacing = 132.dp
private val NodeSize = 76.dp
private val Wiggle = listOf(0.5f, 0.74f, 0.6f, 0.3f, 0.26f, 0.42f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMapScreen(worldId: String, user: UserData, onBack: () -> Unit, onPlay: (String) -> Unit) {
    val world = Worlds.world(worldId) ?: return
    val color = Color(world.colorArgb)
    val unlockedWorld = Progression.isWorldUnlocked(world, user)
    val feedback = LocalFeedback.current
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val rescued = Rewards.worldDone(world, user)
    var showStory by rememberSaveable { mutableStateOf(unlockedWorld && worldId !in user.seenStories) }
    if (showStory) {
        StoryDialog(world.story, world.animal, user.profile?.avatar ?: 0, user.costume) {
            showStory = false
            scope.launch { container.userRepository.markStorySeen(worldId) }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("${world.emoji} ${world.title}") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = color.copy(alpha = 0.14f),
                    scrolledContainerColor = color.copy(alpha = 0.22f),
                ),
                scrollBehavior = scroll,
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(world.animal.emoji, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (rescued) "${world.animal.name} sudah selamat! 💖" else "Misi: selamatkan ${world.animal.name}",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            TextButton(onClick = { showStory = true }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                                Text("Baca ceritanya")
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Konsep: ${world.concept.label}", style = MaterialTheme.typography.titleMedium, color = color)
                    Spacer(Modifier.height(4.dp))
                    Text(world.intro, style = MaterialTheme.typography.bodyMedium)
                    if (!unlockedWorld) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "🔒 Selesaikan dunia ${Worlds.prerequisite(world)?.title} dulu untuk membuka dunia ini.",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            BoxWithConstraints(Modifier.widthIn(max = 560.dp).fillMaxWidth().height(NodeSpacing * world.levels.size)) {
                val width = maxWidth
                val pathColor = color.copy(alpha = 0.45f)
                Canvas(Modifier.fillMaxSize()) {
                    val spacing = NodeSpacing.toPx()
                    val points = world.levels.indices.map { i ->
                        Offset(size.width * Wiggle[i % Wiggle.size], spacing * i + spacing / 2)
                    }
                    points.zipWithNext().forEach { (a, b) ->
                        drawLine(
                            pathColor,
                            a,
                            b,
                            strokeWidth = 10.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 26f)),
                        )
                    }
                }
                world.levels.forEachIndexed { i, level ->
                    val unlocked = Progression.isLevelUnlocked(world, i, user)
                    val record = user.levels[level.id]
                    val isNext = unlocked && record == null
                    val x = width * Wiggle[i % Wiggle.size] - NodeSize / 2
                    val y = NodeSpacing * i + (NodeSpacing - NodeSize) / 2 - 10.dp
                    Column(
                        Modifier.offset(x = x - 30.dp, y = y).width(NodeSize + 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        LevelNode(
                            number = i + 1,
                            color = color,
                            unlocked = unlocked,
                            done = record != null,
                            pulse = isNext,
                            label = level.title,
                            onClick = {
                                if (unlocked) onPlay(level.id) else feedback.fail()
                            },
                        )
                        Spacer(Modifier.height(4.dp))
                        if (record != null) StarRow(record.stars, size = 16.dp)
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.background) {
                            Text(
                                level.title,
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelNode(
    number: Int,
    color: Color,
    unlocked: Boolean,
    done: Boolean,
    pulse: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    val scale by rememberInfiniteTransition(label = "node").animateFloat(
        1f, if (pulse) 1.08f else 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "scale",
    )
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = when {
            !unlocked -> MaterialTheme.colorScheme.surfaceContainerHighest
            done -> color
            else -> MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(5.dp, if (unlocked) color else MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = if (pulse) 10.dp else 3.dp,
        modifier = Modifier
            .size(NodeSize)
            .scale(scale)
            .semantics { contentDescription = "Level $number, $label" + if (!unlocked) ", terkunci" else if (done) ", selesai" else "" },
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (unlocked) {
                Text(
                    "$number",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (done) Color.White else color,
                )
            } else {
                Icon(Icons.Rounded.Lock, null, tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

/** Cerita pembuka dunia: Bito dan hewan yang harus diselamatkan. */
@Composable
private fun StoryDialog(story: String, animal: Animal, avatar: Int, costume: String?, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RobotAvatar(avatar, size = 84.dp, costume = costume)
                    Spacer(Modifier.width(12.dp))
                    Text(animal.emoji, style = MaterialTheme.typography.displayMedium)
                }
                Text(story, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Ayo selamatkan ${animal.name.substringBefore(" si")}!") }
            }
        }
    }
}
