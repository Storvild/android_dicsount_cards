package com.example.discountcards.ui.addedit

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
class AddEditCardViewModelTest {

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
    fun testNewCardInitialState() = runTest {
        val dao = FakeDao()
        val repository = CardRepositoryImpl(dao)
        val viewModel = AddEditCardViewModel(
            repository = repository,
            cardId = null,
            ioDispatcher = testDispatcher
        )

        val state = viewModel.uiState.value
        assertNull(state.cardId)
        assertFalse(state.isEditMode)
        assertEquals("", state.storeName)
        assertEquals("", state.cardNumber)
        assertEquals("CODE_128", state.barcodeFormat)
    }

    @Test
    fun testLoadExistingCardForEdit() = runTest {
        val dao = FakeDao()
        dao.cards.add(
            DiscountCard(
                id = 10,
                storeName = "Perekrestok",
                cardNumber = "777888999",
                barcodeFormat = "EAN_13",
                note = "VIP Card",
                cardColor = 0xFF4CAF50.toInt(),
                iconName = "shopping_cart"
            )
        )

        val repository = CardRepositoryImpl(dao)
        val viewModel = AddEditCardViewModel(
            repository = repository,
            cardId = 10,
            ioDispatcher = testDispatcher
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEditMode)
        assertEquals("Perekrestok", state.storeName)
        assertEquals("777888999", state.cardNumber)
        assertEquals("EAN_13", state.barcodeFormat)
        assertEquals("VIP Card", state.note)
        assertEquals("shopping_cart", state.iconName)
        assertEquals(0xFF4CAF50.toInt(), state.cardColor)
    }

    @Test
    fun testFieldValidationOnSave() = runTest {
        val dao = FakeDao()
        val repository = CardRepositoryImpl(dao)
        val viewModel = AddEditCardViewModel(
            repository = repository,
            cardId = null,
            ioDispatcher = testDispatcher
        )

        viewModel.saveCard()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaved)
        assertNotNull(state.storeNameError)
        assertNotNull(state.cardNumberError)
    }

    @Test
    fun testInsertNewCard() = runTest {
        val dao = FakeDao()
        val repository = CardRepositoryImpl(dao)
        val viewModel = AddEditCardViewModel(
            repository = repository,
            cardId = null,
            ioDispatcher = testDispatcher
        )

        viewModel.onStoreNameChanged("Auchan")
        viewModel.onCardNumberChanged("1122334455")
        viewModel.onBarcodeFormatChanged("QR_CODE")
        viewModel.onNoteChanged("Discount 10%")
        viewModel.onCardColorChanged(0xFFE53935.toInt())

        viewModel.saveCard()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSaved)
        assertEquals(1, dao.cards.size)
        val savedCard = dao.cards.first()
        assertEquals("Auchan", savedCard.storeName)
        assertEquals("1122334455", savedCard.cardNumber)
        assertEquals("QR_CODE", savedCard.barcodeFormat)
        assertEquals("Discount 10%", savedCard.note)
    }

    @Test
    fun testUpdateExistingCard() = runTest {
        val dao = FakeDao()
        dao.cards.add(
            DiscountCard(
                id = 5,
                storeName = "Old Store",
                cardNumber = "111",
                barcodeFormat = "CODE_128"
            )
        )

        val repository = CardRepositoryImpl(dao)
        val viewModel = AddEditCardViewModel(
            repository = repository,
            cardId = 5,
            ioDispatcher = testDispatcher
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onStoreNameChanged("New Store Name")
        viewModel.saveCard()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSaved)
        assertEquals(1, dao.cards.size)
        assertEquals("New Store Name", dao.cards.first().storeName)
    }

    @Test
    fun testBarcodeScannedUpdatesFields() = runTest {
        val dao = FakeDao()
        val repository = CardRepositoryImpl(dao)
        val viewModel = AddEditCardViewModel(
            repository = repository,
            cardId = null,
            ioDispatcher = testDispatcher
        )

        viewModel.openCameraScanner()
        assertTrue(viewModel.uiState.value.isCameraScannerOpen)

        viewModel.onBarcodeScanned("9988776655443", "EAN_13")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isCameraScannerOpen)
        assertEquals("9988776655443", state.cardNumber)
        assertEquals("EAN_13", state.barcodeFormat)
        assertNotNull(state.userMessage)
    }

    @Test
    fun testLoadExistingCardIncludesCreatedAt() = runTest {
        val dao = FakeDao()
        val expectedCreatedAt = 1700000000000L
        dao.cards.add(
            DiscountCard(
                id = 15,
                storeName = "Magnit",
                cardNumber = "123123",
                barcodeFormat = "EAN_13",
                createdAt = expectedCreatedAt
            )
        )

        val repository = CardRepositoryImpl(dao)
        val viewModel = AddEditCardViewModel(
            repository = repository,
            cardId = 15,
            ioDispatcher = testDispatcher
        )

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEditMode)
        assertEquals(expectedCreatedAt, state.createdAt)
    }

    @Test
    fun testScanningDuplicateCardNumberShowsWarning() = runTest {
        val dao = FakeDao()
        dao.cards.add(
            DiscountCard(
                id = 1,
                storeName = "Existing Store",
                cardNumber = "123456789",
                barcodeFormat = "CODE_128"
            )
        )
        val repository = CardRepositoryImpl(dao)
        val viewModel = AddEditCardViewModel(
            repository = repository,
            cardId = null,
            ioDispatcher = testDispatcher
        )

        viewModel.onBarcodeScanned("123456789", "CODE_128")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showDuplicateWarningDialog)
        assertEquals("123456789", state.pendingCardNumber)

        viewModel.onDuplicateWarningConfirmed()
        val confirmedState = viewModel.uiState.value
        assertFalse(confirmedState.showDuplicateWarningDialog)
        assertEquals("123456789", confirmedState.cardNumber)
    }

    @Test
    fun testDismissingDuplicateWarningCancelsScan() = runTest {
        val dao = FakeDao()
        dao.cards.add(
            DiscountCard(
                id = 1,
                storeName = "Existing Store",
                cardNumber = "123456789",
                barcodeFormat = "CODE_128"
            )
        )
        val repository = CardRepositoryImpl(dao)
        val viewModel = AddEditCardViewModel(
            repository = repository,
            cardId = null,
            ioDispatcher = testDispatcher
        )

        viewModel.onBarcodeScanned("123456789", "CODE_128")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showDuplicateWarningDialog)

        viewModel.onDuplicateWarningDismissed()
        val dismissedState = viewModel.uiState.value
        assertFalse(dismissedState.showDuplicateWarningDialog)
        assertEquals("", dismissedState.cardNumber)
    }
}
