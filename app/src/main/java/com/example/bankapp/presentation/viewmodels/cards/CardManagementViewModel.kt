package com.example.bankapp.presentation.viewmodels.cards

import com.example.bankapp.data.local.room.entities.Card
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.br.scan_card.CreditCardData
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.repositories.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
            userPreferences.userId
                .flatMapLatest { userId ->
                    if (!userId.isNullOrBlank()) {
                        database.bankDao().getCardsByUser(userId)
                    } else {
                        flowOf(emptyList())
                    }
                }
                .collect { cardList ->
                    _cards.value = cardList
                }
        }
    }

    fun generateVirtualCard() {
        viewModelScope.launch {
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
            _addCardUiState.update { AddCardUiState.Content }
        }
    }

    fun setScannedCardPreview(cardData: CreditCardData?) {
        Log.i(TAG, "setScannedCardPreview: $cardData")
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
                    number = cardData.number.replace(" ", ""),
                    expiration = cardData.validity,
                    cvv = cardData.cvv,
                    brand = cardData.flag ?: "MASTERCARD"
                )
            }
            _error.value = null
            _addCardUiState.update { AddCardUiState.Content }
        }
    }

    fun confirmAddCard(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val cardToInsert = _previewCard.value
            if (cardToInsert == null) {
                _error.value = "Houve uma falha ao gerar cartão."
            } else {
                // Validação final de segurança antes de inserir no banco
                val currentCards = _cards.value
                if (currentCards.size >= 2) {
                    _error.value = "Limite de 2 cartões atingido."
                    return@launch
                }
                if (currentCards.any { it.type == cardToInsert.type }) {
                    _error.value = "Você já possui um cartão do tipo ${if(cardToInsert.type == TYPE_PHYSICAL) "Físico" else "Virtual"}."
                    return@launch
                }

                database.bankDao().insertCard(cardToInsert)
                onSuccess()
                _addCardUiState.value = AddCardUiState.Loading
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

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            try {
                database.bankDao().deleteCard(card)
                // loadCards() é desnecessário aqui pois já temos um coletor ativo
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting card: ${e.message}")
                _error.value = "Erro ao deletar cartão. Tente novamente."
            }
        }
    }

    companion object {
        const val TYPE_VIRTUAL = "VIRTUAL"
        const val TYPE_PHYSICAL = "PHYSICAL"
    }
}