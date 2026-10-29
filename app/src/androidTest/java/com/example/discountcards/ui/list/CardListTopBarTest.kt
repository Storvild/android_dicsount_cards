package com.example.discountcards.ui.list

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.discountcards.data.repository.SortOrder
import com.example.discountcards.data.repository.ThemeMode
import com.example.discountcards.ui.list.components.CardListTopBar
import com.example.discountcards.ui.theme.DiscountCardsTheme
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests for [CardListTopBar].
 */
@RunWith(AndroidJUnit4::class)
class CardListTopBarTest {

    @get:Rule
    val rule = createComposeRule()

    private var lastCallback: String? = null

    private fun renderComponent(
        query: String = "",
        currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
        currentSortOrder: SortOrder = SortOrder.ALPHABETICAL
    ) {
        rule.setContent {
            DiscountCardsTheme {
                CardListTopBar(
                    query = query,
                    onQueryChange = {},
                    onClearQuery = { lastCallback = "clear" },
                    onExportJsonClick = { lastCallback = "exportJson" },
                    onExportCsvClick = { lastCallback = "exportCsv" },
                    onImportJsonClick = { lastCallback = "importJson" },
                    onImportCsvClick = { lastCallback = "importCsv" },
                    onShareClick = { lastCallback = "share" },
                    onAddCardClick = { lastCallback = "addCard" },
                    onDeleteClick = { lastCallback = "delete" },
                    onArchiveClick = { lastCallback = "archive" },
                    currentThemeMode = currentThemeMode,
                    onThemeModeSelected = { lastCallback = "theme:$it" },
                    currentSortOrder = currentSortOrder,
                    onSortOrderSelected = { lastCallback = "sort:$it" },
                    onSettingsClick = { lastCallback = "settings" }
                )
            }
        }
    }

    // --- Search field ---

    @Test
    fun searchField_displaysQuery() {
        renderComponent(query = "Пятёрочка")
        rule.onNodeWithText("Пятёрочка").assertIsDisplayed()
    }

    @Test
    fun searchField_showsPlaceholder_whenEmpty() {
        renderComponent(query = "")
        // The placeholder text comes from R.string.search_placeholder
        // We verify the text field is visible
        rule.onNodeWithContentDescription("Search").assertIsDisplayed()
    }

    @Test
    fun clearButton_visible_whenQueryNotEmpty() {
        renderComponent(query = "test")
        rule.onNodeWithContentDescription("Clear search").assertIsDisplayed()
    }

    @Test
    fun clearButton_triggersOnClearQuery() {
        renderComponent(query = "test")
        rule.onNodeWithContentDescription("Clear search").performClick()
        Assert.assertEquals("clear", lastCallback)
    }

    // --- Menu ---

    @Test
    fun menuButton_openMenu_showsAllItems() {
        renderComponent()
        // Open the menu
        rule.onNodeWithContentDescription("Menu options").performClick()
        rule.waitForIdle()
        // Verify all menu items are present
        rule.onNodeWithText("Файл").assertIsDisplayed()
        rule.onNodeWithText("Поделиться").assertIsDisplayed()
        rule.onNodeWithText("Архив").assertIsDisplayed()
        rule.onNodeWithText("Добавить").assertIsDisplayed()
        rule.onNodeWithText("Оформление").assertIsDisplayed()
        rule.onNodeWithText("Сортировка").assertIsDisplayed()
        rule.onNodeWithText("Настройки").assertIsDisplayed()
        rule.onNodeWithText("О программе").assertIsDisplayed()
    }

    // --- Menu item callbacks ---

    @Test
    fun menu_exportJsonClick_invokesCallback() {
        renderComponent()
        rule.onNodeWithContentDescription("Menu options").performClick()
        rule.waitForIdle()
        // Open File submenu
        rule.onNodeWithText("Файл").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Экспорт JSON").performClick()
        Assert.assertEquals("exportJson", lastCallback)
    }

    @Test
    fun menu_importJsonClick_invokesCallback() {
        renderComponent()
        rule.onNodeWithContentDescription("Menu options").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Файл").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Импорт JSON").performClick()
        Assert.assertEquals("importJson", lastCallback)
    }

    @Test
    fun menu_shareClick_invokesCallback() {
        renderComponent()
        rule.onNodeWithContentDescription("Menu options").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Поделиться").performClick()
        Assert.assertEquals("share", lastCallback)
    }

    @Test
    fun menu_addCardClick_invokesCallback() {
        renderComponent()
        rule.onNodeWithContentDescription("Menu options").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Добавить").performClick()
        Assert.assertEquals("addCard", lastCallback)
    }

    @Test
    fun menu_archiveClick_invokesCallback() {
        renderComponent()
        rule.onNodeWithContentDescription("Menu options").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Архив").performClick()
        Assert.assertEquals("archive", lastCallback)
    }

    @Test
    fun menu_settingsClick_invokesCallback() {
        renderComponent()
        rule.onNodeWithContentDescription("Menu options").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Настройки").performClick()
        Assert.assertEquals("settings", lastCallback)
    }

    // --- Theme toggle ---

    @Test
    fun themeCheckbox_reflectsCurrentMode() {
        // DARK mode → checkbox should be checked
        renderComponent(currentThemeMode = ThemeMode.DARK)
        rule.onNodeWithContentDescription("Menu options").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Оформление").performClick()
        Assert.assertEquals("theme:DARK", lastCallback)
    }

    @Test
    fun themeCheckbox_toggle_changesMode() {
        // LIGHT mode → toggling should select DARK
        renderComponent(currentThemeMode = ThemeMode.LIGHT)
        rule.onNodeWithContentDescription("Menu options").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Оформление").performClick()
        Assert.assertEquals("theme:DARK", lastCallback)
    }
}
