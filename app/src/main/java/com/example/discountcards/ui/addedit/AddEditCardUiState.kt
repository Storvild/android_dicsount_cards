package com.example.discountcards.ui.addedit

data class AddEditCardUiState(
    val cardId: Long? = null,
    val storeName: String = "",
    val storeNameError: String? = null,
    val cardNumber: String = "",
    val cardNumberError: String? = null,
    val barcodeFormat: String = "CODE_128",
    val note: String = "",
    val cardColor: Int = 0xFF1E88E5.toInt(), // Default Ocean Blue
    val iconName: String = "shopping_bag",
    val isFavorite: Boolean = false,
    val createdAt: Long? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isEditMode: Boolean = false,
    val isCameraScannerOpen: Boolean = false,
    val userMessage: String? = null
)
