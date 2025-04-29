package com.dreamteam.rand.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dreamteam.rand.data.entity.Goal
import kotlinx.coroutines.flow.Flow

// handles all goal database operations
// ai note: could suggest goal adjustments based on spending patterns
@Dao
interface GoalDao {
    // get all goals sorted by date
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY year ASC, month ASC")
    fun getAllGoalsOrdered(userId: String): LiveData<List<Goal>>

    // get all goals for a user
    @Query("SELECT * FROM goals WHERE userId = :userId")
    fun getGoals(userId: String): Flow<List<Goal>>

    // get a specific goal
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoal(id: Long): Goal?

    // add a new goal
    @Insert
    suspend fun insertGoal(goal: Goal): Long

    // remove a goal
    @Delete
    suspend fun deleteGoal(goal: Goal)

    // update how much has been spent and recalculate status
    @Query("""
        UPDATE goals 
        SET currentSpent = :newAmount,
            spendingStatus = CASE
                WHEN :newAmount < minAmount THEN 'BELOW_MINIMUM'
                WHEN :newAmount > maxAmount THEN 'OVER_BUDGET'
                ELSE 'ON_TRACK'
            END,
            createdAt = createdAt
        WHERE id = :goalId
    """)
    suspend fun updateSpentAmount(goalId: Long, newAmount: Double)

    // get goals for a specific month and year
    @Query("SELECT * FROM goals WHERE userId = :userId AND month = :month AND year = :year ORDER BY createdAt DESC")
    fun getGoalsByMonthAndYear(userId: String, month: Int, year: Int): LiveData<List<Goal>>

    // get a goal by its id
    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun getGoalById(goalId: Long): LiveData<Goal>
}