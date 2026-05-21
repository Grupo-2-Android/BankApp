package com.example.bankapp.api

import com.example.bankapp.data.models.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CryptoService {
    @POST("users/login")
    suspend fun login(@Body request: UserLoginRequest): UserLoginResponse

    @GET("crypto/list")
    suspend fun getCryptoList(): CryptoListResponse

    @POST("crypto/purchase")
    suspend fun purchaseCrypto(@Body request: CryptoPurchaseRequest): CryptoPurchaseResponse
}
