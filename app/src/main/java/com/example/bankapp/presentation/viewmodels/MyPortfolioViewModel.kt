package com.example.bankapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.models.CryptoItem
import com.example.bankapp.data.models.OwnedCrypto
import com.example.bankapp.data.repositories.ApiRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyPortfolioViewModel(
    private val repository: ApiRepository = ApiRepository(),
    private val userPreferences: UserPreferences,
    private val database: AppDatabase
) : ViewModel() {

    private val _ownedCryptosState = MutableStateFlow<List<OwnedCrypto>>(emptyList())
    val ownedCryptosState: StateFlow<List<OwnedCrypto>> = _ownedCryptosState

    private val _saleState = MutableStateFlow<SaleUiState>(SaleUiState.Idle)
    val saleState: StateFlow<SaleUiState> = _saleState

    private val _saleEvents = MutableSharedFlow<SaleEvent>()
    val saleEvents: SharedFlow<SaleEvent> = _saleEvents.asSharedFlow()

    init {
        observeUserAndLoadAssets()
    }

    private fun observeUserAndLoadAssets() {
        viewModelScope.launch {
            // Busca preços para valorizar a carteira no emulador (Mock se falhar)
            val prices = try {
                repository.getCryptoList().symbols?.associate { it.symbol to (it.last?.toDoubleOrNull() ?: 0.0) } ?: emptyMap()
            } catch (e: Exception) {
                mapOf("BTC" to 65000.0, "ETH" to 3500.0, "SOL" to 150.0)
            }

            userPreferences.userId.collectLatest { userId ->
                if (userId != null) {
                    database.bankDao().getCryptoAssetsByUser(userId).collectLatest { assets ->
                        val ownedList = assets.map { asset ->
                            val currentPrice = prices[asset.symbol] ?: 0.0
                            OwnedCrypto(
                                cryptoInfo = CryptoItem(
                                    id = asset.cryptoId,
                                    symbol = asset.symbol,
                                    name = asset.symbol,
                                    last = currentPrice.toString()
                                ),
                                quantity = asset.amount,
                                currentPrice = currentPrice
                            )
                        }.filter { it.quantity > 0 }
                        _ownedCryptosState.value = ownedList
                    }
                }
            }
        }
    }

    fun selectCryptoForSale(cryptoId: String) {
        val crypto = _ownedCryptosState.value.find { it.cryptoInfo.id == cryptoId }
        crypto?.let {
            _saleState.value = SaleUiState.CryptoSelected(it)
        }
    }

    fun setSaleQuantity(quantity: Double) {
        val currentState = _saleState.value
        if (currentState is SaleUiState.CryptoSelected) {
            _saleState.value = SaleUiState.QuantityInputed(currentState.ownedCrypto, quantity)
        }
    }

    fun confirmSale() {
        // A lógica de processamento da venda (DB updates) será feita pela outra dev.
        // Aqui apenas emitimos o sucesso para fins de teste de navegação na UI.
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
