package com.codingkidz.app.data

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val store: DataStore<AppSettings>) {
    val settings: Flow<AppSettings> = store.data

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        store.updateData(transform)
    }
}
