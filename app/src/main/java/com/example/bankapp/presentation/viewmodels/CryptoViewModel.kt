package com.example.bankapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.data.models.CryptoItem
import com.example.bankapp.data.repositories.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CryptoViewModel(private val repository: ApiRepository = ApiRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow<CryptoUiState>(CryptoUiState.Loading)
    val uiState: StateFlow<CryptoUiState> = _uiState

    private var _allCryptos = listOf<CryptoItem>()

    init {
        fetchCryptos()
    }

    fun fetchCryptos() {
        viewModelScope.launch {
            _uiState.value = CryptoUiState.Loading
            try {
                val response = repository.getCryptoList()
                _allCryptos = response.result
                _uiState.value = CryptoUiState.Success(_allCryptos)
            } catch (e: Exception) {
                _uiState.value = CryptoUiState.Error(e.message ?: "Erro ao carregar cryptos")
            }
        }
    }

    fun getCryptoById(id: String): CryptoItem? {
        return _allCryptos.find { it.id == id }
    }
}

sealed class CryptoUiState {
    object Loading : CryptoUiState()
    data class Success(val cryptos: List<CryptoItem>) : CryptoUiState()
    data class Error(val message: String) : CryptoUiState()
}
