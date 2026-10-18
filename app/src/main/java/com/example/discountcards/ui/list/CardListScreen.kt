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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.discountcards.R
import com.example.discountcards.ui.list.components.CardListItem
import com.example.discountcards.ui.list.components.EmptyState
import com.example.discountcards.ui.list.components.ExportDialog
import com.example.discountcards.ui.list.components.ImportDialog
import com.example.discountcards.ui.list.components.ImportModeDialog
import com.example.discountcards.ui.list.components.SearchBarComponent

@Composable
fun CardListScreen(
    viewModel: CardListViewModel,
    onCardClick: (Long) -> Unit,
    onAddCardClick: () -> Unit,
    modifier: Modifier = Modifier
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
            viewModel.exportToUri(context.contentResolver, uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.onImportFileSelected(uri)
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
                onExportClick = { exportLauncher.launch("discount_cards_backup.json") },
                onImportClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (uiState.cards.isEmpty()) {
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
                            key = { card -> card.id }
                        ) { card ->
                            CardListItem(
                                card = card,
                                onClick = { onCardClick(card.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    // SAF Import Mode Dialog (Merge vs Overwrite)
    if (uiState.showImportModeDialog && uiState.pendingImportUri != null) {
        ImportModeDialog(
            onDismiss = viewModel::cancelImport,
            onConfirm = { mode ->
                viewModel.confirmImportFromUri(context.contentResolver, mode)
            }
        )
    }

    // Fallback Export Dialog if text export used
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
            onImport = { json, mode ->
                viewModel.importCards(json, mode)
            }
        )
    }
}
