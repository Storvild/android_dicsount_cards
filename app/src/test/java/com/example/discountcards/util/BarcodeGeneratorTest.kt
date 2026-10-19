package com.example.discountcards.util

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BarcodeGeneratorTest {

    @Test
    fun testParseSupportedFormats() {
        for (formatName in BarcodeGenerator.SUPPORTED_FORMATS) {
            if (formatName == "PHONE") {
                val parsed = BarcodeGenerator.parseFormat(formatName)
                assertNull("PHONE format should not parse to ZXing BarcodeFormat", parsed)
            } else {
                val parsed = BarcodeGenerator.parseFormat(formatName)
                assertNotNull("Format $formatName should be parseable", parsed)
            }
        }
    }

    @Test
    fun testBarcodeEncodingFormats() {
        val testCases = mapOf(
            "QR_CODE" to "https://example.com/card/123",
            "CODE_128" to "DISCOUNT123456",
            "EAN_13" to "4006381333931",
            "EAN_8" to "96385074",
            "UPC_A" to "012345678905",
            "CODE_39" to "CARD123",
            "PDF_417" to "PDF417-TEST-CARD-DATA",
            "AZTEC" to "AZTEC-DATA-TEST"
        )

        for ((format, content) in testCases) {
            val result = BarcodeGenerator.encodeToBitMatrixResult(content, format)
            assertTrue("Encoding for $format with content $content should succeed: ${result.exceptionOrNull()?.message}", result.isSuccess)
            val matrix = result.getOrNull()
            assertNotNull("BitMatrix for $format should not be null", matrix)
            assertTrue("Width should be > 0", matrix!!.width > 0)
            assertTrue("Height should be > 0", matrix.height > 0)
        }
    }

    @Test
    fun testPhoneFormatFailsEncoding() {
        val result = BarcodeGenerator.encodeToBitMatrixResult("+79990000000", "PHONE")
        assertTrue("PHONE format should fail bit matrix encoding as it is displayed as text", result.isFailure)
    }

    @Test
    fun testAutoCalculateEan13Checksum() {
        // 12 digits input
        val result = BarcodeGenerator.encodeToBitMatrixResult("400638133393", "EAN_13")
        assertTrue("EAN_13 with 12 digits should auto-calculate checksum and succeed", result.isSuccess)
    }

    @Test
    fun testEmptyContentFails() {
        val result = BarcodeGenerator.encodeToBitMatrixResult("", "QR_CODE")
        assertTrue("Empty content should fail", result.isFailure)
    }
}
