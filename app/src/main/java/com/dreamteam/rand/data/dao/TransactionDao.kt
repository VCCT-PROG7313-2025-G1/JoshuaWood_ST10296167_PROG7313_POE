package com.dreamteam.rand.data.dao

import androidx.room.*
import com.dreamteam.rand.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getTransactions(userId: String): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    fun getTransactionsByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND categoryId = :categoryId 
        ORDER BY date DESC
    """)
    fun getTransactionsByCategory(userId: String, categoryId: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND categoryId = :categoryId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getTransactionsByCategoryAndDateRange(
        userId: String,
        categoryId: Long,
        startDate: Long,
        endDate: Long
    ): List<Transaction>

    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE userId = :userId 
        AND type = :type 
        AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalAmountByTypeAndDateRange(userId: String, type: String, startDate: Long, endDate: Long): Double?

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND strftime('%m', datetime(date/1000, 'unixepoch')) = :month
        AND strftime('%Y', datetime(date/1000, 'unixepoch')) = :year
    """)
    fun getExpensesByMonthAndYear(userId: String, month: String, year: String): Flow<List<Transaction>>
}