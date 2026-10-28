package com.example.discountcards.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.util.DuplicateStrategy
import com.example.discountcards.util.ExportFormat
import com.example.discountcards.util.ImportMode
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun getAllCards(sortOrder: SortOrder = SortOrder.ALPHABETICAL): Flow<List<DiscountCard>>
    fun getAllCardsIncludingArchived(sortOrder: SortOrder = SortOrder.ALPHABETICAL): Flow<List<DiscountCard>>
    fun getArchivedCards(): Flow<List<DiscountCard>>
    fun searchCards(query: String, sortOrder: SortOrder = SortOrder.ALPHABETICAL): Flow<List<DiscountCard>>
    fun searchArchivedCards(query: String): Flow<List<DiscountCard>>
    fun getCardById(id: Long): Flow<DiscountCard?>
    suspend fun getCardByIdDirect(id: Long): DiscountCard?
    suspend fun getCardsByNumber(cardNumber: String): List<DiscountCard>
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)
    suspend fun setArchived(id: Long, isArchived: Boolean)
    suspend fun incrementUsageCount(id: Long)
    suspend fun insertCard(card: DiscountCard): Long
    suspend fun updateCard(card: DiscountCard)
    suspend fun deleteCard(card: DiscountCard)
    suspend fun deleteCardById(id: Long)
    suspend fun deleteAll()
    suspend fun insertAll(cards: List<DiscountCard>)

    suspend fun exportToJson(cardsToExport: List<DiscountCard>? = null): String
    suspend fun exportToCsv(cardsToExport: List<DiscountCard>? = null): String
    suspend fun importFromJson(
        jsonString: String,
        mode: ImportMode = ImportMode.MERGE,
        duplicateStrategy: DuplicateStrategy = DuplicateStrategy.UPDATE_EXISTING
    ): Result<Int>
    suspend fun exportToUri(
        contentResolver: ContentResolver,
        uri: Uri,
        cardsToExport: List<DiscountCard>? = null,
        format: ExportFormat = ExportFormat.JSON
    ): Result<Unit>
    suspend fun importFromUri(
        contentResolver: ContentResolver,
        uri: Uri,
        mode: ImportMode = ImportMode.MERGE,
        duplicateStrategy: DuplicateStrategy = DuplicateStrategy.UPDATE_EXISTING
    ): Result<Int>
    suspend fun importSelectedCards(
        cards: List<DiscountCard>,
        mode: ImportMode = ImportMode.MERGE,
        duplicateStrategy: DuplicateStrategy = DuplicateStrategy.UPDATE_EXISTING
    ): Result<Int>
    suspend fun parseJsonCards(jsonString: String): Result<List<DiscountCard>>
    suspend fun readJsonCardsFromUri(contentResolver: ContentResolver, uri: Uri): Result<List<DiscountCard>>
}
