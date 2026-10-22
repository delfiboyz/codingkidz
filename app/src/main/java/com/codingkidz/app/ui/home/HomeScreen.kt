package com.codingkidz.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.codingkidz.app.data.UserData
import com.codingkidz.app.ui.LocalFeedback

private data class Tab(val label: String, val icon: ImageVector)

private val Tabs = listOf(
    Tab("Belajar", Icons.Rounded.Map),
    Tab("Kreasi", Icons.Rounded.Palette),
    Tab("Kuis", Icons.Rounded.Quiz),
    Tab("Profil", Icons.Rounded.Person),
)

/** Beranda dengan empat tab; memakai rail navigasi di tablet dan bar bawah di ponsel. */
@Composable
fun HomeScreen(
    user: UserData,
    onOpenWorld: (String) -> Unit,
    onPlay: (String) -> Unit,
    onOpenStudio: (Long?, Int?) -> Unit,
    onStartQuiz: () -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenParent: () -> Unit,
    onOpenSettings: () -> Unit,
    onEditProfile: () -> Unit,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val feedback = LocalFeedback.current

    val content: @Composable (Modifier) -> Unit = { modifier ->
        AnimatedContent(
            targetState = tab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tab",
            modifier = modifier,
        ) { t ->
            when (t) {
                0 -> LearnTab(user, onOpenWorld, onPlay, onOpenSettings)
                1 -> StudioTab(user, onOpenStudio)
                2 -> QuizTab(user, onStartQuiz)
                else -> ProfileTab(user, onOpenAchievements, onOpenParent, onOpenSettings, onEditProfile)
            }
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 600.dp) {
            Scaffold { padding ->
                Row(Modifier.padding(padding).fillMaxSize()) {
                    NavigationRail {
                        Tabs.forEachIndexed { i, t ->
                            NavigationRailItem(
                                selected = tab == i,
                                onClick = { tab = i; feedback.tap() },
                                icon = { Icon(t.icon, null) },
                                label = { Text(t.label) },
                            )
                        }
                    }
                    content(Modifier.weight(1f))
                }
            }
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        Tabs.forEachIndexed { i, t ->
                            NavigationBarItem(
                                selected = tab == i,
                                onClick = { tab = i; feedback.tap() },
                                icon = { Icon(t.icon, null) },
                                label = { Text(t.label) },
                            )
                        }
                    }
                },
            ) { padding ->
                Box(Modifier.padding(padding).fillMaxSize()) { content(Modifier.fillMaxSize()) }
            }
        }
    }
}
