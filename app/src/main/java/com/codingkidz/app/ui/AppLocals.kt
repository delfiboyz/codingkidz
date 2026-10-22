package com.codingkidz.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.codingkidz.app.AppContainer
import com.codingkidz.app.data.AppSettings
import com.codingkidz.app.platform.Sfx
import com.codingkidz.app.platform.SoundFx

val LocalAppContainer = staticCompositionLocalOf<AppContainer> { error("AppContainer belum disediakan") }
val LocalSettings = staticCompositionLocalOf { AppSettings() }
val LocalFeedback = staticCompositionLocalOf<Feedback> { error("Feedback belum disediakan") }

/** Suara + getaran dalam satu panggilan, mengikuti pengaturan anak/orang tua. */
@Stable
class Feedback(
    private val sound: SoundFx,
    private val haptics: HapticFeedback,
    private val hapticsEnabled: Boolean,
) {
    fun tap() = emit(Sfx.TAP, HapticFeedbackType.KeyboardTap)
    fun play(sfx: Sfx) = emit(sfx, null)
    fun success() = emit(Sfx.SUCCESS, HapticFeedbackType.Confirm)
    fun fail() = emit(Sfx.FAIL, HapticFeedbackType.Reject)
    fun bump() = emit(Sfx.BUMP, HapticFeedbackType.Reject)

    fun emit(sfx: Sfx?, haptic: HapticFeedbackType?) {
        sfx?.let { sound.play(it) }
        if (hapticsEnabled && haptic != null) haptics.performHapticFeedback(haptic)
    }
}

@Composable
fun rememberFeedback(sound: SoundFx, settings: AppSettings): Feedback {
    val haptics = LocalHapticFeedback.current
    sound.enabled = settings.sound
    return remember(sound, haptics, settings.haptics) { Feedback(sound, haptics, settings.haptics) }
}
