package com.example.discountcards.ui.detail

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.data.repository.CardRepository
import com.example.discountcards.data.repository.UserPreferencesRepository
import com.example.discountcards.util.BarcodeGenerator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CardDetailViewModel(
    private val repository: CardRepository,
    private val cardId: Long,
    private val preferencesRepository: UserPreferencesRepository? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    private var usageIncremented = false

    init {
        observePreferences()
        loadCard()
    }

    private fun observePreferences() {
        preferencesRepository?.let { prefsRepo ->
            viewModelScope.launch {
                prefsRepo.userPreferencesFlow.collect { prefs ->
                    _uiState.update { it.copy(brightnessPercent = prefs.cardBrightnessPercent) }
                }
            }
        }
    }

    private fun loadCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            if (!usageIncremented) {
                usageIncremented = true
                repository.incrementUsageCount(cardId)
            }

            repository.getCardById(cardId).collect { card ->
                if (card == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            card = null,
                            barcodeBitmap = null,
                            error = "Card not found"
                        )
                    }
                } else {
                    val bitmap = generateBarcodeBitmap(card)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            card = card,
                            barcodeBitmap = bitmap,
                            error = null
                        )
                    }
                }
            }
        }
    }

    private suspend fun generateBarcodeBitmap(card: DiscountCard): Bitmap? {
        if (card.barcodeFormat.equals("PHONE", ignoreCase = true)) {
            return null
        }
        return withContext(ioDispatcher) {
            BarcodeGenerator.generateBarcode(
                content = card.cardNumber,
                format = card.barcodeFormat,
                width = 800,
                height = 400
            )
        }
    }

    fun toggleFavorite() {
        val currentCard = _uiState.value.card ?: return
        viewModelScope.launch {
            repository.toggleFavorite(currentCard.id, !currentCard.isFavorite)
        }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(isDeleteDialogVisible = true) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(isDeleteDialogVisible = false) }
    }

    fun deleteCard() {
        viewModelScope.launch {
            repository.deleteCardById(cardId)
            _uiState.update {
                it.copy(
                    isDeleteDialogVisible = false,
                    isDeleted = true
                )
            }
        }
    }
}
