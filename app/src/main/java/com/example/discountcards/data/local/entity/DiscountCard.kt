package com.example.discountcards.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "discount_cards")
data class DiscountCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val storeName: String,
    val cardNumber: String,
    val barcodeFormat: String,
    val note: String = "",
    val cardColor: Int = 0xFF1E88E5.toInt(),
    val iconName: String = "",
    val isFavorite: Boolean = false,
    val cvc: String = "",
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun redact(
        includeCvc: Boolean,
        includeUsageCount: Boolean
    ): DiscountCard = copy(
        cvc = if (includeCvc) cvc else "",
        usageCount = if (includeUsageCount) usageCount else 0
    )
}
