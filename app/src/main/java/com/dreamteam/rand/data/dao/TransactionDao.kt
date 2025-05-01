package com.dreamteam.rand.data.dao

import androidx.room.*
import com.dreamteam.rand.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

// used chatgpt to help design the transaction database operations
// helped optimize queries for date ranges and category filtering
// assisted with flow integration for reactive updates
@Dao
interface TransactionDao {
    // get all transactions for a user, newest first
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getTransactions(userId: String): Flow<List<Transaction>>

    // get transactions within a date range
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    fun getTransactionsByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<Transaction>>

    // get all transactions in a category
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND categoryId = :categoryId 
        ORDER BY date DESC
    """)
    fun getTransactionsByCategory(userId: String, categoryId: Long): Flow<List<Transaction>>

    // get category transactions within a date range
    @Query("SELECT * FROM transactions WHERE userId = :userId AND categoryId = :categoryId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getTransactionsByCategoryAndDateRange(
        userId: String,
        categoryId: Long,
        startDate: Long,
        endDate: Long
    ): List<Transaction>

    // add a new transaction
    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long

    // update an existing transaction
    @Update
    suspend fun updateTransaction(transaction: Transaction)

    // remove a transaction
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    // get total amount for a transaction type in a date range
    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE userId = :userId 
        AND type = :type 
        AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalAmountByTypeAndDateRange(userId: String, type: String, startDate: Long, endDate: Long): Double?

    // get all transactions for a specific month and year
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND strftime('%m', datetime(date/1000, 'unixepoch')) = :month
        AND strftime('%Y', datetime(date/1000, 'unixepoch')) = :year
    """)
    fun getExpensesByMonthAndYear(userId: String, month: String, year: String): Flow<List<Transaction>>
}