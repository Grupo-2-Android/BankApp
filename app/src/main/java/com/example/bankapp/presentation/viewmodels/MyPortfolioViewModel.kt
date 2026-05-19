package com.example.bankapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.data.models.CryptoItem
import com.example.bankapp.data.models.OwnedCrypto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MyPortfolioViewModel : ViewModel() {

    private val mockOwnedCryptos = listOf(
        OwnedCrypto(
            cryptoInfo = CryptoItem("1", "BTC", source = "", ohlc_available_from = "", history_available_from = ""),
            quantity = 0.523,
            currentPrice = 350000.00
        ),
        OwnedCrypto(
            cryptoInfo = CryptoItem("2", "ETH", source = "", ohlc_available_from = "", history_available_from = ""),
            quantity = 4.10,
            currentPrice = 18500.00
        ),
        OwnedCrypto(
            cryptoInfo = CryptoItem("3", "LTC", source = "", ohlc_available_from = "", history_available_from = ""),
            quantity = 25.0,
            currentPrice = 450.00
        )
    )

    private val _ownedCryptosState = MutableStateFlow<List<OwnedCrypto>>(mockOwnedCryptos)
    val ownedCryptosState: StateFlow<List<OwnedCrypto>> = _ownedCryptosState

    private val _saleState = MutableStateFlow<SaleUiState>(SaleUiState.Idle)
    val saleState: StateFlow<SaleUiState> = _saleState

    // Canal de eventos para disparar o Toast e Navegação de forma limpa na MainActivity
    private val _saleEvents = MutableSharedFlow<SaleEvent>()
    val saleEvents: SharedFlow<SaleEvent> = _saleEvents.asSharedFlow()

    fun selectCryptoForSale(cryptoId: String) {
        val crypto = mockOwnedCryptos.find { it.cryptoInfo.id == cryptoId }
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
            // Emite o evento de sucesso capturado pela Activity
            _saleEvents.emit(SaleEvent.Success)
            // Reseta o estado local do fluxo de venda de forma segura
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