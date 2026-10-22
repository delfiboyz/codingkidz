package com.codingkidz.app

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.codingkidz.app.data.AppSettings
import com.codingkidz.app.data.JsonSerializer
import com.codingkidz.app.data.SettingsRepository
import com.codingkidz.app.data.UserData
import com.codingkidz.app.data.UserRepository
import com.codingkidz.app.platform.SoundFx
import com.codingkidz.app.platform.Speaker

/** Dependency injection manual: satu tempat untuk semua objek berumur panjang. */
class AppContainer(context: Context) {
    val userRepository = UserRepository(
        DataStoreFactory.create(JsonSerializer(UserData.serializer(), UserData())) {
            context.dataStoreFile("user.json")
        },
    )
    val settingsRepository = SettingsRepository(
        DataStoreFactory.create(JsonSerializer(AppSettings.serializer(), AppSettings())) {
            context.dataStoreFile("settings.json")
        },
    )
    val sound = SoundFx(context)
    val speaker by lazy { Speaker(context) }
}

class CodingKidzApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
