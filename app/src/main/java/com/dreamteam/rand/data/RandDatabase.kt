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
        Goal::class,
        Achievement::class,
        Budget::class,
        BudgetCategory::class
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
    // get access to achievement operations
    abstract fun achievementDao(): AchievementDao
    // get access to budget operations
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: RandDatabase? = null

        // get or create database instance
        fun getDatabase(context: Context): RandDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RandDatabase::class.java,
                    "rand_database"
                )
                .addCallback(RandDatabaseCallback())
                .fallbackToDestructiveMigration() // This will recreate tables if no migration found
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}