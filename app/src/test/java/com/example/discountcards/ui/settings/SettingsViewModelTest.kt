package com.example.discountcards.ui.settings

import com.example.discountcards.data.repository.AppLanguage
import com.example.discountcards.data.repository.SortOrder
import com.example.discountcards.data.repository.ThemeMode
import com.example.discountcards.data.repository.UserPreferences
import com.example.discountcards.data.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeUserPreferencesRepository : UserPreferencesRepository() {
        private val _prefs = MutableStateFlow(UserPreferences())
        override val userPreferencesFlow: Flow<UserPreferences> = _prefs.asStateFlow()

        override fun getAppLanguageSynchronously(): AppLanguage {
            return _prefs.value.appLanguage
        }

        override suspend fun updateThemeMode(themeMode: ThemeMode) {
            _prefs.update { it.copy(themeMode = themeMode) }
        }

        override suspend fun updateSortOrder(sortOrder: SortOrder) {
            _prefs.update { it.copy(sortOrder = sortOrder) }
        }

        override suspend fun updateCardBrightnessPercent(percent: Int) {
            _prefs.update { it.copy(cardBrightnessPercent = percent) }
        }

        override suspend fun updateShowAddButton(show: Boolean) {
            _prefs.update { it.copy(showAddButton = show) }
        }

        override suspend fun updateShowBarcodesInList(show: Boolean) {
            _prefs.update { it.copy(showBarcodesInList = show) }
        }

        override suspend fun updateAppLanguage(language: AppLanguage) {
            _prefs.update { it.copy(appLanguage = language) }
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testUpdateThemeMode() = runTest {
        val fakeRepo = FakeUserPreferencesRepository()
        val viewModel = SettingsViewModel(fakeRepo)

        backgroundScope.launch { viewModel.userPreferences.collect {} }
        advanceUntilIdle()

        viewModel.setThemeMode(ThemeMode.DARK)
        advanceUntilIdle()

        assertEquals(ThemeMode.DARK, viewModel.userPreferences.value.themeMode)
    }

    @Test
    fun testUpdateSortOrder() = runTest {
        val fakeRepo = FakeUserPreferencesRepository()
        val viewModel = SettingsViewModel(fakeRepo)

        backgroundScope.launch { viewModel.userPreferences.collect {} }
        advanceUntilIdle()

        viewModel.setSortOrder(SortOrder.FREQUENCY)
        advanceUntilIdle()

        assertEquals(SortOrder.FREQUENCY, viewModel.userPreferences.value.sortOrder)
    }

    @Test
    fun testUpdateCardBrightness() = runTest {
        val fakeRepo = FakeUserPreferencesRepository()
        val viewModel = SettingsViewModel(fakeRepo)

        backgroundScope.launch { viewModel.userPreferences.collect {} }
        advanceUntilIdle()

        viewModel.setCardBrightness(75)
        advanceUntilIdle()

        assertEquals(75, viewModel.userPreferences.value.cardBrightnessPercent)
    }

    @Test
    fun testUpdateDisplayOptions() = runTest {
        val fakeRepo = FakeUserPreferencesRepository()
        val viewModel = SettingsViewModel(fakeRepo)

        backgroundScope.launch { viewModel.userPreferences.collect {} }
        advanceUntilIdle()

        viewModel.setShowAddButton(false)
        viewModel.setShowBarcodesInList(false)
        advanceUntilIdle()

        assertEquals(false, viewModel.userPreferences.value.showAddButton)
        assertEquals(false, viewModel.userPreferences.value.showBarcodesInList)
    }

    @Test
    fun testUpdateAppLanguageAndSyncRead() = runTest {
        val fakeRepo = FakeUserPreferencesRepository()
        val viewModel = SettingsViewModel(fakeRepo)

        backgroundScope.launch { viewModel.userPreferences.collect {} }
        advanceUntilIdle()

        viewModel.setAppLanguage(AppLanguage.GERMAN)
        advanceUntilIdle()

        assertEquals(AppLanguage.GERMAN, viewModel.userPreferences.value.appLanguage)
        assertEquals(AppLanguage.GERMAN, viewModel.getSyncAppLanguage())
    }
}
