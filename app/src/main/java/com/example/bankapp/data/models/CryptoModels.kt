package com.example.bankapp.data.models

import com.google.gson.annotations.SerializedName

// --- Modelos de Login ---
data class UserLoginRequest(val login: String, val password: String)
data class UserLoginResponse(val id: String?, val name: String?, val message: String)

// --- Modelos de Mercado/Compra ---
data class CryptoListResponse(val symbols: List<CryptoModel>?)
data class CryptoModel(val symbol: String, val name: String?, val last: String?)

data class CryptoPurchaseRequest(val userId: String, val symbol: String, val quantity: Double)
data class CryptoPurchaseResponse(val transactionId: String?, val message: String)

// --- Compatibilidade com Squad Portfolio ---
// Eles usavam CryptoItem em vez de CryptoModel
data class CryptoItem(
    val id: String,
    val symbol: String,
    val name: String? = null,
    val last: String? = null
)

// --- Modelos de Detalhe ---
data class CryptoDataResponse(val symbols: List<CryptoDetail>?)
data class CryptoDetail(
    val symbol: String,
    val last: String,
    @SerializedName("last_btc") val lastBtc: String?,
    val lowest: String?,
    val highest: String?,
    val date: String?
)
