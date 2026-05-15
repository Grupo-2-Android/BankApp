package com.example.bankapp.data.remote;

import com.example.bankapp.data.models.LoginRequest
import com.example.bankapp.data.models.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST;

interface ApiService {

    @POST("users/login")
    suspend fun login(@Body loginRequest : LoginRequest) : LoginResponse
}