package com.codingkidz.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.codingkidz.app.data.AgeGroup
import com.codingkidz.app.data.Profile
import com.codingkidz.app.ui.LocalAppContainer
import com.codingkidz.app.ui.LocalFeedback
import com.codingkidz.app.ui.components.AvatarColors
import com.codingkidz.app.ui.components.RobotAvatar
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(editing: Boolean, onDone: () -> Unit, onBack: (() -> Unit)?) {
    val container = LocalAppContainer.current
    val feedback = LocalFeedback.current
    val scope = rememberCoroutineScope()
    var step by rememberSaveable { mutableIntStateOf(if (editing) 1 else 0) }
    var name by rememberSaveable { mutableStateOf("") }
    var avatar by rememberSaveable { mutableIntStateOf(0) }
    var age by rememberSaveable { mutableStateOf(AgeGroup.MIDDLE) }

    LaunchedEffect(editing) {
        if (editing) container.userRepository.current().profile?.let {
            name = it.name
            avatar = it.avatar
            age = it.ageGroup
        }
    }

    fun save() {
        scope.launch {
            val previousAge = container.userRepository.current().profile?.ageGroup
            container.userRepository.saveProfile(Profile(name.trim().ifBlank { "Teman" }, avatar, age))
            if (previousAge != age) {
                // Tampilan blok mengikuti tahap usia: gambar (4–6), kata (7–9), kode (10+).
                container.settingsRepository.update {
                    it.copy(
                        blockMode = age.suggestedBlockMode,
                        readAloud = if (age == AgeGroup.YOUNG) true else it.readAloud,
                        speed = if (age == AgeGroup.YOUNG) com.codingkidz.app.data.RunSpeed.SLOW else it.speed,
                    )
                }
            }
            feedback.success()
            onDone()
        }
    }

    val gradient = Brush.verticalGradient(
        listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background),
    )
    Box(Modifier.fillMaxSize().background(gradient).safeDrawingPadding().imePadding()) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.padding(4.dp)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
            }
        }
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it * dir } + fadeIn()) togetherWith (slideOutHorizontally { -it * dir } + fadeOut())
            },
            label = "onboarding",
            modifier = Modifier.align(Alignment.Center),
        ) { s ->
            Column(
                Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (s) {
                    0 -> {
                        RobotAvatar(avatar, size = 160.dp)
                        Text("Halo! Aku Bito 👋", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                        Text(
                            "Aku robot yang suka berpetualang, tapi aku hanya bisa bergerak kalau kamu memberiku perintah. " +
                                "Yuk belajar coding sambil bermain bersamaku!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Feature("🧩", "Susun blok kode untuk memecahkan teka-teki")
                        Feature("🎨", "Buat karya seni piksel dengan kode")
                        Feature("🏆", "Kumpulkan bintang, XP, dan lencana")
                        Button(onClick = { step = 1 }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                            Text("Ayo mulai!", style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    1 -> {
                        RobotAvatar(avatar, size = 120.dp)
                        Text(
                            if (editing) "Ubah profil" else "Siapa namamu?",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { if (it.length <= 20) name = it },
                            label = { Text("Nama panggilan") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { step = 2 }),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            "Cukup nama panggilan saja. Data hanya tersimpan di perangkat ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text("Berapa umurmu?", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AgeGroup.entries.forEach { g ->
                                FilterChip(
                                    selected = age == g,
                                    onClick = { age = g; feedback.tap() },
                                    label = { Text(g.range) },
                                )
                            }
                        }
                        Button(
                            onClick = { step = 2 },
                            enabled = name.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                        ) { Text("Lanjut", style = MaterialTheme.typography.titleMedium) }
                    }

                    else -> {
                        RobotAvatar(avatar, size = 140.dp, happy = true)
                        Text("Pilih warna Bito-mu", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                        Text(AvatarColors[avatar].second, style = MaterialTheme.typography.titleMedium, color = AvatarColors[avatar].first)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            AvatarColors.forEachIndexed { i, (color, label) ->
                                Box(
                                    Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(color.copy(alpha = 0.18f))
                                        .border(if (avatar == i) 4.dp else 0.dp, color, CircleShape)
                                        .clickable { avatar = i; feedback.tap() }
                                        .semantics {
                                            contentDescription = label
                                            selected = avatar == i
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    RobotAvatar(i, size = 56.dp, animated = false)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = ::save, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                            Text(if (editing) "Simpan" else "Mulai petualangan! 🚀", style = MaterialTheme.typography.titleMedium)
                        }
                        TextButton(onClick = { step = 1 }) { Text("Kembali") }
                    }
                }
            }
        }
    }
}

@Composable
private fun Feature(emoji: String, text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emoji, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.size(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
