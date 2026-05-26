package com.example.bankapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.local.room.entities.Transaction
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
    val userPreferences: UserPreferences,
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
            userPreferences.userId.collectLatest { userId ->
                if (userId == null) {
                    _ownedCryptosState.value = emptyList()
                    return@collectLatest
                }

                // Usando collect direto aqui pois o collectLatest externo já trata a troca de usuário
                database.bankDao().getCryptoAssetsByUser(userId).collect { assets ->
                    try {
                        val cryptoList = repository.getCryptoList().result
                        
                        val ownedList = assets
                            .filter { it.amount > 0 }
                            .map { asset ->
                                val cryptoInfo = cryptoList.find { it.id == asset.cryptoId } ?: CryptoItem(
                                    id = asset.cryptoId,
                                    symbol = asset.symbol,
                                    source = "",
                                    ohlc_available_from = "",
                                    history_available_from = ""
                                )
                                
                                val detailResponse = try { 
                                    repository.getCryptoData(asset.symbol) 
                                } catch (e: Exception) { 
                                    null 
                                }
                                val currentPrice = detailResponse?.symbols?.firstOrNull()?.last?.toDoubleOrNull() ?: 0.0

                                OwnedCrypto(
                                    cryptoInfo = cryptoInfo,
                                    quantity = asset.amount,
                                    currentPrice = currentPrice
                                )
                            }
                        _ownedCryptosState.value = ownedList
                    } catch (e: Exception) {
                        _ownedCryptosState.value = emptyList()
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
                
                // Buscar o preço atual para calcular o valor da venda
                val cryptoList = repository.getCryptoList().result
                val symbol = currentState.ownedCrypto.cryptoInfo.symbol
                val detailResponse = try { repository.getCryptoData(symbol) } catch (e: Exception) { null }
                val currentPrice = detailResponse?.symbols?.firstOrNull()?.last?.toDoubleOrNull() ?: 0.0
                
                val saleValue = currentState.quantityToSell * currentPrice

                // Lógica de venda no banco
                val assets = dao.getCryptoAssetsByUser(userId).first()
                val asset = assets.find { it.cryptoId == currentState.ownedCrypto.cryptoInfo.id }
                
                if (asset != null) {
                    val newAmount = asset.amount - currentState.quantityToSell
                    if (newAmount > 0) {
                        dao.upsertCryptoAsset(asset.copy(amount = newAmount))
                    } else {
                        // Implementado como upsert 0, mas se tivermos delete no DAO seria melhor
                        dao.upsertCryptoAsset(asset.copy(amount = 0.0))
                    }

                    // Atualizar Saldo do Usuário
                    val user = dao.getUserById(userId)
                    if (user != null) {
                        dao.updateUser(user.copy(balance = user.balance + saleValue))
                    }

                    // Adicionar transação de venda
                    dao.insertTransaction(
                        Transaction(
                            userId = userId,
                            amount = saleValue,
                            description = "Venda de ${currentState.quantityToSell.toInt()} $symbol",
                            date = System.currentTimeMillis(),
                            operation = "SELL"
                        )
                    )
                }
            }

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
