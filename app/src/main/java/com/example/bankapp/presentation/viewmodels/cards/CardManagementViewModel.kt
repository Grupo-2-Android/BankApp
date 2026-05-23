package com.example.bankapp.presentation.viewmodels.cards

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.br.scan_card.CreditCardData
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.local.room.entities.Card
import com.example.bankapp.data.repositories.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CardManagementViewModel(
    private val repository: ApiRepository = ApiRepository(),
    private val userPreferences: UserPreferences,
    private val database: AppDatabase
) : ViewModel() {

    private val TAG = "CardManagementViewModel"

    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _previewCard = MutableStateFlow<Card?>(null)
    val previewCard: StateFlow<Card?> = _previewCard.asStateFlow()

    private val _addCardUiState = MutableStateFlow<AddCardUiState>(AddCardUiState.Loading)
    val addCardUiState: StateFlow<AddCardUiState> = _addCardUiState.asStateFlow()

    init {
        loadCards()
    }

    private fun loadCards() {
        viewModelScope.launch {
            userPreferences.userId.collect { userId ->
                Log.i(TAG, "userId: $userId")
                if (userId != null) {
                    database.bankDao().getCardsByUser(userId).collect {
                        _cards.value = it
                    }
                }
            }
        }
    }

    fun generateVirtualCard() {
        viewModelScope.launch {
            _addCardUiState.update { AddCardUiState.Content }
            try {
                val userId = userPreferences.userId.first() ?: return@launch
                val virtualCard = repository.getVirtualCard()
                _previewCard.value = Card(
                    userId = userId,
                    type = TYPE_VIRTUAL,
                    number = virtualCard.number,
                    expiration = virtualCard.expirationDate,
                    cvv = virtualCard.securityCode,
                    brand = if ((virtualCard.securityCode).toInt() % 2 == 0) "VISA" else "MASTERCARD"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching virtual card: ${e.message}")
                _error.value = "Erro ao gerar cartão virtual. Tente novamente."
            }
        }
    }

    fun setScannedCardPreview(cardData: CreditCardData?) {
        _addCardUiState.update { AddCardUiState.Content }
        viewModelScope.launch {
            val userId = userPreferences.userId.first() ?: return@launch

            if (cardData == null) {
                _previewCard.value = Card(
                    userId = userId,
                    type = TYPE_PHYSICAL,
                    number = "****************",
                    expiration = "MM/AA",
                    cvv = "***",
                    brand = "BANDEIRA"
                )
                return@launch
            } else {
                _previewCard.value = Card(
                    userId = userId,
                    type = TYPE_PHYSICAL,
                    number = cardData.number,
                    expiration = cardData.validity,
                    cvv = cardData.cvv,
                    brand = cardData.flag ?: "MASTERCARD"
                )
            }
            _error.value = null
        }
    }

    fun confirmAddCard(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            if (_previewCard.value == null) {
                _error.value = "Houve uma falha ao gerar cartão."
            } else {
                database.bankDao().insertCard(_previewCard.value!!)
                _previewCard.value = null
                _error.value = null
                onSuccess()
            }
        }
    }

    fun clearPreview() {
        _previewCard.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun canAddType(type: String): Boolean {
        if (_cards.value.size >= 2) {
            _error.value = "Limite de 2 cartões atingido."
            return false
        }

        val alreadyHasVirtual = _cards.value.any { it.type == TYPE_VIRTUAL }
        val alreadyHasPhysical = _cards.value.any { it.type == TYPE_PHYSICAL }

        return when (type) {
            TYPE_VIRTUAL -> {
                if (alreadyHasVirtual) {
                    _error.value = "Você já possui um cartão virtual."
                    false
                } else {
                    true
                }
            }

            TYPE_PHYSICAL -> {
                if (alreadyHasPhysical) {
                    _error.value = "Você já possui um cartão físico."
                    false
                } else {
                    true
                }
            }

            else -> {
                _error.value = "Tipo de cartão inválido."
                false
            }
        }
    }

    companion object {
        const val TYPE_VIRTUAL = "VIRTUAL"
        const val TYPE_PHYSICAL = "PHYSICAL"
    }
}