package com.dreamteam.rand.data.dao

import androidx.room.*
import com.dreamteam.rand.data.entity.Budget
import com.dreamteam.rand.data.entity.BudgetCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("""
        SELECT * FROM budgets 
        WHERE userId = :userId 
        AND month = :month
    """)
    fun getBudget(userId: String, month: Long): Flow<Budget?>

    @Insert
    suspend fun insertBudget(budget: Budget): Long

    @Update
    suspend fun updateBudget(budget: Budget)

    @Query("""
        SELECT * FROM budget_categories 
        WHERE budgetId = :budgetId
    """)
    fun getBudgetCategories(budgetId: Long): Flow<List<BudgetCategory>>

    @Insert
    suspend fun insertBudgetCategory(category: BudgetCategory)

    @Update
    suspend fun updateBudgetCategory(category: BudgetCategory)

    @Query("""
        SELECT SUM(amount) FROM budget_categories 
        WHERE budgetId = :budgetId
    """)
    suspend fun getTotalBudgetAmount(budgetId: Long): Double?

    @Query("""
        SELECT SUM(spent) FROM budget_categories 
        WHERE budgetId = :budgetId
    """)
    suspend fun getTotalSpentAmount(budgetId: Long): Double?
} 