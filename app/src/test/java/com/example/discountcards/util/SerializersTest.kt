package com.example.discountcards.util

import com.example.discountcards.data.local.entity.DiscountCard
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SerializersTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun testColorAndDateSerialization() {
        val card = DiscountCard(
            id = 1,
            storeName = "Test Store",
            cardNumber = "123456",
            barcodeFormat = "EAN_13",
            cardColor = 0xFF1E88E5.toInt(),
            createdAt = 1700000000000L,
            updatedAt = 1700000000000L
        )

        val encoded = json.encodeToString(card)
        assertTrue("JSON should contain storeName", encoded.contains("Test Store"))

        val decoded = json.decodeFromString<DiscountCard>(encoded)
        assertEquals("Test Store", decoded.storeName)
        assertEquals("123456", decoded.cardNumber)
    }

    @Test
    fun testRawLongTimestampDeserialization() {
        val rawJson = """
            {
                "id": 2,
                "storeName": "Legacy Store",
                "cardNumber": "999",
                "barcodeFormat": "QR_CODE",
                "cardColor": "#FF4CAF50",
                "createdAt": "1700000000000",
                "updatedAt": "1700000000000"
            }
        """.trimIndent()

        val decoded = json.decodeFromString<DiscountCard>(rawJson)
        assertEquals("Legacy Store", decoded.storeName)
        assertEquals(1700000000000L, decoded.createdAt)
    }

    @Test
    fun testIso8601DateDeserialization() {
        val rawJson = """
            {
                "id": 3,
                "storeName": "ISO Store",
                "cardNumber": "888",
                "barcodeFormat": "CODE_128",
                "cardColor": "-16711936",
                "createdAt": "2023-11-14T22:13:20",
                "updatedAt": "2023-11-14T22:13:20"
            }
        """.trimIndent()

        val decoded = json.decodeFromString<DiscountCard>(rawJson)
        assertEquals("ISO Store", decoded.storeName)
        assertTrue("createdAt timestamp should be parsed (> 0)", decoded.createdAt > 0)
    }

    @Test
    fun testInvalidColorFallback() {
        val rawJson = """
            {
                "id": 4,
                "storeName": "Invalid Color Store",
                "cardNumber": "777",
                "barcodeFormat": "EAN_8",
                "cardColor": "not_a_color"
            }
        """.trimIndent()

        val decoded = json.decodeFromString<DiscountCard>(rawJson)
        assertEquals("Invalid Color Store", decoded.storeName)
        assertNotNull(decoded.cardColor)
    }
}
