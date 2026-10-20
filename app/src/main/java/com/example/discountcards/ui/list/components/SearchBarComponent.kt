package com.example.discountcards.ui.list.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.discountcards.R
import com.example.discountcards.data.repository.SortOrder
import com.example.discountcards.data.repository.ThemeMode

@Composable
fun SearchBarComponent(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onExportClick: () -> Unit,
    onShareClick: () -> Unit = {},
    onImportClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeSelected: (ThemeMode) -> Unit = {},
    currentSortOrder: SortOrder = SortOrder.ALPHABETICAL,
    onSortOrderSelected: (SortOrder) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var fileSubmenuExpanded by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
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

                // 2. Поделиться
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

                if (fileSubmenuExpanded) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.export_json_action)) },
                        onClick = {
                            menuExpanded = false
                            fileSubmenuExpanded = false
                            onExportClick()
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
                            onImportClick()
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

                // 3. Оформление (единый пункт меню выбора темы)
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_appearance)) },
                    onClick = {
                        menuExpanded = false
                        showThemeDialog = true
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = null
                        )
                    }
                )

                // 4. Сортировка
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.menu_sort)) },
                    onClick = {
                        menuExpanded = false
                        showSortDialog = true
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
                        showAboutDialog = true
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

    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentThemeMode = currentThemeMode,
            onDismiss = { showThemeDialog = false },
            onSelect = { mode ->
                onThemeModeSelected(mode)
                showThemeDialog = false
            }
        )
    }

    // Sort Selection Dialog
    if (showSortDialog) {
        SortSelectionDialog(
            currentSortOrder = currentSortOrder,
            onDismiss = { showSortDialog = false },
            onSelect = { order ->
                onSortOrderSelected(order)
                showSortDialog = false
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        val aboutContext = LocalContext.current
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(stringResource(R.string.about_dialog_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.about_dialog_text))
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = stringResource(R.string.about_github_link),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/Storvild/android_dicsount_cards")
                            )
                            aboutContext.startActivity(intent)
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }
}

@Composable
fun ThemeSelectionDialog(
    currentThemeMode: ThemeMode,
    onDismiss: () -> Unit,
    onSelect: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.theme_dialog_title)) },
        text = {
            Column {
                ThemeOptionRow(
                    title = stringResource(R.string.theme_system),
                    selected = currentThemeMode == ThemeMode.SYSTEM,
                    onClick = { onSelect(ThemeMode.SYSTEM) }
                )
                ThemeOptionRow(
                    title = stringResource(R.string.theme_light),
                    selected = currentThemeMode == ThemeMode.LIGHT,
                    onClick = { onSelect(ThemeMode.LIGHT) }
                )
                ThemeOptionRow(
                    title = stringResource(R.string.theme_dark),
                    selected = currentThemeMode == ThemeMode.DARK,
                    onClick = { onSelect(ThemeMode.DARK) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}

@Composable
fun SortSelectionDialog(
    currentSortOrder: SortOrder,
    onDismiss: () -> Unit,
    onSelect: (SortOrder) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sort_dialog_title)) },
        text = {
            Column {
                ThemeOptionRow(
                    title = stringResource(R.string.sort_alphabetical),
                    selected = currentSortOrder == SortOrder.ALPHABETICAL,
                    onClick = { onSelect(SortOrder.ALPHABETICAL) }
                )
                ThemeOptionRow(
                    title = stringResource(R.string.sort_frequency),
                    selected = currentSortOrder == SortOrder.FREQUENCY,
                    onClick = { onSelect(SortOrder.FREQUENCY) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}

@Composable
private fun ThemeOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
