package com.example.discountcards.ui.list

// TODO: Enable these tests after the CardListScreen refactoring.
// The real CardListViewModel requires Room + DataStore, which is too heavy for
// a smoke test. We'll use a fake ViewModel injected via ViewModelProvider.Factory.
//
// import androidx.compose.ui.test.assertIsDisplayed
// import androidx.compose.ui.test.junit4.createComposeRule
// import androidx.compose.ui.test.onNodeWithText
// import androidx.compose.ui.test.performClick
// import androidx.lifecycle.ViewModel
// import androidx.lifecycle.ViewModelProvider
// import androidx.test.ext.junit.runners.AndroidJUnit4
// import com.example.discountcards.data.local.entity.DiscountCard
// import com.example.discountcards.ui.theme.DiscountCardsTheme
// import org.junit.Assert
// import org.junit.Rule
// import org.junit.Test
// import org.junit.runner.RunWith
//
// @RunWith(AndroidJUnit4::class)
// class CardListScreenSmokeTest {
//     @get:Rule
//     val rule = createComposeRule()
//
//     // Fake ViewModel that returns a fixed list of cards
//     private class FakeCardListViewModel : CardListViewModel() {
//         // Override uiState to return a fixed state
//     }
//
//     @Test
//     fun cardListScreen_emptyState_shown_whenNoCards() { ... }
//
//     @Test
//     fun cardListScreen_showsCards_whenStateNotEmpty() { ... }
//
//     @Test
//     fun cardListScreen_searchQuery_filters() { ... }
//
//     @Test
//     fun cardListScreen_addButton_triggersCallback() { ... }
//
//     @Test
//     fun cardListScreen_fab_triggersAddCard() { ... }
// }
