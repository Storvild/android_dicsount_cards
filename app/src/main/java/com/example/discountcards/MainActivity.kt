package com.example.discountcards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.discountcards.data.repository.ThemeMode
import com.example.discountcards.data.repository.UserPreferences
import com.example.discountcards.ui.AddEditCardViewModelFactory
import com.example.discountcards.ui.CardDetailViewModelFactory
import com.example.discountcards.ui.CardListViewModelFactory
import com.example.discountcards.ui.SettingsViewModelFactory
import com.example.discountcards.ui.addedit.AddEditCardScreen
import com.example.discountcards.ui.addedit.AddEditCardViewModel
import com.example.discountcards.ui.detail.CardDetailScreen
import com.example.discountcards.ui.detail.CardDetailViewModel
import com.example.discountcards.ui.list.CardListScreen
import com.example.discountcards.ui.list.CardListViewModel
import com.example.discountcards.ui.settings.SettingsScreen
import com.example.discountcards.ui.settings.SettingsViewModel
import com.example.discountcards.ui.theme.DiscountCardsTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface Destination {
    data object CardList : Destination
    data class CardDetail(
        val cardId: Long,
        val key: String = UUID.randomUUID().toString(),
    ) : Destination
    data class AddEditCard(
        val cardId: Long? = null,
        val key: String = UUID.randomUUID().toString(),
    ) : Destination
    data object Settings : Destination
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = applicationContext as DiscountCardsApp
        val preferencesRepository = app.preferencesRepository

        setContent {
            val preferences by preferencesRepository.userPreferencesFlow
                .collectAsStateWithLifecycle(initialValue = UserPreferences())

            val isDarkTheme = when (preferences.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            DiscountCardsTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiscountCardsNavigation()
                }
            }
        }
    }
}

@Composable
fun DiscountCardsNavigation() {
    val context = LocalContext.current
    val app = context.applicationContext as DiscountCardsApp
    val repository = app.repository
    val preferencesRepository = app.preferencesRepository

    val preferences by preferencesRepository.userPreferencesFlow
        .collectAsStateWithLifecycle(initialValue = UserPreferences())

    val backStack = remember { mutableStateListOf<Destination>(Destination.CardList) }
    val currentDestination = backStack.lastOrNull() ?: Destination.CardList

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeAt(backStack.lastIndex)
    }

    when (currentDestination) {
        is Destination.CardList -> {
            val viewModel: CardListViewModel = viewModel(
                factory = CardListViewModelFactory(repository, preferencesRepository)
            )
            CardListScreen(
                viewModel = viewModel,
                onCardClick = { cardId ->
                    backStack.add(Destination.CardDetail(cardId))
                },
                onAddCardClick = {
                    backStack.add(Destination.AddEditCard(null))
                },
                onSettingsClick = {
                    backStack.add(Destination.Settings)
                },
                currentThemeMode = preferences.themeMode,
                onThemeModeSelected = { mode ->
                    CoroutineScope(Dispatchers.Main).launch {
                        preferencesRepository.updateThemeMode(mode)
                    }
                },
                currentSortOrder = preferences.sortOrder,
                onSortOrderSelected = { order ->
                    CoroutineScope(Dispatchers.Main).launch {
                        preferencesRepository.updateSortOrder(order)
                    }
                }
            )
        }
        is Destination.CardDetail -> {
            val cardId = currentDestination.cardId
            val viewModel: CardDetailViewModel = viewModel(
                key = "card_detail_${currentDestination.key}",
                factory = CardDetailViewModelFactory(repository, cardId, preferencesRepository)
            )
            CardDetailScreen(
                viewModel = viewModel,
                onBackClick = {
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                },
                onEditClick = { id ->
                    backStack.add(Destination.AddEditCard(id))
                }
            )
        }
        is Destination.AddEditCard -> {
            val cardId = currentDestination.cardId
            val viewModel: AddEditCardViewModel = viewModel(
                key = "add_edit_card_${currentDestination.key}",
                factory = AddEditCardViewModelFactory(repository, cardId)
            )
            AddEditCardScreen(
                viewModel = viewModel,
                onBackClick = {
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                },
                onSaveSuccess = {
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                }
            )
        }
        is Destination.Settings -> {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(preferencesRepository)
            )
            SettingsScreen(
                viewModel = viewModel,
                onBackClick = {
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                }
            )
        }
    }
}
