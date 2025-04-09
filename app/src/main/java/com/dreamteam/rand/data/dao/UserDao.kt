package com.dreamteam.rand.data.dao

import androidx.room.*
import com.dreamteam.rand.data.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserByUid(uid: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun authenticateUser(email: String, password: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("UPDATE users SET level = :level, xp = :xp WHERE uid = :uid")
    suspend fun updateUserProgress(uid: String, level: Int, xp: Int)

    @Query("UPDATE users SET theme = :theme WHERE uid = :uid")
    suspend fun updateUserTheme(uid: String, theme: String)

    @Query("UPDATE users SET notificationsEnabled = :enabled WHERE uid = :uid")
    suspend fun updateNotificationSettings(uid: String, enabled: Boolean)

    @Query("UPDATE users SET currency = :currency WHERE uid = :uid")
    suspend fun updateUserCurrency(uid: String, currency: String)
} 