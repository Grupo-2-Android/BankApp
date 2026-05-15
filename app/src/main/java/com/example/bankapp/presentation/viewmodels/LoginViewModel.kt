package com.example.bankapp.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.data.models.LoginRequest
import com.example.bankapp.data.repositories.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: ApiRepository = ApiRepository()) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginStatus>(LoginStatus.Idle)
    val loginState: StateFlow<LoginStatus> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginStatus.Loading
            try {
                Log.d("LOGIN_VIEWMODEL", "Iniciando login")
                val request = LoginRequest(login = username, password = password)
                Log.d("LOGIN_VIEWMODEL", "antes do response")
                val response = repository.fetchLogin(request)
                Log.d("LOGIN_VIEWMODEL", "Resposta recebida: ${response.message}")
                
                if (response.message == "Login realizado com sucesso") {
                    _loginState.value = LoginStatus.Success
                } else {
                    _loginState.value = LoginStatus.Error(response.message)
                }
            } catch (e: Exception) {
                Log.d("LOGIN_VIEWMODEL", "Ocorreu um erro")
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
