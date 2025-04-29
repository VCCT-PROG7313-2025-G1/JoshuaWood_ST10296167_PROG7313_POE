package com.dreamteam.rand.data.repository

import com.dreamteam.rand.data.dao.TransactionDao
import com.dreamteam.rand.data.entity.Transaction
import com.dreamteam.rand.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.util.Calendar

// handles all expense-related operations
class ExpenseRepository(private val transactionDao: TransactionDao) {
    // get all expenses for a user
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

    // add a new expense
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
        
        android.util.Log.d("ExpenseRepository", "Inserting expense in repository:")
        android.util.Log.d("ExpenseRepository", "Transaction: $transaction")
        android.util.Log.d("ExpenseRepository", "Transaction date: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(date))}")
        
        val result = transactionDao.insertTransaction(transaction)
        
        if (result > 0) {
            android.util.Log.d("ExpenseRepository", "Transaction inserted with ID: $result")
        } else {
            android.util.Log.e("ExpenseRepository", "Failed to insert transaction")
        }
        
        return result
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