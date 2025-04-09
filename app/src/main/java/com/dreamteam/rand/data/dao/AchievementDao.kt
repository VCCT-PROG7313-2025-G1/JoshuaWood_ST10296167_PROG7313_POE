package com.dreamteam.rand.data.dao

import androidx.room.*
import com.dreamteam.rand.data.entity.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE userId = :userId")
    fun getAchievements(userId: String): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE userId = :userId AND type = :type")
    fun getAchievementsByType(userId: String, type: String): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievement(id: Long): Achievement?

    @Insert
    suspend fun insertAchievement(achievement: Achievement): Long

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("""
        SELECT COUNT(*) FROM achievements 
        WHERE userId = :userId 
        AND type = :type
    """)
    suspend fun getAchievementCountByType(userId: String, type: String): Int
} 