package com.dreamteam.rand.data.dao

import androidx.room.*
import com.dreamteam.rand.data.entity.Budget
import com.dreamteam.rand.data.entity.BudgetCategory
import kotlinx.coroutines.flow.Flow

// handles all budget database operations
@Dao
interface BudgetDao {
    // get budget for a specific month
    @Query("""
        SELECT * FROM budgets 
        WHERE userId = :userId 
        AND month = :month
    """)
    fun getBudget(userId: String, month: Long): Flow<Budget?>

    // add a new budget
    @Insert
    suspend fun insertBudget(budget: Budget): Long

    // update a budget
    @Update
    suspend fun updateBudget(budget: Budget)

    // get all categories for a budget
    @Query("""
        SELECT * FROM budget_categories 
        WHERE budgetId = :budgetId
    """)
    fun getBudgetCategories(budgetId: Long): Flow<List<BudgetCategory>>

    // add a new budget category
    @Insert
    suspend fun insertBudgetCategory(category: BudgetCategory)

    // update a budget category
    @Update
    suspend fun updateBudgetCategory(category: BudgetCategory)

    // get total budget amount
    @Query("""
        SELECT SUM(amount) FROM budget_categories 
        WHERE budgetId = :budgetId
    """)
    suspend fun getTotalBudgetAmount(budgetId: Long): Double?

    // get total amount spent
    @Query("""
        SELECT SUM(spent) FROM budget_categories 
        WHERE budgetId = :budgetId
    """)
    suspend fun getTotalSpentAmount(budgetId: Long): Double?
} 