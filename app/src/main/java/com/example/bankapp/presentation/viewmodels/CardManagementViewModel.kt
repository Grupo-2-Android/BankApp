package com.example.bankapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.local.room.entities.Card
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class CardManagementViewModel(
    private val userPreferences: UserPreferences,
    private val database: AppDatabase
) : ViewModel() {

    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCards()
    }

    private fun loadCards() {
        viewModelScope.launch {
            userPreferences.userId.collect { userId ->
                if (userId != null) {
                    database.bankDao().getCardsByUser(userId).collect {
                        _cards.value = it
                    }
                }
            }
        }
    }

    fun addCard(type: String) {
        viewModelScope.launch {
            val userId = userPreferences.userId.first() ?: return@launch
            val currentCount = database.bankDao().getCardCount(userId)
            
            if (currentCount >= 2) {
                _error.value = "Limite de 2 cartões atingido."
                return@launch
            }

            val newCard = Card(
                userId = userId,
                type = type,
                number = "**** **** **** ${Random.nextInt(1000, 9999)}",
                expiration = "12/28",
                cvv = Random.nextInt(100, 999).toString(),
                brand = "Visa"
            )
            database.bankDao().insertCard(newCard)
            _error.value = null
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
