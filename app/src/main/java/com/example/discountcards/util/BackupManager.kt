package com.example.discountcards.util

import android.content.ContentResolver
import android.net.Uri
import com.example.discountcards.data.local.entity.DiscountCard
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

enum class ExportFormat {
    JSON, CSV
}

class BackupManager(
    private val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }
) {
    fun serialize(cards: List<DiscountCard>, format: ExportFormat = ExportFormat.JSON): String {
        return when (format) {
            ExportFormat.JSON -> serializeJson(cards)
            ExportFormat.CSV -> serializeCsv(cards)
        }
    }

    fun serializeJson(cards: List<DiscountCard>): String {
        return json.encodeToString(cards)
    }

    fun serializeCsv(cards: List<DiscountCard>): String {
        val sb = StringBuilder()
        sb.append("Название,Код,Тип кода,CVC,Примечание\n")
        for (card in cards) {
            val name = escapeCsv(card.storeName)
            val code = escapeCsv(card.cardNumber)
            val format = escapeCsv(card.barcodeFormat)
            val cvc = escapeCsv(card.cvc)
            val note = escapeCsv(card.note)
            sb.append("$name,$code,$format,$cvc,$note\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        if (value.contains("\"") || value.contains(",") || value.contains(";") || value.contains("\n") || value.contains("\r")) {
            val escaped = value.replace("\"", "\"\"")
            return "\"$escaped\""
        }
        return value
    }

    fun deserialize(contentString: String): Result<List<DiscountCard>> {
        if (contentString.isBlank()) {
            return Result.failure(IllegalArgumentException("Import content is empty"))
        }

        val trimmed = contentString.trimStart()
        val isJsonLikely = trimmed.startsWith("[") || trimmed.startsWith("{")

        if (isJsonLikely) {
            val jsonResult = deserializeJson(contentString)
            if (jsonResult.isSuccess) return jsonResult
            val csvResult = deserializeCsv(contentString)
            if (csvResult.isSuccess) return csvResult
            return jsonResult
        } else {
            val csvResult = deserializeCsv(contentString)
            if (csvResult.isSuccess) return csvResult
            val jsonResult = deserializeJson(contentString)
            if (jsonResult.isSuccess) return jsonResult
            return csvResult
        }
    }

    fun deserializeJson(jsonString: String): Result<List<DiscountCard>> {
        return runCatching {
            if (jsonString.isBlank()) {
                throw IllegalArgumentException("JSON string is empty")
            }
            json.decodeFromString<List<DiscountCard>>(jsonString)
        }
    }

    fun deserializeCsv(csvString: String): Result<List<DiscountCard>> {
        return runCatching {
            if (csvString.isBlank()) {
                throw IllegalArgumentException("CSV content is empty")
            }
            val rows = parseCsv(csvString)
            if (rows.isEmpty()) {
                throw IllegalArgumentException("No rows found in CSV")
            }

            val cards = mutableListOf<DiscountCard>()
            val startLine = if (isHeaderRow(rows[0])) 1 else 0

            for (idx in startLine until rows.size) {
                val row = rows[idx]
                val storeName = row.getOrNull(0)?.trim() ?: ""
                val cardNumber = row.getOrNull(1)?.trim() ?: ""
                val rawFormat = row.getOrNull(2)?.trim() ?: ""
                val cvc = row.getOrNull(3)?.trim() ?: ""
                val note = row.getOrNull(4)?.trim() ?: ""

                if (storeName.isBlank() || cardNumber.isBlank()) {
                    continue
                }

                val format = normalizeBarcodeFormat(rawFormat)

                cards.add(
                    DiscountCard(
                        storeName = storeName,
                        cardNumber = cardNumber,
                        barcodeFormat = format,
                        cvc = cvc,
                        note = note
                    )
                )
            }

            if (cards.isEmpty()) {
                throw IllegalArgumentException("No valid discount cards found in CSV")
            }
            cards
        }
    }

    private fun parseCsv(csvText: String): List<List<String>> {
        if (csvText.isBlank()) return emptyList()

        var commaCount = 0
        var semicolonCount = 0
        var tabCount = 0
        var insideQuotes = false

        for (i in csvText.indices) {
            val c = csvText[i]
            if (c == '"') {
                insideQuotes = !insideQuotes
            } else if (!insideQuotes) {
                when (c) {
                    ',' -> commaCount++
                    ';' -> semicolonCount++
                    '\t' -> tabCount++
                }
            }
        }

        val delimiter = when {
            semicolonCount > commaCount && semicolonCount >= tabCount -> ';'
            tabCount > commaCount -> '\t'
            else -> ','
        }

        val rows = mutableListOf<List<String>>()
        val currentCell = StringBuilder()
        val currentRow = mutableListOf<String>()
        var inQuotes = false
        var i = 0

        while (i < csvText.length) {
            val c = csvText[i]
            when {
                inQuotes -> {
                    if (c == '"') {
                        if (i + 1 < csvText.length && csvText[i + 1] == '"') {
                            currentCell.append('"')
                            i++
                        } else {
                            inQuotes = false
                        }
                    } else {
                        currentCell.append(c)
                    }
                }
                c == '"' -> {
                    inQuotes = true
                }
                c == delimiter -> {
                    currentRow.add(currentCell.toString().trim())
                    currentCell.clear()
                }
                c == '\r' || c == '\n' -> {
                    if (c == '\r' && i + 1 < csvText.length && csvText[i + 1] == '\n') {
                        i++
                    }
                    currentRow.add(currentCell.toString().trim())
                    currentCell.clear()
                    if (currentRow.any { it.isNotBlank() }) {
                        rows.add(currentRow.toList())
                    }
                    currentRow.clear()
                }
                else -> {
                    currentCell.append(c)
                }
            }
            i++
        }

        if (currentCell.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentCell.toString().trim())
            if (currentRow.any { it.isNotBlank() }) {
                rows.add(currentRow.toList())
            }
        }

        return rows
    }

    private fun isHeaderRow(row: List<String>): Boolean {
        if (row.isEmpty()) return false
        val firstCol = row[0].lowercase().trim()
        val secondCol = row.getOrNull(1)?.lowercase()?.trim() ?: ""
        val thirdCol = row.getOrNull(2)?.lowercase()?.trim() ?: ""

        val headerKeywordsFirst = setOf(
            "название", "store", "storename", "store_name", "store name",
            "title", "название магазина", "магазин", "name", "название карты"
        )
        val headerKeywordsSecond = setOf(
            "код", "cardnumber", "card_number", "card number", "number",
            "code", "номер", "номер карты", "card_code", "значение", "barcode"
        )
        val headerKeywordsThird = setOf(
            "тип кода", "тип", "format", "barcodeformat", "barcode_format",
            "barcode format", "type", "тип штрихкода", "тип карты"
        )

        return firstCol in headerKeywordsFirst ||
                secondCol in headerKeywordsSecond ||
                thirdCol in headerKeywordsThird
    }

    private fun normalizeBarcodeFormat(input: String): String {
        val clean = input.trim().uppercase().replace("-", "_").replace(" ", "_")
        if (clean.isBlank()) return "CODE_128"

        return when {
            clean == "PHONE" || clean.contains("ТЕЛЕФОН") || clean.contains("PHONE") -> "PHONE"
            clean == "AZTEC" -> "AZTEC"
            clean == "EAN_8" || clean == "EAN8" || clean == "EAN 8" || clean == "8" -> "EAN_8"
            clean == "EAN_13" || clean == "EAN13" || clean == "EAN 13" || clean == "13" -> "EAN_13"
            clean.contains("EAN") && clean.contains("8") -> "EAN_8"
            clean.contains("EAN") -> "EAN_13"
            clean == "QR_CODE" || clean == "QR" || clean == "QRCODE" || clean.contains("QR") -> "QR_CODE"
            clean == "UPC_A" || clean == "UPCA" || clean.contains("UPC") -> "UPC_A"
            clean == "CODE_39" || clean == "CODE39" || clean.contains("39") -> "CODE_39"
            clean == "PDF_417" || clean == "PDF417" || clean.contains("417") || clean.contains("PDF") -> "PDF_417"
            clean == "CODE_128" || clean == "CODE128" || clean.contains("128") -> "CODE_128"
            clean in BarcodeGenerator.SUPPORTED_FORMATS -> clean
            else -> "CODE_128"
        }
    }

    fun writeToStream(outputStream: OutputStream, cards: List<DiscountCard>, format: ExportFormat = ExportFormat.JSON): Result<Unit> {
        return runCatching {
            val contentString = serialize(cards, format)
            outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(contentString)
                writer.flush()
            }
        }
    }

    fun readFromStream(inputStream: InputStream): Result<List<DiscountCard>> {
        return runCatching {
            val contentString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            deserialize(contentString).getOrThrow()
        }
    }

    fun writeToUri(contentResolver: ContentResolver, uri: Uri, cards: List<DiscountCard>, format: ExportFormat = ExportFormat.JSON): Result<Unit> {
        return runCatching {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                writeToStream(outputStream, cards, format).getOrThrow()
            } ?: throw IllegalStateException("Cannot open output stream for Uri: $uri")
        }
    }

    fun readFromUri(contentResolver: ContentResolver, uri: Uri): Result<List<DiscountCard>> {
        return runCatching {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                readFromStream(inputStream).getOrThrow()
            } ?: throw IllegalStateException("Cannot open input stream for Uri: $uri")
        }
    }
}
