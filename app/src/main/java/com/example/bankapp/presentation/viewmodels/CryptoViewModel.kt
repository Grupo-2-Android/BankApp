package com.example.bankapp.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.R
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.local.room.entities.CryptoAsset
import com.example.bankapp.data.local.room.entities.Transaction
import com.example.bankapp.data.models.CryptoItem
import com.example.bankapp.data.repositories.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CryptoViewModel(
    application: Application,
    private val repository: ApiRepository = ApiRepository(),
    private val userPreferences: UserPreferences,
    private val database: AppDatabase
) : AndroidViewModel(application) {

    private val ctx get() = getApplication<Application>()

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
                _uiState.value = CryptoUiState.Error(e.message ?: ctx.getString(R.string.crypto_vm_error_load_list))
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
                    _detailState.value = CryptoDetailUiState.Error(ctx.getString(R.string.crypto_vm_error_detail_not_found))
                }
            } catch (e: Exception) {
                _detailState.value = CryptoDetailUiState.Error(e.message ?: ctx.getString(R.string.crypto_vm_error_load_detail))
            }
        }
    }

    fun getCryptoById(id: String): CryptoItem? {
        return _allCryptos.find { it.id == id }
    }

    fun buyCrypto(cryptoId: String, symbol: String, amount: Double, price: Double) {
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
                        description = ctx.getString(R.string.crypto_vm_buy_description, amount.toString(), symbol),
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
            }
        }
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
    data class Success(val detail: com.example.bankapp.data.models.CryptoDetail) : CryptoDetailUiState()
    data class Error(val message: String) : CryptoDetailUiState()
}
