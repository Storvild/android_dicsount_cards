package com.example.discountcards.util

import android.content.ContentResolver
import android.net.Uri
import com.example.discountcards.data.local.entity.DiscountCard
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class BackupManager(
    private val json: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }
) {
    fun serialize(cards: List<DiscountCard>): String {
        return json.encodeToString(cards)
    }

    fun deserialize(jsonString: String): Result<List<DiscountCard>> {
        return runCatching {
            if (jsonString.isBlank()) {
                throw IllegalArgumentException("JSON string is empty")
            }
            json.decodeFromString<List<DiscountCard>>(jsonString)
        }
    }

    fun writeToStream(outputStream: OutputStream, cards: List<DiscountCard>): Result<Unit> {
        return runCatching {
            val jsonString = serialize(cards)
            outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(jsonString)
                writer.flush()
            }
        }
    }

    fun readFromStream(inputStream: InputStream): Result<List<DiscountCard>> {
        return runCatching {
            val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            deserialize(jsonString).getOrThrow()
        }
    }

    fun writeToUri(contentResolver: ContentResolver, uri: Uri, cards: List<DiscountCard>): Result<Unit> {
        return runCatching {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                writeToStream(outputStream, cards).getOrThrow()
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
