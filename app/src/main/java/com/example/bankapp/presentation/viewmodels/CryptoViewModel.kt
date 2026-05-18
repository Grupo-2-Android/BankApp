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

    private val _detailState = MutableStateFlow<CryptoDetailUiState>(CryptoDetailUiState.Idle)
    val detailState: StateFlow<CryptoDetailUiState> = _detailState

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

    fun fetchCryptoDetail(symbol: String) {
        viewModelScope.launch {
            _detailState.value = CryptoDetailUiState.Loading
            try {
                val response = repository.getCryptoData(symbol)
                val detail = response.symbols.firstOrNull()
                if (detail != null) {
                    _detailState.value = CryptoDetailUiState.Success(detail)
                } else {
                    _detailState.value = CryptoDetailUiState.Error("Detalhes não encontrados")
                }
            } catch (e: Exception) {
                _detailState.value = CryptoDetailUiState.Error(e.message ?: "Erro ao carregar detalhes")
            }
        }
    }

    fun getCryptoById(id: String): com.example.bankapp.data.models.CryptoItem? {
        return _allCryptos.find { it.id == id }
    }
}

sealed class CryptoUiState {
    object Loading : CryptoUiState()
    data class Success(val cryptos: List<com.example.bankapp.data.models.CryptoItem>) : CryptoUiState()
    data class Error(val message: String) : CryptoUiState()
}

sealed class CryptoDetailUiState {
    object Idle : CryptoDetailUiState()
    object Loading : CryptoDetailUiState()
    data class Success(val detail: com.example.bankapp.data.models.CryptoDetail) : CryptoDetailUiState()
    data class Error(val message: String) : CryptoDetailUiState()
}
