package com.example.bankapp.data.models

data class OwnedCrypto(
    val cryptoInfo: CryptoItem,
    val quantity: Double,
    val currentPrice: Double
)