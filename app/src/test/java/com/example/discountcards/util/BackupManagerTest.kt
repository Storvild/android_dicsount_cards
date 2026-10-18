package com.example.discountcards.util

import com.example.discountcards.data.local.entity.DiscountCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class BackupManagerTest {

    private val backupManager = BackupManager()

    @Test
    fun testSerializeAndDeserialize() {
        val cards = listOf(
            DiscountCard(id = 1, storeName = "Lenta", cardNumber = "12345", barcodeFormat = "EAN_13", note = "Bonus card"),
            DiscountCard(id = 2, storeName = "Auchan", cardNumber = "67890", barcodeFormat = "QR_CODE")
        )

        val json = backupManager.serialize(cards)
        assertTrue("JSON should contain Lenta", json.contains("Lenta"))
        assertTrue("JSON should contain Auchan", json.contains("Auchan"))

        val result = backupManager.deserialize(json)
        assertTrue("Deserialization should succeed", result.isSuccess)

        val restoredCards = result.getOrNull()!!
        assertEquals(2, restoredCards.size)
        assertEquals("Lenta", restoredCards[0].storeName)
        assertEquals("Auchan", restoredCards[1].storeName)
    }

    @Test
    fun testStreamReadAndWrite() {
        val cards = listOf(
            DiscountCard(id = 10, storeName = "Pyaterochka", cardNumber = "555444", barcodeFormat = "CODE_128")
        )

        val outputStream = ByteArrayOutputStream()
        val writeResult = backupManager.writeToStream(outputStream, cards)
        assertTrue("Stream write should succeed", writeResult.isSuccess)

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val readResult = backupManager.readFromStream(inputStream)
        assertTrue("Stream read should succeed", readResult.isSuccess)

        val readCards = readResult.getOrNull()!!
        assertEquals(1, readCards.size)
        assertEquals("Pyaterochka", readCards[0].storeName)
    }

    @Test
    fun testDeserializeInvalidJsonReturnsFailure() {
        val invalidJson = "this is not json"
        val result = backupManager.deserialize(invalidJson)
        assertTrue("Deserialization should fail on invalid JSON", result.isFailure)
    }

    @Test
    fun testDeserializeEmptyJsonReturnsFailure() {
        val emptyJson = ""
        val result = backupManager.deserialize(emptyJson)
        assertTrue("Deserialization should fail on empty string", result.isFailure)
    }
}
