package com.example.discountcards.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesRepositoryTest {

    @Test
    fun testUserPreferencesDefaults() {
        val prefs = UserPreferences()
        assertEquals(ThemeMode.SYSTEM, prefs.themeMode)
        assertEquals(SortOrder.ALPHABETICAL, prefs.sortOrder)
        assertEquals(100, prefs.cardBrightnessPercent)
        assertEquals(true, prefs.showAddButton)
        assertEquals(true, prefs.showBarcodesInList)
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
