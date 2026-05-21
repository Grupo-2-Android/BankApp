package com.example.bankapp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.data.models.UserLoginRequest
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.local.room.entities.UserAccount
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
        val cleanUser = username.trim().lowercase()
        viewModelScope.launch {
            _loginState.value = LoginStatus.Loading
            try {
                // Tenta login via API
                val request = UserLoginRequest(login = cleanUser, password = password)
                val response = repository.fetchLogin(request)
                
                if (response.id != null) {
                    processSuccessfulLogin(response.id, response.name ?: username)
                } else {
                    _loginState.value = LoginStatus.Error(response.message)
                }
            } catch (e: Exception) {
                // MODO EMULADOR: Se falhar a conexão, permite entrar com estes usuários para teste offline
                if (cleanUser == "fernanda.correia" || cleanUser == "admin" || cleanUser == "fernanda") {
                     processSuccessfulLogin(cleanUser, "Fernanda")
                } else {
                    _loginState.value = LoginStatus.Error("Erro de Conexão: Verifique se o servidor local está rodando na porta 8080")
                }
            }
        }
    }

    private suspend fun processSuccessfulLogin(userId: String, name: String) {
        userPreferences.saveUser(userId, name)
        
        val dao = database.bankDao()
        val existingUser = dao.getUserById(userId)
        
        // Garante saldo alto para conseguir comprar Bitcoin no emulador sem erros
        val initialBalance = 1000000.0 
        if (existingUser == null) {
            dao.insertUser(UserAccount(id = userId, name = name, balance = initialBalance))
        } else if (existingUser.balance < 1000.0) {
            // Se o saldo acabou em testes anteriores, recarrega para permitir novos testes
            dao.updateUser(existingUser.copy(balance = initialBalance))
        }

        _loginState.value = LoginStatus.Success
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
