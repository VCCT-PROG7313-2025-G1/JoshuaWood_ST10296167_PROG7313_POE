package com.dreamteam.rand.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dreamteam.rand.data.dao.*
import com.dreamteam.rand.data.firebase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// handles database setup and initialization
class RandDatabaseCallback private constructor(
    private val userDao: UserDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val goalDao: GoalDao
) : RoomDatabase.Callback() {

    private val userFirebase = UserFirebase()
    private val categoryFirebase = CategoryFirebase()
    
    // called when database is first created
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Initialize default data
        CoroutineScope(Dispatchers.IO).launch {
            syncDataFromFirebase()
        }
    }

    // called when database is opened
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        // Sync data from Firebase when database is opened
        CoroutineScope(Dispatchers.IO).launch {
            syncDataFromFirebase()
        }
    }

    private suspend fun syncDataFromFirebase() {
        try {
            android.util.Log.d(TAG, "Starting Firebase data sync")
            
            // Sync all users
            userFirebase.getAllUsers().collect { users ->
                users.forEach { user ->
                    val localUser = userDao.getUserByUid(user.uid)
                    if (localUser == null || localUser != user) {
                        userDao.insertUser(user)
                    }
                }
            }
            
            // Sync all categories
            android.util.Log.d(TAG, "Syncing categories from Firebase")
            categoryFirebase.getAllCategories().collect { categories ->
                android.util.Log.d(TAG, "Received ${categories.size} categories from Firebase")
                categories.forEach { category ->
                    val localCategory = categoryDao.getCategory(category.id)
                    if (localCategory == null || localCategory != category) {
                        android.util.Log.d(TAG, "Inserting/updating category: ${category.name}")
                        categoryDao.insertCategory(category)
                    }
                }
            }
            
            // TODO: Add similar sync logic for transactions and goals
            // when those Firebase handlers are implemented
            
        } catch (e: Exception) {
            // Log error but don't crash the app
            android.util.Log.e(TAG, "Error syncing data from Firebase", e)
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "RandDatabaseCallback"
        
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