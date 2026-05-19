package com.example.bankapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Adicionado o context path '/api-android-helper/' que apareceu nos logs da sua API
    private const val BASE_URL = "http://10.0.2.2:8080/api-android-helper/"

    val instance: CryptoService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(CryptoService::class.java)
    }
}
