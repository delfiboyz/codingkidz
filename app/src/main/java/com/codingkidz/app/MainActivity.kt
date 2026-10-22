package com.codingkidz.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.codingkidz.app.data.today
import com.codingkidz.app.ui.CodingKidzRoot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val loaded = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splash.setKeepOnScreenCondition { !loaded.value }

        val container = (application as CodingKidzApp).container
        val state = combine(container.userRepository.data, container.settingsRepository.settings) { user, settings ->
            user to settings
        }

        // Hitung waktu bermain (per menit) hanya saat aplikasi benar-benar tampil di layar.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    delay(60_000)
                    container.userRepository.addMinute(today())
                }
            }
        }

        setContent {
            val pair by state.collectAsStateWithLifecycle(initialValue = null)
            val (user, settings) = pair ?: return@setContent
            loaded.value = true
            CodingKidzRoot(container, user, settings)
        }
    }
}
