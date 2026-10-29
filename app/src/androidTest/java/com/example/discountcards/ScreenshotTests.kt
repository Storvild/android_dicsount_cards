package com.example.discountcards

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.discountcards.data.local.entity.DiscountCard
import com.example.discountcards.ui.list.components.CardListItem
import com.example.discountcards.ui.list.components.EmptyState
import com.example.discountcards.ui.theme.DiscountCardsTheme
import java.io.File
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Screenshot tests using Compose's `createComposeRule` + manual bitmap capture.
 *
 * Each test renders the target composable, captures a screenshot via
 * [android.graphics.Bitmap], saves it as PNG to
 * `app/src/androidTest/snapshots/<testName>.png`.
 *
 * On first run: creates the baseline file and passes.
 * On subsequent runs: compares the current screenshot to the baseline.
 * If they differ the test fails with a descriptive message.
 */
@RunWith(AndroidJUnit4::class)
class ScreenshotTests {

    @org.junit.Rule
    val rule = createComposeRule()

    private val snapshotsDir = File("app/src/androidTest/snapshots").apply { mkdirs() }

    private val sampleCard = DiscountCard(
        id = 1L,
        storeName = "Пятёрочка",
        cardNumber = "1234567890123",
        barcodeFormat = "EAN_13",
        note = "Карта семьи",
        cardColor = 0x0EE8A5.toInt(),
        iconName = "shopping_cart",
        isFavorite = true,
        cvc = "123",
        usageCount = 5,
        createdAt = 0L,
        updatedAt = 0L,
        isArchived = false
    )

    private fun captureScreenshot(): Bitmap {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val activity = (context as? android.app.Activity)
            ?: throw IllegalStateException("Expected Activity context")
        val window = activity.window
        val view = window.decorView
        val bitmap = Bitmap.createBitmap(
            view.width, view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun saveOrCompare(bitmap: Bitmap, name: String) {
        val file = File(snapshotsDir, "$name.png")
        if (!file.exists()) {
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } else {
            val baseline = BitmapFactory.decodeFile(file.absolutePath)
                ?: throw IllegalStateException("Failed to decode baseline: $file")
            Assert.assertEquals("Screenshot width mismatch for $name", baseline.width, bitmap.width)
            Assert.assertEquals("Screenshot height mismatch for $name", baseline.height, bitmap.height)
            var mismatch = false
            outer@ for (x in 0 until bitmap.width) {
                for (y in 0 until bitmap.height) {
                    if (bitmap.getPixel(x, y) != baseline.getPixel(x, y)) {
                        mismatch = true
                        break@outer
                    }
                }
            }
            Assert.assertFalse(
                "Screenshot for '$name' differs from baseline. " +
                    "Delete app/src/androidTest/snapshots/$name.png to regenerate.",
                mismatch
            )
        }
    }

    @Test
    fun cardListScreen_screenshot_empty() {
        rule.setContent {
            DiscountCardsTheme {
                EmptyState(
                    isSearching = false,
                    onAddCardClick = {},
                    onImportClick = {}
                )
            }
        }
        rule.waitForIdle()
        saveOrCompare(captureScreenshot(), "cardListScreen_screenshot_empty")
    }

    @Test
    fun cardListScreen_screenshot_withCards() {
        rule.setContent {
            DiscountCardsTheme {
                CardListItem(
                    card = sampleCard,
                    onClick = {},
                    onFavoriteToggle = {},
                    showBarcode = true,
                    onEditClick = {},
                    onDeleteClick = {}
                )
            }
        }
        rule.waitForIdle()
        saveOrCompare(captureScreenshot(), "cardListScreen_screenshot_withCards")
    }

    @Test
    fun cardListItem_screenshot() {
        rule.setContent {
            DiscountCardsTheme {
                CardListItem(
                    card = sampleCard,
                    onClick = {},
                    onFavoriteToggle = {},
                    showBarcode = true,
                    onEditClick = {},
                    onDeleteClick = {}
                )
            }
        }
        rule.waitForIdle()
        saveOrCompare(captureScreenshot(), "cardListItem_screenshot")
    }

    @Test
    fun cardListItem_screenshot_compact() {
        rule.setContent {
            DiscountCardsTheme {
                CardListItem(
                    card = sampleCard,
                    onClick = {},
                    onFavoriteToggle = {},
                    showBarcode = false,
                    onEditClick = {},
                    onDeleteClick = {}
                )
            }
        }
        rule.waitForIdle()
        saveOrCompare(captureScreenshot(), "cardListItem_screenshot_compact")
    }

    @Test
    fun cardListItem_screenshot_favorite() {
        val favoriteCard = sampleCard.copy(isFavorite = true)
        rule.setContent {
            DiscountCardsTheme {
                CardListItem(
                    card = favoriteCard,
                    onClick = {},
                    onFavoriteToggle = {},
                    showBarcode = true,
                    onEditClick = {},
                    onDeleteClick = {}
                )
            }
        }
        rule.waitForIdle()
        saveOrCompare(captureScreenshot(), "cardListItem_screenshot_favorite")
    }

    @Test
    fun cardListItem_screenshot_archived() {
        val archivedCard = sampleCard.copy(isArchived = true)
        rule.setContent {
            DiscountCardsTheme {
                CardListItem(
                    card = archivedCard,
                    onClick = {},
                    onFavoriteToggle = {},
                    showBarcode = true,
                    onEditClick = {},
                    onDeleteClick = {}
                )
            }
        }
        rule.waitForIdle()
        saveOrCompare(captureScreenshot(), "cardListItem_screenshot_archived")
    }

    @Test
    fun emptyState_screenshot() {
        rule.setContent {
            DiscountCardsTheme {
                EmptyState(
                    isSearching = false,
                    onAddCardClick = {},
                    onImportClick = {}
                )
            }
        }
        rule.waitForIdle()
        saveOrCompare(captureScreenshot(), "emptyState_screenshot")
    }
}
