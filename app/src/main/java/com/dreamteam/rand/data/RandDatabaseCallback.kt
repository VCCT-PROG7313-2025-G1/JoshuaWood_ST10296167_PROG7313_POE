package com.dreamteam.rand.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dreamteam.rand.data.dao.*
import com.dreamteam.rand.data.firebase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import android.util.Log

// handles database setup and initialization
class RandDatabaseCallback private constructor(
    private val userDao: UserDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val goalDao: GoalDao
) : RoomDatabase.Callback() {
    private val TAG = "RandDatabaseCallback"
    private val userFirebase = UserFirebase()
    private val categoryFirebase = CategoryFirebase()
    private val goalFirebase = GoalFirebase()
    private val transactionFirebase = TransactionFirebase()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // called when database is first created
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.d(TAG, "Database created - performing initial sync")
        scope.launch {
            try {
                syncDataFromFirebase(isInitialSync = true)
            } catch (e: Exception) {
                Log.e(TAG, "Error during initial sync", e)
            }
        }
    }

    // called when database is opened
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        Log.d(TAG, "Database opened - checking sync status")
        scope.launch {
            try {
                syncDataFromFirebase(isInitialSync = false)
            } catch (e: Exception) {
                Log.e(TAG, "Error during database open sync", e)
            }
        }
    }

    private suspend fun syncDataFromFirebase(isInitialSync: Boolean) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting Firebase data sync (initial: $isInitialSync)")

            // Sync users first since other data depends on user IDs
            userFirebase.getAllUsers().collect { users ->
                users.forEach { user ->
                    try {
                        val localUser = userDao.getUserByUid(user.uid)
                        if (localUser == null) {
                            Log.d(TAG, "Inserting new user: ${user.email}")
                            userDao.insertUser(user)
                        } else if (localUser != user) {
                            Log.d(TAG, "Updating existing user: ${user.email}")
                            userDao.updateUser(user)
                        }

                        // After user is synced, sync their data
                        syncUserCategories(user.uid, isInitialSync)
                        syncUserGoals(user.uid, isInitialSync)
                        syncUserTransactions(user.uid, isInitialSync)
                        
                        // After all data is synced, update goal amounts
//                        syncGoalAmounts(user.uid)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing user: ${user.email}", e)
                    }
                }
            }

            Log.d(TAG, "Firebase sync completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during Firebase sync", e)
            throw e
        }
    }

    private suspend fun syncUserCategories(userId: String, isInitialSync: Boolean) {
        try {
            Log.d(TAG, "Syncing categories for user: $userId")
            
            val currentCount = categoryDao.getCategoryCount(userId)
            Log.d(TAG, "Current category count in local database: $currentCount")
            
            val shouldFullSync = isInitialSync || currentCount == 0
            Log.d(TAG, "Should perform full sync? $shouldFullSync (isInitialSync=$isInitialSync)")
            
            if (shouldFullSync) {
                Log.d(TAG, "Performing full category sync")
                val firebaseCategories = categoryFirebase.getAllCategories().first()
                Log.d(TAG, "Retrieved ${firebaseCategories.size} total categories from Firebase")
                
                val userCategories = firebaseCategories.filter { it.userId == userId }
                Log.d(TAG, "Filtered ${userCategories.size} categories for user $userId")
                
                if (userCategories.isNotEmpty()) {
                    Log.d(TAG, "Starting sync of ${userCategories.size} categories")
                    categoryDao.syncCategories(userId, userCategories)
                    val finalCount = categoryDao.getCategoryCount(userId)
                    Log.d(TAG, "Sync complete. Final category count: $finalCount")
                } else {
                    Log.d(TAG, "No categories found for user $userId in Firebase")
                }
            } else {
                Log.d(TAG, "Skipping full sync - categories already cached")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing categories for user: $userId", e)
            throw e
        }
    }

    private suspend fun syncUserGoals(userId: String, isInitialSync: Boolean) {
        try {
            Log.d(TAG, "Syncing goals for user: $userId")
            
            // Only perform full sync if it's initial or cache is empty
            val shouldFullSync = isInitialSync || goalDao.getGoalCount(userId) == 0
            
            if (shouldFullSync) {
                Log.d(TAG, "Performing full goals sync")
                goalFirebase.getAllGoals().collect { goals ->
                    val userGoals = goals.filter { it.userId == userId }
                    if (userGoals.isNotEmpty()) {
                        Log.d(TAG, "Syncing ${userGoals.size} goals for user $userId")
                        goalDao.syncGoals(userId, userGoals)
                        
                        // After goals are synced, update their amounts
                        syncGoalAmounts(userId)
                    }
                }
            } else {
                Log.d(TAG, "Skipping full sync - goals already cached")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing goals for user: $userId", e)
            throw e
        }
    }

    private suspend fun syncUserTransactions(userId: String, isInitialSync: Boolean) {
        try {
            Log.d(TAG, "Syncing transactions for user: $userId")
            
            // Only sync if cache is empty or it's initial sync
            val shouldFullSync = isInitialSync || transactionDao.getTransactionCount(userId) == 0
            Log.d(TAG, "Should perform full sync? $shouldFullSync (isInitialSync=$isInitialSync)")
            
            if (shouldFullSync) {
                Log.d(TAG, "Performing full transactions sync")
                transactionFirebase.getAllTransactions().collect { transactions ->
                    val userTransactions = transactions.filter { it.userId == userId }
                    Log.d(TAG, "Retrieved ${transactions.size} total transactions from Firebase")
                    Log.d(TAG, "Filtered ${userTransactions.size} transactions for user $userId")
                    
                    if (userTransactions.isNotEmpty()) {
                        Log.d(TAG, "Starting sync of ${userTransactions.size} transactions")
                        transactionDao.syncTransactions(userId, userTransactions)
                        val finalCount = transactionDao.getTransactionCount(userId)
                        Log.d(TAG, "Sync complete. Final transaction count: $finalCount")
                    } else {
                        Log.d(TAG, "No transactions found for user $userId in Firebase")
                    }
                }
            } else {
                Log.d(TAG, "Skipping sync - transactions already cached")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing transactions for user: $userId", e)
            throw e
        }
    }

    private suspend fun syncGoalAmounts(userId: String) {
        try {
            Log.d(TAG, "Starting goal amount sync for user: $userId")
            
            // Get all goals for the user
            val goals = goalDao.getGoals(userId).first()
            if (goals.isEmpty()) {
                Log.d(TAG, "No goals found for user $userId")
                return
            }
            
            // Get all transactions for the user
            val transactions = transactionDao.getTransactions(userId).first()
            if (transactions.isEmpty()) {
                Log.d(TAG, "No transactions found for user $userId")
                return
            }
            
            // For each goal, calculate total spent from matching transactions
            goals.forEach { goal ->
                try {
                    var totalSpent = 0.0
                    
                    // For each transaction, check if it matches this goal's month/year
                    transactions.forEach { transaction ->
                        // Convert transaction date to month/year
                        val date = java.util.Date(transaction.date)
                        val month = date.month + 1  // Convert 0-based month to 1-based
                        val year = date.year + 1900 // Convert years since 1900 to actual year
                        
                        // If transaction matches goal's month/year, add to total
                        if (month == goal.month && year == goal.year) {
                            Log.d(TAG, "Found matching transaction for goal ${goal.id}: $${transaction.amount} (${goal.month}/$goal.year)")
                            totalSpent += transaction.amount
                        }
                    }
                    
                    // If total spent differs from goal's current spent, update the goal
                    if (goal.currentSpent != totalSpent) {
                        Log.d(TAG, "Updating goal ${goal.id} spent amount from ${goal.currentSpent} to $totalSpent")
                        val updatedGoal = goal.copy(currentSpent = totalSpent)
                        goalDao.updateGoal(updatedGoal)
                        goalFirebase.updateGoal(updatedGoal)
                    } else {
                        Log.d(TAG, "Goal ${goal.id} already has correct spent amount: $totalSpent")
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing amounts for goal ${goal.id}", e)
                }
            }
            
            Log.d(TAG, "Completed goal amount sync for user $userId")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during goal amount sync: ${e.message}", e)
        }
    }

    companion object {
        fun createCallback(database: RandDatabase): RandDatabaseCallback {
            return RandDatabaseCallback(
                userDao = database.userDao(),
                transactionDao = database.transactionDao(),
                categoryDao = database.categoryDao(),
                goalDao = database.goalDao()
            )
        }
    }
}