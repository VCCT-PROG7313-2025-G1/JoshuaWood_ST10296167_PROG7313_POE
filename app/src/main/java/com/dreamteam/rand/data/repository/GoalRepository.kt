package com.dreamteam.rand.data.repository

import com.dreamteam.rand.data.dao.GoalDao
import com.dreamteam.rand.data.entity.Goal
import com.dreamteam.rand.data.entity.GoalSpendingStatus
import kotlinx.coroutines.flow.Flow

class GoalRepository(private val goalDao: GoalDao) {
    
    fun getGoals(userId: String): Flow<List<Goal>> {
        return goalDao.getGoals(userId)
    }
    
    fun getGoalsByMonth(userId: String, monthYear: Long): Flow<List<Goal>> {
        return goalDao.getGoalsByMonth(userId, monthYear)
    }
    
    suspend fun getGoal(id: Long): Goal? {
        return goalDao.getGoal(id)
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