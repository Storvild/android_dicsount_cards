package com.example.discountcards.ui.list

import android.net.Uri
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.util.DuplicateStrategy
import com.example.discountcards.util.ImportMode

data class CardListUiState(
    val searchQuery: String = "",
    val cards: List<DiscountCard> = emptyList(),
    val isLoading: Boolean = true,
    val userMessage: String? = null,
    val exportedJson: String? = null,
    val pendingImportUri: Uri? = null,
    val showImportModeDialog: Boolean = false,

    // Export selection dialog state
    val showExportSelectionDialog: Boolean = false,
    val exportSelectionCards: List<DiscountCard> = emptyList(),
    val selectedExportCardIds: Set<Long> = emptySet(),
    val exportIncludeCvc: Boolean = false,
    val exportIncludeUsageCount: Boolean = false,

    // Share selection dialog state
    val showShareSelectionDialog: Boolean = false,
    val shareSelectionCards: List<DiscountCard> = emptyList(),
    val selectedShareCardIds: Set<Long> = emptySet(),
    val shareIncludeCvc: Boolean = false,
    val shareIncludeUsageCount: Boolean = false,

    // Delete selection dialog state
    val showDeleteSelectionDialog: Boolean = false,
    val deleteSelectionCards: List<DiscountCard> = emptyList(),
    val selectedDeleteCardIds: Set<Long> = emptySet(),

    // Import selection dialog state
    val showImportSelectionDialog: Boolean = false,
    val importSelectionCards: List<DiscountCard> = emptyList(),
    val selectedImportIndexes: Set<Int> = emptySet(),
    val existingStoreNames: Set<String> = emptySet(),
    val importMode: ImportMode = ImportMode.MERGE,
    val duplicateStrategy: DuplicateStrategy = DuplicateStrategy.UPDATE_EXISTING
)

