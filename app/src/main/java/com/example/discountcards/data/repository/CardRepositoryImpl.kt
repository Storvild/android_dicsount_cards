package com.example.discountcards.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.discountcards.data.local.dao.DiscountCardDao
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.util.BackupManager
import com.example.discountcards.util.ImportMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CardRepositoryImpl(
    private val dao: DiscountCardDao,
    private val backupManager: BackupManager = BackupManager()
) : CardRepository {

    override fun getAllCards(): Flow<List<DiscountCard>> = dao.getAllCards()

    override fun searchCards(query: String): Flow<List<DiscountCard>> = dao.searchCards(query)

    override fun getCardById(id: Long): Flow<DiscountCard?> = dao.getCardById(id)

    override suspend fun getCardByIdDirect(id: Long): DiscountCard? = dao.getCardByIdDirect(id)

    override suspend fun insertCard(card: DiscountCard): Long = dao.insertCard(card)

    override suspend fun updateCard(card: DiscountCard) = dao.updateCard(card)

    override suspend fun deleteCard(card: DiscountCard) = dao.deleteCard(card)

    override suspend fun deleteCardById(id: Long) = dao.deleteCardById(id)

    override suspend fun deleteAll() = dao.deleteAll()

    override suspend fun insertAll(cards: List<DiscountCard>) = dao.insertAll(cards)

    override suspend fun exportToJson(): String {
        val cards = dao.getAllCards().first()
        return backupManager.serialize(cards)
    }

    override suspend fun importFromJson(jsonString: String, mode: ImportMode): Result<Int> {
        return runCatching {
            val cards = backupManager.deserialize(jsonString).getOrThrow()
            performImport(cards, mode)
        }
    }

    override suspend fun exportToUri(contentResolver: ContentResolver, uri: Uri): Result<Unit> {
        return runCatching {
            val cards = dao.getAllCards().first()
            backupManager.writeToUri(contentResolver, uri, cards).getOrThrow()
        }
    }

    override suspend fun importFromUri(contentResolver: ContentResolver, uri: Uri, mode: ImportMode): Result<Int> {
        return runCatching {
            val cards = backupManager.readFromUri(contentResolver, uri).getOrThrow()
            performImport(cards, mode)
        }
    }

    private suspend fun performImport(cards: List<DiscountCard>, mode: ImportMode): Int {
        if (mode == ImportMode.OVERWRITE) {
            dao.deleteAll()
        }
        val cardsToInsert = cards.map { it.copy(id = 0) }
        dao.insertAll(cardsToInsert)
        return cardsToInsert.size
    }
}
