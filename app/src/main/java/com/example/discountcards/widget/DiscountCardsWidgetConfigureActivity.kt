package com.example.discountcards.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.discountcards.DiscountCardsApp
import com.example.discountcards.R
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.data.repository.SortOrder
import com.example.discountcards.ui.theme.DiscountCardsTheme
import com.example.discountcards.util.BarcodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DiscountCardsWidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            DiscountCardsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WidgetConfigureScreen(
                        appWidgetId = appWidgetId,
                        onSave = { cardId, showCode, showName ->
                            WidgetPrefs.saveWidgetConfig(
                                context = this@DiscountCardsWidgetConfigureActivity,
                                appWidgetId = appWidgetId,
                                cardId = cardId,
                                showCode = showCode,
                                showStoreName = showName
                            )

                            val appWidgetManager = AppWidgetManager.getInstance(this@DiscountCardsWidgetConfigureActivity)
                            DiscountCardsWidgetProvider.updateAppWidget(
                                this@DiscountCardsWidgetConfigureActivity,
                                appWidgetManager,
                                appWidgetId
                            )

                            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            setResult(RESULT_OK, resultValue)
                            finish()
                        },
                        onCancel = {
                            finish()
                        }
                    )
                }
            }
        }
    }
}

enum class WidgetSizeOption(
    val label: String,
    val cellsW: Int,
    val cellsH: Int,
    val widthDp: Dp,
    val heightDp: Dp
) {
    SIZE_1X1("1×1", 1, 1, 80.dp, 80.dp),
    SIZE_2X1("2×1", 2, 1, 150.dp, 80.dp),
    SIZE_3X1("3×1", 3, 1, 225.dp, 80.dp),
    SIZE_4X1("4×1", 4, 1, 300.dp, 80.dp),
    SIZE_2X2("2×2", 2, 2, 150.dp, 140.dp),
    SIZE_3X2("3×2", 3, 2, 225.dp, 140.dp),
    SIZE_4X2("4×2", 4, 2, 300.dp, 140.dp),
    SIZE_3X3("3×3", 3, 3, 225.dp, 190.dp),
    SIZE_4X4("4×4", 4, 4, 300.dp, 230.dp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigureScreen(
    appWidgetId: Int,
    onSave: (Long, Boolean, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as DiscountCardsApp

    val existingConfig = remember { WidgetPrefs.loadWidgetConfig(context, appWidgetId) }

    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val options = remember(appWidgetId) {
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            appWidgetManager.getAppWidgetOptions(appWidgetId)
        } else Bundle.EMPTY
    }
    val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 70)
    val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 70)

    val initialCellsW = ((minWidth - 10) / 73).coerceAtLeast(1).coerceAtMost(4)
    val initialCellsH = ((minHeight - 10) / 73).coerceAtLeast(1).coerceAtMost(4)

    val initialSizeOption = remember(initialCellsW, initialCellsH) {
        WidgetSizeOption.entries.find { it.cellsW == initialCellsW && it.cellsH == initialCellsH }
            ?: when {
                initialCellsW <= 1 && initialCellsH <= 1 -> WidgetSizeOption.SIZE_1X1
                initialCellsW >= 2 && initialCellsH <= 1 -> WidgetSizeOption.SIZE_2X1
                initialCellsW <= 2 && initialCellsH <= 2 -> WidgetSizeOption.SIZE_2X2
                else -> WidgetSizeOption.SIZE_3X2
            }
    }

    var cards by remember { mutableStateOf<List<DiscountCard>>(emptyList()) }
    var selectedCardId by remember { mutableStateOf<Long?>(if (existingConfig.cardId != -1L) existingConfig.cardId else null) }
    var showCode by remember { mutableStateOf(existingConfig.showCode) }
    var showStoreName by remember { mutableStateOf(existingConfig.showStoreName) }
    var sortOrder by remember { mutableStateOf(SortOrder.ALPHABETICAL) }
    var selectedSizeOption by remember { mutableStateOf(initialSizeOption) }

    val selectedCard = remember(cards, selectedCardId) {
        cards.find { it.id == selectedCardId }
    }

    LaunchedEffect(sortOrder) {
        val loaded = withContext(Dispatchers.IO) {
            val dao = app.database.discountCardDao()
            when (sortOrder) {
                SortOrder.ALPHABETICAL -> dao.getAllCardsDirect()
                SortOrder.FREQUENCY -> dao.getAllCardsDirectByFrequency()
                SortOrder.CREATION_DATE -> dao.getAllCardsDirectByCreationDate()
            }
        }
        cards = loaded
        if (selectedCardId == null && loaded.isNotEmpty()) {
            selectedCardId = loaded.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.widget_configure_title)) }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onCancel
                    ) {
                        Text(stringResource(android.R.string.cancel))
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = selectedCardId != null && (showCode || showStoreName),
                        onClick = {
                            selectedCardId?.let { id ->
                                onSave(id, showCode, showStoreName)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.widget_save_button))
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.widget_edit_note),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // 1. Select Card
            item {
                Text(
                    text = stringResource(R.string.widget_select_card),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (cards.isEmpty()) {
                item {
                    Text(text = stringResource(R.string.widget_no_cards), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(cards) { card ->
                    val isSelected = card.id == selectedCardId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCardId = card.id },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = card.storeName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = card.cardNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedCardId = card.id }
                            )
                        }
                    }
                }
            }

            // 2. Sorting
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.menu_sort),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sortOrder == SortOrder.ALPHABETICAL,
                            onClick = { sortOrder = SortOrder.ALPHABETICAL }
                        )
                        Text(stringResource(R.string.sort_alphabetical))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sortOrder == SortOrder.FREQUENCY,
                            onClick = { sortOrder = SortOrder.FREQUENCY }
                        )
                        Text(stringResource(R.string.sort_frequency))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = sortOrder == SortOrder.CREATION_DATE,
                            onClick = { sortOrder = SortOrder.CREATION_DATE }
                        )
                        Text(stringResource(R.string.sort_creation_date))
                    }
                }
            }

            // 3. Display & Style Settings
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.widget_display_style),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.widget_show_name))
                        Switch(
                            checked = showStoreName,
                            onCheckedChange = { showStoreName = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.widget_show_code))
                        Switch(
                            checked = showCode,
                            onCheckedChange = { showCode = it }
                        )
                    }
                }
            }

            // 4. Live Widget Preview & Size Choice (Moved to the end)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                WidgetPreviewSection(
                    card = selectedCard,
                    showStoreName = showStoreName,
                    showCode = showCode,
                    selectedSize = selectedSizeOption,
                    onSizeSelected = { selectedSizeOption = it }
                )
            }
        }
    }
}

