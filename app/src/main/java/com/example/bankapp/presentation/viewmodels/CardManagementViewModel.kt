package com.example.bankapp.presentation.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.br.scan_card.CreditCardData
import com.example.bankapp.R
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.local.room.entities.Card
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
    application: Application,
    private val repository: ApiRepository = ApiRepository(),
    private val userPreferences: UserPreferences,
    private val database: AppDatabase
) : AndroidViewModel(application) {

    private val ctx get() = getApplication<Application>()

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
                _error.value = ctx.getString(R.string.cards_vm_error_generate_virtual)
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
                    number = ctx.getString(R.string.add_card_placeholder_number),
                    expiration = ctx.getString(R.string.cards_vm_placeholder_expiration),
                    cvv = ctx.getString(R.string.add_card_placeholder_cvv),
                    brand = ctx.getString(R.string.cards_vm_placeholder_brand)
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
                _error.value = ctx.getString(R.string.cards_vm_error_generate_failed)
            } else {
                // Validação final de segurança antes de inserir no banco
                val currentCards = _cards.value
                if (currentCards.size >= 2) {
                    _error.value = ctx.getString(R.string.cards_vm_error_limit_reached)
                    return@launch
                }
                if (currentCards.any { it.type == cardToInsert.type }) {
                    val typeLabel = if (cardToInsert.type == TYPE_PHYSICAL) {
                        ctx.getString(R.string.common_physical)
                    } else {
                        ctx.getString(R.string.common_virtual)
                    }
                    _error.value = ctx.getString(R.string.cards_vm_error_type_already_exists, typeLabel)
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
            _error.value = ctx.getString(R.string.cards_vm_error_limit_reached)
            return false
        }

        val alreadyHasVirtual = _cards.value.any { it.type == TYPE_VIRTUAL }
        val alreadyHasPhysical = _cards.value.any { it.type == TYPE_PHYSICAL }

        return when (type) {
            TYPE_VIRTUAL -> {
                if (alreadyHasVirtual) {
                    _error.value = ctx.getString(R.string.cards_vm_error_virtual_exists)
                    false
                } else {
                    true
                }
            }

            TYPE_PHYSICAL -> {
                if (alreadyHasPhysical) {
                    _error.value = ctx.getString(R.string.cards_vm_error_physical_exists)
                    false
                } else {
                    true
                }
            }

            else -> {
                _error.value = ctx.getString(R.string.cards_vm_error_invalid_type)
                false
            }
        }
    }

    fun deleteCard(card: Card) {
        viewModelScope.launch {
            try {
                database.bankDao().deleteCard(card)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting card: ${e.message}")
                _error.value = ctx.getString(R.string.cards_vm_error_delete_failed)
            }
        }
    }

    companion object {
        const val TYPE_VIRTUAL = "VIRTUAL"
        const val TYPE_PHYSICAL = "PHYSICAL"
    }
}

sealed class AddCardUiState {
    data object Loading : AddCardUiState()
    data object Content : AddCardUiState()
}