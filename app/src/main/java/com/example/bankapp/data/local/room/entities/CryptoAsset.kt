package com.example.bankapp.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crypto_assets")
data class CryptoAsset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val cryptoId: String,
    val symbol: String,
    val amount: Double
)
