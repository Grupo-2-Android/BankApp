package com.example.bankapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.local.room.entities.CryptoAsset
import com.example.bankapp.data.local.room.entities.Transaction
import com.example.bankapp.data.models.CryptoItem
import com.example.bankapp.data.repositories.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CryptoViewModel(
    private val repository: ApiRepository = ApiRepository(),
    private val userPreferences: UserPreferences,
    private val database: AppDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CryptoUiState>(CryptoUiState.Loading)
    val uiState: StateFlow<CryptoUiState> = _uiState

    private var _allCryptos = listOf<CryptoItem>()

    init {
        fetchCryptos()
    }

    private val _detailState = MutableStateFlow<CryptoDetailUiState>(CryptoDetailUiState.Idle)
    val detailState: StateFlow<CryptoDetailUiState> = _detailState

    // Balance and Buying Flow Logic
    val userBalance: StateFlow<Double> = userPreferences.userId.flatMapLatest { id ->
        if (id != null) database.bankDao().getUserFlow(id) else flowOf(null)
    }.map { it?.balance ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _brlAmount = MutableStateFlow("")
    val brlAmount: StateFlow<String> = _brlAmount

    fun onBrlAmountChange(amount: String) {
        // Only allow numeric input with one decimal separator
        if (amount.isEmpty() || amount.matches(Regex("^\\d*[.,]?\\d*\$"))) {
            _brlAmount.value = amount.replace(",", ".")
        }
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

    fun buyCrypto(cryptoId: String, symbol: String, amountCrypto: Double, amountBrl: Double) {
        viewModelScope.launch {
            val userId = userPreferences.userId.first() ?: return@launch
            
            val dao = database.bankDao()
            val user = dao.getUserById(userId) ?: return@launch

            if (user.balance >= amountBrl) {
                // Update Balance
                val updatedUser = user.copy(balance = user.balance - amountBrl)
                dao.updateUser(updatedUser)

                // Add Transaction
                dao.insertTransaction(
                    Transaction(
                        userId = userId,
                        amount = amountBrl,
                        description = "Compra de $amountCrypto $symbol",
                        date = System.currentTimeMillis(),
                        operation = "BUY"
                    )
                )

                // Update Crypto Assets
                val existingAssets = dao.getCryptoAssetsByUser(userId).first()
                val asset = existingAssets.find { it.cryptoId == cryptoId }
                if (asset != null) {
                    dao.upsertCryptoAsset(asset.copy(amount = asset.amount + amountCrypto))
                } else {
                    dao.upsertCryptoAsset(
                        CryptoAsset(
                            userId = userId,
                            cryptoId = cryptoId,
                            symbol = symbol,
                            amount = amountCrypto
                        )
                    )
                }
                // Clear input after success
                _brlAmount.value = ""
            }
        }
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
