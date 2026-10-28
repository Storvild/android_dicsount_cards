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
    fun testSerializeCsvAndDeserialize() {
        val cards = listOf(
            DiscountCard(id = 1, storeName = "Пятёрочка", cardNumber = "123456", barcodeFormat = "EAN_13", cvc = "999", note = "VIP"),
            DiscountCard(id = 2, storeName = "Магнит", cardNumber = "654321", barcodeFormat = "CODE_128")
        )

        val csv = backupManager.serialize(cards, ExportFormat.CSV)
        assertTrue("CSV should contain header", csv.contains("Название,Код,Тип кода,CVC,Примечание"))
        assertTrue("CSV should contain Пятёрочка", csv.contains("Пятёрочка"))

        val result = backupManager.deserialize(csv)
        assertTrue("Deserialization of generated CSV should succeed", result.isSuccess)

        val restored = result.getOrNull()!!
        assertEquals(2, restored.size)
        assertEquals("Пятёрочка", restored[0].storeName)
        assertEquals("123456", restored[0].cardNumber)
        assertEquals("EAN_13", restored[0].barcodeFormat)
        assertEquals("999", restored[0].cvc)
        assertEquals("VIP", restored[0].note)
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
    fun testDeserializeInvalidContentReturnsFailure() {
        val invalidString = "    \n  "
        val result = backupManager.deserialize(invalidString)
        assertTrue("Deserialization should fail on empty or whitespace string", result.isFailure)
    }

    @Test
    fun testDeserializeEmptyJsonReturnsFailure() {
        val emptyJson = ""
        val result = backupManager.deserialize(emptyJson)
        assertTrue("Deserialization should fail on empty string", result.isFailure)
    }

    @Test
    fun testDeserializeCsv5Columns() {
        val csv = """
            Название,Код,Тип кода,CVC,Примечание
            Пятёрочка,1234567890123,EAN_13,123,Карта скидок
            Магнит,987654321,CODE_128,777,
        """.trimIndent()

        val result = backupManager.deserialize(csv)
        assertTrue("Deserialization of CSV should succeed", result.isSuccess)

        val cards = result.getOrNull()!!
        assertEquals(2, cards.size)

        assertEquals("Пятёрочка", cards[0].storeName)
        assertEquals("1234567890123", cards[0].cardNumber)
        assertEquals("EAN_13", cards[0].barcodeFormat)
        assertEquals("123", cards[0].cvc)
        assertEquals("Карта скидок", cards[0].note)

        assertEquals("Магнит", cards[1].storeName)
        assertEquals("987654321", cards[1].cardNumber)
        assertEquals("CODE_128", cards[1].barcodeFormat)
        assertEquals("777", cards[1].cvc)
        assertEquals("", cards[1].note)
    }

    @Test
    fun testDeserializeCsv3ColumnsAnd2Columns() {
        val csv = """
            Перекрёсток,770001,QR
            Лента,555111
        """.trimIndent()

        val result = backupManager.deserialize(csv)
        assertTrue("Deserialization of CSV should succeed", result.isSuccess)

        val cards = result.getOrNull()!!
        assertEquals(2, cards.size)

        assertEquals("Перекрёсток", cards[0].storeName)
        assertEquals("770001", cards[0].cardNumber)
        assertEquals("QR_CODE", cards[0].barcodeFormat)
        assertEquals("", cards[0].cvc)
        assertEquals("", cards[0].note)

        assertEquals("Лента", cards[1].storeName)
        assertEquals("555111", cards[1].cardNumber)
        assertEquals("CODE_128", cards[1].barcodeFormat)
    }

    @Test
    fun testDeserializeCsvSemicolonSeparatorWithHeader() {
        val csv = """
            Название;Код;Тип кода;CVC;Примечание
            Золотое Яблоко;888111;PHONE;000;Мой телефон
        """.trimIndent()

        val result = backupManager.deserialize(csv)
        assertTrue("Deserialization of CSV with semicolons should succeed", result.isSuccess)

        val cards = result.getOrNull()!!
        assertEquals(1, cards.size)

        assertEquals("Золотое Яблоко", cards[0].storeName)
        assertEquals("888111", cards[0].cardNumber)
        assertEquals("PHONE", cards[0].barcodeFormat)
        assertEquals("000", cards[0].cvc)
        assertEquals("Мой телефон", cards[0].note)
    }

    @Test
    fun testDeserializeCsvWithQuotes() {
        val csv = "\"Магазин \"\"X\"\"\",\"123\",\"EAN_13\",\"\",\"Примечание с кавычками\""

        val result = backupManager.deserialize(csv)
        assertTrue("Deserialization of quoted CSV should succeed", result.isSuccess)

        val cards = result.getOrNull()!!
        assertEquals(1, cards.size)

        assertEquals("Магазин \"X\"", cards[0].storeName)
        assertEquals("123", cards[0].cardNumber)
        assertEquals("EAN_13", cards[0].barcodeFormat)
        assertEquals("Примечание с кавычками", cards[0].note)
    }

    @Test
    fun testDeserializeCsvFromStream() {
        val csv = "ВкусВилл,112233,EAN_13\n"
        val inputStream = ByteArrayInputStream(csv.toByteArray(Charsets.UTF_8))

        val result = backupManager.readFromStream(inputStream)
        assertTrue("Stream read of CSV should succeed", result.isSuccess)

        val cards = result.getOrNull()!!
        assertEquals(1, cards.size)
        assertEquals("ВкусВилл", cards[0].storeName)
        assertEquals("112233", cards[0].cardNumber)
    }
}
