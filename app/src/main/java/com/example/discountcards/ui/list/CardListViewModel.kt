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

            showDeleteSelectionDialog = args[8] as Boolean,
            deleteSelectionCards = args[9] as List<DiscountCard>,
            selectedDeleteCardIds = args[10] as Set<Long>,

            showImportSelectionDialog = args[11] as Boolean,
            importSelectionCards = args[12] as List<DiscountCard>,
            selectedImportIndexes = args[13] as Set<Int>,
            existingStoreNames = args[14] as Set<String>,
            importMode = args[15] as ImportMode,
            duplicateStrategy = args[16] as DuplicateStrategy
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
            val allCards = repository.getAllCards().first()
            if (allCards.isEmpty()) {
                userMessage.value = "EXPORT_FAILED:No cards to export"
                return@launch
            }
            exportSelectionCards.value = allCards
            selectedExportCardIds.value = allCards.map { it.id }.toSet()
            showExportSelectionDialog.value = true
        }
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
                val json = repository.exportToJson(selectedCards)
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
            val result = repository.exportToUri(contentResolver, uri, selectedCards)
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

    fun exportToUri(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            val selectedCards = exportSelectionCards.value.filter {
                selectedExportCardIds.value.contains(it.id)
            }
            val cardsToExport = if (selectedCards.isNotEmpty()) selectedCards else null
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
            val allCards = repository.getAllCards().first()
            if (allCards.isEmpty()) {
                userMessage.value = "DELETE_FAILED:No cards to delete"
                return@launch
            }
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

    fun clearUserMessage() {
        userMessage.value = null
    }
}

