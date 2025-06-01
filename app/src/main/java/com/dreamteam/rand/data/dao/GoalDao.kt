package com.dreamteam.rand.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dreamteam.rand.data.entity.Goal
import kotlinx.coroutines.flow.Flow

// handles all goal database operations
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

    // add a new goal or replace if exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    // add multiple goals with conflict resolution
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoals(goals: List<Goal>)

    // update a goal
    @Update
    suspend fun updateGoal(goal: Goal)

    // remove a goal
    @Delete
    suspend fun deleteGoal(goal: Goal)

    // get goals for a specific month and year
    @Query("SELECT * FROM goals WHERE userId = :userId AND month = :month AND year = :year ORDER BY createdAt DESC")
    fun getGoalsByMonthAndYear(userId: String, month: Int, year: Int): LiveData<List<Goal>>

    // get a goal by its id
    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun getGoalById(goalId: Long): LiveData<Goal>

    // Clear all goals for a user (used during sync)
    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun deleteAllUserGoals(userId: String)

    // Get goal count for a user (used to check if sync needed)
    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId")
    suspend fun getGoalCount(userId: String): Int

    // Transaction to sync goals from Firebase to Room
    @Transaction
    suspend fun syncGoals(userId: String, goals: List<Goal>) {
        deleteAllUserGoals(userId)
        insertGoals(goals)
    }
}