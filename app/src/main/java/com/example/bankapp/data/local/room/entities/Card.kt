package com.example.bankapp.data.local.room.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cards",
    indices = [Index(value = ["userId", "type"], unique = true)]
)
data class Card(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val type: String, // "PHYSICAL" or "VIRTUAL"
    val number: String,
    val expiration: String,
    val cvv: String,
    val brand: String
)
