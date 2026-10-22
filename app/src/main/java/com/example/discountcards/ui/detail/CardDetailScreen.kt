package com.example.discountcards.ui.detail

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Note
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.discountcards.R
import com.example.discountcards.ui.detail.components.BarcodeDisplay
import com.example.discountcards.ui.detail.components.DeleteConfirmDialog
import com.example.discountcards.ui.list.components.ShareSelectionDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    viewModel: CardDetailViewModel,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Set screen brightness according to user preference (0% = no change, >0% = percentage)
    DisposableEffect(uiState.brightnessPercent) {
        val activity = context as? Activity
        val window = activity?.window
        window?.let { w ->
            val params = w.attributes
            if (uiState.brightnessPercent <= 0) {
                params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            } else {
                params.screenBrightness = (uiState.brightnessPercent / 100f).coerceIn(0.01f, 1.0f)
            }
            w.attributes = params
        }

        onDispose {
            window?.let { w ->
                val params = w.attributes
                params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                w.attributes = params
            }
        }
    }

    // Automatically navigate back after card is deleted
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onBackClick()
        }
    }

    val card = uiState.card
    val cardColor = card?.let { Color(it.cardColor) } ?: MaterialTheme.colorScheme.primary

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = card?.storeName ?: stringResource(R.string.card_details_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                },
                actions = {
                    if (card != null) {
                        // Favorite Star Toggle
                        IconButton(onClick = viewModel::toggleFavorite) {
                            Icon(
                                imageVector = if (card.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = stringResource(R.string.favorite_toggle_desc),
                                tint = if (card.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { onEditClick(card.id) }) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.edit_button)
                            )
                        }
                        IconButton(onClick = viewModel::prepareShare) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = stringResource(R.string.share_action)
                            )
                        }
                        IconButton(onClick = viewModel::showDeleteDialog) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.delete_button),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (card != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Accent Stripe
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .background(cardColor)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Main Barcode Container
                    BarcodeDisplay(
                        bitmap = uiState.barcodeBitmap,
                        cardNumber = card.cardNumber,
                        barcodeFormat = card.barcodeFormat
                    )

                    // CVC Code Toggle (only shown if CVC is not empty)
                    if (card.cvc.isNotBlank()) {
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (uiState.isCvcVisible) {
                                    "CVC: ${card.cvc}"
                                } else {
                                    "CVC: •••"
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            TextButton(
                                onClick = viewModel::toggleCvcVisibility
                            ) {
                                Icon(
                                    imageVector = if (uiState.isCvcVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(4.dp))
                                Text(
                                    text = stringResource(
                                        if (uiState.isCvcVisible) R.string.hide_cvc else R.string.show_cvc
                                    ),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    // Note Section if available
                    if (card.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Note,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(top = 2.dp)
                                )

                                Spacer(modifier = Modifier.size(12.dp))

                                Column {
                                    Text(
                                        text = stringResource(R.string.notes_label),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = card.note,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Creation Date at bottom left
                    if (card.createdAt > 0) {
                        Spacer(modifier = Modifier.height(24.dp))

                        val dateFormat = remember {
                            java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                        }
                        val formattedDate = remember(card.createdAt) {
                            dateFormat.format(java.util.Date(card.createdAt))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = stringResource(R.string.created_at_label, formattedDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: stringResource(R.string.empty_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (uiState.isDeleteDialogVisible) {
        DeleteConfirmDialog(
            onDismiss = viewModel::hideDeleteDialog,
            onConfirmDelete = viewModel::deleteCard
        )
    }

    if (uiState.showShareDialog && card != null) {
        ShareSelectionDialog(
            cards = listOf(card),
            selectedCardIds = setOf(card.id),
            onToggleCard = {},
            onSelectAll = {},
            onDeselectAll = {},
            onInvert = {},
            includeCvc = uiState.shareIncludeCvc,
            includeUsageCount = uiState.shareIncludeUsageCount,
            onIncludeCvcChange = viewModel::toggleShareIncludeCvc,
            onIncludeUsageCountChange = viewModel::toggleShareIncludeUsageCount,
            onConfirm = { viewModel.shareSingleCard(context) },
            onDismiss = viewModel::cancelShare
        )
    }
}
