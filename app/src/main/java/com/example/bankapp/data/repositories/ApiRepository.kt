package com.example.bankapp.data.repositories

import com.example.bankapp.data.models.LoginRequest
import com.example.bankapp.data.models.LoginResponse
import com.example.bankapp.data.remote.RetrofitInstance

class ApiRepository {
    suspend fun fetchLogin(loginRequest : LoginRequest) : LoginResponse {
        return RetrofitInstance.api.login(loginRequest);
    }
}