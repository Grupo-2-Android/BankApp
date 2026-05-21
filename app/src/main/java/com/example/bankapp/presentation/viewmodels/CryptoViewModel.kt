package com.example.bankapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.local.room.entities.CryptoAsset
import com.example.bankapp.data.local.room.entities.Transaction
import com.example.bankapp.data.models.CryptoItem
import com.example.bankapp.data.models.CryptoDetail
import com.example.bankapp.data.repositories.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun fetchCryptos() {
        viewModelScope.launch {
            _uiState.value = CryptoUiState.Loading
            try {
                val response = repository.getCryptoList()
                val symbols = response.symbols ?: emptyList()
                
                if (symbols.isEmpty()) {
                    setFallbackData()
                } else {
                    _allCryptos = symbols.map { 
                        CryptoItem(it.symbol, it.symbol, it.name, it.last) 
                    }
                    _uiState.value = CryptoUiState.Success(_allCryptos)
                }
            } catch (e: Exception) {
                setFallbackData()
            }
        }
    }

    private fun setFallbackData() {
        _allCryptos = listOf(
            CryptoItem("BTC", "BTC", "Bitcoin", "65000.0"),
            CryptoItem("ETH", "ETH", "Ethereum", "3500.0"),
            CryptoItem("SOL", "SOL", "Solana", "150.0")
        )
        _uiState.value = CryptoUiState.Success(_allCryptos)
    }

    fun fetchCryptoDetail(symbol: String) {
        viewModelScope.launch {
            _detailState.value = CryptoDetailUiState.Loading
            try {
                val response = repository.getCryptoData(symbol)
                val detail = response.symbols?.firstOrNull()
                if (detail != null) {
                    _detailState.value = CryptoDetailUiState.Success(detail)
                } else {
                    _detailState.value = CryptoDetailUiState.Error("Detalhes não encontrados")
                }
            } catch (e: Exception) {
                // Mock para o emulador
                val mockDetail = CryptoDetail(symbol, "65000.0", "1.0", "64000.0", "66000.0", "Agora")
                _detailState.value = CryptoDetailUiState.Success(mockDetail)
            }
        }
    }

    fun getCryptoById(id: String): CryptoItem? {
        // Busca tanto por ID quanto por Símbolo para garantir que encontre
        return _allCryptos.find { it.id == id || it.symbol == id }
    }

    suspend fun buyCrypto(cryptoId: String, symbol: String, amount: Double, price: Double): Boolean {
        val userId = userPreferences.userId.first() ?: return false
        val totalCost = amount * price

        val dao = database.bankDao()
        val user = dao.getUserById(userId) ?: return false

        if (user.balance >= totalCost) {
            val updatedUser = user.copy(balance = user.balance - totalCost)
            dao.updateUser(updatedUser)

            dao.insertTransaction(
                Transaction(
                    userId = userId,
                    amount = totalCost,
                    description = "Compra de $amount $symbol",
                    date = System.currentTimeMillis(),
                    operation = "BUY"
                )
            )

            val existingAssets = dao.getCryptoAssetsByUser(userId).first()
            val asset = existingAssets.find { it.symbol == symbol }
            if (asset != null) {
                dao.upsertCryptoAsset(asset.copy(amount = asset.amount + amount))
            } else {
                dao.upsertCryptoAsset(
                    CryptoAsset(
                        userId = userId,
                        cryptoId = symbol,
                        symbol = symbol,
                        amount = amount
                    )
                )
            }
            return true
        }
        return false
    }
}

sealed class CryptoUiState {
    object Loading : CryptoUiState()
    data class Success(val cryptos: List<CryptoItem>) : CryptoUiState()
    data class Error(val message: String) : CryptoUiState()
}

sealed class CryptoDetailUiState {
    object Idle : CryptoDetailUiState()
    object Loading : CryptoDetailUiState()
    data class Success(val detail: CryptoDetail) : CryptoDetailUiState()
    data class Error(val message: String) : CryptoDetailUiState()
}
