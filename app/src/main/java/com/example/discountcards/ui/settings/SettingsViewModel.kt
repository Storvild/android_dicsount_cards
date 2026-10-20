package com.example.discountcards.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.discountcards.data.repository.SortOrder
import com.example.discountcards.data.repository.ThemeMode
import com.example.discountcards.data.repository.UserPreferences
import com.example.discountcards.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.updateThemeMode(mode)
        }
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            preferencesRepository.updateSortOrder(order)
        }
    }

    fun setCardBrightness(percent: Int) {
        viewModelScope.launch {
            preferencesRepository.updateCardBrightnessPercent(percent)
        }
    }

    fun setShowAddButton(show: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateShowAddButton(show)
        }
    }
}
