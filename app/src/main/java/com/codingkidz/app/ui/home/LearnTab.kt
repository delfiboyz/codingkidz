package com.codingkidz.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.codingkidz.app.content.DailyChallenge
import com.codingkidz.app.content.Worlds
import com.codingkidz.app.data.PlayerLevels
import com.codingkidz.app.data.Progression
import com.codingkidz.app.data.UserData
import com.codingkidz.app.data.today
import com.codingkidz.app.engine.World
import com.codingkidz.app.ui.components.EmojiBadge
import com.codingkidz.app.ui.components.ProgressBar
import com.codingkidz.app.ui.components.RobotAvatar
import com.codingkidz.app.ui.components.SectionTitle
import com.codingkidz.app.ui.components.StarGold
import com.codingkidz.app.ui.components.StatChip

@Composable
fun LearnTab(
    user: UserData,
    onOpenWorld: (String) -> Unit,
    onPlay: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val day = today()
    val next = remember(user.levels, user.unlockAll) { Progression.nextUp(user) }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) { Greeting(user, day, onOpenSettings) }
        item(span = { GridItemSpan(maxLineSpan) }) { XpCard(user) }
        item { DailyCard(done = day in user.dailyDone, onPlay = { onPlay(DailyChallenge.idFor(day)) }) }
        item {
            if (next != null) ContinueCard(next.first, next.second.title, Worlds.all.indexOf(next.first), next.first.levels.indexOf(next.second) + 1) {
                onPlay(next.second.id)
            } else AllDoneCard()
        }
        item(span = { GridItemSpan(maxLineSpan) }) { SectionTitle("Dunia Petualangan") }
        items(Worlds.all, key = { it.id }) { world ->
            WorldCard(world, user, onClick = { onOpenWorld(world.id) })
        }
    }
}

@Composable
private fun Greeting(user: UserData, day: Long, onOpenSettings: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RobotAvatar(user.profile?.avatar ?: 0, size = 60.dp, costume = user.costume)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Halo, ${user.profile?.name ?: "Teman"}! 👋", style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Siap memecahkan teka-teki hari ini?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        val streak = user.currentStreak(day)
        StatChip(if (streak > 0) "🔥" else "🌱", "$streak")
        IconButton(onClick = onOpenSettings) { Icon(Icons.Rounded.Settings, contentDescription = "Pengaturan") }
    }
}

@Composable
private fun XpCard(user: UserData) {
    val level = user.playerLevel
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFFDB2777))))
                .padding(18.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                        Text("Lv $level", color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(PlayerLevels.title(level), color = Color.White, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.size(12.dp))
                ProgressBar(PlayerLevels.progress(user.xp), color = Color(0xFFFDE047), trackColor = Color.White.copy(alpha = 0.28f))
                Spacer(Modifier.size(8.dp))
                Row {
                    Text("${user.xp} XP", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Text("⭐ ${user.totalStars}   🏅 ${user.achievements.size}", color = Color.White, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun DailyCard(done: Boolean, onPlay: () -> Unit) {
    Card(
        onClick = onPlay,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (done) "✅" else "📅", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Tantangan Harian", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(
                    if (done) "Selesai! Datang lagi besok untuk labirin baru." else "Labirin baru setiap hari • +30 XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
private fun ContinueCard(world: World, title: String, worldIndex: Int, levelNumber: Int, onPlay: () -> Unit) {
    val color = Color(world.colorArgb)
    Card(
        onClick = onPlay,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.14f)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Lanjutkan • Dunia ${worldIndex + 1}-$levelNumber", style = MaterialTheme.typography.labelLarge, color = color)
                Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(world.concept.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledIconButton(
                onClick = onPlay,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = color),
                modifier = Modifier.size(56.dp),
            ) { Icon(Icons.Rounded.PlayArrow, contentDescription = "Main", tint = Color.White, modifier = Modifier.size(32.dp)) }
        }
    }
}

@Composable
private fun AllDoneCard() {
    Card(shape = RoundedCornerShape(24.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🏆", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.width(14.dp))
            Text("Semua level terbuka sudah selesai! Coba raih 3 bintang di setiap level.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun WorldCard(world: World, user: UserData, onClick: () -> Unit) {
    val unlocked = Progression.isWorldUnlocked(world, user)
    val color = Color(world.colorArgb)
    val done = Progression.worldCompleted(world, user)
    val stars = Progression.worldStars(world, user)
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            EmojiBadge(world.emoji, color, locked = !unlocked, size = 60.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Dunia ${world.number} • ${world.concept.label}", style = MaterialTheme.typography.labelMedium, color = if (unlocked) color else MaterialTheme.colorScheme.outline)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(world.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f, fill = false))
                    Text(
                        if (done == world.levels.size) "  ${world.animal.emoji}💖" else "  ${world.animal.emoji}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Spacer(Modifier.size(6.dp))
                if (unlocked) {
                    ProgressBar(done / world.levels.size.toFloat(), color = color, height = 8.dp)
                    Spacer(Modifier.size(4.dp))
                    Row {
                        Text("$done/${world.levels.size} level", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                        Text("★ $stars/${world.levels.size * 3}", style = MaterialTheme.typography.labelSmall, color = StarGold)
                    }
                } else {
                    val pre = Worlds.prerequisite(world)
                    Text(
                        "Selesaikan ${pre?.title ?: "dunia sebelumnya"} untuk membuka",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                    )
                }
            }
        }
    }
}
