package com.dreamteam.rand.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.dreamteam.rand.data.dao.GoalDao
import com.dreamteam.rand.data.entity.Goal
import com.dreamteam.rand.data.firebase.GoalFirebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope

// handles all goal-related operations
class GoalRepository(
    private val goalDao: GoalDao,
    private val goalFirebase: GoalFirebase = GoalFirebase()
) {
    private val TAG = "GoalRepository"
    // allow setting the coroutineScope from ViewModel
    lateinit var coroutineScope: CoroutineScope

    // get all goals for a user - use local cache first
    fun getGoals(userId: String): Flow<List<Goal>> {
        Log.d(TAG, "Getting goals for user: $userId from local cache")
        return goalDao.getGoals(userId)
    }

    // get goals for a specific month and year from local cache
    fun getGoalsByMonthAndYear(userId: String, month: Int, year: Int): LiveData<List<Goal>> {
        Log.d(TAG, "Getting goals for user: $userId, month: $month, year: $year from local cache")
        return goalDao.getGoalsByMonthAndYear(userId, month, year)
    }

    // get a goal by ID from local cache
    fun getGoalById(goalId: Long): LiveData<Goal> {
        Log.d(TAG, "Getting goal by ID: $goalId from local cache")
        return goalDao.getGoalById(goalId)
    }

    // get all goals sorted by date from local cache
    fun getAllGoalsOrdered(userId: String): LiveData<List<Goal>> {
        Log.d(TAG, "Getting all ordered goals for user: $userId from local cache")
        return goalDao.getAllGoalsOrdered(userId)
    }

    // add a new goal - to Firebase and cache
    suspend fun insertGoal(goal: Goal): Long = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Inserting new goal: ${goal.name}")
            
            // Insert into Firebase first to get the ID
            val firestoreId = goalFirebase.insertGoal(goal)
            if (firestoreId <= 0) {
                Log.e(TAG, "Failed to insert goal in Firebase")
                return@withContext -1L
            }
            
            // Create goal with Firebase ID
            val goalWithId = goal.copy(id = firestoreId)
            
            // Cache in Room
            try {
                goalDao.insertGoal(goalWithId)
                Log.d(TAG, "Goal cached successfully with ID: $firestoreId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cache goal in Room", e)
            }
            
            return@withContext firestoreId
            
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting goal", e)
            return@withContext -1L
        }
    }

    // update how much has been spent on a goal - in Firebase and cache
    suspend fun updateSpentAmount(goalId: Long, newAmount: Double) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating spent amount for goal: $goalId to $newAmount")
            
            // Get current goal
            val goal = goalDao.getGoal(goalId) ?: return@withContext
            val updatedGoal = goal.copy(currentSpent = newAmount)
            
            // Update Firebase first
            val firestoreSuccess = goalFirebase.updateGoal(updatedGoal)
            if (!firestoreSuccess) {
                Log.w(TAG, "Failed to update goal in Firebase")
            }
            
            // Update local cache
            goalDao.updateGoal(updatedGoal)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating goal spent amount", e)
            throw e
        }
    }

    // remove a goal
    suspend fun deleteGoal(goal: Goal) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting goal: ${goal.id}")
            
            // delete from Firebase first
            val firestoreSuccess = goalFirebase.deleteGoal(goal)
            if (!firestoreSuccess) {
                Log.w(TAG, "Failed to delete goal from Firebase")
            }
            
            // then delete from local cache
            goalDao.deleteGoal(goal)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting goal", e)
            throw e
        }
    }
}