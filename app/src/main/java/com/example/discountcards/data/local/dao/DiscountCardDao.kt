package com.example.discountcards.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.discountcards.data.local.entity.DiscountCard
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscountCardDao {

    @Query("SELECT * FROM discount_cards WHERE isArchived = 0 ORDER BY isFavorite DESC, storeName ASC")
    fun getAllCardsAlphabetical(): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards WHERE isArchived = 0 ORDER BY isFavorite DESC, usageCount DESC, storeName ASC")
    fun getAllCardsByFrequency(): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards WHERE isArchived = 0 ORDER BY isFavorite DESC, createdAt DESC, storeName ASC")
    fun getAllCardsByCreationDate(): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards WHERE isArchived = 0 ORDER BY isFavorite DESC, storeName ASC")
    fun getAllCards(): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards ORDER BY isFavorite DESC, storeName ASC")
    fun getAllCardsIncludingArchivedAlphabetical(): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards ORDER BY isFavorite DESC, usageCount DESC, storeName ASC")
    fun getAllCardsIncludingArchivedByFrequency(): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards ORDER BY isFavorite DESC, createdAt DESC, storeName ASC")
    fun getAllCardsIncludingArchivedByCreationDate(): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards ORDER BY isFavorite DESC, storeName ASC")
    fun getAllCardsIncludingArchived(): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards WHERE isArchived = 0 ORDER BY storeName ASC")
    suspend fun getAllCardsDirect(): List<DiscountCard>

    @Query("SELECT * FROM discount_cards WHERE isArchived = 1 ORDER BY storeName ASC")
    fun getArchivedCards(): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards WHERE isArchived = 1 AND (storeName LIKE '%' || :query || '%' OR cardNumber LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%') ORDER BY storeName ASC")
    fun searchArchivedCards(query: String): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards WHERE isArchived = 0 AND (storeName LIKE '%' || :query || '%' OR cardNumber LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%') ORDER BY isFavorite DESC, storeName ASC")
    fun searchCardsAlphabetical(query: String): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards WHERE isArchived = 0 AND (storeName LIKE '%' || :query || '%' OR cardNumber LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%') ORDER BY isFavorite DESC, usageCount DESC, storeName ASC")
    fun searchCardsByFrequency(query: String): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards WHERE isArchived = 0 AND (storeName LIKE '%' || :query || '%' OR cardNumber LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%') ORDER BY isFavorite DESC, createdAt DESC, storeName ASC")
    fun searchCardsByCreationDate(query: String): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards WHERE isArchived = 0 AND (storeName LIKE '%' || :query || '%' OR cardNumber LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%') ORDER BY isFavorite DESC, storeName ASC")
    fun searchCards(query: String): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards WHERE id = :id")
    fun getCardById(id: Long): Flow<DiscountCard?>

    @Query("SELECT * FROM discount_cards WHERE id = :id")
    suspend fun getCardByIdDirect(id: Long): DiscountCard?

    @Query("SELECT * FROM discount_cards WHERE cardNumber = :cardNumber")
    suspend fun getCardsByNumber(cardNumber: String): List<DiscountCard>

    @Query("UPDATE discount_cards SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE discount_cards SET isArchived = :isArchived, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setArchived(id: Long, isArchived: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE discount_cards SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsageCount(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: DiscountCard): Long

    @Update
    suspend fun updateCard(card: DiscountCard)

    @Delete
    suspend fun deleteCard(card: DiscountCard)

    @Query("DELETE FROM discount_cards WHERE id = :id")
    suspend fun deleteCardById(id: Long)

    @Query("DELETE FROM discount_cards")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<DiscountCard>)
}
