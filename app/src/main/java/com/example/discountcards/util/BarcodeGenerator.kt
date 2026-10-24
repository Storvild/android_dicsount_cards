package com.example.discountcards.util

import android.graphics.Bitmap
import android.graphics.Color
import android.util.LruCache
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.util.EnumMap

object BarcodeGenerator {

    val SUPPORTED_FORMATS = listOf(
        "QR_CODE",
        "CODE_128",
        "EAN_13",
        "EAN_8",
        "UPC_A",
        "CODE_39",
        "PDF_417",
        "AZTEC",
        "PHONE"
    )

    private val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSizeKb = (maxMemoryKb / 8).coerceIn(2048, 16384)

    private val barcodeCache = object : LruCache<String, Bitmap>(cacheSizeKb) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    fun generateBarcodeCached(
        content: String,
        format: String,
        width: Int = 600,
        height: Int = 300,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        val cacheKey = "${format}_${content}_${width}x${height}_${foregroundColor}_${backgroundColor}"
        synchronized(barcodeCache) {
            barcodeCache.get(cacheKey)?.let { return it }
        }
        val bitmap = generateBarcode(content, format, width, height, foregroundColor, backgroundColor)
        if (bitmap != null) {
            synchronized(barcodeCache) {
                barcodeCache.put(cacheKey, bitmap)
            }
        }
        return bitmap
    }

    fun clearCache() {
        synchronized(barcodeCache) {
            barcodeCache.evictAll()
        }
    }

    fun generateBarcode(
        content: String,
        format: String,
        width: Int = 600,
        height: Int = 300,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        return generateBarcodeResult(
            content = content,
            format = format,
            width = width,
            height = height,
            foregroundColor = foregroundColor,
            backgroundColor = backgroundColor
        ).getOrNull()
    }

    fun generateBarcodeResult(
        content: String,
        format: String,
        width: Int = 600,
        height: Int = 300,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Result<Bitmap> {
        return runCatching {
            val bitMatrix = encodeToBitMatrixResult(content, format, width, height).getOrThrow()
            createBitmapFromMatrix(bitMatrix, foregroundColor, backgroundColor)
        }
    }

    fun encodeToBitMatrixResult(
        content: String,
        format: String,
        width: Int = 600,
        height: Int = 300
    ): Result<BitMatrix> {
        if (content.isBlank()) {
            return Result.failure(IllegalArgumentException("Content cannot be empty"))
        }

        if (format.equals("PHONE", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("PHONE format uses text view"))
        }

        val zxingFormat = parseFormat(format)
            ?: return Result.failure(IllegalArgumentException("Unsupported barcode format: $format"))

        val targetContent = sanitizeContent(content, zxingFormat)

        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, 1)
        }

        val (finalWidth, finalHeight) = adjustDimensions(zxingFormat, width, height)

