package com.example.discountcards.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.discountcards.data.repository.CardRepository
import com.example.discountcards.ui.addedit.AddEditCardViewModel
import com.example.discountcards.ui.detail.CardDetailViewModel
import com.example.discountcards.ui.list.CardListViewModel

class CardListViewModelFactory(
    private val repository: CardRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardListViewModel::class.java)) {
            return CardListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class CardDetailViewModelFactory(
    private val repository: CardRepository,
    private val cardId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardDetailViewModel::class.java)) {
            return CardDetailViewModel(repository, cardId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class AddEditCardViewModelFactory(
    private val repository: CardRepository,
    private val cardId: Long? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditCardViewModel::class.java)) {
            return AddEditCardViewModel(repository, cardId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
