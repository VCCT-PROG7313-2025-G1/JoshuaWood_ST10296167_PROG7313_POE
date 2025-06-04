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

    private suspend fun syncUserProfile(userId: String) {
        val user = userFirebase.getUserByUid(userId)
        if (user != null) {
            userDao.upsertUser(user)
            Log.d("FirebaseSync", "User profile synced for: $userId")
        } else {
            Log.w("FirebaseSync", "User not found in Firebase: $userId")
        }
    }

    private suspend fun syncUserCategories(userId: String) {
        val firebaseCategories = categoryFirebase.getAllCategories(userId).first()
        val localCategories = categoryDao.getCategories(userId).first()

        val localMap = localCategories.associateBy { it.id }
        val firebaseMap = firebaseCategories.associateBy { it.id }

        // Insert or update
        for (remote in firebaseCategories) {
            val local = localMap[remote.id]
            if (local == null || local != remote) {
                categoryDao.upsertCategory(remote)
                Log.d("FirebaseSync", "Inserted/Updated category: ${remote.name}")
            }
        }

        // Delete missing local entries
        for (local in localCategories) {
            if (!firebaseMap.containsKey(local.id)) {
                categoryDao.deleteCategory(local)
                Log.d("FirebaseSync", "Deleted category: ${local.name}")
            }
        }

        Log.d("FirebaseSync", "Category sync complete for user: $userId")
    }

    private suspend fun syncUserGoals(userId: String) {
        val firebaseGoals = goalFirebase.getAllGoals(userId).first()
        val localGoals = goalDao.getGoals(userId).first()

        val localMap = localGoals.associateBy { it.id }
        val firebaseMap = firebaseGoals.associateBy { it.id }

        for (remote in firebaseGoals) {
            val local = localMap[remote.id]
            if (local == null || local != remote) {
                goalDao.upsertGoal(remote)
                Log.d("FirebaseSync", "Inserted/Updated goal: ${remote.name}")
            }
        }

        for (local in localGoals) {
            if (!firebaseMap.containsKey(local.id)) {
                goalDao.deleteGoal(local)
                Log.d("FirebaseSync", "Deleted goal: ${local.name}")
            }
        }

        Log.d("FirebaseSync", "Goal sync complete for user: $userId")
    }

    private suspend fun syncUserTransactions(userId: String) {
        val firebaseTransactions = transactionFirebase.getAllTransactions(userId).first()
        val localTransactions = transactionDao.getTransactions(userId).first()

        val localMap = localTransactions.associateBy { it.id }
        val firebaseMap = firebaseTransactions.associateBy { it.id }

        for (remote in firebaseTransactions) {
            val local = localMap[remote.id]
            if (local == null || local != remote) {
                transactionDao.upsertTransaction(remote)
                Log.d("FirebaseSync", "Inserted/Updated transaction: ${remote.description} for ${remote.amount}")
            }
        }

        for (local in localTransactions) {
            if (!firebaseMap.containsKey(local.id)) {
                transactionDao.deleteTransaction(local)
                Log.d("FirebaseSync", "Deleted transaction: ${local.description} for ${local.amount}")
            }
        }

        Log.d("FirebaseSync", "Transaction sync complete for user: $userId")
    }

    private suspend fun syncGoalAmounts(userId: String) {
        val goals = goalDao.getGoals(userId).first()
        val transactions = transactionDao.getTransactions(userId).first()

        for (goal in goals) {
            val totalSpent = transactions
                    .filter { tx ->
                    val date = java.util.Date(tx.date)
                val month = date.month + 1
                val year = date.year + 1900
                month == goal.month && year == goal.year
            }
                .sumOf { it.amount }

            if (goal.currentSpent != totalSpent) {
                val updatedGoal = goal.copy(currentSpent = totalSpent)
                goalDao.updateGoal(updatedGoal)
                goalFirebase.updateGoal(updatedGoal)
            }
        }
    }
}
