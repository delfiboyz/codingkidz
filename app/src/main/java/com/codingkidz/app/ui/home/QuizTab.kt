package com.codingkidz.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codingkidz.app.content.QuizBank
import com.codingkidz.app.data.UserData

@Composable
fun QuizTab(user: UserData, onStart: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("🧠", style = MaterialTheme.typography.displayLarge)
        Text("Kuis Logika", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Latih otakmu dengan teka-teki pola, arah, perulangan, logika benar–salah, dan berburu bug. " +
                "Setiap kuis berisi 5 soal acak dari ${QuizBank.all.size} soal.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.widthIn(max = 480.dp),
        )
        Row(Modifier.widthIn(max = 480.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuizStat("Kuis dimainkan", "${user.quizSessions}", Modifier.weight(1f))
            val accuracy = if (user.quizAnswered == 0) "–" else "${user.quizCorrect * 100 / user.quizAnswered}%"
            QuizStat("Ketepatan", accuracy, Modifier.weight(1f))
            QuizStat("Skor sempurna", "${user.quizPerfect}", Modifier.weight(1f))
        }
        Button(onClick = onStart, modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().height(56.dp)) {
            Text("Mulai kuis", style = MaterialTheme.typography.titleMedium)
        }
        Text("+5 XP setiap jawaban benar, bonus +10 XP untuk skor sempurna", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuizStat(label: String, value: String, modifier: Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}
