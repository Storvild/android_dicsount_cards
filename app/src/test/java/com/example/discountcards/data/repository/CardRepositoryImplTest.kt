package com.example.discountcards.data.repository

import com.example.discountcards.data.local.dao.DiscountCardDao
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.util.ImportMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CardRepositoryImplTest {

    private class FakeDiscountCardDao : DiscountCardDao {
        val cards = mutableListOf<DiscountCard>()
        private var nextId = 1L

        override fun getAllCardsAlphabetical(): Flow<List<DiscountCard>> {
            return flowOf(cards.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenBy { it.storeName }))
        }

        override fun getAllCardsByFrequency(): Flow<List<DiscountCard>> {
            return flowOf(cards.sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenByDescending { it.usageCount }.thenBy { it.storeName }))
        }

        override fun getAllCards(): Flow<List<DiscountCard>> = getAllCardsAlphabetical()

        override suspend fun getAllCardsDirect(): List<DiscountCard> = cards.toList()

        override fun searchCardsAlphabetical(query: String): Flow<List<DiscountCard>> {
            return flowOf(cards.filter { it.storeName.contains(query, ignoreCase = true) || it.cardNumber.contains(query, ignoreCase = true) }
                .sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenBy { it.storeName }))
        }

        override fun searchCardsByFrequency(query: String): Flow<List<DiscountCard>> {
            return flowOf(cards.filter { it.storeName.contains(query, ignoreCase = true) || it.cardNumber.contains(query, ignoreCase = true) }
                .sortedWith(compareByDescending<DiscountCard> { it.isFavorite }.thenByDescending { it.usageCount }.thenBy { it.storeName }))
        }

        override fun searchCards(query: String): Flow<List<DiscountCard>> = searchCardsAlphabetical(query)

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
            if (index != -1) {
                cards[index] = card
            }
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

    @Test
    fun testExportAndImportJsonMergeMode() = runTest {
        val dao = FakeDiscountCardDao()
        val repository = CardRepositoryImpl(dao)

        val existingCard = DiscountCard(id = 1, storeName = "Lenta", cardNumber = "555111", barcodeFormat = "EAN_13")
        dao.insertCard(existingCard)

        val importJson = """
            [
                {
                    "id": 1,
                    "storeName": "Perekrestok",
                    "cardNumber": "770001",
                    "barcodeFormat": "EAN_13",
                    "note": "VIP card"
                }
            ]
        """.trimIndent()

        val importResult = repository.importFromJson(importJson, ImportMode.MERGE)
        assertTrue("Import should succeed", importResult.isSuccess)
        assertEquals(1, importResult.getOrNull())
        assertEquals("Merge should preserve existing card and add new card", 2, dao.cards.size)
        assertEquals("Lenta", dao.cards[0].storeName)
        assertEquals("Perekrestok", dao.cards[1].storeName)
    }

    @Test
    fun testImportJsonOverwriteMode() = runTest {
        val dao = FakeDiscountCardDao()
        val repository = CardRepositoryImpl(dao)

        dao.insertCard(DiscountCard(id = 1, storeName = "Old Store", cardNumber = "0000", barcodeFormat = "CODE_128"))
        assertEquals(1, dao.cards.size)

        val importJson = """
            [
                {
                    "id": 1,
                    "storeName": "New Store 1",
                    "cardNumber": "1111",
                    "barcodeFormat": "EAN_8"
                },
                {
                    "id": 2,
                    "storeName": "New Store 2",
                    "cardNumber": "2222",
                    "barcodeFormat": "QR_CODE"
                }
            ]
        """.trimIndent()

        val importResult = repository.importFromJson(importJson, ImportMode.OVERWRITE)
        assertTrue("Import should succeed", importResult.isSuccess)
        assertEquals(2, importResult.getOrNull())
        assertEquals("Overwrite should clear old cards and set new ones", 2, dao.cards.size)
        assertEquals("New Store 1", dao.cards[0].storeName)
        assertEquals("New Store 2", dao.cards[1].storeName)
    }

    @Test
    fun testImportInvalidJsonReturnsFailure() = runTest {
        val dao = FakeDiscountCardDao()
        val repository = CardRepositoryImpl(dao)

        val result = repository.importFromJson("invalid json data", ImportMode.MERGE)
        assertTrue("Importing invalid JSON should return failure", result.isFailure)
    }

    @Test
    fun testImportDuplicateStrategyUpdateExisting() = runTest {
        val dao = FakeDiscountCardDao()
        val repository = CardRepositoryImpl(dao)

        val existingCard = DiscountCard(id = 1, storeName = "Lenta", cardNumber = "111111", barcodeFormat = "EAN_13", note = "Old note")
        dao.insertCard(existingCard)

        val importedCard = DiscountCard(id = 0, storeName = "Lenta", cardNumber = "999999", barcodeFormat = "QR_CODE", note = "Updated note")

        val result = repository.importSelectedCards(
            cards = listOf(importedCard),
            mode = ImportMode.MERGE,
            duplicateStrategy = com.example.discountcards.util.DuplicateStrategy.UPDATE_EXISTING
        )

        assertTrue(result.isSuccess)
        assertEquals(1, dao.cards.size)
        assertEquals(1L, dao.cards[0].id)
        assertEquals("999999", dao.cards[0].cardNumber)
        assertEquals("Updated note", dao.cards[0].note)
    }

    @Test
    fun testImportDuplicateStrategySkip() = runTest {
        val dao = FakeDiscountCardDao()
        val repository = CardRepositoryImpl(dao)

        val existingCard = DiscountCard(id = 1, storeName = "Lenta", cardNumber = "111111", barcodeFormat = "EAN_13")
        dao.insertCard(existingCard)

        val importedCard = DiscountCard(id = 0, storeName = "Lenta", cardNumber = "999999", barcodeFormat = "QR_CODE")

        val result = repository.importSelectedCards(
            cards = listOf(importedCard),
            mode = ImportMode.MERGE,
            duplicateStrategy = com.example.discountcards.util.DuplicateStrategy.SKIP
        )

        assertTrue(result.isSuccess)
        assertEquals(1, dao.cards.size)
        assertEquals("111111", dao.cards[0].cardNumber)
    }

}
