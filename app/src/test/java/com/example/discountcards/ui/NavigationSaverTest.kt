package com.example.discountcards.ui

import androidx.compose.runtime.saveable.SaverScope
import com.example.discountcards.Destination
import com.example.discountcards.destinationSaver
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationSaverTest {

    private val dummyScope = object : SaverScope {
        override fun canBeSaved(value: Any): Boolean = true
    }

    @Test
    fun testDestinationSaverCardList() {
        val saver = destinationSaver()
        val original = Destination.CardList
        val saved = with(saver) { dummyScope.save(original) }
        assertEquals("CardList", saved)

        val restored = saver.restore(saved!!)
        assertEquals(Destination.CardList, restored)
    }

    @Test
    fun testDestinationSaverCardDetail() {
        val saver = destinationSaver()
        val original = Destination.CardDetail(cardId = 42, key = "test-key-123")
        val saved = with(saver) { dummyScope.save(original) }
        assertEquals("CardDetail:42:test-key-123", saved)

        val restored = saver.restore(saved!!) as Destination.CardDetail
        assertEquals(42L, restored.cardId)
        assertEquals("test-key-123", restored.key)
    }

    @Test
    fun testDestinationSaverAddEditCard() {
        val saver = destinationSaver()
        val originalNewCard = Destination.AddEditCard(cardId = null, key = "new-key")
        val savedNew = with(saver) { dummyScope.save(originalNewCard) }
        assertEquals("AddEditCard:-1:new-key", savedNew)

        val restoredNew = saver.restore(savedNew!!) as Destination.AddEditCard
        assertEquals(null, restoredNew.cardId)
        assertEquals("new-key", restoredNew.key)

        val originalEditCard = Destination.AddEditCard(cardId = 15L, key = "edit-key")
        val savedEdit = with(saver) { dummyScope.save(originalEditCard) }
        assertEquals("AddEditCard:15:edit-key", savedEdit)

        val restoredEdit = saver.restore(savedEdit!!) as Destination.AddEditCard
        assertEquals(15L, restoredEdit.cardId)
        assertEquals("edit-key", restoredEdit.key)
    }

    @Test
    fun testDestinationSaverSettingsAndArchive() {
        val saver = destinationSaver()

        val settingsSaved = with(saver) { dummyScope.save(Destination.Settings) }
        assertEquals("Settings", settingsSaved)
        assertEquals(Destination.Settings, saver.restore(settingsSaved!!))

        val archiveSaved = with(saver) { dummyScope.save(Destination.Archive) }
        assertEquals("Archive", archiveSaved)
        assertEquals(Destination.Archive, saver.restore(archiveSaved!!))
    }

    @Test
    fun testDestinationSaverUnknownFallback() {
        val saver = destinationSaver()
        val restored = saver.restore("UnknownScreen:123")
        assertEquals(Destination.CardList, restored)
    }
}
