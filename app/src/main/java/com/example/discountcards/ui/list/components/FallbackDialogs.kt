package com.example.discountcards.ui.list.components

import androidx.compose.runtime.Composable
import com.example.discountcards.ui.list.components.dialogs.ExportDialog

/**
 * Renders the fallback export dialog that is shown when the SAF file-picker
 * launcher is cancelled and the ViewModel falls back to showing the JSON text
 * in a dialog.
 */
@Composable
fun FallbackDialogs(
    exportedJson: String?,
    onClearExportedJson: () -> Unit
) {
    exportedJson?.let { json ->
        ExportDialog(
            jsonText = json,
            onDismiss = onClearExportedJson
        )
    }
}
