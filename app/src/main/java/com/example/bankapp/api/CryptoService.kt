package com.example.bankapp.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CryptoService {
    @POST("users/login")
    suspend fun login(@Body request: com.example.bankapp.UserLoginRequest): com.example.bankapp.UserLoginResponse

    @GET("crypto/list")
    suspend fun getCryptoList(): com.example.bankapp.CryptoListResponse

    @POST("crypto/purchase")
    suspend fun purchaseCrypto(@Body request: com.example.bankapp.CryptoPurchaseRequest): com.example.bankapp.CryptoPurchaseResponse
}
