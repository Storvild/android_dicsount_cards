package com.example.discountcards.ui.addedit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.data.repository.CardRepository
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddEditCardViewModel(
    private val repository: CardRepository,
    private val cardId: Long? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditCardUiState(cardId = cardId))
    val uiState: StateFlow<AddEditCardUiState> = _uiState.asStateFlow()

    init {
        if (cardId != null && cardId > 0) {
            loadCard(cardId)
        }
    }

    private fun loadCard(id: Long) {
        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isLoading = true) }
            val card = repository.getCardByIdDirect(id)
            if (card != null) {
                _uiState.update {
                    it.copy(
                        storeName = card.storeName,
                        cardNumber = card.cardNumber,
                        barcodeFormat = card.barcodeFormat,
                        cvc = card.cvc,
                        note = card.note,
                        cardColor = card.cardColor,
                        iconName = card.iconName.ifBlank { "shopping_bag" },
                        isFavorite = card.isFavorite,
                        usageCount = card.usageCount,
                        createdAt = card.createdAt,
                        isLoading = false,
                        isEditMode = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userMessage = "Card not found"
                    )
                }
            }
        }
    }

    fun onStoreNameChanged(name: String) {
        _uiState.update {
            it.copy(
                storeName = name,
                storeNameError = if (name.isNotBlank()) null else it.storeNameError
            )
        }
    }

    fun onCardNumberChanged(number: String) {
        _uiState.update {
            it.copy(
                cardNumber = number,
                cardNumberError = if (number.isNotBlank()) null else it.cardNumberError
            )
        }
    }

    fun onBarcodeFormatChanged(format: String) {
        _uiState.update { it.copy(barcodeFormat = format) }
    }

    fun onCvcChanged(cvc: String) {
        _uiState.update { it.copy(cvc = cvc) }
    }

    fun onNoteChanged(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onCardColorChanged(colorInt: Int) {
        _uiState.update { it.copy(cardColor = colorInt) }
    }

    fun onIconNameChanged(iconName: String) {
        _uiState.update { it.copy(iconName = iconName) }
    }

    fun onFavoriteChanged(isFavorite: Boolean) {
        _uiState.update { it.copy(isFavorite = isFavorite) }
    }

    fun openCameraScanner() {
        _uiState.update { it.copy(isCameraScannerOpen = true) }
    }

    fun closeCameraScanner() {
        _uiState.update { it.copy(isCameraScannerOpen = false) }
    }

    fun onBarcodeScanned(cardNumber: String, barcodeFormat: String) {
        _uiState.update {
            it.copy(
                cardNumber = cardNumber,
                barcodeFormat = barcodeFormat,
                cardNumberError = null,
                isCameraScannerOpen = false,
                userMessage = "Barcode recognized: $cardNumber"
            )
        }
    }

    fun processGalleryImage(context: Context, uri: Uri) {
        viewModelScope.launch(ioDispatcher) {
            runCatching {
                val inputImage = InputImage.fromFilePath(context, uri)
                val scanner = BarcodeScanning.getClient()
                scanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            val barcode = barcodes.first()
                            val rawValue = barcode.rawValue ?: barcode.displayValue ?: ""
                            val detectedFormat = mapMlKitFormatToString(barcode.format)
                            if (rawValue.isNotBlank()) {
                                onBarcodeScanned(rawValue, detectedFormat)
                            } else {
                                _uiState.update { it.copy(userMessage = "ERROR_BARCODE_NOT_FOUND") }
                            }
                        } else {
                            _uiState.update { it.copy(userMessage = "ERROR_BARCODE_NOT_FOUND") }
                        }
                    }
                    .addOnFailureListener {
                        _uiState.update { it.copy(userMessage = "ERROR_BARCODE_NOT_FOUND") }
                    }
            }.onFailure {
                _uiState.update { it.copy(userMessage = "ERROR_BARCODE_NOT_FOUND") }
            }
        }
    }

    fun saveCard() {
        val currentState = _uiState.value
        val isStoreNameValid = currentState.storeName.isNotBlank()
        val isCardNumberValid = currentState.cardNumber.isNotBlank()

        if (!isStoreNameValid || !isCardNumberValid) {
            _uiState.update {
                it.copy(
                    storeNameError = if (!isStoreNameValid) "ERROR_STORE_NAME_EMPTY" else null,
                    cardNumberError = if (!isCardNumberValid) "ERROR_CARD_NUMBER_EMPTY" else null
                )
            }
            return
        }

        viewModelScope.launch(ioDispatcher) {
            _uiState.update { it.copy(isSaving = true) }
            val card = DiscountCard(
                id = currentState.cardId ?: 0,
                storeName = currentState.storeName.trim(),
                cardNumber = currentState.cardNumber.trim(),
                barcodeFormat = currentState.barcodeFormat,
                note = currentState.note.trim(),
                cardColor = currentState.cardColor,
                iconName = currentState.iconName,
                isFavorite = currentState.isFavorite,
                cvc = currentState.cvc,
                createdAt = currentState.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            if (currentState.isEditMode) {
                repository.updateCard(card)
            } else {
                repository.insertCard(card)
            }

            _uiState.update {
                it.copy(
                    isSaving = false,
                    isSaved = true
                )
            }
        }
    }

    fun onUserMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    companion object {
        fun mapMlKitFormatToString(mlKitFormat: Int): String {
            return when (mlKitFormat) {
                Barcode.FORMAT_QR_CODE -> "QR_CODE"
                Barcode.FORMAT_CODE_128 -> "CODE_128"
                Barcode.FORMAT_EAN_13 -> "EAN_13"
                Barcode.FORMAT_EAN_8 -> "EAN_8"
                Barcode.FORMAT_UPC_A -> "UPC_A"
                Barcode.FORMAT_CODE_39 -> "CODE_39"
                Barcode.FORMAT_PDF417 -> "PDF_417"
                Barcode.FORMAT_AZTEC -> "AZTEC"
                else -> "CODE_128"
            }
        }
    }
}
