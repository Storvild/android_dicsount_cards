package com.example.discountcards.ui.list

import com.example.discountcards.data.local.dao.DiscountCardDao
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.data.repository.CardRepositoryImpl
import com.example.discountcards.util.ImportMode
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeDao : DiscountCardDao {
        val cards = mutableListOf<DiscountCard>()
        private var nextId = 1L

        override fun getAllCards(): Flow<List<DiscountCard>> = flowOf(cards.toList())

        override fun searchCards(query: String): Flow<List<DiscountCard>> {
            return flowOf(cards.filter {
                it.storeName.contains(query, ignoreCase = true) ||
                        it.note.contains(query, ignoreCase = true) ||
                        it.cardNumber.contains(query, ignoreCase = true)
            })
        }

        override fun getCardById(id: Long): Flow<DiscountCard?> = flowOf(cards.find { it.id == id })

        override suspend fun getCardByIdDirect(id: Long): DiscountCard? = cards.find { it.id == id }

        override suspend fun insertCard(card: DiscountCard): Long {
            val insertedId = if (card.id == 0L) nextId++ else card.id
            val insertedCard = card.copy(id = insertedId)
            cards.add(insertedCard)
            return insertedId
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
            cards.forEach { insertCard(it) }
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
    fun testInitialStateAndSearch() = runTest {
        val dao = FakeDao()
        dao.cards.add(DiscountCard(id = 1, storeName = "Lenta", cardNumber = "12345", barcodeFormat = "EAN_13", note = "Grocery"))
        dao.cards.add(DiscountCard(id = 2, storeName = "Pyaterochka", cardNumber = "67890", barcodeFormat = "QR_CODE", note = "Supermarket"))

        val repository = CardRepositoryImpl(dao)
        val viewModel = CardListViewModel(repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals(2, state.cards.size)

        viewModel.onSearchQueryChange("Lenta")
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertEquals("Lenta", state.searchQuery)
        assertEquals(1, state.cards.size)
        assertEquals("Lenta", state.cards[0].storeName)

        viewModel.onClearSearch()
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertEquals(2, state.cards.size)
    }

    @Test
    fun testExportAndImportInViewModel() = runTest {
        val dao = FakeDao()
        dao.cards.add(DiscountCard(id = 1, storeName = "Auchan", cardNumber = "111222", barcodeFormat = "CODE_128"))

        val repository = CardRepositoryImpl(dao)
        val viewModel = CardListViewModel(repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.exportCards()
        advanceUntilIdle()

        val exportedJson = viewModel.uiState.value.exportedJson
        assertNotNull(exportedJson)

        viewModel.clearExportedJson()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.exportedJson)

        val importJson = """
            [
                {
                    "id": 10,
                    "storeName": "Dixy",
                    "cardNumber": "999888",
                    "barcodeFormat": "EAN_8",
                    "note": "Imported"
                }
            ]
        """.trimIndent()

        viewModel.importCards(importJson, ImportMode.MERGE)
        advanceUntilIdle()

        val msg = viewModel.uiState.value.userMessage
        assertNotNull(msg)
        assertEquals("IMPORT_SUCCESS:1", msg)
        assertEquals(2, dao.cards.size)
    }
}
