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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyPortfolioViewModel(
    private val userPreferences: UserPreferences,
    private val database: AppDatabase,
    private val repository: ApiRepository = ApiRepository()
) : ViewModel() {

    private val _ownedCryptosState = MutableStateFlow<List<OwnedCrypto>>(emptyList())
    val ownedCryptosState: StateFlow<List<OwnedCrypto>> = _ownedCryptosState

    private val _saleState = MutableStateFlow<SaleUiState>(SaleUiState.Idle)
    val saleState: StateFlow<SaleUiState> = _saleState

    private val _saleEvents = MutableSharedFlow<SaleEvent>()
    val saleEvents: SharedFlow<SaleEvent> = _saleEvents.asSharedFlow()

    init {
        observeAssets()
    }

    private fun observeAssets() {
        viewModelScope.launch {
            val userId = userPreferences.userId.first() ?: return@launch
            database.bankDao().getCryptoAssetsByUser(userId).collectLatest { assets ->
                val cryptoList = repository.getCryptoList().result
                
                val ownedList = assets.map { asset ->
                    val cryptoInfo = cryptoList.find { it.id == asset.cryptoId } ?: CryptoItem(
                        id = asset.cryptoId,
                        symbol = asset.symbol,
                        source = "",
                        ohlc_available_from = "",
                        history_available_from = ""
                    )
                    
                    // Busca preço real da API
                    val currentPrice = try {
                        val response = repository.getCryptoData(asset.symbol)
                        response.symbols.firstOrNull()?.last?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                    } catch (e: Exception) {
                        0.0
                    }

                    OwnedCrypto(
                        cryptoInfo = cryptoInfo,
                        quantity = asset.amount,
                        currentPrice = currentPrice
                    )
                }
                _ownedCryptosState.value = ownedList
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
            if (quantity > 0 && quantity <= currentState.ownedCrypto.quantity) {
                _saleState.value = SaleUiState.QuantityInputed(currentState.ownedCrypto, quantity)
            }
        }
    }

    fun confirmSale() {
        viewModelScope.launch {
            val currentState = _saleState.value
            if (currentState is SaleUiState.QuantityInputed) {
                val userId = userPreferences.userId.first() ?: return@launch
                val dao = database.bankDao()
                
                val totalReceive = currentState.quantityToSell * currentState.ownedCrypto.currentPrice
                
                // 1. Atualizar saldo do usuário
                val user = dao.getUserById(userId)
                if (user != null) {
                    dao.updateUser(user.copy(balance = user.balance + totalReceive))
                }

                // 2. Atualizar Assets
                val asset = dao.getCryptoAssetsByUser(userId).first().find { it.cryptoId == currentState.ownedCrypto.cryptoInfo.id }
                if (asset != null) {
                    val newAmount = asset.amount - currentState.quantityToSell
                    if (newAmount > 0) {
                        dao.upsertCryptoAsset(asset.copy(amount = newAmount))
                    } else {
                        dao.upsertCryptoAsset(asset.copy(amount = 0.0))
                    }
                }

                // 3. Registrar Transação
                dao.insertTransaction(
                    com.example.bankapp.data.local.room.entities.Transaction(
                        userId = userId,
                        amount = totalReceive,
                        description = "Venda de ${currentState.quantityToSell} ${currentState.ownedCrypto.cryptoInfo.symbol}",
                        date = System.currentTimeMillis(),
                        operation = "SELL"
                    )
                )

                _saleEvents.emit(SaleEvent.Success)
                _saleState.value = SaleUiState.Idle
            }
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
