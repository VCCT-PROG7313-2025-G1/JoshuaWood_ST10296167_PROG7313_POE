package com.dreamteam.rand.data.dao

import androidx.room.*
import com.dreamteam.rand.data.entity.User
import kotlinx.coroutines.flow.Flow

// handles all user database operations
@Dao
interface UserDao {
    // get user by their unique id
    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserByUid(uid: String): User?

    // get user by email
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    // check if email and password match
    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun authenticateUser(email: String, password: String): User?

    // get all users
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    // add a new user
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User)

    // update user info
    @Update
    suspend fun updateUser(user: User)

    // remove a user
    @Delete
    suspend fun deleteUser(user: User)

    // update user's level and xp
    @Query("UPDATE users SET level = :level, xp = :xp WHERE uid = :uid")
    suspend fun updateUserProgress(uid: String, level: Int, xp: Int)

    // change user's theme
    @Query("UPDATE users SET theme = :theme WHERE uid = :uid")
    suspend fun updateUserTheme(uid: String, theme: String)

    // toggle notifications
    @Query("UPDATE users SET notificationsEnabled = :enabled WHERE uid = :uid")
    suspend fun updateNotificationSettings(uid: String, enabled: Boolean)

    // change user's currency
    @Query("UPDATE users SET currency = :currency WHERE uid = :uid")
    suspend fun updateUserCurrency(uid: String, currency: String)
} 