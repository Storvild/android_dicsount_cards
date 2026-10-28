package com.example.discountcards.ui.list.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import com.example.discountcards.BuildConfig
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
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.example.discountcards.R
import com.example.discountcards.data.repository.SortOrder
import com.example.discountcards.data.repository.ThemeMode
import com.example.discountcards.util.BarcodeGenerator

@Composable
fun SearchBarComponent(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onExportClick: () -> Unit,
    onShareClick: () -> Unit = {},
    onAddCardClick: () -> Unit = {},
    onImportClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    onArchiveClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeSelected: (ThemeMode) -> Unit = {},
    currentSortOrder: SortOrder = SortOrder.ALPHABETICAL,
    onSortOrderSelected: (SortOrder) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var fileSubmenuExpanded by remember { mutableStateOf(false) }
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
        val appVersion = BuildConfig.VERSION_NAME
        val aboutDialogText = stringResource(R.string.about_dialog_text, appVersion)
        val aboutGithubLink = stringResource(R.string.about_github_link)
        val shareTitle = stringResource(R.string.share_action)
        val appName = stringResource(R.string.app_name)
        val copyDesc = stringResource(R.string.copy_name_to_clipboard_desc)
        val qrDesc = stringResource(R.string.qr_code_desc)
        val githubUrl = "https://github.com/Storvild/android_discount_cards"

        // QR code bitmap for the GitHub URL
        val qrBitmap: Bitmap? = remember(githubUrl) {
            BarcodeGenerator.generateBarcode(
                content = githubUrl,
                format = "QR_CODE",
                width = 300,
                height = 300,
                foregroundColor = android.graphics.Color.BLACK,
                backgroundColor = android.graphics.Color.WHITE
            )
        }

        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val clipboard = aboutContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = ClipData.newPlainText("app_name", appName)
                            clipboard.setPrimaryClip(clipData)
                            Toast.makeText(
                                aboutContext,
                                R.string.copied_to_clipboard,
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = copyDesc,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(aboutDialogText, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = aboutGithubLink,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(githubUrl)
                                )
                                aboutContext.startActivity(intent)
                            }
                    )
                    if (qrBitmap != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = qrDesc,
                            modifier = Modifier.size(160.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val shareText = "$aboutDialogText\n\n$aboutGithubLink"
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        aboutContext.startActivity(
                            Intent.createChooser(
                                shareIntent,
                                shareTitle
                            )
                        )
                    }
                ) {
                    Text(shareTitle)
                }
            }
        )
    }
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
                ThemeOptionRow(
                    title = stringResource(R.string.sort_creation_date),
                    selected = currentSortOrder == SortOrder.CREATION_DATE,
                    onClick = { onSelect(SortOrder.CREATION_DATE) }
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
