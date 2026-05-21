package com.example.bankapp.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bankapp.data.local.room.dao.BankDao
import com.example.bankapp.data.local.room.entities.*

@Database(
    entities = [UserAccount::class, Transaction::class, Card::class, CryptoAsset::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bankDao(): BankDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bank_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
