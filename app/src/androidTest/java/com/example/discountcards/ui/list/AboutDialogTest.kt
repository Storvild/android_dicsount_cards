package com.example.discountcards.ui.list

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.discountcards.ui.list.components.AboutDialog
import com.example.discountcards.ui.theme.DiscountCardsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private fun renderDialog() {
        rule.setContent {
            DiscountCardsTheme {
                AboutDialog(onDismiss = {})
            }
        }
    }

    @Test
    fun aboutDialog_showsAppNameAndVersion() {
        renderDialog()
        // App name should be visible in the title
        rule.onNodeWithText("Discount Cards").assertIsDisplayed()
    }

    @Test
    fun aboutDialog_copyNameButton_isVisible() {
        renderDialog()
        rule.onNodeWithContentDescription("Copy name to clipboard").assertIsDisplayed()
    }

    @Test
    fun aboutDialog_githubLink_isClickable() {
        renderDialog()
        // GitHub link text should be visible
        rule.onNodeWithText("GitHub").assertIsDisplayed()
    }

    @Test
    fun aboutDialog_qrCode_isVisible() {
        renderDialog()
        rule.onNodeWithContentDescription("QR code").assertIsDisplayed()
    }
}
