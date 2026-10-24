package com.example.discountcards.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesRepositoryTest {

    @Test
    fun testUserPreferencesDefaults() {
        val prefs = UserPreferences()
        assertEquals(ThemeMode.SYSTEM, prefs.themeMode)
        assertEquals(SortOrder.ALPHABETICAL, prefs.sortOrder)
        assertEquals(0, prefs.cardBrightnessPercent)
        assertEquals(true, prefs.showAddButton)
        assertEquals(true, prefs.showBarcodesInList)
        assertEquals(AppLanguage.SYSTEM, prefs.appLanguage)
    }

    @Test
    fun testAppLanguageFromOrdinal() {
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromOrdinal(0))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromOrdinal(1))
        assertEquals(AppLanguage.RUSSIAN, AppLanguage.fromOrdinal(2))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromOrdinal(999))
    }

    @Test
    fun testUserPreferencesCustomValues() {
        val prefs = UserPreferences(
            themeMode = ThemeMode.DARK,
            sortOrder = SortOrder.FREQUENCY,
            cardBrightnessPercent = 80,
            showAddButton = false,
            showBarcodesInList = false
        )
        assertEquals(ThemeMode.DARK, prefs.themeMode)
        assertEquals(SortOrder.FREQUENCY, prefs.sortOrder)
        assertEquals(80, prefs.cardBrightnessPercent)
        assertEquals(false, prefs.showAddButton)
        assertEquals(false, prefs.showBarcodesInList)
    }
}
