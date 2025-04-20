package com.dreamteam.rand.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dreamteam.rand.data.entity.Goal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY year ASC, month ASC")
    fun getAllGoalsOrdered(userId: String): LiveData<List<Goal>>

    @Query("SELECT * FROM goals WHERE userId = :userId")
    fun getGoals(userId: String): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoal(id: Long): Goal?

    @Insert
    suspend fun insertGoal(goal: Goal): Long

    @Delete
    suspend fun deleteGoal(goal: Goal)

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

    @Query("SELECT * FROM goals WHERE userId = :userId AND month = :month AND year = :year ORDER BY createdAt DESC")
    fun getGoalsByMonthAndYear(userId: String, month: Int, year: Int): LiveData<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun getGoalById(goalId: Long): LiveData<Goal>
}