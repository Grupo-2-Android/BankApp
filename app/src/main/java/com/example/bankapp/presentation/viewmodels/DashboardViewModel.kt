package com.example.bankapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.local.room.entities.UserAccount
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val userPreferences: UserPreferences,
    private val database: AppDatabase
) : ViewModel() {

    private val _userAccount = MutableStateFlow<UserAccount?>(null)
    val userAccount: StateFlow<UserAccount?> = _userAccount

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            userPreferences.userId.collectLatest { id ->
                id?.let {
                    database.bankDao().getUserFlow(it).collectLatest { account ->
                        _userAccount.value = account
                    }
                }
            }
        }
    }
}
