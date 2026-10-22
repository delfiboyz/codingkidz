package com.codingkidz.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codingkidz.app.content.Worlds
import com.codingkidz.app.data.Achievements
import com.codingkidz.app.data.PlayerLevels
import com.codingkidz.app.data.Rewards
import kotlinx.coroutines.launch
import com.codingkidz.app.data.UserData
import com.codingkidz.app.data.today
import com.codingkidz.app.ui.components.EmojiBadge
import com.codingkidz.app.ui.components.ProgressBar
import com.codingkidz.app.ui.components.RobotAvatar
import com.codingkidz.app.ui.components.SectionTitle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileTab(
    user: UserData,
    onOpenAchievements: () -> Unit,
    onOpenParent: () -> Unit,
    onOpenSettings: () -> Unit,
    onEditProfile: () -> Unit,
) {
    val container = com.codingkidz.app.ui.LocalAppContainer.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val onCostume: (String?) -> Unit = { id -> scope.launch { container.userRepository.setCostume(id) } }
    val profile = user.profile
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onOpenSettings) { Icon(Icons.Rounded.Settings, contentDescription = "Pengaturan") }
        }
        RobotAvatar(profile?.avatar ?: 0, size = 120.dp, costume = user.costume)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(profile?.name ?: "Teman", style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = onEditProfile) { Icon(Icons.Rounded.Edit, contentDescription = "Ubah profil") }
        }
        Text(
            "Level ${user.playerLevel} • ${PlayerLevels.title(user.playerLevel)}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        ProgressBar(PlayerLevels.progress(user.xp), Modifier.widthIn(max = 420.dp).fillMaxWidth())
        Text(
            "${user.xp} XP • ${PlayerLevels.xpForLevel(user.playerLevel + 1) - user.xp} XP lagi menuju level ${user.playerLevel + 1}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Stat("⭐", "${user.totalStars}", "bintang")
            Stat("🧩", "${user.levels.size}/${Worlds.allLevels.size}", "level")
            Stat("🔥", "${user.currentStreak(today())}", "hari beruntun")
            Stat("🏅", "${user.achievements.size}/${Achievements.all.size}", "lencana")
            Stat("📅", "${user.dailyDone.size}", "tantangan harian")
            Stat("🎨", "${user.creations.size}", "karya")
        }

        SectionTitle("Teman Bito")
        Text(
            "Hewan yang sudah kamu selamatkan di setiap dunia.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Worlds.all.forEach { w ->
                val saved = Rewards.worldDone(w, user)
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (saved) Color(w.colorArgb).copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                ) {
                    Column(Modifier.width(96.dp).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (saved) w.animal.emoji else "❔", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            if (saved) w.animal.name.substringBefore(" si") else w.title,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
                    }
                }
            }
        }

        SectionTitle("Lemari Kostum")
        Text(
            "Kostum terbuka saat kamu menguasai konsep baru. Ketuk untuk memakai.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Rewards.costumes.forEach { c ->
                val unlocked = c.isUnlocked(user)
                val wearing = user.costume == c.id
                Card(
                    onClick = { if (unlocked) onCostume(if (wearing) null else c.id) },
                    enabled = unlocked,
                    shape = RoundedCornerShape(18.dp),
                    border = if (wearing) androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    Column(Modifier.width(96.dp).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (unlocked) RobotAvatar(user.profile?.avatar ?: 0, size = 56.dp, animated = false, costume = c.id)
                        else Text("🔒", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(8.dp))
                        Text(c.name, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, maxLines = 2)
                        if (!unlocked) {
                            Text(c.requirement, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center, maxLines = 3)
                        } else if (wearing) {
                            Text("Dipakai", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        SectionTitle("Lencana") {
            TextButton(onClick = onOpenAchievements) { Text("Lihat semua") }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Achievements.all.take(10).forEach { a ->
                EmojiBadge(a.emoji, MaterialTheme.colorScheme.tertiary, locked = a.id !in user.achievements)
            }
        }

        Spacer(Modifier.size(4.dp))
        Card(
            onClick = onOpenParent,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.fillMaxWidth(),
        ) {
            ListItem(
                headlineContent = { Text("Area Orang Tua") },
                supportingContent = { Text("Laporan belajar, batas waktu layar, dan pengaturan lain") },
                leadingContent = { Icon(Icons.Rounded.FamilyRestroom, null, tint = Color(0xFF0D9488)) },
                colors = androidx.compose.material3.ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
    }
}

@Composable
private fun Stat(emoji: String, value: String, label: String) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.width(100.dp).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Text(value, style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
