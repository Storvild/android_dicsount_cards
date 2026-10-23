package com.example.discountcards.util

import android.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ColorHexSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ColorHex", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Int) {
        val alpha = Color.alpha(value)
        val hex = if (alpha == 255) {
            String.format("#%06X", 0xFFFFFF and value)
        } else {
            String.format("#%08X", value)
        }
        encoder.encodeString(hex)
    }

    override fun deserialize(decoder: Decoder): Int {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be decoded only by Json")
        val content = jsonDecoder.decodeJsonElement().jsonPrimitive.content
        return try {
            if (content.startsWith("#")) {
                Color.parseColor(content)
            } else {
                content.toInt()
            }
        } catch (e: Exception) {
            // Default color if parsing fails
            0xFF1E88E5.toInt()
        }
    }
}

object DateIso8601Serializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DateIso8601", PrimitiveKind.STRING)
    
    // ThreadLocal is used because SimpleDateFormat is not thread-safe
    private val formatter = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        }
    }

    override fun serialize(encoder: Encoder, value: Long) {
        encoder.encodeString(formatter.get()!!.format(Date(value)))
    }

    override fun deserialize(decoder: Decoder): Long {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be decoded only by Json")
        val content = jsonDecoder.decodeJsonElement().jsonPrimitive.content
        
        return try {
            // Check if it was saved as a raw Long timestamp
            content.toLong()
        } catch (e: NumberFormatException) {
            // Try parsing string to Date, fallback to current time
            try {
                formatter.get()!!.parse(content)?.time ?: System.currentTimeMillis()
            } catch (e2: ParseException) {
                System.currentTimeMillis()
            }
        }
    }
}