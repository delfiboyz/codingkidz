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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.codingkidz.app.content.Studio
import com.codingkidz.app.data.UserData
import com.codingkidz.app.ui.components.CanvasThumbnail
import com.codingkidz.app.ui.components.SectionTitle
import java.text.DateFormat
import java.util.Date

@Composable
fun StudioTab(user: UserData, onOpenStudio: (Long?, Int?) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Text("Studio Kreasi", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Buat game, animasi, cerita interaktif, atau lukisan piksel dengan kodemu sendiri!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Card(
                onClick = { onOpenStudio(null, null) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF0D9488), Color(0xFF22C55E), Color(0xFFEAB308))))
                        .padding(20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Karya baru", color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text("Desain peta, pasang tombol, lalu mainkan!", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) { SectionTitle("Contoh untuk dicoba") }
        itemsIndexed(Studio.examples) { i, example ->
            ArtCard(title = "${example.emoji} ${example.title}", subtitle = example.description, program = example.program, map = example.map) {
                onOpenStudio(null, i)
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) { SectionTitle("Galeri Karyaku (${user.creations.size})") }
        if (user.creations.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "Belum ada karya. Buat karya baru lalu tekan Simpan — karyamu akan muncul di sini! 🎨",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(user.creations, key = { it.id }) { creation ->
            ArtCard(
                title = creation.title,
                subtitle = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(creation.createdAt)),
                program = creation.program,
                map = creation.map,
            ) { onOpenStudio(creation.id, null) }
        }
    }
}

@Composable
private fun ArtCard(title: String, subtitle: String, program: com.codingkidz.app.engine.Program, map: List<String>?, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(10.dp)) {
            CanvasThumbnail(program, map, Modifier.fillMaxWidth())
            Spacer(Modifier.size(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
