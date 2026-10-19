package com.example.discountcards.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.util.ImportMode
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun getAllCards(sortOrder: SortOrder = SortOrder.ALPHABETICAL): Flow<List<DiscountCard>>
    fun searchCards(query: String, sortOrder: SortOrder = SortOrder.ALPHABETICAL): Flow<List<DiscountCard>>
    fun getCardById(id: Long): Flow<DiscountCard?>
    suspend fun getCardByIdDirect(id: Long): DiscountCard?
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)
    suspend fun incrementUsageCount(id: Long)
    suspend fun insertCard(card: DiscountCard): Long
    suspend fun updateCard(card: DiscountCard)
    suspend fun deleteCard(card: DiscountCard)
    suspend fun deleteCardById(id: Long)
    suspend fun deleteAll()
    suspend fun insertAll(cards: List<DiscountCard>)

    suspend fun exportToJson(): String
    suspend fun importFromJson(jsonString: String, mode: ImportMode = ImportMode.MERGE): Result<Int>
    suspend fun exportToUri(contentResolver: ContentResolver, uri: Uri): Result<Unit>
    suspend fun importFromUri(contentResolver: ContentResolver, uri: Uri, mode: ImportMode): Result<Int>
}
