package com.example.bankapp.data.local.room.dao

import androidx.room.*
import com.example.bankapp.data.local.room.entities.Card
import com.example.bankapp.data.local.room.entities.CryptoAsset
import com.example.bankapp.data.local.room.entities.Transaction
import com.example.bankapp.data.local.room.entities.UserAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface BankDao {
    // User
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: UserAccount)

    @Query("SELECT * FROM user_accounts WHERE id = :userId")
    suspend fun getUserById(userId: String): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE id = :userId")
    fun getUserFlow(userId: String): Flow<UserAccount?>

    @Update
    suspend fun updateUser(user: UserAccount)

    // Transactions
    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getTransactionsByUser(userId: String): Flow<List<Transaction>>

    // Cards
    @Insert
    suspend fun insertCard(card: Card)

    @Query("SELECT * FROM cards WHERE userId = :userId")
    fun getCardsByUser(userId: String): Flow<List<Card>>

    @Query("SELECT COUNT(*) FROM cards WHERE userId = :userId")
    suspend fun getCardCount(userId: String): Int

    // Crypto Assets
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCryptoAsset(asset: CryptoAsset)

    @Query("SELECT * FROM crypto_assets WHERE userId = :userId")
    fun getCryptoAssetsByUser(userId: String): Flow<List<CryptoAsset>>
}
