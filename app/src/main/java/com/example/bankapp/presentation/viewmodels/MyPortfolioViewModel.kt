package com.example.bankapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.models.CryptoItem
import com.example.bankapp.data.models.OwnedCrypto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MyPortfolioViewModel(
    private val userPreferences: UserPreferences,
    private val database: AppDatabase
) : ViewModel() {

    val userName: StateFlow<String> = userPreferences.userId
        .flatMapLatest { id ->
            if (id != null) database.bankDao().getUserFlow(id) else flowOf(null)
        }
        .map { it?.name ?: "Usuário" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Usuário")

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val ownedCryptosState: StateFlow<List<OwnedCrypto>> = userPreferences.userId
        .flatMapLatest { userId ->
            if (userId != null) {
                database.bankDao().getCryptoAssetsByUser(userId).map { assets ->
                    assets
                        .filter { it.cryptoId != "BTC" } // LIMPEZA: Remove a entrada duplicada de teste para a entrega
                        .map { asset ->
                        OwnedCrypto(
                            cryptoInfo = CryptoItem(
                                id = asset.cryptoId,
                                symbol = asset.symbol,
                                source = "",
                                ohlc_available_from = "",
                                history_available_from = ""
                            ),
                            quantity = asset.amount,
                            currentPrice = when (asset.symbol) {
                                "BTC" -> 354120.0
                                "ETH" -> 15200.0
                                "LTC" -> 450.0
                                else -> 100.0
                            }
                        )
                    }
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saleState = MutableStateFlow<SaleUiState>(SaleUiState.Idle)
    val saleState: StateFlow<SaleUiState> = _saleState

    private val _saleEvents = MutableSharedFlow<SaleEvent>()
    val saleEvents: SharedFlow<SaleEvent> = _saleEvents.asSharedFlow()

    fun selectCryptoForSale(cryptoId: String) {
        val crypto = ownedCryptosState.value.find { it.cryptoInfo.id == cryptoId }
        crypto?.let {
            _saleState.value = SaleUiState.CryptoSelected(it)
        }
    }

    fun setSaleQuantity(quantity: Double) {
        val currentState = _saleState.value
        if (currentState is SaleUiState.CryptoSelected) {
            if (quantity > 0 && quantity <= currentState.ownedCrypto.quantity) {
                _saleState.value = SaleUiState.QuantityInputed(currentState.ownedCrypto, quantity)
            }
        }
    }

    fun confirmSale() {
        viewModelScope.launch {
            _saleEvents.emit(SaleEvent.Success)
            _saleState.value = SaleUiState.Idle
        }
    }

    fun resetSaleProcess() {
        _saleState.value = SaleUiState.Idle
    }
}

sealed class SaleUiState {
    object Idle : SaleUiState()
    data class CryptoSelected(val ownedCrypto: OwnedCrypto) : SaleUiState()
    data class QuantityInputed(val ownedCrypto: OwnedCrypto, val quantityToSell: Double) : SaleUiState()
}

sealed class SaleEvent {
    object Success : SaleEvent()
}
