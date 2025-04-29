package com.dreamteam.rand.data.repository

import androidx.lifecycle.LiveData
import com.dreamteam.rand.data.dao.GoalDao
import com.dreamteam.rand.data.entity.Goal
import kotlinx.coroutines.flow.Flow

// handles all goal-related operations
class GoalRepository(private val goalDao: GoalDao) {
    // get all goals for a user
    fun getGoals(userId: String): Flow<List<Goal>> {
        return goalDao.getGoals(userId)
    }

    // get a specific goal
    suspend fun getGoal(id: Long): Goal? {
        return goalDao.getGoal(id)
    }
    
    // get goals for a specific month and year
    fun getGoalsByMonthAndYear(userId: String, month: Int, year: Int): LiveData<List<Goal>> {
        return goalDao.getGoalsByMonthAndYear(userId, month, year)
    }

    // get a goal by its id
    fun getGoalById(goalId: Long): LiveData<Goal> {
        return goalDao.getGoalById(goalId)
    }

    // get all goals sorted by date
    fun getAllGoalsOrdered(userId: String): LiveData<List<Goal>> {
        return goalDao.getAllGoalsOrdered(userId)
    }

    // add a new goal
    suspend fun insertGoal(goal: Goal): Long {
        return goalDao.insertGoal(goal)
    }

    // remove a goal
    suspend fun deleteGoal(goal: Goal) {
        goalDao.deleteGoal(goal)
    }

    // update how much has been spent on a goal
    suspend fun updateSpentAmount(goalId: Long, newAmount: Double) {
        goalDao.updateSpentAmount(goalId, newAmount)
    }
}