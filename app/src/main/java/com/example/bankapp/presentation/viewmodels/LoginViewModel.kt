package com.example.bankapp.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bankapp.R
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.local.room.entities.UserAccount
import com.example.bankapp.data.models.LoginRequest
import com.example.bankapp.data.repositories.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application,
    private val repository: ApiRepository = ApiRepository(),
    private val userPreferences: UserPreferences,
    private val database: AppDatabase
) : AndroidViewModel(application) {

    private val ctx get() = getApplication<Application>()

    private val _loginState = MutableStateFlow<LoginStatus>(LoginStatus.Idle)
    val loginState: StateFlow<LoginStatus> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginStatus.Loading

            try {

                val request = LoginRequest(
                    login = username,
                    password = password
                )

                val response = repository.fetchLogin(request)
                if (response.message == ctx.getString(R.string.login_vm_success_message)) {
                    val userId = response.id ?: ""
                    val name = response.name ?: ""

                    userPreferences.saveUser(userId, name)

                    // Room cache local
                    val dao = database.bankDao()
                    val existingUser = dao.getUserById(userId)

                    if (existingUser == null) {
                        dao.insertUser(
                            UserAccount(
                                id = userId,
                                name = name
                            )
                        )
                    }

                    _loginState.value = LoginStatus.Success

                } else {
                    _loginState.value = LoginStatus.Error(response.message)
                }

            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val message = try {
                    com.google.gson.Gson().fromJson(errorBody, com.example.bankapp.data.models.LoginResponse::class.java).message
                } catch (ex: Exception) {
                    "Usuário ou senha inválidos"
                }
                _loginState.value = LoginStatus.Error(message)
            } catch (e: Exception) {
                _loginState.value = LoginStatus.Error(e.message ?: ctx.getString(R.string.login_vm_error_generic))
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