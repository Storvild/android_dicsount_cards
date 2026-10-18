package com.example.discountcards.ui.list

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.data.repository.CardRepository
import com.example.discountcards.util.ImportMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CardListViewModel(
    private val repository: CardRepository
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val isLoading = MutableStateFlow(true)
    private val userMessage = MutableStateFlow<String?>(null)
    private val exportedJson = MutableStateFlow<String?>(null)
    private val pendingImportUri = MutableStateFlow<Uri?>(null)
    private val showImportModeDialog = MutableStateFlow(false)

    private val cardsFlow = searchQuery.flatMapLatest { query ->
        isLoading.value = true
        if (query.isBlank()) {
            repository.getAllCards()
        } else {
            repository.searchCards(query)
        }
    }

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<CardListUiState> = combine(
        searchQuery,
        cardsFlow,
        userMessage,
        exportedJson,
        pendingImportUri,
        showImportModeDialog
    ) { flows ->
        val query = flows[0] as String
        val cards = flows[1] as List<DiscountCard>
        val msg = flows[2] as String?
        val json = flows[3] as String?
        val uri = flows[4] as Uri?
        val showDialog = flows[5] as Boolean

        CardListUiState(
            searchQuery = query,
            cards = cards,
            isLoading = false,
            userMessage = msg,
            exportedJson = json,
            pendingImportUri = uri,
            showImportModeDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CardListUiState()
    )

    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun onClearSearch() {
        searchQuery.value = ""
    }

    fun exportCards() {
        viewModelScope.launch {
            try {
                val json = repository.exportToJson()
                exportedJson.value = json
            } catch (e: Exception) {
                userMessage.value = "EXPORT_FAILED:${e.localizedMessage ?: "Error"}"
            }
        }
    }

    fun exportToUri(contentResolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            val result = repository.exportToUri(contentResolver, uri)
            result.fold(
                onSuccess = {
                    userMessage.value = "EXPORT_SUCCESS"
                },
                onFailure = { error ->
                    userMessage.value = "EXPORT_FAILED:${error.localizedMessage ?: "Error"}"
                }
            )
        }
    }

    fun onImportFileSelected(uri: Uri) {
        pendingImportUri.value = uri
        showImportModeDialog.value = true
    }

    fun confirmImportFromUri(contentResolver: ContentResolver, mode: ImportMode) {
        val uri = pendingImportUri.value ?: run {
            showImportModeDialog.value = false
            return
        }
        viewModelScope.launch {
            val result = repository.importFromUri(contentResolver, uri, mode)
            result.fold(
                onSuccess = { count ->
                    userMessage.value = "IMPORT_SUCCESS:$count"
                },
                onFailure = { error ->
                    userMessage.value = "IMPORT_FAILED:${error.localizedMessage ?: "Invalid format"}"
                }
            )
            pendingImportUri.value = null
            showImportModeDialog.value = false
        }
    }

    fun cancelImport() {
        pendingImportUri.value = null
        showImportModeDialog.value = false
    }

    fun clearExportedJson() {
        exportedJson.value = null
    }

    fun importCards(jsonString: String, mode: ImportMode = ImportMode.MERGE) {
        viewModelScope.launch {
            val result = repository.importFromJson(jsonString, mode)
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

    fun clearUserMessage() {
        userMessage.value = null
    }
}
