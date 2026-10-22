package com.codingkidz.app.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Semua tujuan navigasi (Navigation 3, kunci yang bisa diserialisasi). */
@Serializable
sealed interface Route : NavKey {
    @Serializable
    data class ProfileSetup(val editing: Boolean = false) : Route

    @Serializable
    data object Main : Route

    @Serializable
    data class WorldMap(val worldId: String) : Route

    @Serializable
    data class Game(val levelId: String) : Route

    @Serializable
    data class Studio(val creationId: Long? = null, val exampleIndex: Int? = null) : Route

    @Serializable
    data object Quiz : Route

    @Serializable
    data object Achievements : Route

    @Serializable
    data object Parent : Route

    @Serializable
    data object Settings : Route
}
