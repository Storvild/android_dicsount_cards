package com.example.discountcards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.discountcards.ui.AddEditCardViewModelFactory
import com.example.discountcards.ui.CardDetailViewModelFactory
import com.example.discountcards.ui.CardListViewModelFactory
import com.example.discountcards.ui.addedit.AddEditCardScreen
import com.example.discountcards.ui.addedit.AddEditCardViewModel
import com.example.discountcards.ui.detail.CardDetailScreen
import com.example.discountcards.ui.detail.CardDetailViewModel
import com.example.discountcards.ui.list.CardListScreen
import com.example.discountcards.ui.list.CardListViewModel
import com.example.discountcards.ui.theme.DiscountCardsTheme

sealed interface Destination {
    data object CardList : Destination
    data class CardDetail(val cardId: Long) : Destination
    data class AddEditCard(val cardId: Long? = null) : Destination
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DiscountCardsTheme {
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

    val backStack = remember { mutableStateListOf<Destination>(Destination.CardList) }
    val currentDestination = backStack.lastOrNull() ?: Destination.CardList

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeAt(backStack.lastIndex)
    }

    when (currentDestination) {
        is Destination.CardList -> {
            val viewModel: CardListViewModel = viewModel(
                factory = CardListViewModelFactory(repository)
            )
            CardListScreen(
                viewModel = viewModel,
                onCardClick = { cardId ->
                    backStack.add(Destination.CardDetail(cardId))
                },
                onAddCardClick = {
                    backStack.add(Destination.AddEditCard(null))
                }
            )
        }
        is Destination.CardDetail -> {
            val cardId = currentDestination.cardId
            val viewModel: CardDetailViewModel = viewModel(
                key = "card_detail_$cardId",
                factory = CardDetailViewModelFactory(repository, cardId)
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
                key = "add_edit_card_${cardId ?: "new"}",
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
    }
}
