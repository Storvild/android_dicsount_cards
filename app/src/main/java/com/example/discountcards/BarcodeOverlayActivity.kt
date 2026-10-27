package com.example.discountcards

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.discountcards.util.BarcodeGenerator
import kotlin.math.min

/**
 * A dedicated fullscreen activity that displays a barcode/phone number at maximum
 * brightness with no text, for easy scanning.
 *
 * Launched via [newIntent].
 */
class BarcodeOverlayActivity : Activity() {

    private lateinit var imageView: ImageView
    private lateinit var rootLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen: hide status bar and nav bar via modern WindowInsetsController
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Maximum brightness
        window.attributes = window.attributes.apply {
            screenBrightness = 1.0f
        }

        // Build the UI: white background, centered ImageView, no text
        rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(this@BarcodeOverlayActivity, android.R.color.white))
            setGravity(android.view.Gravity.CENTER)
        }
        imageView = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        rootLayout.addView(imageView)
        setContentView(rootLayout)

        // Generate the barcode from the passed card number and format
        val cardNumber = intent.getStringExtra(EXTRA_CARD_NUMBER).orEmpty()
        val barcodeFormat = intent.getStringExtra(EXTRA_BARCODE_FORMAT).orEmpty()

        if (barcodeFormat.equals("PHONE", ignoreCase = true)) {
            // For PHONE format, render the card number as large text on white
            imageView.visibility = View.GONE
            val textView = android.widget.TextView(this).apply {
                text = cardNumber
                textSize = 64f
                typeface = android.graphics.Typeface.MONOSPACE
                setTextColor(android.graphics.Color.BLACK)
                setPadding(64, 64, 64, 64)
            }
            rootLayout.addView(textView)
        } else {
            val bitmap = generateHighResBarcode(cardNumber, barcodeFormat)
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun generateHighResBarcode(cardNumber: String, format: String): Bitmap? {
        // Use high resolution for clarity on any screen
        val displayMetrics = resources.displayMetrics
        val screenDiagonal = min(displayMetrics.widthPixels, displayMetrics.heightPixels)
        // Target ~3x screen size for maximum sharpness
        val targetSize = screenDiagonal * 3

        return when (format.uppercase()) {
            "QR_CODE", "QR", "QRCODE", "AZTEC" -> {
                BarcodeGenerator.generateBarcode(
                    content = cardNumber,
                    format = format,
                    width = targetSize,
                    height = targetSize,
                    foregroundColor = android.graphics.Color.BLACK,
                    backgroundColor = android.graphics.Color.WHITE
                )
            }
            else -> {
                BarcodeGenerator.generateBarcode(
                    content = cardNumber,
                    format = format,
                    width = targetSize,
                    height = targetSize / 2,
                    foregroundColor = android.graphics.Color.BLACK,
                    backgroundColor = android.graphics.Color.WHITE
                )
            }
        }
    }

    companion object {
        private const val EXTRA_CARD_NUMBER = "card_number"
        private const val EXTRA_BARCODE_FORMAT = "barcode_format"

        fun newIntent(context: Context, cardNumber: String, barcodeFormat: String): Intent =
            Intent(context, BarcodeOverlayActivity::class.java).apply {
                putExtra(EXTRA_CARD_NUMBER, cardNumber)
                putExtra(EXTRA_BARCODE_FORMAT, barcodeFormat)
            }
    }
}
