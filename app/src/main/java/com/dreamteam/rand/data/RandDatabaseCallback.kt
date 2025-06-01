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

            // Sync users first since categories depend on user IDs
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

                        // After user is synced, sync their categories
                        syncUserCategories(user.uid, isInitialSync)
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
            
            // Only perform full sync if it's initial or cache is empty
            val shouldFullSync = isInitialSync || categoryDao.getCategoryCount(userId) == 0
            
            if (shouldFullSync) {
                Log.d(TAG, "Performing full category sync")
                val firebaseCategories = categoryFirebase.getAllCategories().first()
                val userCategories = firebaseCategories.filter { it.userId == userId }
                
                if (userCategories.isNotEmpty()) {
                    Log.d(TAG, "Syncing ${userCategories.size} categories for user $userId")
                    categoryDao.syncCategories(userId, userCategories)
                }
            } else {
                Log.d(TAG, "Skipping full sync - categories already cached")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing categories for user: $userId", e)
            throw e
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