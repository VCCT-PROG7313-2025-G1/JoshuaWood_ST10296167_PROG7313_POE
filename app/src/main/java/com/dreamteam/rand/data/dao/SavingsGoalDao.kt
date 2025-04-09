package com.dreamteam.rand.data.dao

import androidx.room.*
import com.dreamteam.rand.data.entity.SavingsGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals WHERE userId = :userId")
    fun getSavingsGoals(userId: String): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals WHERE userId = :userId AND status = :status")
    fun getSavingsGoalsByStatus(userId: String, status: String): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getSavingsGoal(id: Long): SavingsGoal?

    @Insert
    suspend fun insertSavingsGoal(goal: SavingsGoal): Long

    @Update
    suspend fun updateSavingsGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteSavingsGoal(goal: SavingsGoal)

    @Query("""
        SELECT SUM(currentAmount) FROM savings_goals 
        WHERE userId = :userId 
        AND status = :status
    """)
    suspend fun getTotalSavingsByStatus(userId: String, status: String): Double?
} 