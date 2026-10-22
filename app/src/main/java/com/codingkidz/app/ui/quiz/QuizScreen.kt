package com.codingkidz.app.ui.quiz

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codingkidz.app.content.Question
import com.codingkidz.app.content.QuizBank
import com.codingkidz.app.data.Reward
import com.codingkidz.app.data.UserRepository
import com.codingkidz.app.platform.Sfx
import com.codingkidz.app.ui.LocalAppContainer
import com.codingkidz.app.ui.LocalFeedback
import com.codingkidz.app.ui.components.ConfettiOverlay
import com.codingkidz.app.ui.components.ProgressBar
import com.codingkidz.app.ui.components.RobotAvatar
import com.codingkidz.app.ui.theme.CodeTextStyle
import com.codingkidz.app.ui.theme.LocalGameColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuizUi(
    val questions: List<Question>,
    val index: Int = 0,
    val selected: Int? = null,
    val correct: Int = 0,
    val reward: Reward? = null,
    val finished: Boolean = false,
) {
    val current: Question get() = questions[index]
}

class QuizViewModel(private val users: UserRepository) : ViewModel() {
    private val _ui = MutableStateFlow(QuizUi(QuizBank.session()))
    val ui = _ui.asStateFlow()

    fun answer(option: Int): Boolean {
        val s = _ui.value
        if (s.selected != null) return false
        val right = option == s.current.answer
        _ui.update { it.copy(selected = option, correct = it.correct + if (right) 1 else 0) }
        return right
    }

    fun next() {
        val s = _ui.value
        if (s.index + 1 < s.questions.size) {
            _ui.update { it.copy(index = it.index + 1, selected = null) }
        } else {
            viewModelScope.launch {
                val reward = users.recordQuiz(s.correct, s.questions.size)
                _ui.update { it.copy(finished = true, reward = reward) }
            }
        }
    }

    fun restart() {
        _ui.value = QuizUi(QuizBank.session())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(avatar: Int, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val vm: QuizViewModel = viewModel { QuizViewModel(container.userRepository) }
    val ui by vm.ui.collectAsStateWithLifecycle()
    val feedback = LocalFeedback.current

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (!ui.finished) {
                            ProgressBar((ui.index + if (ui.selected != null) 1 else 0) / ui.questions.size.toFloat(), Modifier.fillMaxWidth().padding(end = 16.dp))
                        } else Text("Hasil Kuis")
                    },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.Close, contentDescription = "Tutup kuis") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                )
            },
        ) { padding ->
            AnimatedContent(
                targetState = if (ui.finished) -1 else ui.index,
                transitionSpec = { (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut()) },
                label = "question",
                modifier = Modifier.padding(padding).fillMaxSize(),
            ) { idx ->
                if (idx == -1) {
                    QuizResult(ui, avatar, onRestart = vm::restart, onBack = onBack)
                } else {
                    QuestionView(
                        question = ui.questions[idx],
                        number = idx + 1,
                        total = ui.questions.size,
                        selected = ui.selected,
                        onAnswer = { option ->
                            if (vm.answer(option)) feedback.emit(Sfx.CORRECT, androidx.compose.ui.hapticfeedback.HapticFeedbackType.Confirm)
                            else feedback.emit(Sfx.WRONG, androidx.compose.ui.hapticfeedback.HapticFeedbackType.Reject)
                        },
                        onNext = vm::next,
                    )
                }
            }
        }
        ConfettiOverlay(if (ui.finished && ui.correct == ui.questions.size) 1 else 0)
    }
}

@Composable
private fun QuestionView(
    question: Question,
    number: Int,
    total: Int,
    selected: Int?,
    onAnswer: (Int) -> Unit,
    onNext: () -> Unit,
) {
    val game = LocalGameColors.current
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            "Soal $number dari $total • ${question.concept.label}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            question.prompt,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 560.dp),
        )
        question.code?.let { code ->
            Box(
                Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(game.codeBackground)
                    .padding(16.dp),
            ) {
                Text(code, style = CodeTextStyle.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize), color = game.codePlain)
            }
        }
        Column(Modifier.widthIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            question.options.forEachIndexed { i, option ->
                val isAnswer = i == question.answer
                val target = when {
                    selected == null -> MaterialTheme.colorScheme.surfaceContainerHigh
                    isAnswer -> Color(0xFFBBF7D0)
                    i == selected -> Color(0xFFFECACA)
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                }
                val color by animateColorAsState(target, label = "option")
                Surface(
                    onClick = { onAnswer(i) },
                    enabled = selected == null,
                    shape = RoundedCornerShape(18.dp),
                    color = color,
                    border = BorderStroke(2.dp, if (selected != null && isAnswer) Color(0xFF16A34A) else Color.Transparent),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${'A' + i}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            option,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selected != null && (isAnswer || i == selected)) Color(0xFF1B1B2F) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        if (selected != null && isAnswer) Text("✅")
                        if (selected == i && !isAnswer) Text("❌")
                    }
                }
            }
        }
        AnimatedVisibility(selected != null) {
            Column(Modifier.widthIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        (if (selected == question.answer) "Benar! " else "Belum tepat. ") + question.explanation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(14.dp),
                    )
                }
                Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                    Text(if (number < total) "Lanjut" else "Lihat hasil", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun QuizResult(ui: QuizUi, avatar: Int, onRestart: () -> Unit, onBack: () -> Unit) {
    val perfect = ui.correct == ui.questions.size
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RobotAvatar(avatar, size = 140.dp, happy = ui.correct * 2 >= ui.questions.size)
        Text(
            when {
                perfect -> "Sempurna! 🧠"
                ui.correct * 2 >= ui.questions.size -> "Kerja bagus!"
                else -> "Terus berlatih!"
            },
            style = MaterialTheme.typography.headlineLarge,
        )
        Text("${ui.correct} dari ${ui.questions.size} benar", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        ui.reward?.let { r ->
            if (r.xpGained > 0) Text("+${r.xpGained} XP", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
            r.leveledUpTo?.let { Text("🎉 Naik ke level $it!", style = MaterialTheme.typography.titleMedium) }
        }
        Button(onClick = onRestart, modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().height(54.dp)) { Text("Main lagi") }
        OutlinedButton(onClick = onBack, modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth()) { Text("Kembali") }
    }
}
