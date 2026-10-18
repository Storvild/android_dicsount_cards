package com.example.discountcards.ui.detail

import android.graphics.Bitmap
import com.example.discountcards.data.local.entity.DiscountCard

data class CardDetailUiState(
    val isLoading: Boolean = true,
    val card: DiscountCard? = null,
    val barcodeBitmap: Bitmap? = null,
    val isDeleteDialogVisible: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)