        return runCatching {
            try {
                MultiFormatWriter().encode(
                    targetContent,
                    zxingFormat,
                    finalWidth,
                    finalHeight,
                    hints
                )
            } catch (e: WriterException) {
                val fallbackContent = prepareFallbackContent(targetContent, zxingFormat)
                if (fallbackContent != null && (fallbackContent != targetContent)) {
                    MultiFormatWriter().encode(
                        fallbackContent,
                        zxingFormat,
                        finalWidth,
                        finalHeight,
                        hints
                    )
                } else {
                    throw e
                }
            }
        }
    }

    fun parseFormat(formatStr: String): BarcodeFormat? {
        return when (formatStr.uppercase().trim()) {
            "QR_CODE", "QR", "QRCODE" -> BarcodeFormat.QR_CODE
            "CODE_128", "CODE128" -> BarcodeFormat.CODE_128
            "EAN_13", "EAN13" -> BarcodeFormat.EAN_13
            "EAN_8", "EAN8" -> BarcodeFormat.EAN_8
            "UPC_A", "UPCA" -> BarcodeFormat.UPC_A
            "CODE_39", "CODE39" -> BarcodeFormat.CODE_39
            "PDF_417", "PDF417", "PDF_417_STANDARD" -> BarcodeFormat.PDF_417
            "AZTEC" -> BarcodeFormat.AZTEC
            "PHONE" -> null
            else -> try {
                BarcodeFormat.valueOf(formatStr.uppercase().trim())
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun adjustDimensions(format: BarcodeFormat, width: Int, height: Int): Pair<Int, Int> {
        return when (format) {
            BarcodeFormat.QR_CODE, BarcodeFormat.AZTEC -> {
                val size = minOf(width, height)
                Pair(size, size)
            }
            else -> Pair(width, height)
        }
    }

    private fun sanitizeContent(content: String, format: BarcodeFormat): String {
        val trimmed = content.trim()
        val digitsOnly = trimmed.filter { it.isDigit() }

        return when (format) {
            BarcodeFormat.EAN_13 -> {
                if (digitsOnly.length == 12) {
                    digitsOnly + calculateEanChecksum(digitsOnly)
                } else if (digitsOnly.length > 13) {
                    digitsOnly.take(13)
                } else if (digitsOnly.length < 12) {
                    val padded = digitsOnly.padStart(12, '0')
                    padded + calculateEanChecksum(padded)
                } else {
                    digitsOnly
                }
            }
            BarcodeFormat.EAN_8 -> {
                if (digitsOnly.length == 7) {
                    digitsOnly + calculateEanChecksum(digitsOnly)
                } else if (digitsOnly.length > 8) {
                    digitsOnly.take(8)
                } else if (digitsOnly.length < 7) {
                    val padded = digitsOnly.padStart(7, '0')
                    padded + calculateEanChecksum(padded)
                } else {
                    digitsOnly
                }
            }
            BarcodeFormat.UPC_A -> {
                if (digitsOnly.length == 11) {
                    digitsOnly + calculateUpcChecksum(digitsOnly)
                } else if (digitsOnly.length > 12) {
                    digitsOnly.take(12)
                } else if (digitsOnly.length < 11) {
                    val padded = digitsOnly.padStart(11, '0')
                    padded + calculateUpcChecksum(padded)
                } else {
                    digitsOnly
                }
            }
            BarcodeFormat.CODE_39 -> {
                val allowedChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ -.$/+%"
                trimmed.uppercase().filter { it in allowedChars }
            }
            else -> trimmed
        }
    }

    private fun prepareFallbackContent(content: String, format: BarcodeFormat): String? {
        val digitsOnly = content.filter { it.isDigit() }
        return when (format) {
            BarcodeFormat.EAN_13 -> digitsOnly.padStart(13, '0').takeLast(13)
            BarcodeFormat.EAN_8 -> digitsOnly.padStart(8, '0').takeLast(8)
            BarcodeFormat.UPC_A -> digitsOnly.padStart(12, '0').takeLast(12)
            else -> null
        }
    }

    private fun calculateEanChecksum(digits: String): Int {
        var sum = 0
        for (i in digits.indices.reversed()) {
            val digit = digits[i].digitToInt()
            val weight = if ((digits.length - i) % 2 == 1) 3 else 1
            sum += digit * weight
        }
        val remainder = sum % 10
        return if (remainder == 0) 0 else 10 - remainder
    }

    private fun calculateUpcChecksum(digits: String): Int {
        var sumOdd = 0
        var sumEven = 0
        for (i in digits.indices) {
            val digit = digits[i].digitToInt()
            if (i % 2 == 0) {
                sumOdd += digit
            } else {
                sumEven += digit
            }
        }
        val total = (sumOdd * 3) + sumEven
        val remainder = total % 10
        return if (remainder == 0) 0 else 10 - remainder
    }

    private fun createBitmapFromMatrix(
        bitMatrix: BitMatrix,
        foregroundColor: Int,
        backgroundColor: Int
    ): Bitmap {
        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height
        val pixels = IntArray(matrixWidth * matrixHeight)

        for (y in 0 until matrixHeight) {
            val offset = y * matrixWidth
            for (x in 0 until matrixWidth) {
                pixels[offset + x] = if (bitMatrix[x, y]) foregroundColor else backgroundColor
            }
        }

        val bitmap = try {
            Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
        } catch (_: Throwable) {
            null
        } ?: throw IllegalStateException("Unable to create bitmap")
        try {
            bitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)
        } catch (_: Throwable) {
            // Ignored on stubbed unit tests
        }
        return bitmap
    }
}
