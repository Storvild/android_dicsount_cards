package com.example.discountcards

import android.app.Application
import com.example.discountcards.data.local.AppDatabase
import com.example.discountcards.data.repository.CardRepository
import com.example.discountcards.data.repository.CardRepositoryImpl
import com.example.discountcards.data.repository.UserPreferencesRepository

class DiscountCardsApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val repository: CardRepository by lazy {
        CardRepositoryImpl(database.discountCardDao())
    }

    val preferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(this)
    }


}
