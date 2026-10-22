package com.codingkidz.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.codingkidz.app.AppContainer
import com.codingkidz.app.data.Achievement
import com.codingkidz.app.data.AppSettings
import com.codingkidz.app.data.UserData
import com.codingkidz.app.data.today
import com.codingkidz.app.platform.Sfx
import com.codingkidz.app.ui.achievements.AchievementsScreen
import com.codingkidz.app.ui.game.GameScreen
import com.codingkidz.app.ui.home.HomeScreen
import com.codingkidz.app.ui.navigation.Route
import com.codingkidz.app.ui.onboarding.ProfileSetupScreen
import com.codingkidz.app.ui.parent.BreakScreen
import com.codingkidz.app.ui.parent.ParentScreen
import com.codingkidz.app.ui.quiz.QuizScreen
import com.codingkidz.app.ui.settings.SettingsScreen
import com.codingkidz.app.ui.studio.StudioScreen
import com.codingkidz.app.ui.theme.CodingKidzTheme
import com.codingkidz.app.ui.worlds.WorldMapScreen
import kotlinx.coroutines.delay

@Composable
fun CodingKidzRoot(container: AppContainer, user: UserData, settings: AppSettings) {
    CodingKidzTheme(themeMode = settings.theme, dynamicColor = settings.dynamicColor) {
        val feedback = rememberFeedback(container.sound, settings)
        CompositionLocalProvider(
            LocalAppContainer provides container,
            LocalSettings provides settings,
            LocalFeedback provides feedback,
        ) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                val start: Route = if (user.profile == null) Route.ProfileSetup() else Route.Main
                val backStack = rememberNavBackStack(start)
                AppNavigation(backStack, user)
                AchievementBanner(container)

                var parentUnlocked by remember { mutableStateOf(false) }
                val limit = settings.dailyLimitMinutes
                val overLimit = limit > 0 && user.minutesOn(today()) >= limit && !parentUnlocked
                if (overLimit && user.profile != null) {
                    BreakScreen(limit = limit, onParentUnlock = { parentUnlocked = true })
                }
            }
        }
    }
}

@Composable
private fun AppNavigation(backStack: NavBackStack<NavKey>, user: UserData) {
    val avatar = user.profile?.avatar ?: 0
    val costume = user.costume
    fun push(route: Route) {
        backStack.add(route)
    }
    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    NavDisplay(
        backStack = backStack,
        onBack = { pop() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        transitionSpec = {
            (slideInHorizontally(tween(320)) { it / 3 } + fadeIn(tween(320))) togetherWith
                (slideOutHorizontally(tween(320)) { -it / 4 } + fadeOut(tween(200)))
        },
        popTransitionSpec = {
            (slideInHorizontally(tween(320)) { -it / 4 } + fadeIn(tween(320))) togetherWith
                (slideOutHorizontally(tween(320)) { it / 3 } + fadeOut(tween(200)))
        },
        predictivePopTransitionSpec = {
            (slideInHorizontally(tween(320)) { -it / 4 } + fadeIn(tween(320))) togetherWith
                (slideOutHorizontally(tween(320)) { it / 3 } + fadeOut(tween(200)))
        },
        entryProvider = entryProvider {
            entry<Route.ProfileSetup> { key ->
                ProfileSetupScreen(
                    editing = key.editing,
                    onDone = {
                        if (key.editing) pop() else {
                            backStack.clear()
                            backStack.add(Route.Main)
                        }
                    },
                    onBack = if (key.editing) ({ pop() }) else null,
                )
            }
            entry<Route.Main> {
                HomeScreen(
                    user = user,
                    onOpenWorld = { push(Route.WorldMap(it)) },
                    onPlay = { push(Route.Game(it)) },
                    onOpenStudio = { id, example -> push(Route.Studio(id, example)) },
                    onStartQuiz = { push(Route.Quiz) },
                    onOpenAchievements = { push(Route.Achievements) },
                    onOpenParent = { push(Route.Parent) },
                    onOpenSettings = { push(Route.Settings) },
                    onEditProfile = { push(Route.ProfileSetup(editing = true)) },
                )
            }
            entry<Route.WorldMap> { key ->
                WorldMapScreen(worldId = key.worldId, user = user, onBack = { pop() }, onPlay = { push(Route.Game(it)) })
            }
            entry<Route.Game> { key ->
                GameScreen(
                    levelId = key.levelId,
                    avatar = avatar,
                    costume = costume,
                    onBack = { pop() },
                    onNext = { next ->
                        pop()
                        push(Route.Game(next))
                    },
                )
            }
            entry<Route.Studio> { key ->
                StudioScreen(creationId = key.creationId, exampleIndex = key.exampleIndex, avatar = avatar, costume = costume, onBack = { pop() })
            }
            entry<Route.Quiz> { QuizScreen(avatar = avatar, onBack = { pop() }) }
            entry<Route.Achievements> { AchievementsScreen(user = user, onBack = { pop() }) }
            entry<Route.Parent> { ParentScreen(user = user, onBack = { pop() }) }
            entry<Route.Settings> {
                SettingsScreen(
                    onBack = { pop() },
                    onEditProfile = { push(Route.ProfileSetup(editing = true)) },
                    onOpenParent = { push(Route.Parent) },
                )
            }
        },
    )
}

/** Banner lencana yang muncul dari atas di layar mana pun. */
@Composable
private fun AchievementBanner(container: AppContainer) {
    var current by remember { mutableStateOf<Achievement?>(null) }
    var visible by remember { mutableStateOf(false) }
    val feedback = LocalFeedback.current
    LaunchedEffect(container) {
        container.userRepository.achievementEvents.collect { achievement ->
            current = achievement
            visible = true
            feedback.play(Sfx.UNLOCK)
            delay(3200)
            visible = false
            delay(400)
        }
    }
    Box(Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp), contentAlignment = Alignment.TopCenter) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
        ) {
            current?.let { a ->
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shadowElevation = 12.dp,
                ) {
                    Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(a.emoji, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Lencana baru terbuka!", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            Text(a.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }
            }
        }
    }
}
