package com.dreamteam.rand.data.repository

import android.util.Log
import com.dreamteam.rand.data.dao.*
import com.dreamteam.rand.data.firebase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class FirebaseRepository(
        private val userDao:UserDao,
        private val transactionDao:TransactionDao,
        private val categoryDao:CategoryDao,
        private val goalDao:GoalDao
) {
    private val userFirebase = UserFirebase()
    private val categoryFirebase = CategoryFirebase()
    private val goalFirebase = GoalFirebase()
    private val transactionFirebase = TransactionFirebase()

    // this method is called after login and syncs the apps local cache with up-to-date firestore data
    suspend fun syncAllUserData(userId: String) = withContext(Dispatchers.IO) {
        try {
            Log.d("FirebaseSync", "Starting full sync for user: $userId")
            syncUserProfile(userId)
            syncUserCategories(userId)
            syncUserGoals(userId)
            syncUserTransactions(userId)
            syncGoalAmounts(userId)
            Log.d("FirebaseSync", "Completed full sync for user: $userId")
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Error during sync", e)
            throw e
        }
    }

    // syncs a user's profile
    private suspend fun syncUserProfile(userId: String) {
        val user = userFirebase.getUserByUid(userId)
        if (user != null) {
            userDao.upsertUser(user)
            Log.d("FirebaseSync", "User profile synced for: $userId")
        } else {
            Log.w("FirebaseSync", "User not found in Firebase: $userId")
        }
    }

    // AI DECLARATION:
    // used Claude to help in constructing these sync methods

    // sync a users categories by comparing firestore data to local cache data
    private suspend fun syncUserCategories(userId: String) {
        val firebaseCategories = categoryFirebase.getAllCategories(userId).first()
        val localCategories = categoryDao.getCategories(userId).first()

        val localMap = localCategories.associateBy { it.id }
        val firebaseMap = firebaseCategories.associateBy { it.id }

        // insert or update category
        for (remote in firebaseCategories) {
            val local = localMap[remote.id]
            if (local == null || local != remote) {
                categoryDao.upsertCategory(remote)
                Log.d("FirebaseSync", "Inserted/Updated category: ${remote.name}")
            }
        }

        // delete any categories not found in firestore
        for (local in localCategories) {
            if (!firebaseMap.containsKey(local.id)) {
                categoryDao.deleteCategory(local)
                Log.d("FirebaseSync", "Deleted category: ${local.name}")
            }
        }
        Log.d("FirebaseSync", "Category sync complete for user: $userId")
    }

    // sync a users goals by comparing firestore data to local cache data
    private suspend fun syncUserGoals(userId: String) {
        val firebaseGoals = goalFirebase.getAllGoals(userId).first()
        val localGoals = goalDao.getGoals(userId).first()

        val localMap = localGoals.associateBy { it.id }
        val firebaseMap = firebaseGoals.associateBy { it.id }

        // insert or update goal
        for (remote in firebaseGoals) {
            val local = localMap[remote.id]
            if (local == null || local != remote) {
                goalDao.upsertGoal(remote)
                Log.d("FirebaseSync", "Inserted/Updated goal: ${remote.name}")
            }
        }

        // delete any goals not found in firestore
        for (local in localGoals) {
            if (!firebaseMap.containsKey(local.id)) {
                goalDao.deleteGoal(local)
                Log.d("FirebaseSync", "Deleted goal: ${local.name}")
            }
        }
        Log.d("FirebaseSync", "Goal sync complete for user: $userId")
    }

    // sync a users expenses by comparing firestore data to local cache data
    private suspend fun syncUserTransactions(userId: String) {
        val firebaseTransactions = transactionFirebase.getAllTransactions(userId).first()
        val localTransactions = transactionDao.getTransactions(userId).first()

        val localMap = localTransactions.associateBy { it.id }
        val firebaseMap = firebaseTransactions.associateBy { it.id }

        // insert or update expense
        for (remote in firebaseTransactions) {
            val local = localMap[remote.id]
            if (local == null || local != remote) {
                transactionDao.upsertTransaction(remote)
                Log.d("FirebaseSync", "Inserted/Updated transaction: ${remote.description} for ${remote.amount}")
            }
        }

        // delete any expenses not found in firestore
        for (local in localTransactions) {
            if (!firebaseMap.containsKey(local.id)) {
                transactionDao.deleteTransaction(local)
                Log.d("FirebaseSync", "Deleted transaction: ${local.description} for ${local.amount}")
            }
        }
        Log.d("FirebaseSync", "Transaction sync complete for user: $userId")
    }

    // sync a users goals with current up-to-date expense amounts
    private suspend fun syncGoalAmounts(userId: String) {
        val goals = goalDao.getGoals(userId).first()
        val transactions = transactionDao.getTransactions(userId).first()

        // work out the total expense amount for goal month
        for (goal in goals) {
            val totalSpent = transactions
                    .filter { tx ->
                    val date = java.util.Date(tx.date)
                val month = date.month + 1
                val year = date.year + 1900
                month == goal.month && year == goal.year
            }
                .sumOf { it.amount }

            // if this amount does match with the current spent amount for goals, then update
            if (goal.currentSpent != totalSpent) {
                val updatedGoal = goal.copy(currentSpent = totalSpent)
                goalDao.updateGoal(updatedGoal)
                goalFirebase.updateGoal(updatedGoal)
            }
        }
    }

    // get necessary data to be used for achievemnts
    suspend fun getAchievementData(userId: String): List<Number?>{
        val expenseCount = transactionDao.getTransactionCount(userId)
        val categoryCount = categoryDao.getCategoryCount(userId)
        val goalCount = goalDao.getGoalCount(userId)
        val totalExpenses = transactionDao.getTotalAmount(userId)

        return listOf(expenseCount, categoryCount, goalCount, totalExpenses)
    }
}
