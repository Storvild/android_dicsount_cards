package com.example.discountcards.ui.detail

import com.example.discountcards.data.local.dao.DiscountCardDao
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.data.repository.CardRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeDao : DiscountCardDao {
        val cards = mutableListOf<DiscountCard>()

        override fun getAllCards(): Flow<List<DiscountCard>> = flowOf(cards.toList())

        override fun searchCards(query: String): Flow<List<DiscountCard>> = flowOf(cards)

        override fun getCardById(id: Long): Flow<DiscountCard?> = flowOf(cards.find { it.id == id })

        override suspend fun getCardByIdDirect(id: Long): DiscountCard? = cards.find { it.id == id }

        override suspend fun insertCard(card: DiscountCard): Long {
            cards.add(card)
            return card.id
        }

        override suspend fun updateCard(card: DiscountCard) {
            val index = cards.indexOfFirst { it.id == card.id }
            if (index != -1) cards[index] = card
        }

        override suspend fun deleteCard(card: DiscountCard) {
            cards.removeAll { it.id == card.id }
        }

        override suspend fun deleteCardById(id: Long) {
            cards.removeAll { it.id == id }
        }

        override suspend fun deleteAll() {
            cards.clear()
        }

        override suspend fun insertAll(cards: List<DiscountCard>) {
            this.cards.addAll(cards)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadCard() = runTest {
        val dao = FakeDao()
        dao.cards.add(
            DiscountCard(
                id = 42,
                storeName = "Spar",
                cardNumber = "1234567890128",
                barcodeFormat = "EAN_13",
                note = "Gold Card"
            )
        )

        val repository = CardRepositoryImpl(dao)
        val viewModel = CardDetailViewModel(
            repository = repository,
            cardId = 42,
            ioDispatcher = testDispatcher
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.card)
        assertEquals("Spar", state.card?.storeName)
        assertEquals("1234567890128", state.card?.cardNumber)
    }

    @Test
    fun testCardNotFound() = runTest {
        val dao = FakeDao()
        val repository = CardRepositoryImpl(dao)
        val viewModel = CardDetailViewModel(
            repository = repository,
            cardId = 999,
            ioDispatcher = testDispatcher
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.card)
        assertEquals("Card not found", state.error)
    }

    @Test
    fun testDeleteCardDialogAndAction() = runTest {
        val dao = FakeDao()
        dao.cards.add(DiscountCard(id = 5, storeName = "Magnit", cardNumber = "555", barcodeFormat = "QR_CODE"))

        val repository = CardRepositoryImpl(dao)
        val viewModel = CardDetailViewModel(
            repository = repository,
            cardId = 5,
            ioDispatcher = testDispatcher
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.showDeleteDialog()
        assertTrue(viewModel.uiState.value.isDeleteDialogVisible)

        viewModel.hideDeleteDialog()
        assertFalse(viewModel.uiState.value.isDeleteDialogVisible)

        viewModel.showDeleteDialog()
        viewModel.deleteCard()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isDeleteDialogVisible)
        assertTrue(state.isDeleted)
        assertEquals(0, dao.cards.size)
    }
}
