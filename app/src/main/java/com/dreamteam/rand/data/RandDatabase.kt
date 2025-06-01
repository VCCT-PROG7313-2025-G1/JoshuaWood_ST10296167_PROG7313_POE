package com.dreamteam.rand.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dreamteam.rand.data.dao.*
import com.dreamteam.rand.data.entity.*

// main database class - handles all database operations
@Database(
    entities = [
        User::class,
        Transaction::class,
        Category::class,
        Goal::class
    ],
    version = 3,  // changed from 1 to 3 after 2 db schema changes
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RandDatabase : RoomDatabase() {
    // get access to user operations
    abstract fun userDao(): UserDao
    // get access to transaction operations
    abstract fun transactionDao(): TransactionDao
    // get access to category operations
    abstract fun categoryDao(): CategoryDao
    // get access to goal operations
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: RandDatabase? = null

        // get or create database instance
        fun getDatabase(context: Context): RandDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    RandDatabase::class.java,
                    "rand_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Database was just created, initialize it
                        INSTANCE?.let { database ->
                            RandDatabaseCallback.createCallback(database)
                                .onCreate(db)
                        }
                    }

                    override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Database was opened, sync with Firebase
                        INSTANCE?.let { database ->
                            RandDatabaseCallback.createCallback(database)
                                .onOpen(db)
                        }
                    }
                })
                .build()
                
                INSTANCE = db
                db
            }
        }
    }
}