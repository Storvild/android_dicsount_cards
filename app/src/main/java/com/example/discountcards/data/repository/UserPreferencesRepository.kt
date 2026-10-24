package com.example.discountcards.data.repository

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class SortOrder {
    ALPHABETICAL,
    FREQUENCY,
    CREATION_DATE
}

enum class AppLanguage {
    SYSTEM,
    ENGLISH,
    RUSSIAN,
    FRENCH,
    GERMAN,
    JAPANESE,
    SPANISH,
    PORTUGUESE,
    HINDI,
    ARABIC,
    TURKISH,
    CHINESE_SIMPLIFIED;

    companion object {
        fun fromOrdinal(ordinal: Int): AppLanguage =
            entries.getOrElse(ordinal) { SYSTEM }
    }
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val sortOrder: SortOrder = SortOrder.ALPHABETICAL,
    val cardBrightnessPercent: Int = 0,
    val showAddButton: Boolean = true,
    val showBarcodesInList: Boolean = true,
    val appLanguage: AppLanguage = AppLanguage.SYSTEM
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

open class UserPreferencesRepository(private val context: Context? = null) {

    private object PreferencesKeys {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val SORT_ORDER = intPreferencesKey("sort_order")
        val CARD_BRIGHTNESS = intPreferencesKey("card_brightness")
        val SHOW_ADD_BUTTON = booleanPreferencesKey("show_add_button")
        val SHOW_BARCODES_IN_LIST = booleanPreferencesKey("show_barcodes_in_list")
        val APP_LANGUAGE = intPreferencesKey("app_language")
    }

    private val sharedPreferences: SharedPreferences?
        get() = context?.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    open fun getAppLanguageSynchronously(): AppLanguage {
        val ordinal = sharedPreferences?.getInt("app_language", AppLanguage.SYSTEM.ordinal)
            ?: AppLanguage.SYSTEM.ordinal
        return AppLanguage.fromOrdinal(ordinal)
    }

    open val userPreferencesFlow: Flow<UserPreferences> = context?.dataStore?.data?.map { preferences ->
        val themeOrdinal = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.ordinal
        val sortOrdinal = preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.ALPHABETICAL.ordinal
        val brightness = preferences[PreferencesKeys.CARD_BRIGHTNESS] ?: 0
        val showAddButton = preferences[PreferencesKeys.SHOW_ADD_BUTTON] ?: true
        val showBarcodesInList = preferences[PreferencesKeys.SHOW_BARCODES_IN_LIST] ?: true
        val languageOrdinal = preferences[PreferencesKeys.APP_LANGUAGE] ?: AppLanguage.SYSTEM.ordinal

        UserPreferences(
            themeMode = ThemeMode.entries.getOrElse(themeOrdinal) { ThemeMode.SYSTEM },
            sortOrder = SortOrder.entries.getOrElse(sortOrdinal) { SortOrder.ALPHABETICAL },
            cardBrightnessPercent = brightness.coerceIn(0, 100),
            showAddButton = showAddButton,
            showBarcodesInList = showBarcodesInList,
            appLanguage = AppLanguage.fromOrdinal(languageOrdinal)
        )
    } ?: kotlinx.coroutines.flow.flowOf(UserPreferences())

    open suspend fun updateThemeMode(themeMode: ThemeMode) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.ordinal
        }
    }

    open suspend fun updateSortOrder(sortOrder: SortOrder) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.ordinal
        }
    }

    open suspend fun updateCardBrightnessPercent(percent: Int) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.CARD_BRIGHTNESS] = percent.coerceIn(0, 100)
        }
    }

    open suspend fun updateShowAddButton(show: Boolean) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.SHOW_ADD_BUTTON] = show
        }
    }

    open suspend fun updateShowBarcodesInList(show: Boolean) {
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.SHOW_BARCODES_IN_LIST] = show
        }
    }

    open suspend fun updateAppLanguage(language: AppLanguage) {
        sharedPreferences?.edit()?.putInt("app_language", language.ordinal)?.apply()
        context?.dataStore?.edit { preferences ->
            preferences[PreferencesKeys.APP_LANGUAGE] = language.ordinal
        }
    }

    open fun getLocalizedContext(base: Context): Context {
        val language = getAppLanguageSynchronously()
        if (language == AppLanguage.SYSTEM) return base
        val locale = when (language) {
            AppLanguage.ENGLISH -> Locale.ENGLISH
            AppLanguage.RUSSIAN -> Locale("ru")
            AppLanguage.FRENCH -> Locale.FRENCH
            AppLanguage.GERMAN -> Locale.GERMAN
            AppLanguage.JAPANESE -> Locale.JAPANESE
            AppLanguage.SPANISH -> Locale("es")
            AppLanguage.PORTUGUESE -> Locale("pt")
            AppLanguage.HINDI -> Locale("hi")
            AppLanguage.ARABIC -> Locale("ar")
            AppLanguage.TURKISH -> Locale("tr")
            AppLanguage.CHINESE_SIMPLIFIED -> Locale.SIMPLIFIED_CHINESE
            AppLanguage.SYSTEM -> Locale.getDefault()
        }
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        Log.d("UserPrefs", "Localized context: $locale")
        return ContextWrapper(base.createConfigurationContext(config))
    }
}
