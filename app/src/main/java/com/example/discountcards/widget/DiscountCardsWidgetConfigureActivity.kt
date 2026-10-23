package com.example.discountcards.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.discountcards.DiscountCardsApp
import com.example.discountcards.R
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.data.repository.SortOrder
import com.example.discountcards.ui.theme.DiscountCardsTheme
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
                        onSave = { cardId, showCode, showName, isTransparent ->
                            WidgetPrefs.saveWidgetConfig(
                                context = this@DiscountCardsWidgetConfigureActivity,
                                appWidgetId = appWidgetId,
                                cardId = cardId,
                                showCode = showCode,
                                showStoreName = showName,
                                isTransparent = isTransparent
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigureScreen(
    appWidgetId: Int,
    onSave: (Long, Boolean, Boolean, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as DiscountCardsApp

    val existingConfig = remember { WidgetPrefs.loadWidgetConfig(context, appWidgetId) }

    var cards by remember { mutableStateOf<List<DiscountCard>>(emptyList()) }
    var selectedCardId by remember { mutableStateOf<Long?>(if (existingConfig.cardId != -1L) existingConfig.cardId else null) }
    var showCode by remember { mutableStateOf(existingConfig.showCode) }
    var showStoreName by remember { mutableStateOf(existingConfig.showStoreName) }
    var isTransparent by remember { mutableStateOf(existingConfig.isTransparent) }
    var sortOrder by remember { mutableStateOf(SortOrder.ALPHABETICAL) }

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
                                onSave(id, showCode, showStoreName, isTransparent)
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
                Text(
                    text = stringResource(R.string.widget_select_card),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (cards.isEmpty()) {
                item {
                    Text(text = "Нет доступных карточек", color = MaterialTheme.colorScheme.onSurfaceVariant)
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

            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Сортировка",
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

            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Отображение и стиль",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.widget_1x1_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.widget_transparent_bg))
                        Switch(
                            checked = isTransparent,
                            onCheckedChange = { isTransparent = it }
                        )
                    }
                }
            }
        }
    }
}
