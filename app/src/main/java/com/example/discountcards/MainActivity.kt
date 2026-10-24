package com.example.discountcards

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.IntentCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.discountcards.data.repository.AppLanguage
import com.example.discountcards.data.repository.ThemeMode
import com.example.discountcards.data.repository.UserPreferences
import com.example.discountcards.ui.AddEditCardViewModelFactory
import com.example.discountcards.ui.CardDetailViewModelFactory
import com.example.discountcards.ui.CardListViewModelFactory
import com.example.discountcards.ui.SettingsViewModelFactory
import com.example.discountcards.ui.addedit.AddEditCardScreen
import com.example.discountcards.ui.addedit.AddEditCardViewModel
import com.example.discountcards.ui.archive.ArchiveScreen
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
    data object Archive : Destination
}

fun destinationSaver(): Saver<Destination, String> = Saver(
    save = { destination ->
        when (destination) {
            is Destination.CardList -> "CardList"
            is Destination.CardDetail -> "CardDetail:${destination.cardId}:${destination.key}"
            is Destination.AddEditCard -> "AddEditCard:${destination.cardId ?: -1L}:${destination.key}"
            is Destination.Settings -> "Settings"
            is Destination.Archive -> "Archive"
        }
    },
    restore = { value ->
        val parts = value.split(":")
        when (parts[0]) {
            "CardList" -> Destination.CardList
            "CardDetail" -> Destination.CardDetail(
                cardId = parts[1].toLong(),
                key = parts.getOrNull(2) ?: UUID.randomUUID().toString()
            )
            "AddEditCard" -> {
                val id = parts[1].toLong()
                Destination.AddEditCard(
                    cardId = if (id == -1L) null else id,
                    key = parts.getOrNull(2) ?: UUID.randomUUID().toString()
                )
            }
            "Settings" -> Destination.Settings
            "Archive" -> Destination.Archive
            else -> Destination.CardList
        }
    }
)

val backStackSaver = Saver<SnapshotStateList<Destination>, List<String>>(
    save = { list ->
        val destSaver = destinationSaver()
        list.mapNotNull { item -> with(destSaver) { save(item) } }
    },
    restore = { savedList ->
        val destSaver = destinationSaver()
        mutableStateListOf<Destination>().apply {
            addAll(savedList.mapNotNull { destSaver.restore(it) })
        }
    }
)

class MainActivity : ComponentActivity() {
    private val desiredLanguage: AppLanguage
        get() = (applicationContext as DiscountCardsApp)
            .preferencesRepository.getAppLanguageSynchronously()

    override fun attachBaseContext(newBase: Context) {
        val app = newBase.applicationContext as DiscountCardsApp
        val localizedContext = app.preferencesRepository.getLocalizedContext(newBase)
        super.attachBaseContext(localizedContext)
    }

    // Safety net: if the activity is recreated without a new process (e.g. via
    // recreate() or configuration change), attachBaseContext is not called
    // again, so the old locale could linger. Recreate once more if the stored
    // language changed while this process was alive.
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        val newLang = (applicationContext as DiscountCardsApp)
            .preferencesRepository.getAppLanguageSynchronously()
        if (newLang != desiredLanguage && !isChangingConfigurations) {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = applicationContext as DiscountCardsApp
        val preferencesRepository = app.preferencesRepository
        val initialCardId = intent?.getLongExtra("card_id", -1L) ?: -1L
        val incomingUri = when (intent?.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
            else -> intent?.data
        }

        if (savedInstanceState == null && incomingUri != null) {
            intent?.action = null
            intent?.data = null
        }

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
                    DiscountCardsNavigation(
                        initialCardId = initialCardId,
                        initialImportUri = if (savedInstanceState == null) incomingUri else null,
                        onLanguageChange = { recreate() }
                    )
                }
            }
        }
    }
}

@Composable
fun DiscountCardsNavigation(
    initialCardId: Long = -1L,
    initialImportUri: Uri? = null,
    onLanguageChange: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as DiscountCardsApp
    val repository = app.repository
    val preferencesRepository = app.preferencesRepository

    val preferences by preferencesRepository.userPreferencesFlow
        .collectAsStateWithLifecycle(initialValue = UserPreferences())

    val backStack = rememberSaveable(saver = backStackSaver) {
        mutableStateListOf<Destination>().apply {
            add(Destination.CardList)
            if (initialCardId != -1L) {
                add(Destination.CardDetail(initialCardId))
            }
        }
    }
    val currentDestination = backStack.lastOrNull() ?: Destination.CardList

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeAt(backStack.lastIndex)
    }

    when (currentDestination) {
        is Destination.CardList -> {
            val viewModel: CardListViewModel = viewModel(
                factory = CardListViewModelFactory(repository, preferencesRepository)
            )

            var handledImportUri by remember { mutableStateOf(initialImportUri) }
            LaunchedEffect(handledImportUri) {
                handledImportUri?.let { uri ->
                    viewModel.onImportFileSelected(uri, context.contentResolver)
                    handledImportUri = null
                }
            }

            CardListScreen(
                viewModel = viewModel,
                onCardClick = { cardId ->
                    backStack.add(Destination.CardDetail(cardId))
                },
                onEditCardClick = { cardId ->
                    backStack.add(Destination.AddEditCard(cardId))
                },
                onAddCardClick = {
                    backStack.add(Destination.AddEditCard(null))
                },
                onArchiveClick = {
                    backStack.add(Destination.Archive)
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
                },
                showAddButton = preferences.showAddButton,
                showBarcodesInList = preferences.showBarcodesInList
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
                },
                onLanguageChange = { language ->
                    // Recreate the activity to apply the new locale via attachBaseContext
                    onLanguageChange()
                }
            )
        }
        is Destination.Archive -> {
            var archivedSearchQuery by remember { mutableStateOf("") }
            // Rebuild the source flow when the search query changes, then collect it.
            val sourceFlow = remember(archivedSearchQuery) {
                if (archivedSearchQuery.isBlank()) {
                    repository.getArchivedCards()
                } else {
                    repository.searchArchivedCards(archivedSearchQuery)
                }
            }

            val archivedCardsState = sourceFlow.collectAsStateWithLifecycle(initialValue = emptyList())
            val archivedCards = archivedCardsState.value
            // Track loading: true until the first emission from the flow.
            var isArchivedLoaded by remember(sourceFlow) { mutableStateOf(false) }
            LaunchedEffect(sourceFlow) {
                sourceFlow.collect {
                    isArchivedLoaded = true
                }
            }
            val isLoadingArchived = !isArchivedLoaded

            ArchiveScreen(
                cards = archivedCards,
                isLoading = isLoadingArchived,
                searchQuery = archivedSearchQuery,
                onSearchQueryChange = { archivedSearchQuery = it },
                onCardClick = { cardId ->
                    backStack.add(Destination.CardDetail(cardId))
                },
                onAddCardClick = {
                    backStack.add(Destination.AddEditCard(null))
                },
                onBackClick = {
                    if (backStack.size > 1) {
                        backStack.removeAt(backStack.lastIndex)
                    }
                },
                showBarcodesInList = preferences.showBarcodesInList
            )
        }
    }
}
