package com.example.bankapp.data.models

data class CryptoListResponse(
    val result: List<CryptoItem>
)

data class CryptoItem(
    val id: String,
    val symbol: String,
    val source: String,
    val ohlc_available_from: String,
    val history_available_from: String
)
