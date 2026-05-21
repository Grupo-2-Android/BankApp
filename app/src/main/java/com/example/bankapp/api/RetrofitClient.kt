package com.example.bankapp.api

import com.example.bankapp.data.remote.RetrofitInstance

object RetrofitClient {
    // Agora usa a mesma instância centralizada para evitar conflitos de conexão
    val instance: CryptoService by lazy {
        RetrofitInstance.cryptoService
    }
}
