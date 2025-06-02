package com.dreamteam.rand.data.repository

import com.dreamteam.rand.data.dao.TransactionDao
import com.dreamteam.rand.data.entity.Transaction
import com.dreamteam.rand.data.entity.TransactionType
import com.dreamteam.rand.data.firebase.TransactionFirebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.util.Calendar

// handles all expense-related operations
class ExpenseRepository(
    private val transactionDao: TransactionDao,
    private val transactionFirebase: TransactionFirebase = TransactionFirebase()
) {
    // get all expenses for a user - uses local cache by default
    fun getExpenses(userId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactions(userId)
    }

    // get expenses within a date range
    fun getExpensesByDateRange(userId: String, startDate: Long?, endDate: Long?): Flow<List<Transaction>> {
        return if (startDate != null && endDate != null) {
            transactionDao.getTransactionsByDateRange(userId, startDate, endDate)
        } else {
            transactionDao.getTransactions(userId)
        }
    }

    // get expenses in a category
    fun getExpensesByCategory(userId: String, categoryId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(userId, categoryId)
    }

    // get category expenses within a date range
    fun getExpensesByCategoryAndDateRange(
        userId: String,
        categoryId: Long,
        startDate: Long?,
        endDate: Long?
    ): Flow<List<Transaction>> = flow {
        val transactions = if (startDate != null && endDate != null) {
            transactionDao.getTransactionsByCategoryAndDateRange(
                userId = userId,
                categoryId = categoryId,
                startDate = startDate,
                endDate = endDate
            )
        } else {
            transactionDao.getTransactionsByCategory(userId, categoryId).first()
        }
        emit(transactions)
    }

    // get expenses for a specific month and year
    fun getExpensesByMonthAndYear(
        userId: String,
        month: Int,
        year: Int
    ): Flow<List<Transaction>> {
        // Format month and year as strings with leading zeros for month
        val monthStr = String.format("%02d", month)
        val yearStr = year.toString()
        return transactionDao.getExpensesByMonthAndYear(userId, monthStr, yearStr)
    }

    // get total expenses in a date range
    suspend fun getTotalExpensesByDateRange(userId: String, startDate: Long?, endDate: Long?): Double {
        return if (startDate != null && endDate != null) {
            transactionDao.getTotalAmountByTypeAndDateRange(
                userId = userId,
                type = TransactionType.EXPENSE.name,
                startDate = startDate,
                endDate = endDate
            ) ?: 0.0
        } else {
            // Get total for all time if no date range is specified
            val calendar = Calendar.getInstance()
            val endDate = calendar.timeInMillis
            calendar.set(1970, 0, 1, 0, 0, 0)
            val startDate = calendar.timeInMillis
            
            transactionDao.getTotalAmountByTypeAndDateRange(
                userId = userId,
                type = TransactionType.EXPENSE.name,
                startDate = startDate,
                endDate = endDate
            ) ?: 0.0
        }
    }

    // get total expenses for a category in a date range
    suspend fun getTotalExpensesByCategoryAndDateRange(
        userId: String,
        categoryId: Long,
        startDate: Long?,
        endDate: Long?
    ): Double {
        // If we have a date range, use it, otherwise calculate for all time
        val (actualStartDate, actualEndDate) = if (startDate != null && endDate != null) {
            Pair(startDate, endDate)
        } else {
            val calendar = Calendar.getInstance()
            val end = calendar.timeInMillis
            calendar.set(1970, 0, 1, 0, 0, 0)
            val start = calendar.timeInMillis
            Pair(start, end)
        }
        
        // Get all transactions for this category in the date range
        val transactions = transactionDao.getTransactionsByCategoryAndDateRange(
            userId = userId,
            categoryId = categoryId,
            startDate = actualStartDate,
            endDate = actualEndDate
        )
        
        // Calculate the total
        return transactions.sumOf { it.amount }
    }

    // insert a new expense - now includes Firebase
    suspend fun insertExpense(
        userId: String,
        amount: Double,
        description: String,
        categoryId: Long?,
        receiptUri: String?,
        date: Long = System.currentTimeMillis()
    ): Long {
        val transaction = Transaction(
            userId = userId,
            amount = amount,
            description = description,
            type = TransactionType.EXPENSE,
            categoryId = categoryId,
            receiptUri = receiptUri,
            date = date,
            createdAt = System.currentTimeMillis()
        )
        
        android.util.Log.d("ExpenseRepository", "Inserting expense:")
        android.util.Log.d("ExpenseRepository", "Transaction: $transaction")
        
        // Insert into Firebase first
        val firestoreId = transactionFirebase.insertTransaction(transaction)
        if (firestoreId <= 0) {
            android.util.Log.e("ExpenseRepository", "Failed to insert transaction in Firebase")
            return -1L
        }
        
        // Create transaction with Firebase ID
        val transactionWithId = transaction.copy(id = firestoreId)
        
        // Cache in Room
        val result = transactionDao.insertTransaction(transactionWithId)
        
        if (result > 0) {
            android.util.Log.d("ExpenseRepository", "Transaction inserted with ID: $result")
        } else {
            android.util.Log.e("ExpenseRepository", "Failed to insert transaction")
        }
        
        return result
    }

    // sync expenses from Firebase to Room - only called during initial sync or when cache is empty
    suspend fun syncExpenses(userId: String) {
        android.util.Log.d("ExpenseRepository", "Starting expenses sync for user: $userId")
        
        try {
            // Only sync if cache is empty to avoid unnecessary network calls
            if (transactionDao.getTransactionCount(userId) == 0) {
                transactionFirebase.getAllTransactions().collect { transactions ->
                    val userTransactions = transactions.filter { it.userId == userId }
                    if (userTransactions.isNotEmpty()) {
                        android.util.Log.d("ExpenseRepository", "Syncing ${userTransactions.size} expenses for user $userId")
                        transactionDao.syncTransactions(userId, userTransactions)
                    } else {
                        android.util.Log.d("ExpenseRepository", "No expenses found for user $userId in Firebase")
                    }
                }
            } else {
                android.util.Log.d("ExpenseRepository", "Skipping sync - expenses already cached")
            }
        } catch (e: Exception) {
            android.util.Log.e("ExpenseRepository", "Error syncing expenses: ${e.message}", e)
            // Don't throw - let the app continue with cached data
        }
    }

    // update an existing expense
    suspend fun updateExpense(expense: Transaction) {
        transactionDao.updateTransaction(expense)
    }

    // remove an expense
    suspend fun deleteExpense(expense: Transaction) {
        transactionDao.deleteTransaction(expense)
    }

    // get total expenses for current month
    suspend fun getTotalExpensesForMonth(userId: String): Double {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        calendar.set(year, month, 1, 0, 0, 0)
        val startDate = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endDate = calendar.timeInMillis

        return transactionDao.getTotalAmountByTypeAndDateRange(
            userId = userId,
            type = TransactionType.EXPENSE.name,
            startDate = startDate,
            endDate = endDate
        ) ?: 0.0
    }
}