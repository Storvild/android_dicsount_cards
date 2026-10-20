package com.example.discountcards.ui.list

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.discountcards.R
import com.example.discountcards.data.repository.SortOrder
import com.example.discountcards.data.repository.ThemeMode
import com.example.discountcards.ui.list.components.CardListItem
import com.example.discountcards.ui.list.components.EmptyState
import com.example.discountcards.ui.list.components.ExportDialog
import com.example.discountcards.ui.list.components.DeleteSelectionDialog
import com.example.discountcards.ui.list.components.ExportSelectionDialog
import com.example.discountcards.ui.list.components.ShareSelectionDialog
import com.example.discountcards.ui.list.components.ImportDialog
import com.example.discountcards.ui.list.components.ImportSelectionDialog
import com.example.discountcards.ui.list.components.SearchBarComponent

@Composable
fun CardListScreen(
    viewModel: CardListViewModel,
    onCardClick: (Long) -> Unit,
    onAddCardClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeSelected: (ThemeMode) -> Unit = {},
    currentSortOrder: SortOrder = SortOrder.ALPHABETICAL,
    onSortOrderSelected: (SortOrder) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = context.resources

    var showTextImportDialog by remember { mutableStateOf(false) }

    // SAF Launchers for Export and Import
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.confirmExportToUri(context.contentResolver, uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.onImportFileSelected(uri, context.contentResolver)
        }
    }

    // Toast notifications for actions
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            val displayMsg = when {
                msg == "EXPORT_SUCCESS" -> resources.getString(R.string.export_success)
                msg.startsWith("EXPORT_FAILED:") -> {
                    val detail = msg.removePrefix("EXPORT_FAILED:")
                    resources.getString(R.string.export_failed, detail)
                }
                msg.startsWith("IMPORT_SUCCESS:") -> {
                    val count = msg.removePrefix("IMPORT_SUCCESS:").toIntOrNull() ?: 0
                    resources.getString(R.string.import_success, count)
                }
                msg.startsWith("IMPORT_FAILED:") -> resources.getString(R.string.import_failed)
                msg.startsWith("DELETE_SUCCESS:") -> {
                    val count = msg.removePrefix("DELETE_SUCCESS:").toIntOrNull() ?: 0
                    resources.getString(R.string.delete_cards_success, count)
                }
                msg.startsWith("DELETE_FAILED:") -> {
                    val detail = msg.removePrefix("DELETE_FAILED:")
                    resources.getString(R.string.delete_cards_error, detail)
                }
                else -> msg
            }
            Toast.makeText(context, displayMsg, Toast.LENGTH_SHORT).show()
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCardClick,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_first_card)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchBarComponent(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onClearQuery = viewModel::onClearSearch,
                onExportClick = { viewModel.prepareExportSelection() },
                onShareClick = { viewModel.prepareShareSelection() },
                onImportClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                onDeleteClick = { viewModel.prepareDeleteSelection() },
                currentThemeMode = currentThemeMode,
                onThemeModeSelected = onThemeModeSelected,
                currentSortOrder = currentSortOrder,
                onSortOrderSelected = onSortOrderSelected,
                onSettingsClick = onSettingsClick
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (uiState.isLoading && uiState.cards.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (uiState.cards.isEmpty()) {
                    EmptyState(
                        isSearching = uiState.searchQuery.isNotBlank(),
                        onAddCardClick = onAddCardClick
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 280.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = uiState.cards,
                            key = { card -> card.id },
                            contentType = { "discount_card" }
                        ) { card ->
                            CardListItem(
                                card = card,
                                onClick = { onCardClick(card.id) },
                                onFavoriteToggle = { viewModel.toggleFavorite(card.id, card.isFavorite) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Export Selection Dialog
    if (uiState.showExportSelectionDialog) {
        ExportSelectionDialog(
            cards = uiState.exportSelectionCards,
            selectedCardIds = uiState.selectedExportCardIds,
            onToggleCard = viewModel::toggleExportCardSelection,
            onSelectAll = viewModel::selectAllExportCards,
            onDeselectAll = viewModel::deselectAllExportCards,
            onInvert = viewModel::invertExportCardSelection,
            onConfirm = { exportLauncher.launch("discount_cards_backup.json") },
            onDismiss = viewModel::cancelExportSelection
        )
    }

    // Share Selection Dialog
    if (uiState.showShareSelectionDialog) {
        ShareSelectionDialog(
            cards = uiState.shareSelectionCards,
            selectedCardIds = uiState.selectedShareCardIds,
            onToggleCard = viewModel::toggleShareCardSelection,
            onSelectAll = viewModel::selectAllShareCards,
            onDeselectAll = viewModel::deselectAllShareCards,
            onInvert = viewModel::invertShareCardSelection,
            onConfirm = { viewModel.shareExportCards(context) },
            onDismiss = viewModel::cancelShareSelection
        )
    }

    // Delete Selection Dialog
    if (uiState.showDeleteSelectionDialog) {
        DeleteSelectionDialog(
            cards = uiState.deleteSelectionCards,
            selectedCardIds = uiState.selectedDeleteCardIds,
            onToggleCard = viewModel::toggleDeleteCardSelection,
            onSelectAll = viewModel::selectAllDeleteCards,
            onDeselectAll = viewModel::deselectAllDeleteCards,
            onInvert = viewModel::invertDeleteCardSelection,
            onConfirm = viewModel::confirmDeleteSelected,
            onDismiss = viewModel::cancelDeleteSelection
        )
    }

    // Import Selection Dialog
    if (uiState.showImportSelectionDialog) {
        ImportSelectionDialog(
            cards = uiState.importSelectionCards,
            selectedIndexes = uiState.selectedImportIndexes,
            existingStoreNames = uiState.existingStoreNames,
            importMode = uiState.importMode,
            duplicateStrategy = uiState.duplicateStrategy,
            onToggleCard = viewModel::toggleImportCardSelection,
            onSelectAll = viewModel::selectAllImportCards,
            onDeselectAll = viewModel::deselectAllImportCards,
            onInvert = viewModel::invertImportCardSelection,
            onModeChanged = viewModel::setImportMode,
            onDuplicateStrategyChanged = viewModel::setDuplicateStrategy,
            onConfirm = viewModel::confirmImportSelected,
            onDismiss = viewModel::cancelImportSelection
        )
    }

    // Fallback Text Export Dialog
    uiState.exportedJson?.let { json ->
        ExportDialog(
            jsonText = json,
            onDismiss = viewModel::clearExportedJson
        )
    }

    // Fallback Text Import Dialog
    if (showTextImportDialog) {
        ImportDialog(
            onDismiss = { showTextImportDialog = false },
            onImport = { json ->
                viewModel.onImportTextEntered(json)
            }
        )
    }
}

