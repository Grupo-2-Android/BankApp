package com.example.bankapp.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val amount: Double,
    val description: String,
    val date: Long,
    val operation: String // "BUY", "SELL", "INITIAL"
)
