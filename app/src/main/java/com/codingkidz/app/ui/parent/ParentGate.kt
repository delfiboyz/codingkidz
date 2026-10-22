package com.codingkidz.app.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codingkidz.app.ui.components.RobotAvatar
import kotlin.random.Random

/**
 * Gerbang orang tua: soal perkalian dua digit yang mudah bagi orang dewasa tetapi
 * menyaring anak kecil, mengikuti praktik umum aplikasi anak.
 */
@Composable
fun ParentGateDialog(onPassed: () -> Unit, onDismiss: () -> Unit) {
    val (a, b) = remember { Random.nextInt(6, 13) to Random.nextInt(11, 20) }
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Khusus orang tua") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Untuk melanjutkan, jawab pertanyaan ini:")
                Text("$a × $b = ?", style = MaterialTheme.typography.headlineMedium)
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it.filter(Char::isDigit).take(4); error = false },
                    singleLine = true,
                    isError = error,
                    supportingText = if (error) ({ Text("Jawaban belum tepat") }) else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (answer.toIntOrNull() == a * b) onPassed() else error = true
            }) { Text("Lanjut") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}

/** Layar istirahat saat batas waktu harian tercapai; menutupi seluruh aplikasi. */
@Composable
fun BreakScreen(limit: Int, onParentUnlock: () -> Unit) {
    var gate by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF4338CA))))
            .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } }
            .safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.widthIn(max = 480.dp).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RobotAvatar(0, size = 140.dp)
            Text("Waktunya istirahat! 😴", style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(
                "Kamu sudah belajar $limit menit hari ini. Hebat! Saatnya mengistirahatkan mata, " +
                    "bermain di luar, atau minum air putih. Sampai jumpa besok!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
            )
            Button(onClick = { gate = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("Orang tua: izinkan lanjut")
            }
        }
    }
    if (gate) {
        ParentGateDialog(onPassed = { gate = false; onParentUnlock() }, onDismiss = { gate = false })
    }
}
