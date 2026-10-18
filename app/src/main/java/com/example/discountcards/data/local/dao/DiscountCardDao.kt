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

    @Query("SELECT * FROM discount_cards ORDER BY storeName ASC")
    fun getAllCards(): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards WHERE storeName LIKE '%' || :query || '%' OR cardNumber LIKE '%' || :query || '%' ORDER BY storeName ASC")
    fun searchCards(query: String): Flow<List<DiscountCard>>

    @Query("SELECT * FROM discount_cards WHERE id = :id")
    fun getCardById(id: Long): Flow<DiscountCard?>

    @Query("SELECT * FROM discount_cards WHERE id = :id")
    suspend fun getCardByIdDirect(id: Long): DiscountCard?

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
