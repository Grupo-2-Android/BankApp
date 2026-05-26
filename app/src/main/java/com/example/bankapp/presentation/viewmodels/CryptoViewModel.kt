package com.example.bankapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.local.room.entities.CryptoAsset
import com.example.bankapp.data.local.room.entities.Transaction
import com.example.bankapp.data.models.CryptoItem
import com.example.bankapp.data.repositories.ApiRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _buyState = MutableStateFlow<BuyUiState>(BuyUiState.Idle)
    val buyState: StateFlow<BuyUiState> = _buyState

    private val _buyEvents = MutableSharedFlow<BuyEvent>()
    val buyEvents: SharedFlow<BuyEvent> = _buyEvents.asSharedFlow()

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

    fun startBuyFlow(crypto: CryptoItem, price: Double) {
        _buyState.value = BuyUiState.CryptoSelected(crypto, price)
    }

    fun setBuyAmount(amount: Double) {
        val currentState = _buyState.value
        if (currentState is BuyUiState.CryptoSelected) {
            _buyState.value = BuyUiState.QuantityInputed(currentState.crypto, amount, currentState.price)
        }
    }

    fun confirmBuy() {
        val currentState = _buyState.value
        if (currentState is BuyUiState.QuantityInputed) {
            buyCrypto(
                currentState.crypto.id,
                currentState.crypto.symbol,
                currentState.amount,
                currentState.price
            )
        }
    }

    private fun buyCrypto(cryptoId: String, symbol: String, amount: Double, price: Double) {
        viewModelScope.launch {
            val userId = userPreferences.userId.first() ?: return@launch
            val totalCost = amount * price

            val dao = database.bankDao()
            val user = dao.getUserById(userId) ?: return@launch

            if (user.balance >= totalCost) {
                // Update Balance
                val updatedUser = user.copy(balance = user.balance - totalCost)
                dao.updateUser(updatedUser)

                // Add Transaction
                dao.insertTransaction(
                    Transaction(
                        userId = userId,
                        amount = totalCost,
                        description = "Compra de $amount $symbol",
                        date = System.currentTimeMillis(),
                        operation = "BUY"
                    )
                )

                // Update Crypto Assets
                val existingAssets = dao.getCryptoAssetsByUser(userId).first()
                val asset = existingAssets.find { it.cryptoId == cryptoId }
                if (asset != null) {
                    dao.upsertCryptoAsset(asset.copy(amount = asset.amount + amount))
                } else {
                    dao.upsertCryptoAsset(
                        CryptoAsset(
                            userId = userId,
                            cryptoId = cryptoId,
                            symbol = symbol,
                            amount = amount
                        )
                    )
                }
                _buyEvents.emit(BuyEvent.Success)
                _buyState.value = BuyUiState.Idle
            } else {
                _buyEvents.emit(BuyEvent.Error("Saldo insuficiente"))
            }
        }
    }

    fun resetBuyProcess() {
        _buyState.value = BuyUiState.Idle
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

sealed class BuyUiState {
    object Idle : BuyUiState()
    data class CryptoSelected(val crypto: CryptoItem, val price: Double) : BuyUiState()
    data class QuantityInputed(val crypto: CryptoItem, val amount: Double, val price: Double) : BuyUiState()
}

sealed class BuyEvent {
    object Success : BuyEvent()
    data class Error(val message: String) : BuyEvent()
}