@Composable
fun WidgetPreviewSection(
    card: DiscountCard?,
    showStoreName: Boolean,
    showCode: Boolean,
    selectedSize: WidgetSizeOption,
    onSizeSelected: (WidgetSizeOption) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.widget_preview_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Size Choice FilterChips (1x1 through 4x4)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(WidgetSizeOption.entries) { size ->
                FilterChip(
                    selected = size == selectedSize,
                    onClick = { onSizeSelected(size) },
                    label = { Text(size.label, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        // Simulated Home Screen Wallpaper container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E1B4B),
                            Color(0xFF312E81),
                            Color(0xFF4338CA)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            val is1x1 = selectedSize == WidgetSizeOption.SIZE_1X1

            // Outer Widget Card (Maximized to full size of widget)
            Box(
                modifier = Modifier
                    .width(selectedSize.widthDp)
                    .height(selectedSize.heightDp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (card != null) {
                        if (showStoreName && card.storeName.isNotBlank()) {
                            Text(
                                text = card.storeName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = if (is1x1) 11.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (showCode && card.cardNumber.isNotBlank()) {
                            val targetW = with(density) {
                                (selectedSize.widthDp - 16.dp).toPx().toInt().coerceAtLeast(100)
                            }
                            val targetH = with(density) {
                                (selectedSize.heightDp - (if (showStoreName) 36.dp else 16.dp)).toPx().toInt().coerceAtLeast(50)
                            }

                            val barcodeBitmap = remember(
                                card.cardNumber,
                                card.barcodeFormat,
                                targetW,
                                targetH
                            ) {
                                BarcodeGenerator.generateBarcodeCached(
                                    content = card.cardNumber,
                                    format = card.barcodeFormat,
                                    width = targetW,
                                    height = targetH,
                                    foregroundColor = android.graphics.Color.BLACK,
                                    backgroundColor = android.graphics.Color.TRANSPARENT
                                )
                            }

                            if (barcodeBitmap != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Image(
                                    bitmap = barcodeBitmap.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    text = card.cardNumber,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Black),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.widget_select_card),
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                        )
                    }
                }

                // Settings icon mimic at top right
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.Black.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}
