package com.example.discountcards.ui.list.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.discountcards.R
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.util.DuplicateStrategy
import com.example.discountcards.util.ImportMode

@Composable
private fun SelectionControlBar(
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onInvert: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onDeselectAll,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = stringResource(R.string.deselect_all),
                fontSize = 12.sp
            )
        }
        TextButton(
            onClick = onSelectAll,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = stringResource(R.string.select_all),
                fontSize = 12.sp
            )
        }
        TextButton(
            onClick = onInvert,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = stringResource(R.string.invert_selection),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ImportSelectionDialog(
    cards: List<DiscountCard>,
    selectedIndexes: Set<Int>,
    existingStoreNames: Set<String>,
    importMode: ImportMode,
    duplicateStrategy: DuplicateStrategy,
    onToggleCard: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onInvert: () -> Unit,
    onModeChanged: (ImportMode) -> Unit,
    onDuplicateStrategyChanged: (DuplicateStrategy) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = stringResource(R.string.import_selection_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.selected_count, selectedIndexes.size, cards.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                SelectionControlBar(
                    onSelectAll = onSelectAll,
                    onDeselectAll = onDeselectAll,
                    onInvert = onInvert
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable content starting from Mode Selection ("Добавить к текущим")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mode Selection (MERGE vs OVERWRITE)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = importMode == ImportMode.MERGE,
                                onClick = { onModeChanged(ImportMode.MERGE) }
                            )
                            Text(
                                text = stringResource(R.string.import_mode_merge),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.clickable { onModeChanged(ImportMode.MERGE) }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = importMode == ImportMode.OVERWRITE,
                                onClick = { onModeChanged(ImportMode.OVERWRITE) }
                            )
                            Text(
                                text = stringResource(R.string.import_mode_overwrite),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.clickable { onModeChanged(ImportMode.OVERWRITE) }
                            )
                        }
                    }

                    if (importMode == ImportMode.MERGE) {
                        Text(
                            text = stringResource(R.string.duplicate_strategy_title),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = duplicateStrategy == DuplicateStrategy.UPDATE_EXISTING,
                                    onClick = { onDuplicateStrategyChanged(DuplicateStrategy.UPDATE_EXISTING) }
                                )
                                Text(
                                    text = stringResource(R.string.duplicate_strategy_update),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.clickable { onDuplicateStrategyChanged(DuplicateStrategy.UPDATE_EXISTING) }
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = duplicateStrategy == DuplicateStrategy.ADD_AS_NEW,
                                    onClick = { onDuplicateStrategyChanged(DuplicateStrategy.ADD_AS_NEW) }
                                )
                                Text(
                                    text = stringResource(R.string.duplicate_strategy_add_new),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.clickable { onDuplicateStrategyChanged(DuplicateStrategy.ADD_AS_NEW) }
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = duplicateStrategy == DuplicateStrategy.SKIP,
                                    onClick = { onDuplicateStrategyChanged(DuplicateStrategy.SKIP) }
                                )
                                Text(
                                    text = stringResource(R.string.duplicate_strategy_skip),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.clickable { onDuplicateStrategyChanged(DuplicateStrategy.SKIP) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Card List
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        cards.forEachIndexed { index, card ->
                            val isSelected = selectedIndexes.contains(index)
                            val isDuplicate = existingStoreNames.contains(card.storeName.trim().lowercase())

                            Surface(
                                onClick = { onToggleCard(index) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { onToggleCard(index) }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(Color(card.cardColor))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = card.storeName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            if (isDuplicate && importMode == ImportMode.MERGE) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = stringResource(R.string.duplicate_badge),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 9.sp,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        if (card.cardNumber.isNotBlank()) {
                                            Text(
                                                text = card.cardNumber,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedIndexes.isNotEmpty(),
                onClick = onConfirm
            ) {
                Text(text = stringResource(R.string.import_selected_button, selectedIndexes.size))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel_button))
            }
        }
    )
}

@Composable
fun ImportDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var importText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.import_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.import_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 200.dp),
                    placeholder = { Text("JSON или CSV:\nНазвание;Код;Тип кода;CVC;Примечание") },
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = importText.isNotBlank(),
                onClick = {
                    onImport(importText)
                    onDismiss()
                }
            ) {
                Text(text = stringResource(R.string.import_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel_button))
            }
        }
    )
}
