package com.dreamteam.rand.data.repository

import androidx.lifecycle.LiveData
import com.dreamteam.rand.data.dao.GoalDao
import com.dreamteam.rand.data.entity.Goal
import kotlinx.coroutines.flow.Flow

class GoalRepository(private val goalDao: GoalDao) {
    
    fun getGoals(userId: String): Flow<List<Goal>> {
        return goalDao.getGoals(userId)
    }

    suspend fun getGoal(id: Long): Goal? {
        return goalDao.getGoal(id)
    }
    
    fun getGoalsByMonthAndYear(userId: String, month: Int, year: Int): LiveData<List<Goal>> {
        return goalDao.getGoalsByMonthAndYear(userId, month, year)
    }

    fun getGoalById(goalId: Long): LiveData<Goal> {
        return goalDao.getGoalById(goalId)
    }

    fun getAllGoalsOrdered(userId: String): LiveData<List<Goal>> {
        return goalDao.getAllGoalsOrdered(userId)
    }

    suspend fun insertGoal(goal: Goal): Long {
        return goalDao.insertGoal(goal)
    }

    suspend fun deleteGoal(goal: Goal) {
        goalDao.deleteGoal(goal)
    }

    suspend fun updateSpentAmount(goalId: Long, newAmount: Double) {
        goalDao.updateSpentAmount(goalId, newAmount)
    }
}