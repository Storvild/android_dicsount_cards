package com.example.discountcards

import android.app.Application
import com.example.discountcards.data.local.AppDatabase
import com.example.discountcards.data.repository.CardRepository
import com.example.discountcards.data.repository.CardRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DiscountCardsApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val repository: CardRepository by lazy {
        CardRepositoryImpl(database.discountCardDao())
    }

    override fun onCreate() {
        super.onCreate()
        // Pre-warm Room database asynchronously on Dispatchers.IO
        CoroutineScope(Dispatchers.IO).launch {
            repository.getAllCards().firstOrNull()
        }
    }
}
