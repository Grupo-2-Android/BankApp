package com.example.bankapp.presentation.viewmodels.cards

sealed class AddCardUiState {
    data object Loading : AddCardUiState()
    data object Content : AddCardUiState()
}