package com.example.bankapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase

class ViewModelFactory(
    private val userPreferences: UserPreferences,
    private val database: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(userPreferences = userPreferences, database = database) as T
        }
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(userPreferences = userPreferences, database = database) as T
        }
        if (modelClass.isAssignableFrom(CryptoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CryptoViewModel(userPreferences = userPreferences, database = database) as T
        }
        if (modelClass.isAssignableFrom(TransactionHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionHistoryViewModel(userPreferences = userPreferences, database = database) as T
        }
        if (modelClass.isAssignableFrom(CardManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CardManagementViewModel(userPreferences = userPreferences, database = database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
