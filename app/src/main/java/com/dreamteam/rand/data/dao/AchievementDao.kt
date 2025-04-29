package com.dreamteam.rand.data.dao

import androidx.room.*
import com.dreamteam.rand.data.entity.Achievement
import kotlinx.coroutines.flow.Flow

// handles all achievement database operations
// ai note: could suggest personalized achievement goals
@Dao
interface AchievementDao {
    // get all achievements for a user
    @Query("SELECT * FROM achievements WHERE userId = :userId")
    fun getAchievements(userId: String): Flow<List<Achievement>>

    // get achievements of a specific type
    @Query("SELECT * FROM achievements WHERE userId = :userId AND type = :type")
    fun getAchievementsByType(userId: String, type: String): Flow<List<Achievement>>

    // get a specific achievement
    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievement(id: Long): Achievement?

    // add a new achievement
    @Insert
    suspend fun insertAchievement(achievement: Achievement): Long

    // update an achievement
    @Update
    suspend fun updateAchievement(achievement: Achievement)

    // count achievements of a type
    @Query("""
        SELECT COUNT(*) FROM achievements 
        WHERE userId = :userId 
        AND type = :type
    """)
    suspend fun getAchievementCountByType(userId: String, type: String): Int
} 