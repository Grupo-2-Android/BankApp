package com.example.bankapp.data.repositories

import com.example.bankapp.data.models.CryptoDataResponse
import com.example.bankapp.data.models.CryptoListResponse
import com.example.bankapp.data.models.LoginRequest
import com.example.bankapp.data.models.LoginResponse
import com.example.bankapp.data.models.VirtualCard
import com.example.bankapp.data.remote.RetrofitInstance

class ApiRepository {
    suspend fun fetchLogin(loginRequest : LoginRequest) : LoginResponse {
        return RetrofitInstance.api.login(loginRequest);
    }

    suspend fun getCryptoList(): CryptoListResponse {
        return RetrofitInstance.api.getCryptoList()
    }

    suspend fun getCryptoData(symbol: String): CryptoDataResponse {
        return RetrofitInstance.api.getCryptoData(symbol)
    }

    suspend fun getVirtualCard(): VirtualCard {
        return RetrofitInstance.api.getVirtualCard()
    }
}