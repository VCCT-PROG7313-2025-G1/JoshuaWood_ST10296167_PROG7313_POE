package com.dreamteam.rand.data.repository

import com.dreamteam.rand.data.dao.TransactionDao
import com.dreamteam.rand.data.entity.Transaction
import com.dreamteam.rand.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.util.Calendar

class ExpenseRepository(private val transactionDao: TransactionDao) {
    fun getExpenses(userId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactions(userId)
    }

    fun getExpensesByDateRange(userId: String, startDate: Long?, endDate: Long?): Flow<List<Transaction>> {
        return if (startDate != null && endDate != null) {
            transactionDao.getTransactionsByDateRange(userId, startDate, endDate)
        } else {
            transactionDao.getTransactions(userId)
        }
    }

    fun getExpensesByCategory(userId: String, categoryId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(userId, categoryId)
    }

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

    suspend fun insertExpense(
        userId: String,
        amount: Double,
        description: String,
        categoryId: Long?,
        receiptUri: String?
    ): Long {
        val transaction = Transaction(
            userId = userId,
            amount = amount,
            description = description,
            type = TransactionType.EXPENSE,
            categoryId = categoryId,
            receiptUri = receiptUri,
            date = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateExpense(expense: Transaction) {
        transactionDao.updateTransaction(expense)
    }

    suspend fun deleteExpense(expense: Transaction) {
        transactionDao.deleteTransaction(expense)
    }

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