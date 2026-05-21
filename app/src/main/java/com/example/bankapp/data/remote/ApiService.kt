package com.example.bankapp.data.remote;

import com.example.bankapp.data.models.CryptoDataResponse
import com.example.bankapp.data.models.CryptoListResponse
import com.example.bankapp.data.models.LoginRequest
import com.example.bankapp.data.models.LoginResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("users/login")
    suspend fun login(@Body loginRequest : LoginRequest) : LoginResponse

    @GET("crypto/list")
    suspend fun getCryptoList() : CryptoListResponse

    @GET("crypto/data")
    suspend fun getCryptoData(@Query("symbol") symbol: String) : CryptoDataResponse
}