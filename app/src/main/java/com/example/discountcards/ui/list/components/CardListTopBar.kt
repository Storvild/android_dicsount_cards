package com.example.discountcards.ui.list.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.discountcards.R
import com.example.discountcards.data.repository.SortOrder
import com.example.discountcards.data.repository.ThemeMode

@Composable
fun CardListTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onExportJsonClick: () -> Unit,
    onExportCsvClick: () -> Unit,
    onImportJsonClick: () -> Unit,
    onImportCsvClick: () -> Unit,
    onShareClick: () -> Unit,
    onAddCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onArchiveClick: () -> Unit,
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeSelected: (ThemeMode) -> Unit = {},
    currentSortOrder: SortOrder = SortOrder.ALPHABETICAL,
    onSortOrderSelected: (SortOrder) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showSortDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchField(
            query = query,
            onQueryChange = onQueryChange,
            onClearQuery = onClearQuery,
            modifier = Modifier.weight(1f)
        )

        CardListOverflowMenu(
            onExportJsonClick = onExportJsonClick,
            onExportCsvClick = onExportCsvClick,
            onImportJsonClick = onImportJsonClick,
            onImportCsvClick = onImportCsvClick,
            onShareClick = onShareClick,
            onAddCardClick = onAddCardClick,
            onDeleteClick = onDeleteClick,
            onArchiveClick = onArchiveClick,
            currentThemeMode = currentThemeMode,
            onThemeModeSelected = onThemeModeSelected,
            onSortClick = { showSortDialog = true },
            onAboutClick = { showAboutDialog = true },
            onSettingsClick = onSettingsClick
        )
    }

    // Sort Selection Dialog
    if (showSortDialog) {
        SortSelectionDialog(
            currentSortOrder = currentSortOrder,
            onDismiss = { showSortDialog = false },
            onSelect = { order: SortOrder ->
                onSortOrderSelected(order)
                showSortDialog = false
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
internal fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.search_icon_desc)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.clear_search_desc)
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Composable
private fun CardListOverflowMenu(
    onExportJsonClick: () -> Unit,
    onExportCsvClick: () -> Unit,
    onImportJsonClick: () -> Unit,
    onImportCsvClick: () -> Unit,
    onShareClick: () -> Unit,
    onAddCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onArchiveClick: () -> Unit,
    currentThemeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onSortClick: () -> Unit,
    onAboutClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var fileSubmenuExpanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { menuExpanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.menu_options_desc)
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = {
                menuExpanded = false
                fileSubmenuExpanded = false
            }
        ) {
            // 1. Файл
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_file)) },
                onClick = {
                    fileSubmenuExpanded = !fileSubmenuExpanded
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null
                    )
                }
            )

            if (fileSubmenuExpanded) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.export_json_action)) },
                    onClick = {
                        menuExpanded = false
                        fileSubmenuExpanded = false
                        onExportJsonClick()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Upload,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.import_json_action)) },
                    onClick = {
                        menuExpanded = false
                        fileSubmenuExpanded = false
                        onImportJsonClick()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.export_csv_action)) },
                    onClick = {
                        menuExpanded = false
                        fileSubmenuExpanded = false
                        onExportCsvClick()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Upload,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.import_csv_action)) },
                    onClick = {
                        menuExpanded = false
                        fileSubmenuExpanded = false
                        onImportCsvClick()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete_cards_action)) },
                    onClick = {
                        menuExpanded = false
                        fileSubmenuExpanded = false
                        onDeleteClick()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                )
            }

            // Поделиться
            DropdownMenuItem(
                text = { Text(stringResource(R.string.share_json_action)) },
                onClick = {
                    menuExpanded = false
                    fileSubmenuExpanded = false
                    onShareClick()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null
                    )
                }
            )

            // Архив
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_archive)) },
                onClick = {
                    menuExpanded = false
                    fileSubmenuExpanded = false
                    onArchiveClick()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Archive,
                        contentDescription = null
                    )
                }
            )

            // Добавить карточку
            DropdownMenuItem(
                text = { Text(stringResource(R.string.add_card_action)) },
                onClick = {
                    menuExpanded = false
                    fileSubmenuExpanded = false
                    onAddCardClick()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null
                    )
                }
            )

            // 3. Темная тема (checkbox: включение/выключение тёмной темы)
            val isDarkThemeEffective = when (currentThemeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_appearance)) },
                onClick = {
                    val newMode = if (isDarkThemeEffective) ThemeMode.LIGHT else ThemeMode.DARK
                    onThemeModeSelected(newMode)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    Checkbox(
                        checked = isDarkThemeEffective,
                        onCheckedChange = {},
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .semantics(mergeDescendants = true) { }
                    )
                }
            )

            // 4. Сортировка
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_sort)) },
                onClick = {
                    menuExpanded = false
                    onSortClick()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Sort,
                        contentDescription = null
                    )
                }
            )

            // 5. Настройки
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_settings)) },
                onClick = {
                    menuExpanded = false
                    onSettingsClick()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null
                    )
                }
            )

            // 6. О программе
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_about)) },
                onClick = {
                    menuExpanded = false
                    onAboutClick()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null
                    )
                }
            )
        }
    }
}
