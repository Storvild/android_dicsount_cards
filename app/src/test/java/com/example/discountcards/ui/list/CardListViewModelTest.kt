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

        override fun getAllCardsAlphabetical(): Flow<List<DiscountCard>> {
            return flowOf(cards.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenBy { it.storeName }))
        }

        override fun getAllCardsByFrequency(): Flow<List<DiscountCard>> {
            return flowOf(cards.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenByDescending { it.usageCount }.thenBy { it.storeName }))
        }

        override fun getAllCardsByCreationDate(): Flow<List<DiscountCard>> {
            return flowOf(cards.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenByDescending { it.createdAt }.thenBy { it.storeName }))
        }

        override fun getAllCards(): Flow<List<DiscountCard>> = getAllCardsAlphabetical()

        override fun getAllCardsIncludingArchivedAlphabetical(): Flow<List<DiscountCard>> {
            return flowOf(cards.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenBy { it.storeName }))
        }

        override fun getAllCardsIncludingArchivedByFrequency(): Flow<List<DiscountCard>> {
            return flowOf(cards.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenByDescending { it.usageCount }.thenBy { it.storeName }))
        }

        override fun getAllCardsIncludingArchivedByCreationDate(): Flow<List<DiscountCard>> {
            return flowOf(cards.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenByDescending { it.createdAt }.thenBy { it.storeName }))
        }

        override fun getAllCardsIncludingArchived(): Flow<List<DiscountCard>> = getAllCardsIncludingArchivedAlphabetical()

        override suspend fun getAllCardsDirect(): List<DiscountCard> = cards.toList()

        override suspend fun getCardsByNumber(cardNumber: String): List<DiscountCard> = cards.filter { it.cardNumber == cardNumber }

        override fun searchCardsAlphabetical(query: String): Flow<List<DiscountCard>> {
            return flowOf(cards.filter {
                it.storeName.contains(query, ignoreCase = true) ||
                        it.note.contains(query, ignoreCase = true) ||
                        it.cardNumber.contains(query, ignoreCase = true)
            }.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenBy { it.storeName }))
        }

        override fun searchCardsByFrequency(query: String): Flow<List<DiscountCard>> {
            return flowOf(cards.filter {
                it.storeName.contains(query, ignoreCase = true) ||
                        it.note.contains(query, ignoreCase = true) ||
                        it.cardNumber.contains(query, ignoreCase = true)
            }.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenByDescending { it.usageCount }.thenBy { it.storeName }))
        }

        override fun searchCardsByCreationDate(query: String): Flow<List<DiscountCard>> {
            return flowOf(cards.filter {
                it.storeName.contains(query, ignoreCase = true) ||
                        it.note.contains(query, ignoreCase = true) ||
                        it.cardNumber.contains(query, ignoreCase = true)
            }.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenByDescending { it.createdAt }.thenBy { it.storeName }))
        }

        override fun searchCards(query: String): Flow<List<DiscountCard>> = searchCardsAlphabetical(query)

        override fun getArchivedCards(): Flow<List<DiscountCard>> = flowOf(cards.filter { it.isArchived }.sortedWith(compareBy { it.storeName }))

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

        viewModel.confirmExportToText()
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

    @Test
    fun testExportSelectionControls() = runTest {
        val dao = FakeDao()
        dao.cards.add(DiscountCard(id = 1, storeName = "Lenta", cardNumber = "111", barcodeFormat = "EAN_13"))
        dao.cards.add(DiscountCard(id = 2, storeName = "Auchan", cardNumber = "222", barcodeFormat = "QR_CODE"))

        val repository = CardRepositoryImpl(dao)
        val viewModel = CardListViewModel(repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.prepareExportSelection()
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals(true, state.showExportSelectionDialog)
        assertEquals(2, state.selectedExportCardIds.size)

        viewModel.deselectAllExportCards()
        advanceUntilIdle()
        state = viewModel.uiState.value
        assertEquals(0, state.selectedExportCardIds.size)

        viewModel.selectAllExportCards()
        advanceUntilIdle()
        state = viewModel.uiState.value
        assertEquals(2, state.selectedExportCardIds.size)

        viewModel.toggleExportCardSelection(1L)
        advanceUntilIdle()
        state = viewModel.uiState.value
        assertEquals(1, state.selectedExportCardIds.size)
        assertEquals(true, state.selectedExportCardIds.contains(2L))

        viewModel.invertExportCardSelection()
        advanceUntilIdle()
        state = viewModel.uiState.value
        assertEquals(1, state.selectedExportCardIds.size)
        assertEquals(true, state.selectedExportCardIds.contains(1L))
    }

}
