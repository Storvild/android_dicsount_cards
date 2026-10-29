package com.example.discountcards.ui.list.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.discountcards.R
import com.example.discountcards.data.repository.SortOrder

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
            androidx.compose.foundation.layout.Column {
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
