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
                    _uiState.update {
                        it.copy(
                            brightnessPercent = prefs.cardBrightnessPercent,
                            showBrightnessInCard = prefs.showBrightnessInCard
                        )
                    }
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

    fun toggleArchive() {
        val currentCard = _uiState.value.card ?: return
        val newState = !currentCard.isArchived
        viewModelScope.launch {
            repository.setArchived(cardId, newState)
            _uiState.update { it.copy(isArchivedMessage = if (newState) "ARCHIVED" else "RESTORED") }
        }
    }

    fun clearArchiveMessage() {
        _uiState.update { it.copy(isArchivedMessage = null) }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(isDeleteDialogVisible = true) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(isDeleteDialogVisible = false) }
    }

    fun toggleBarcodeFullscreen() {
        _uiState.update { it.copy(isBarcodeFullscreenVisible = !it.isBarcodeFullscreenVisible) }
    }

    fun toggleCvcVisibility() {
        _uiState.update { it.copy(isCvcVisible = !it.isCvcVisible) }
    }

    fun setBrightness(percent: Int) {
        val coerced = percent.coerceIn(0, 100)
        _uiState.update { it.copy(brightnessPercent = coerced) }
        viewModelScope.launch {
            preferencesRepository?.updateCardBrightnessPercent(coerced)
        }
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

    fun prepareShare() {
        _uiState.value.card ?: return
        _uiState.update {
            it.copy(
                showShareDialog = true,
                shareIncludeCvc = false,
                shareIncludeUsageCount = false
            )
        }
    }

    fun toggleShareIncludeCvc(include: Boolean) {
        _uiState.update { it.copy(shareIncludeCvc = include) }
    }

    fun toggleShareIncludeUsageCount(include: Boolean) {
        _uiState.update { it.copy(shareIncludeUsageCount = include) }
    }

    fun cancelShare() {
        _uiState.update { it.copy(showShareDialog = false) }
    }

    fun shareSingleCard(context: android.content.Context) {
        val card = _uiState.value.card ?: return
        viewModelScope.launch {
            try {
                val cardToExport = card.redact(
                    includeCvc = _uiState.value.shareIncludeCvc,
                    includeUsageCount = _uiState.value.shareIncludeUsageCount
                )
                val json = repository.exportToJson(listOf(cardToExport))
                cancelShare()
                context.cacheDir.listFiles()?.filter { it.name.startsWith("discount_cards_share") }?.forEach { it.delete() }
                val file = java.io.File(context.cacheDir, "discount_cards_share_${System.currentTimeMillis()}.json")
                file.writeText(json)
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    clipData = android.content.ClipData.newRawUri("", uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    android.content.Intent.createChooser(shareIntent, null)
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage ?: "Error") }
            }
        }
    }

    fun shareBarcodeImage(context: android.content.Context) {
        val bitmap = _uiState.value.barcodeBitmap
        if (bitmap == null) {
            _uiState.update { it.copy(error = "Barcode image not available") }
            return
        }
        viewModelScope.launch {
            try {
                val paddedBitmap = withContext(ioDispatcher) {
                    val padX = (bitmap.width * 0.08f).toInt().coerceAtLeast(1)
                    val padY = (bitmap.height * 0.08f).toInt().coerceAtLeast(1)
                    val newWidth = bitmap.width + padX * 2
                    val newHeight = bitmap.height + padY * 2
                    val result = android.graphics.Bitmap.createBitmap(newWidth, newHeight, android.graphics.Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(result)
                    // White background
                    canvas.drawColor(android.graphics.Color.WHITE)
                    // Draw original barcode centered
                    canvas.drawBitmap(bitmap, padX.toFloat(), padY.toFloat(), null)
                    result
                }
                context.cacheDir.listFiles()?.filter { it.name.startsWith("discount_cards_barcode") }?.forEach { it.delete() }
                val file = java.io.File(context.cacheDir, "discount_cards_barcode_${System.currentTimeMillis()}.png")
                java.io.FileOutputStream(file).use { fos ->
                    paddedBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
                }
                paddedBitmap.recycle()
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    clipData = android.content.ClipData.newRawUri("", uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    android.content.Intent.createChooser(shareIntent, null)
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage ?: "Error") }
            }
        }
    }
}
