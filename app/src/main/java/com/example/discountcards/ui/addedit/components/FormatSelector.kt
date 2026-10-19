package com.example.discountcards.ui.addedit.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.discountcards.R
import com.example.discountcards.util.BarcodeGenerator

@Composable
fun getFormatDisplayName(format: String): String {
    return if (format.equals("PHONE", ignoreCase = true)) {
        stringResource(R.string.format_phone)
    } else {
        format.replace("_", " ")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatSelector(
    selectedFormat: String,
    onFormatSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.format_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = getFormatDisplayName(selectedFormat),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.format_label)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                BarcodeGenerator.SUPPORTED_FORMATS.forEach { format ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = getFormatDisplayName(format),
                                fontWeight = if (format == selectedFormat) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onFormatSelected(format)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
