package com.example.discountcards.ui.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.discountcards.data.repository.SortOrder
import com.example.discountcards.ui.list.components.SortSelectionDialog
import com.example.discountcards.ui.theme.DiscountCardsTheme
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SortSelectionDialogTest {

    @get:Rule
    val rule = createComposeRule()

    private var selectedOrder: SortOrder? = null

    private fun renderDialog(currentSortOrder: SortOrder = SortOrder.ALPHABETICAL) {
        rule.setContent {
            DiscountCardsTheme {
                SortSelectionDialog(
                    currentSortOrder = currentSortOrder,
                    onDismiss = {},
                    onSelect = { selectedOrder = it }
                )
            }
        }
    }

    @Test
    fun sortDialog_showsThreeOptions() {
        renderDialog()
        rule.onNodeWithText("По алфавиту").assertIsDisplayed()
        rule.onNodeWithText("По частоте").assertIsDisplayed()
        rule.onNodeWithText("По дате").assertIsDisplayed()
    }

    @Test
    fun sortDialog_selectAlphabetical_invokesCallback() {
        renderDialog()
        rule.onNodeWithText("По алфавиту").performClick()
        Assert.assertEquals(SortOrder.ALPHABETICAL, selectedOrder)
    }

    @Test
    fun sortDialog_selectFrequency_invokesCallback() {
        renderDialog()
        rule.onNodeWithText("По частоте").performClick()
        Assert.assertEquals(SortOrder.FREQUENCY, selectedOrder)
    }

    @Test
    fun sortDialog_selectCreationDate_invokesCallback() {
        renderDialog()
        rule.onNodeWithText("По дате").performClick()
        Assert.assertEquals(SortOrder.CREATION_DATE, selectedOrder)
    }
}
