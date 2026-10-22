package com.codingkidz.app.ui.parent

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.codingkidz.app.content.Worlds
import com.codingkidz.app.data.Progression
import com.codingkidz.app.data.UserData
import com.codingkidz.app.data.today
import com.codingkidz.app.platform.Reminders
import com.codingkidz.app.ui.LocalAppContainer
import com.codingkidz.app.ui.LocalSettings
import com.codingkidz.app.ui.components.ProgressBar
import com.codingkidz.app.ui.components.SectionTitle
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentScreen(user: UserData, onBack: () -> Unit) {
    var passed by rememberSaveable { mutableStateOf(false) }
    if (!passed) {
        ParentGateDialog(onPassed = { passed = true }, onDismiss = onBack)
        return
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Area Orang Tua") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali") } },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.widthIn(max = 720.dp).align(Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Summary(user)
                WeeklyChart(user)
                ConceptProgress(user)
                Insights(user)
                Controls(user)
                Tips()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Summary(user: UserData) {
    SectionTitle("Ringkasan ${user.profile?.name ?: ""}".trim())
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        maxItemsInEachRow = 2,
    ) {
        SummaryCard("Waktu belajar total", formatMinutes(user.totalMinutes))
        SummaryCard("Hari ini", formatMinutes(user.minutesOn(today())))
        SummaryCard("Level selesai", "${user.levels.size}/${Worlds.allLevels.size}")
        SummaryCard("Bintang", "${user.totalStars}/${Worlds.allLevels.size * 3}")
        SummaryCard("Akurasi kuis", if (user.quizAnswered == 0) "–" else "${user.quizCorrect * 100 / user.quizAnswered}%")
        SummaryCard("Runtutan terbaik", "${user.bestStreak} hari")
        SummaryCard("Program dijalankan", "${user.programRuns}×")
        SummaryCard("Petunjuk dipakai", "${user.hintsUsed}×")
    }
}

@Composable
private fun androidx.compose.foundation.layout.FlowRowScope.SummaryCard(label: String, value: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.weight(1f),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun WeeklyChart(user: UserData) {
    val todayDay = today()
    val days = (6 downTo 0).map { todayDay - it }
    val minutes = days.map { user.minutesOn(it) }
    val max = maxOf(minutes.max(), 10)
    val barColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    SectionTitle("7 hari terakhir")
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .semantics { contentDescription = "Menit belajar per hari: " + minutes.joinToString() },
            ) {
                val slot = size.width / days.size
                val barW = slot * 0.5f
                minutes.forEachIndexed { i, m ->
                    val x = slot * i + (slot - barW) / 2
                    drawRoundRect(trackColor, Offset(x, 0f), Size(barW, size.height), CornerRadius(barW / 2))
                    val h = size.height * m / max
                    if (h > 0) drawRoundRect(barColor, Offset(x, size.height - h), Size(barW, h), CornerRadius(barW / 2))
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                days.forEachIndexed { i, d ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${minutes[i]}m", style = MaterialTheme.typography.labelSmall)
                        Text(
                            LocalDate.ofEpochDay(d).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("id")),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConceptProgress(user: UserData) {
    SectionTitle("Penguasaan konsep")
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Worlds.all.forEach { world ->
                val done = Progression.worldCompleted(world, user)
                Column {
                    Row {
                        Text("${world.emoji} ${world.concept.label}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        Text("$done/${world.levels.size} • ★${Progression.worldStars(world, user)}", style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.height(4.dp))
                    ProgressBar(done / world.levels.size.toFloat(), color = Color(world.colorArgb), height = 8.dp)
                }
            }
        }
    }
}

@Composable
private fun Insights(user: UserData) {
    val hardest = user.attempts.entries
        .filter { it.value >= 4 }
        .sortedByDescending { it.value }
        .take(3)
        .mapNotNull { (id, n) -> Worlds.level(id)?.let { it to n } }
    if (hardest.isEmpty()) return
    SectionTitle("Level yang menantang")
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(vertical = 8.dp)) {
            hardest.forEach { (level, n) ->
                ListItem(
                    headlineContent = { Text(level.title) },
                    supportingContent = {
                        Text(if (level.id in user.levels) "Berhasil setelah $n percobaan — tanda ketekunan!" else "$n percobaan, belum selesai. Mungkin bisa dibantu bersama?")
                    },
                    leadingContent = { Text(Worlds.worldOf(level.id)?.emoji ?: "🧩", style = MaterialTheme.typography.titleLarge) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Controls(user: UserData) {
    val container = LocalAppContainer.current
    val settings = LocalSettings.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            scope.launch { container.settingsRepository.update { it.copy(reminder = true) } }
            Reminders.schedule(context, settings.reminderHour)
        }
    }

    SectionTitle("Kontrol")
    Card(shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Batas waktu layar per hari", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 15, 30, 45, 60, 90).forEach { m ->
                    FilterChip(
                        selected = settings.dailyLimitMinutes == m,
                        onClick = { scope.launch { container.settingsRepository.update { it.copy(dailyLimitMinutes = m) } } },
                        label = { Text(if (m == 0) "Tanpa batas" else "$m menit") },
                    )
                }
            }
            SwitchRow(
                title = "Pengingat latihan harian",
                subtitle = "Notifikasi lembut pukul ${settings.reminderHour}.00 jika belum bermain hari itu",
                checked = settings.reminder,
                onChange = { on ->
                    if (on) {
                        if (Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        else {
                            scope.launch { container.settingsRepository.update { it.copy(reminder = true) } }
                            Reminders.schedule(context, settings.reminderHour)
                        }
                    } else {
                        scope.launch { container.settingsRepository.update { it.copy(reminder = false) } }
                        Reminders.cancel(context)
                    }
                },
            )
            if (settings.reminder) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(7, 15, 17, 19).forEach { h ->
                        FilterChip(
                            selected = settings.reminderHour == h,
                            onClick = {
                                scope.launch { container.settingsRepository.update { it.copy(reminderHour = h) } }
                                Reminders.schedule(context, h)
                            },
                            label = { Text("%02d.00".format(h)) },
                        )
                    }
                }
            }
            SwitchRow(
                title = "Buka semua level",
                subtitle = "Untuk guru atau anak yang sudah mahir",
                checked = user.unlockAll,
                onChange = { on -> scope.launch { container.userRepository.setUnlockAll(on) } },
            )
            OutlinedButton(onClick = { confirmReset = true }) { Text("Reset progres belajar") }
        }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset progres?") },
            text = { Text("Semua bintang, XP, lencana, karya, dan riwayat akan dihapus. Profil anak tetap disimpan. Tindakan ini tidak bisa dibatalkan.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    scope.launch { container.userRepository.resetProgress() }
                }) { Text("Reset", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Batal") } },
        )
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun Tips() {
    SectionTitle("Tips mendampingi")
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "Tanyakan \"Apa yang akan dilakukan Bito?\" sebelum menekan Jalankan — ini melatih prediksi.",
                "Rayakan kesalahan: menemukan bug adalah bagian penting dari belajar coding.",
                "Gunakan tombol Langkah untuk menelusuri program bersama, satu blok setiap kali.",
                "Ajak anak menjelaskan programnya dengan kata-kata sendiri.",
                "Semua data tersimpan di perangkat ini saja; tidak ada iklan, akun, atau pelacakan.",
            ).forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer) }
        }
    }
}

private fun formatMinutes(m: Int): String = if (m < 60) "$m menit" else "${m / 60} j ${m % 60} m"
