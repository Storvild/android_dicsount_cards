package com.example.discountcards.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class SortOrder {
    ALPHABETICAL,
    FREQUENCY
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val sortOrder: SortOrder = SortOrder.ALPHABETICAL,
    val cardBrightnessPercent: Int = 100,
    val showAddButton: Boolean = true
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val SORT_ORDER = intPreferencesKey("sort_order")
        val CARD_BRIGHTNESS = intPreferencesKey("card_brightness")
        val SHOW_ADD_BUTTON = booleanPreferencesKey("show_add_button")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val themeOrdinal = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.ordinal
        val sortOrdinal = preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.ALPHABETICAL.ordinal
        val brightness = preferences[PreferencesKeys.CARD_BRIGHTNESS] ?: 100
        val showAddButton = preferences[PreferencesKeys.SHOW_ADD_BUTTON] ?: true

        UserPreferences(
            themeMode = ThemeMode.entries.getOrElse(themeOrdinal) { ThemeMode.SYSTEM },
            sortOrder = SortOrder.entries.getOrElse(sortOrdinal) { SortOrder.ALPHABETICAL },
            cardBrightnessPercent = brightness.coerceIn(0, 100),
            showAddButton = showAddButton
        )
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.ordinal
        }
    }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.ordinal
        }
    }

    suspend fun updateCardBrightnessPercent(percent: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CARD_BRIGHTNESS] = percent.coerceIn(0, 100)
        }
    }

    suspend fun updateShowAddButton(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_ADD_BUTTON] = show
        }
    }
}
