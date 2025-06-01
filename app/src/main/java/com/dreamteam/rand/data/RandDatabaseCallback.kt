package com.dreamteam.rand.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dreamteam.rand.data.dao.*
import com.dreamteam.rand.data.firebase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// handles database setup and initialization
class RandDatabaseCallback(
    private val userDao: UserDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val goalDao: GoalDao
) : RoomDatabase.Callback() {

    private val userFirebase = UserFirebase()
    
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
            // Sync all users
            userFirebase.getAllUsers().collect { users ->
                users.forEach { user ->
                    val localUser = userDao.getUserByUid(user.uid)
                    if (localUser == null || localUser != user) {
                        userDao.insertUser(user)
                    }
                }
            }
            
            // TODO: Add similar sync logic for transactions, categories, and goals
            // when those Firebase handlers are implemented
        } catch (e: Exception) {
            // Log error but don't crash the app
            e.printStackTrace()
        }
    }
}