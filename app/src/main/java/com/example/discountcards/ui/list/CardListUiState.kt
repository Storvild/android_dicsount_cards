package com.example.discountcards.ui.list

import android.net.Uri
import com.example.discountcards.data.local.entity.DiscountCard

data class CardListUiState(
    val searchQuery: String = "",
    val cards: List<DiscountCard> = emptyList(),
    val isLoading: Boolean = true,
    val userMessage: String? = null,
    val exportedJson: String? = null,
    val pendingImportUri: Uri? = null,
    val showImportModeDialog: Boolean = false
)
