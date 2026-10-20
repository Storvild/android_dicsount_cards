package com.example.discountcards.ui.addedit

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.discountcards.R
import com.example.discountcards.ui.addedit.components.CameraBarcodeScannerDialog
import com.example.discountcards.ui.addedit.components.ColorPicker
import com.example.discountcards.ui.addedit.components.FormatSelector
import com.example.discountcards.ui.addedit.components.IconPicker
import com.example.discountcards.ui.addedit.components.getCategoryIconByName
import com.example.discountcards.ui.addedit.components.getFormatDisplayName
import com.example.discountcards.util.BarcodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    viewModel: AddEditCardViewModel,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = context.resources
    val snackbarHostState = remember { SnackbarHostState() }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.processGalleryImage(context, uri)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaveSuccess()
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            val displayMsg = when (msg) {
                "ERROR_STORE_NAME_EMPTY" -> resources.getString(R.string.error_store_name_empty)
                "ERROR_CARD_NUMBER_EMPTY" -> resources.getString(R.string.error_card_number_empty)
                "ERROR_BARCODE_NOT_FOUND" -> resources.getString(R.string.error_barcode_not_found)
                else -> msg
            }
            snackbarHostState.showSnackbar(displayMsg)
            viewModel.onUserMessageShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) {
                            stringResource(R.string.edit_card_title)
                        } else {
                            stringResource(R.string.add_card_title)
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Real-time Card Live Preview Card
                CardPreviewHeader(
                    storeName = uiState.storeName,
                    cardNumber = uiState.cardNumber,
                    barcodeFormat = uiState.barcodeFormat,
                    note = uiState.note,
                    cardColorInt = uiState.cardColor,
                    iconName = uiState.iconName
                )

                // Store Name Input
                OutlinedTextField(
                    value = uiState.storeName,
                    onValueChange = viewModel::onStoreNameChanged,
                    label = { Text(stringResource(R.string.store_name_label)) },
                    placeholder = { Text(stringResource(R.string.store_name_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Storefront,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (uiState.storeName.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onStoreNameChanged("") }) {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = stringResource(R.string.clear_store_name)
                                )
                            }
                        }
                    },
                    isError = uiState.storeNameError != null,
                    supportingText = {
                        uiState.storeNameError?.let { errKey ->
                            val text = if (errKey == "ERROR_STORE_NAME_EMPTY") {
                                stringResource(R.string.error_store_name_empty)
                            } else {
                                errKey
                            }
                            Text(text = text, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Card Number Input with Camera & Gallery Scan Buttons
                OutlinedTextField(
                    value = uiState.cardNumber,
                    onValueChange = viewModel::onCardNumberChanged,
                    label = { Text(stringResource(R.string.card_number_label)) },
                    placeholder = { Text(stringResource(R.string.card_number_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.CreditCard,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.cardNumber.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onCardNumberChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Clear,
                                        contentDescription = stringResource(R.string.clear_card_number)
                                    )
                                }
                            }

                            // Gallery Pick Button
                            IconButton(
                                onClick = { galleryLauncher.launch("image/*") }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = stringResource(R.string.pick_gallery_desc),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Camera Scan Button
                            IconButton(
                                onClick = viewModel::openCameraScanner
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.QrCodeScanner,
                                    contentDescription = stringResource(R.string.scan_camera_desc),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    isError = uiState.cardNumberError != null,
                    supportingText = {
                        uiState.cardNumberError?.let { errKey ->
                            val text = if (errKey == "ERROR_CARD_NUMBER_EMPTY") {
                                stringResource(R.string.error_card_number_empty)
                            } else {
                                errKey
                            }
                            Text(text = text, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Barcode Format Dropdown Selector
                FormatSelector(
                    selectedFormat = uiState.barcodeFormat,
                    onFormatSelected = viewModel::onBarcodeFormatChanged,
                    modifier = Modifier.fillMaxWidth()
                )

                // Favorite Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (uiState.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.favorite_label),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Switch(
                        checked = uiState.isFavorite,
                        onCheckedChange = viewModel::onFavoriteChanged
                    )
                }

                // Color Picker
                ColorPicker(
                    selectedColorInt = uiState.cardColor,
                    onColorSelected = viewModel::onCardColorChanged,
                    modifier = Modifier.fillMaxWidth()
                )

                // Icon / Category Picker
                IconPicker(
                    selectedIconName = uiState.iconName,
                    onIconSelected = viewModel::onIconNameChanged,
                    modifier = Modifier.fillMaxWidth()
                )

                // CVC Input
                OutlinedTextField(
                    value = uiState.cvc,
                    onValueChange = viewModel::onCvcChanged,
                    label = { Text(stringResource(R.string.cvc_label)) },
                    placeholder = { Text(stringResource(R.string.cvc_placeholder)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Note Input Field
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = viewModel::onNoteChanged,
                    label = { Text(stringResource(R.string.note_label)) },
                    placeholder = { Text(stringResource(R.string.note_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Notes,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                // Read-only usage counter and creation date (only in edit mode)
                if (uiState.isEditMode) {
                    Text(
                        text = context.resources.getString(
                            R.string.shown_count_label, uiState.usageCount
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val createdAtMs = uiState.createdAt
                    if (createdAtMs != null && createdAtMs > 0) {
                        val dateFormat = remember {
                            java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                        }
                        val formattedDate = remember(createdAtMs) {
                            dateFormat.format(java.util.Date(createdAtMs))
                        }
                        Text(
                            text = context.resources.getString(
                                R.string.created_at_label, formattedDate
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Submit / Save Button
                Button(
                    onClick = viewModel::saveCard,
                    enabled = !uiState.isSaving,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (uiState.isEditMode) {
                                stringResource(R.string.save_changes)
                            } else {
                                stringResource(R.string.add_card_button)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Camera Scanner Fullscreen Dialog
    if (uiState.isCameraScannerOpen) {
        CameraBarcodeScannerDialog(
            onDismissRequest = viewModel::closeCameraScanner,
            onBarcodeScanned = { cardNumber, format ->
                viewModel.onBarcodeScanned(cardNumber, format)
            }
        )
    }
}

@Composable
private fun CardPreviewHeader(
    storeName: String,
    cardNumber: String,
    barcodeFormat: String,
    note: String,
    cardColorInt: Int,
    iconName: String,
    modifier: Modifier = Modifier
) {
    val cardColor = Color(cardColorInt)
    val categoryIcon = getCategoryIconByName(iconName)
    val isPhoneFormat = barcodeFormat.equals("PHONE", ignoreCase = true)

    val thumbnailState = produceState<Bitmap?>(initialValue = null, cardNumber, barcodeFormat) {
        value = withContext(Dispatchers.Default) {
            if (cardNumber.isNotBlank() && !isPhoneFormat) {
                BarcodeGenerator.generateBarcode(
                    content = cardNumber,
                    format = barcodeFormat,
                    width = 280,
                    height = 90
                )
            } else null
        }
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Color Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(cardColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = cardColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = cardColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = storeName.ifBlank { stringResource(R.string.preview_store_name) },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = getFormatDisplayName(barcodeFormat),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Barcode or Phone Preview Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        if (isPhoneFormat) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Phone,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = cardNumber.ifBlank { "+7 (999) 000-00-00" },
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.Black
                                )
                            }
                        } else {
                            val thumbnail = thumbnailState.value
                            if (thumbnail != null) {
                                Image(
                                    bitmap = thumbnail.asImageBitmap(),
                                    contentDescription = stringResource(R.string.barcode_preview_desc),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                )
                            } else {
                                Text(
                                    text = cardNumber.ifBlank { "•••• •••• ••••" },
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 2.sp
                                    ),
                                    color = Color.Gray,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cardNumber.ifBlank { stringResource(R.string.card_number_default) },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (note.isNotBlank()) {
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }
            }
        }
    }
}
