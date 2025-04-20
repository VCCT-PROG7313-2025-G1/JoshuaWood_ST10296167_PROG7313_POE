package com.dreamteam.rand.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dreamteam.rand.data.dao.*
import com.dreamteam.rand.data.entity.*

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
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RandDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun goalDao(): GoalDao
    abstract fun achievementDao(): AchievementDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: RandDatabase? = null

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