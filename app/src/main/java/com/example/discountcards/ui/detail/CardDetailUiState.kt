package com.example.discountcards.ui.detail

import android.graphics.Bitmap
import com.example.discountcards.data.local.entity.DiscountCard

data class CardDetailUiState(
    val isLoading: Boolean = true,
    val card: DiscountCard? = null,
    val barcodeBitmap: Bitmap? = null,
    val brightnessPercent: Int = 0,
    val showBrightnessInCard: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    val isDeleted: Boolean = false,
    val isCvcVisible: Boolean = false,
    val error: String? = null,
    val showShareDialog: Boolean = false,
    val shareIncludeCvc: Boolean = false,
    val shareIncludeUsageCount: Boolean = false,
    val isArchivedMessage: String? = null,
    val isBarcodeFullscreenVisible: Boolean = false
) 
