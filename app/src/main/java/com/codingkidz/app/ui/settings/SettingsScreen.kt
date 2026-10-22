package com.codingkidz.app.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.selection.toggleable
import com.codingkidz.app.BuildConfig
import com.codingkidz.app.data.AppSettings
import com.codingkidz.app.data.BlockMode
import com.codingkidz.app.engine.CodeLanguage
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Extension
import com.codingkidz.app.data.RunSpeed
import com.codingkidz.app.data.ThemeMode
import com.codingkidz.app.ui.LocalAppContainer
import com.codingkidz.app.ui.LocalSettings
import com.codingkidz.app.ui.components.SectionTitle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, onEditProfile: () -> Unit, onOpenParent: () -> Unit) {
    val container = LocalAppContainer.current
    val settings = LocalSettings.current
    val scope = rememberCoroutineScope()
    fun update(transform: (AppSettings) -> AppSettings) {
        scope.launch { container.settingsRepository.update(transform) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(Modifier.widthIn(max = 640.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Suara & sentuhan")
                Card(shape = RoundedCornerShape(20.dp)) {
                    ToggleItem(Icons.AutoMirrored.Rounded.VolumeUp, "Efek suara", "Bunyi saat Bito bergerak dan berhasil", settings.sound) { on -> update { it.copy(sound = on) } }
                    ToggleItem(Icons.Rounded.Vibration, "Getaran", "Getar halus saat menekan tombol atau menabrak", settings.haptics) { on -> update { it.copy(haptics = on) } }
                    ToggleItem(Icons.Rounded.RecordVoiceOver, "Bacakan instruksi", "Instruksi level dibacakan otomatis (cocok untuk 5–7 tahun)", settings.readAloud) { on -> update { it.copy(readAloud = on) } }
                }

                SectionTitle("Cara belajar")
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Label(Icons.Rounded.Extension, "Tampilan blok")
                        Text(
                            "Mulai dari gambar, lalu kata, lalu kode teks sungguhan — naikkan tahapnya saat anak siap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        BlockMode.entries.forEach { m ->
                            FilterChip(
                                selected = settings.blockMode == m,
                                onClick = { update { it.copy(blockMode = m) } },
                                label = {
                                    Column(Modifier.padding(vertical = 6.dp)) {
                                        Text(m.label, style = MaterialTheme.typography.titleSmall)
                                        Text(m.description, style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Label(Icons.Rounded.Code, "Bahasa kode teks")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CodeLanguage.entries.forEach { lang ->
                                FilterChip(selected = settings.codeLanguage == lang, onClick = { update { it.copy(codeLanguage = lang) } }, label = { Text(lang.label) })
                            }
                        }
                    }
                }

                SectionTitle("Tampilan")
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Label(Icons.Rounded.DarkMode, "Tema")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemeMode.entries.forEach { mode ->
                                FilterChip(selected = settings.theme == mode, onClick = { update { it.copy(theme = mode) } }, label = { Text(mode.label) })
                            }
                        }
                        Label(Icons.Rounded.Speed, "Kecepatan animasi robot")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RunSpeed.entries.forEach { s ->
                                FilterChip(selected = settings.speed == s, onClick = { update { it.copy(speed = s) } }, label = { Text(s.label) })
                            }
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ToggleItem(Icons.Rounded.Palette, "Warna dinamis", "Ikuti warna wallpaper (Material You)", settings.dynamicColor) { on -> update { it.copy(dynamicColor = on) } }
                    }
                }

                SectionTitle("Akun")
                Card(shape = RoundedCornerShape(20.dp)) {
                    LinkItem(Icons.Rounded.Edit, "Ubah profil", "Nama, umur, dan warna Bito", onEditProfile)
                    LinkItem(Icons.Rounded.FamilyRestroom, "Area Orang Tua", "Laporan, batas waktu, pengingat", onOpenParent)
                }

                SectionTitle("Tentang")
                Card(shape = RoundedCornerShape(20.dp)) {
                    ListItem(
                        headlineContent = { Text("CodingKidz ${BuildConfig.VERSION_NAME}") },
                        supportingContent = {
                            Text("Belajar berpikir komputasional: urutan, perulangan, kondisi, fungsi, dan debugging. Tanpa iklan dan berfungsi sepenuhnya offline.")
                        },
                        leadingContent = { Icon(Icons.Rounded.Info, null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }
        }
    }
}

@Composable
private fun Label(icon: ImageVector, text: String) {
    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text("  $text", style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun ToggleItem(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
    )
}

@Composable
private fun LinkItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.Transparent)) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(subtitle) },
            leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
            trailingContent = { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}
