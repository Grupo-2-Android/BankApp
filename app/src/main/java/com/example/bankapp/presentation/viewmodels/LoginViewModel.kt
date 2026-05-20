package com.example.bankapp.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.local.room.entities.UserAccount
import com.example.bankapp.data.models.LoginRequest
import com.example.bankapp.data.repositories.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: ApiRepository = ApiRepository(),
    private val userPreferences: UserPreferences,
    private val database: AppDatabase
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginStatus>(LoginStatus.Idle)
    val loginState: StateFlow<LoginStatus> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginStatus.Loading
            try {
                val request = LoginRequest(login = username, password = password)
                val response = repository.fetchLogin(request)
                if (response.message == "Login realizado com sucesso") {
                    val userId = response.id ?: ""
                    val name = response.name ?: ""
                    
                    // Save to DataStore
                    userPreferences.saveUser(userId, name)
                    
                    // Initialize Room UserAccount if not exists
                    val dao = database.bankDao()
                    val existingUser = dao.getUserById(userId)
                    if (existingUser == null) {
                        dao.insertUser(UserAccount(id = userId, name = name))
                    }

                    _loginState.value = LoginStatus.Success
                } else {
                    _loginState.value = LoginStatus.Error(response.message)
                }
            } catch (e: Exception) {
                _loginState.value = LoginStatus.Error(e.message ?: "Ocorreu um erro")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginStatus.Idle
    }
}

sealed class LoginStatus {
    object Idle : LoginStatus()
    object Loading : LoginStatus()
    object Success : LoginStatus()
    data class Error(val message: String) : LoginStatus()
}
