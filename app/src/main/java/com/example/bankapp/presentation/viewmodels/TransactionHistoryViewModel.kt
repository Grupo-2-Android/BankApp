package com.example.bankapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.local.room.entities.Transaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionHistoryViewModel(
    private val userPreferences: UserPreferences,
    private val database: AppDatabase
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            userPreferences.userId.collect { userId ->
                if (userId != null) {
                    database.bankDao().getTransactionsByUser(userId).collect {
                        _transactions.value = it
                    }
                }
            }
        }
    }
}
