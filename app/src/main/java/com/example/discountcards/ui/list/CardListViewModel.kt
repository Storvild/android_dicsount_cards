package com.example.discountcards.ui.list

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.data.repository.CardRepository
import com.example.discountcards.data.repository.SortOrder
import com.example.discountcards.data.repository.UserPreferencesRepository
import com.example.discountcards.util.DuplicateStrategy
import com.example.discountcards.util.ImportMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CardListViewModel(
    private val repository: CardRepository,
    private val preferencesRepository: UserPreferencesRepository? = null
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val isLoading = MutableStateFlow(true)
    private val userMessage = MutableStateFlow<String?>(null)
    private val exportedJson = MutableStateFlow<String?>(null)
    private val pendingImportUri = MutableStateFlow<Uri?>(null)

    // Export selection
    private val showExportSelectionDialog = MutableStateFlow(false)
    private val exportSelectionCards = MutableStateFlow<List<DiscountCard>>(emptyList())
    private val selectedExportCardIds = MutableStateFlow<Set<Long>>(emptySet())
    private val exportIncludeCvc = MutableStateFlow(false)
    private val exportIncludeUsageCount = MutableStateFlow(false)

    // Share selection
    private val showShareSelectionDialog = MutableStateFlow(false)
    private val shareSelectionCards = MutableStateFlow<List<DiscountCard>>(emptyList())
    private val selectedShareCardIds = MutableStateFlow<Set<Long>>(emptySet())
    private val shareIncludeCvc = MutableStateFlow(false)
    private val shareIncludeUsageCount = MutableStateFlow(false)

    // Delete selection
    private val showDeleteSelectionDialog = MutableStateFlow(false)
    private val deleteSelectionCards = MutableStateFlow<List<DiscountCard>>(emptyList())
    private val selectedDeleteCardIds = MutableStateFlow<Set<Long>>(emptySet())

    // Import selection
    private val showImportSelectionDialog = MutableStateFlow(false)
    private val importSelectionCards = MutableStateFlow<List<DiscountCard>>(emptyList())
    private val selectedImportIndexes = MutableStateFlow<Set<Int>>(emptySet())
    private val existingStoreNames = MutableStateFlow<Set<String>>(emptySet())
    private val importMode = MutableStateFlow(ImportMode.MERGE)
    private val duplicateStrategy = MutableStateFlow(DuplicateStrategy.UPDATE_EXISTING)

    private val sortOrderFlow = preferencesRepository?.userPreferencesFlow?.map { it.sortOrder }
        ?: MutableStateFlow(SortOrder.ALPHABETICAL)

    private val cardsFlow = combine(searchQuery, sortOrderFlow) { query, sortOrder ->
        Pair(query, sortOrder)
    }.flatMapLatest { (query, sortOrder) ->
        isLoading.value = true
        if (query.isBlank()) {
            repository.getAllCards(sortOrder)
        } else {
            repository.searchCards(query, sortOrder)
        }
    }

    val uiState: StateFlow<CardListUiState> = combine(
        searchQuery,
        cardsFlow,
        userMessage,
        exportedJson,
        pendingImportUri,
        showExportSelectionDialog,
        exportSelectionCards,
        selectedExportCardIds,
        exportIncludeCvc,
        exportIncludeUsageCount,
        showShareSelectionDialog,
        shareSelectionCards,
        selectedShareCardIds,
        shareIncludeCvc,
        shareIncludeUsageCount,
        showDeleteSelectionDialog,
        deleteSelectionCards,
        selectedDeleteCardIds,
        showImportSelectionDialog,
        importSelectionCards,
        selectedImportIndexes,
        existingStoreNames,
        importMode,
        duplicateStrategy
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        CardListUiState(
            searchQuery = args[0] as String,
            cards = args[1] as List<DiscountCard>,
            isLoading = false,
            userMessage = args[2] as String?,
            exportedJson = args[3] as String?,
            pendingImportUri = args[4] as Uri?,
            showImportModeDialog = false,

            showExportSelectionDialog = args[5] as Boolean,
            exportSelectionCards = args[6] as List<DiscountCard>,
            selectedExportCardIds = args[7] as Set<Long>,
            exportIncludeCvc = args[8] as Boolean,
            exportIncludeUsageCount = args[9] as Boolean,

            showShareSelectionDialog = args[10] as Boolean,
            shareSelectionCards = args[11] as List<DiscountCard>,
            selectedShareCardIds = args[12] as Set<Long>,
            shareIncludeCvc = args[13] as Boolean,
            shareIncludeUsageCount = args[14] as Boolean,

            showDeleteSelectionDialog = args[15] as Boolean,
            deleteSelectionCards = args[16] as List<DiscountCard>,
            selectedDeleteCardIds = args[17] as Set<Long>,

            showImportSelectionDialog = args[18] as Boolean,
            importSelectionCards = args[19] as List<DiscountCard>,
            selectedImportIndexes = args[20] as Set<Int>,
            existingStoreNames = args[21] as Set<String>,
            importMode = args[22] as ImportMode,
            duplicateStrategy = args[23] as DuplicateStrategy
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CardListUiState(isLoading = true)
    )

    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun onClearSearch() {
        searchQuery.value = ""
    }

    fun toggleFavorite(cardId: Long, currentFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(cardId, !currentFavorite)
        }
    }

    // Export Selection Methods
    fun prepareExportSelection() {
        viewModelScope.launch {
            val allCards = repository.getAllCardsIncludingArchived().first()
            if (allCards.isEmpty()) {
                userMessage.value = "EXPORT_FAILED:No cards to export"
                return@launch
            }
            exportSelectionCards.value = allCards
            // Select only non-archived cards by default
            selectedExportCardIds.value = allCards.filter { !it.isArchived }.map { it.id }.toSet() 
            exportIncludeCvc.value = false
            exportIncludeUsageCount.value = false
            showExportSelectionDialog.value = true
        }
    }

    fun setExportIncludeCvc(include: Boolean) {
        exportIncludeCvc.value = include
    }

    fun setExportIncludeUsageCount(include: Boolean) {
        exportIncludeUsageCount.value = include
    }

    fun toggleExportCardSelection(cardId: Long) {
        val current = selectedExportCardIds.value
        selectedExportCardIds.value = if (current.contains(cardId)) {
            current - cardId
        } else {
            current + cardId
        }
    }

    fun selectAllExportCards() {
        selectedExportCardIds.value = exportSelectionCards.value.map { it.id }.toSet()
    }

    fun deselectAllExportCards() {
        selectedExportCardIds.value = emptySet()
    }

    fun invertExportCardSelection() {
        val allIds = exportSelectionCards.value.map { it.id }.toSet()
        selectedExportCardIds.value = allIds - selectedExportCardIds.value
    }

    fun cancelExportSelection() {
        showExportSelectionDialog.value = false
        exportSelectionCards.value = emptyList()
        selectedExportCardIds.value = emptySet()
    }

    fun confirmExportToText() {
        viewModelScope.launch {
            try {
                val selectedCards = exportSelectionCards.value.filter {
                    selectedExportCardIds.value.contains(it.id)
                }
                if (selectedCards.isEmpty()) {
                    userMessage.value = "EXPORT_FAILED:No cards selected"
                    return@launch
                }
                val cardsToExport = selectedCards.map {
                    it.redact(exportIncludeCvc.value, exportIncludeUsageCount.value)
                }
                val json = repository.exportToJson(cardsToExport)
                cancelExportSelection()
                exportedJson.value = json
            } catch (e: Exception) {
                userMessage.value = "EXPORT_FAILED:${e.localizedMessage ?: "Error"}"
            }
        }
    }

    fun confirmExportToUri(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            val selectedCards = exportSelectionCards.value.filter {
                selectedExportCardIds.value.contains(it.id)
            }
            if (selectedCards.isEmpty()) {
                userMessage.value = "EXPORT_FAILED:No cards selected"
                return@launch
            }
            val cardsToExport = selectedCards.map {
                it.redact(exportIncludeCvc.value, exportIncludeUsageCount.value)
            }
            val result = repository.exportToUri(contentResolver, uri, cardsToExport)
            result.fold(
                onSuccess = {
                    cancelExportSelection()
                    userMessage.value = "EXPORT_SUCCESS"
                },
                onFailure = { error ->
                    userMessage.value = "EXPORT_FAILED:${error.localizedMessage ?: "Error"}"
                }
            )
        }
    }

    fun exportCards() {
        prepareExportSelection()
    }

    // Share Selection Methods
    fun prepareShareSelection() {
        viewModelScope.launch {
            val allCards = repository.getAllCardsIncludingArchived().first()
            if (allCards.isEmpty()) {
                userMessage.value = "EXPORT_FAILED:No cards to share"
                return@launch
            }
            shareSelectionCards.value = allCards
            // Select only non-archived cards by default
            selectedShareCardIds.value = allCards.filter { !it.isArchived }.map { it.id }.toSet()
            shareIncludeCvc.value = false
            shareIncludeUsageCount.value = false
            showShareSelectionDialog.value = true
        }
    }

    fun setShareIncludeCvc(include: Boolean) {
        shareIncludeCvc.value = include
    }

    fun setShareIncludeUsageCount(include: Boolean) {
        shareIncludeUsageCount.value = include
    }

    fun toggleShareCardSelection(cardId: Long) {
        val current = selectedShareCardIds.value
        selectedShareCardIds.value = if (current.contains(cardId)) {
            current - cardId
        } else {
            current + cardId
        }
    }

    fun selectAllShareCards() {
        selectedShareCardIds.value = shareSelectionCards.value.map { it.id }.toSet()
    }

    fun deselectAllShareCards() {
        selectedShareCardIds.value = emptySet()
    }

    fun invertShareCardSelection() {
        val allIds = shareSelectionCards.value.map { it.id }.toSet()
        selectedShareCardIds.value = allIds - selectedShareCardIds.value
    }

    fun cancelShareSelection() {
        showShareSelectionDialog.value = false
        shareSelectionCards.value = emptyList()
        selectedShareCardIds.value = emptySet()
    }

    fun shareExportCards(context: android.content.Context) {
        viewModelScope.launch {
            val selectedCards = shareSelectionCards.value.filter {
                selectedShareCardIds.value.contains(it.id)
            }
            if (selectedCards.isEmpty()) {
                userMessage.value = "EXPORT_FAILED:No cards selected"
                return@launch
            }
            try {
                val cardsToExport = selectedCards.map {
                    it.redact(shareIncludeCvc.value, shareIncludeUsageCount.value)
                }
                val json = repository.exportToJson(cardsToExport)
                cancelShareSelection()
                // Write to a temp file and share via ACTION_SEND
                val file = java.io.File(context.cacheDir, "discount_cards_share.json")
                file.writeText(json)
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    android.content.Intent.createChooser(shareIntent, null)
                )
            } catch (e: Exception) {
                userMessage.value = "EXPORT_FAILED:" + (e.localizedMessage ?: "Error")
            }
        }
    }

    fun exportToUri(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            val selectedCards = exportSelectionCards.value.filter {
                selectedExportCardIds.value.contains(it.id)
            }
            val cardsToExport = if (selectedCards.isNotEmpty()) {
                selectedCards.map { it.redact(exportIncludeCvc.value, exportIncludeUsageCount.value) }
            } else {
                null
            }
            val result = repository.exportToUri(contentResolver, uri, cardsToExport)
            result.fold(
                onSuccess = {
                    cancelExportSelection()
                    userMessage.value = "EXPORT_SUCCESS"
                },
                onFailure = { error ->
                    userMessage.value = "EXPORT_FAILED:${error.localizedMessage ?: "Error"}"
                }
            )
        }
    }

    // Delete Selection Methods
    fun prepareDeleteSelection() {
        viewModelScope.launch {
            val allCards = repository.getAllCardsIncludingArchived().first()
            if (allCards.isEmpty()) {
                userMessage.value = "DELETE_FAILED:No cards to delete"
                return@launch
            }
            // Include archived cards in the delete list as well
            deleteSelectionCards.value = allCards
            selectedDeleteCardIds.value = emptySet()
            showDeleteSelectionDialog.value = true
        }
    }

    fun toggleDeleteCardSelection(cardId: Long) {
        val current = selectedDeleteCardIds.value
        selectedDeleteCardIds.value = if (current.contains(cardId)) {
            current - cardId
        } else {
            current + cardId
        }
    }

    fun selectAllDeleteCards() {
        selectedDeleteCardIds.value = deleteSelectionCards.value.map { it.id }.toSet()
    }

    fun deselectAllDeleteCards() {
        selectedDeleteCardIds.value = emptySet()
    }

    fun invertDeleteCardSelection() {
        val allIds = deleteSelectionCards.value.map { it.id }.toSet()
        selectedDeleteCardIds.value = allIds - selectedDeleteCardIds.value
    }

    fun cancelDeleteSelection() {
        showDeleteSelectionDialog.value = false
        deleteSelectionCards.value = emptyList()
        selectedDeleteCardIds.value = emptySet()
    }

    fun confirmDeleteSelected() {
        viewModelScope.launch {
            val selectedCards = deleteSelectionCards.value.filter {
                selectedDeleteCardIds.value.contains(it.id)
            }
            if (selectedCards.isEmpty()) {
                userMessage.value = "DELETE_FAILED:No cards selected"
                return@launch
            }
            try {
                for (card in selectedCards) {
                    repository.deleteCard(card)
                }
                val count = selectedCards.size
                cancelDeleteSelection()
                userMessage.value = "DELETE_SUCCESS:$count"
            } catch (e: Exception) {
                userMessage.value = "DELETE_FAILED:${e.localizedMessage ?: "Error"}"
            }
        }
    }

    // Import Selection Methods
    fun onImportFileSelected(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val result = repository.readJsonCardsFromUri(contentResolver, uri)
            result.fold(
                onSuccess = { cards ->
                    if (cards.isEmpty()) {
                        userMessage.value = "IMPORT_FAILED:Empty file"
                        return@fold
                    }
                    val existing = repository.getAllCards().first()
                    existingStoreNames.value = existing.map { it.storeName.trim().lowercase() }.toSet()
                    pendingImportUri.value = uri
                    importSelectionCards.value = cards
                    selectedImportIndexes.value = cards.indices.toSet()
                    importMode.value = ImportMode.MERGE
                    duplicateStrategy.value = DuplicateStrategy.UPDATE_EXISTING
                    showImportSelectionDialog.value = true
                },
                onFailure = { error ->
                    userMessage.value = "IMPORT_FAILED:${error.localizedMessage ?: "Invalid format"}"
                }
            )
        }
    }

    fun onImportTextEntered(jsonString: String) {
        viewModelScope.launch {
            val result = repository.parseJsonCards(jsonString)
            result.fold(
                onSuccess = { cards ->
                    if (cards.isEmpty()) {
                        userMessage.value = "IMPORT_FAILED:Empty JSON"
                        return@fold
                    }
                    val existing = repository.getAllCards().first()
                    existingStoreNames.value = existing.map { it.storeName.trim().lowercase() }.toSet()
                    importSelectionCards.value = cards
                    selectedImportIndexes.value = cards.indices.toSet()
                    importMode.value = ImportMode.MERGE
                    duplicateStrategy.value = DuplicateStrategy.UPDATE_EXISTING
                    showImportSelectionDialog.value = true
                },
                onFailure = { error ->
                    userMessage.value = "IMPORT_FAILED:${error.localizedMessage ?: "Invalid format"}"
                }
            )
        }
    }

    fun toggleImportCardSelection(index: Int) {
        val current = selectedImportIndexes.value
        selectedImportIndexes.value = if (current.contains(index)) {
            current - index
        } else {
            current + index
        }
    }

    fun selectAllImportCards() {
        selectedImportIndexes.value = importSelectionCards.value.indices.toSet()
    }

    fun deselectAllImportCards() {
        selectedImportIndexes.value = emptySet()
    }

    fun invertImportCardSelection() {
        val allIndices = importSelectionCards.value.indices.toSet()
        selectedImportIndexes.value = allIndices - selectedImportIndexes.value
    }

    fun setImportMode(mode: ImportMode) {
        importMode.value = mode
    }

    fun setDuplicateStrategy(strategy: DuplicateStrategy) {
        duplicateStrategy.value = strategy
    }

    fun cancelImportSelection() {
        showImportSelectionDialog.value = false
        pendingImportUri.value = null
        importSelectionCards.value = emptyList()
        selectedImportIndexes.value = emptySet()
        existingStoreNames.value = emptySet()
    }

    fun confirmImportSelected() {
        viewModelScope.launch {
            val selectedCards = importSelectionCards.value.filterIndexed { index, _ ->
                selectedImportIndexes.value.contains(index)
            }
            if (selectedCards.isEmpty()) {
                userMessage.value = "IMPORT_FAILED:No cards selected"
                return@launch
            }
            val result = repository.importSelectedCards(selectedCards, importMode.value, duplicateStrategy.value)
            result.fold(
                onSuccess = { count ->
                    userMessage.value = "IMPORT_SUCCESS:$count"
                    cancelImportSelection()
                },
                onFailure = { error ->
                    userMessage.value = "IMPORT_FAILED:${error.localizedMessage ?: "Invalid format"}"
                }
            )
        }
    }

    fun importCards(jsonString: String, mode: ImportMode = ImportMode.MERGE) {
        viewModelScope.launch {
            val result = repository.importFromJson(jsonString, mode, DuplicateStrategy.UPDATE_EXISTING)
            result.fold(
                onSuccess = { count ->
                    userMessage.value = "IMPORT_SUCCESS:$count"
                },
                onFailure = { error ->
                    userMessage.value = "IMPORT_FAILED:${error.localizedMessage ?: "Invalid format"}"
                }
            )
        }
    }

    fun clearExportedJson() {
        exportedJson.value = null
    }

    fun deleteCard(card: DiscountCard) {
        viewModelScope.launch {
            runCatching {
                repository.deleteCard(card)
                userMessage.value = "DELETE_SUCCESS:1"
            }.onFailure { e ->
                userMessage.value = "DELETE_FAILED:${e.localizedMessage ?: "Error"}"
            }
        }
    }

    fun clearUserMessage() {
        userMessage.value = null
    }
}

