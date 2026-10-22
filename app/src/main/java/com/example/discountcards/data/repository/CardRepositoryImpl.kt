package com.example.discountcards.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.discountcards.data.local.dao.DiscountCardDao
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.util.BackupManager
import com.example.discountcards.util.DuplicateStrategy
import com.example.discountcards.util.ImportMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CardRepositoryImpl(
    private val dao: DiscountCardDao,
    private val backupManager: BackupManager = BackupManager()
) : CardRepository {

    override fun getAllCards(sortOrder: SortOrder): Flow<List<DiscountCard>> {
        return when (sortOrder) {
            SortOrder.ALPHABETICAL -> dao.getAllCardsAlphabetical()
            SortOrder.FREQUENCY -> dao.getAllCardsByFrequency()
            SortOrder.CREATION_DATE -> dao.getAllCardsByCreationDate()
        }
    }

    override fun getAllCardsIncludingArchived(sortOrder: SortOrder): Flow<List<DiscountCard>> {
        return when (sortOrder) {
            SortOrder.ALPHABETICAL -> dao.getAllCardsIncludingArchivedAlphabetical()
            SortOrder.FREQUENCY -> dao.getAllCardsIncludingArchivedByFrequency()
            SortOrder.CREATION_DATE -> dao.getAllCardsIncludingArchivedByCreationDate()
        }
    }

    override fun searchCards(query: String, sortOrder: SortOrder): Flow<List<DiscountCard>> {
        return when (sortOrder) {
            SortOrder.ALPHABETICAL -> dao.searchCardsAlphabetical(query)
            SortOrder.FREQUENCY -> dao.searchCardsByFrequency(query)
            SortOrder.CREATION_DATE -> dao.searchCardsByCreationDate(query)
        }
    }

    override fun getArchivedCards(): Flow<List<DiscountCard>> = dao.getArchivedCards()

    override fun searchArchivedCards(query: String): Flow<List<DiscountCard>> = dao.searchArchivedCards(query)

    override fun getCardById(id: Long): Flow<DiscountCard?> = dao.getCardById(id)

    override suspend fun getCardByIdDirect(id: Long): DiscountCard? = dao.getCardByIdDirect(id)

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        dao.updateFavorite(id, isFavorite)
    }

    override suspend fun setArchived(id: Long, isArchived: Boolean) {
        dao.setArchived(id, isArchived)
    }

    override suspend fun incrementUsageCount(id: Long) {
        dao.incrementUsageCount(id)
    }

    override suspend fun insertCard(card: DiscountCard): Long = dao.insertCard(card)

    override suspend fun updateCard(card: DiscountCard) = dao.updateCard(card)

    override suspend fun deleteCard(card: DiscountCard) = dao.deleteCard(card)

    override suspend fun deleteCardById(id: Long) = dao.deleteCardById(id)

    override suspend fun deleteAll() = dao.deleteAll()

    override suspend fun insertAll(cards: List<DiscountCard>) = dao.insertAll(cards)

    override suspend fun exportToJson(cardsToExport: List<DiscountCard>?): String {
        val cards = cardsToExport ?: dao.getAllCards().first()
        return backupManager.serialize(cards)
    }

    override suspend fun importFromJson(
        jsonString: String,
        mode: ImportMode,
        duplicateStrategy: DuplicateStrategy
    ): Result<Int> {
        return runCatching {
            val cards = backupManager.deserialize(jsonString).getOrThrow()
            performImport(cards, mode, duplicateStrategy)
        }
    }

    override suspend fun exportToUri(
        contentResolver: ContentResolver,
        uri: Uri,
        cardsToExport: List<DiscountCard>?
    ): Result<Unit> {
        return runCatching {
            val cards = cardsToExport ?: dao.getAllCards().first()
            backupManager.writeToUri(contentResolver, uri, cards).getOrThrow()
        }
    }

    override suspend fun importFromUri(
        contentResolver: ContentResolver,
        uri: Uri,
        mode: ImportMode,
        duplicateStrategy: DuplicateStrategy
    ): Result<Int> {
        return runCatching {
            val cards = backupManager.readFromUri(contentResolver, uri).getOrThrow()
            performImport(cards, mode, duplicateStrategy)
        }
    }

    override suspend fun importSelectedCards(
        cards: List<DiscountCard>,
        mode: ImportMode,
        duplicateStrategy: DuplicateStrategy
    ): Result<Int> {
        return runCatching {
            performImport(cards, mode, duplicateStrategy)
        }
    }

    override suspend fun parseJsonCards(jsonString: String): Result<List<DiscountCard>> {
        return backupManager.deserialize(jsonString)
    }

    override suspend fun readJsonCardsFromUri(contentResolver: ContentResolver, uri: Uri): Result<List<DiscountCard>> {
        return backupManager.readFromUri(contentResolver, uri)
    }

    private suspend fun performImport(
        cards: List<DiscountCard>,
        mode: ImportMode,
        duplicateStrategy: DuplicateStrategy
    ): Int {
        if (cards.isEmpty()) return 0

        if (mode == ImportMode.OVERWRITE) {
            dao.deleteAll()
            val cardsToInsert = cards.map { it.copy(id = 0) }
            dao.insertAll(cardsToInsert)
            return cardsToInsert.size
        }

        // MERGE mode logic
        val existingCards = dao.getAllCardsDirect()
        val existingCardsByName = existingCards.associateBy { it.storeName.trim().lowercase() }

        var count = 0
        cards.forEach { importedCard ->
            val key = importedCard.storeName.trim().lowercase()
            val existingCard = existingCardsByName[key]

            if (existingCard != null) {
                when (duplicateStrategy) {
                    DuplicateStrategy.UPDATE_EXISTING -> {
                        val updatedCard = existingCard.copy(
                            cardNumber = importedCard.cardNumber,
                            barcodeFormat = importedCard.barcodeFormat,
                            cvc = importedCard.cvc,
                            note = importedCard.note,
                            cardColor = importedCard.cardColor,
                            iconName = importedCard.iconName,
                            updatedAt = System.currentTimeMillis()
                        )
                        dao.updateCard(updatedCard)
                        count++
                    }
                    DuplicateStrategy.ADD_AS_NEW -> {
                        dao.insertCard(importedCard.copy(id = 0))
                        count++
                    }
                    DuplicateStrategy.SKIP -> {
                        // skip duplicate card
                    }
                }
            } else {
                dao.insertCard(importedCard.copy(id = 0))
                count++
            }
        }
        return count
    }
}

