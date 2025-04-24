package com.dreamteam.rand.data.repository

import com.dreamteam.rand.data.dao.TransactionDao
import com.dreamteam.rand.data.entity.Transaction
import com.dreamteam.rand.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ExpenseRepository(private val transactionDao: TransactionDao) {
    fun getExpenses(userId: String): Flow<List<Transaction>> {
        return transactionDao.getTransactions(userId)
    }

    fun getExpensesByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByDateRange(userId, startDate, endDate)
    }

    fun getExpensesByCategory(
        userId: String,
        categoryId: Long
    ): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByCategory(userId, categoryId)
    }

    suspend fun insertExpense(expense: Transaction): Long {
        return transactionDao.insertTransaction(expense)
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
            userId,
            TransactionType.EXPENSE.name,
            startDate,
            endDate
        ) ?: 0.0
    }
} 