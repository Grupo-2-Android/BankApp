package com.example.bankapp.data.local.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val id: String,
    val name: String,
    val balance: Double = 10000000.0
)
