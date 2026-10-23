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

        override fun getAllCardsAlphabetical(): Flow<List<DiscountCard>> {
            return flowOf(cards.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenBy { it.storeName }))
        }

        override fun getAllCardsByFrequency(): Flow<List<DiscountCard>> {
            return flowOf(cards.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenByDescending { it.usageCount }.thenBy { it.storeName }))
        }

        override fun getAllCardsByCreationDate(): Flow<List<DiscountCard>> = flowOf(cards)

        override fun getAllCards(): Flow<List<DiscountCard>> = getAllCardsAlphabetical()

        override fun getAllCardsIncludingArchivedAlphabetical(): Flow<List<DiscountCard>> {
            return flowOf(cards.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenBy { it.storeName }))
        }

        override fun getAllCardsIncludingArchivedByFrequency(): Flow<List<DiscountCard>> {
            return flowOf(cards.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenByDescending { it.usageCount }.thenBy { it.storeName }))
        }

        override fun getAllCardsIncludingArchivedByCreationDate(): Flow<List<DiscountCard>> = flowOf(cards)

        override fun getAllCardsIncludingArchived(): Flow<List<DiscountCard>> = getAllCardsIncludingArchivedAlphabetical()

        override suspend fun getAllCardsDirect(): List<DiscountCard> = cards.toList()
        override suspend fun getAllCardsDirectByFrequency(): List<DiscountCard> = cards.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenByDescending { it.usageCount }.thenBy { it.storeName })
        override suspend fun getAllCardsDirectByCreationDate(): List<DiscountCard> = cards.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenByDescending { it.createdAt }.thenBy { it.storeName })

        override suspend fun getCardsByNumber(cardNumber: String): List<DiscountCard> = cards.filter { it.cardNumber == cardNumber }

        override fun searchCardsAlphabetical(query: String): Flow<List<DiscountCard>> = flowOf(cards)

        override fun searchCardsByFrequency(query: String): Flow<List<DiscountCard>> = flowOf(cards)

        override fun searchCardsByCreationDate(query: String): Flow<List<DiscountCard>> = flowOf(cards)

        override fun searchCards(query: String): Flow<List<DiscountCard>> = flowOf(cards)

        override fun getArchivedCards(): Flow<List<DiscountCard>> = flowOf(cards.filter { it.isArchived })

        override fun searchArchivedCards(query: String): Flow<List<DiscountCard>> = flowOf(cards.filter { it.isArchived && (it.storeName.contains(query, ignoreCase = true) || it.cardNumber.contains(query, ignoreCase = true)) })

        override suspend fun setArchived(id: Long, isArchived: Boolean, updatedAt: Long) {
            val index = cards.indexOfFirst { it.id == id }
            if (index != -1) {
                cards[index] = cards[index].copy(isArchived = isArchived, updatedAt = updatedAt)
            }
        }

        override fun getCardById(id: Long): Flow<DiscountCard?> = flowOf(cards.find { it.id == id })

        override suspend fun getCardByIdDirect(id: Long): DiscountCard? = cards.find { it.id == id }

        override suspend fun updateFavorite(id: Long, isFavorite: Boolean, updatedAt: Long) {
            val index = cards.indexOfFirst { it.id == id }
            if (index != -1) {
                cards[index] = cards[index].copy(isFavorite = isFavorite, updatedAt = updatedAt)
            }
        }

        override suspend fun incrementUsageCount(id: Long) {
            val index = cards.indexOfFirst { it.id == id }
            if (index != -1) {
                cards[index] = cards[index].copy(usageCount = cards[index].usageCount + 1)
            }
        }

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
        assertEquals(1, dao.cards.first().usageCount)
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
