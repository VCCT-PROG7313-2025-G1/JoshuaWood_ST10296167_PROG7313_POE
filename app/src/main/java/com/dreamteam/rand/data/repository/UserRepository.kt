package com.dreamteam.rand.data.repository

import com.dreamteam.rand.data.dao.UserDao
import com.dreamteam.rand.data.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// handles all user-related operations
class UserRepository(private val userDao: UserDao) {

    // register a new user
    suspend fun registerUser(name: String, email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // Check if user already exists
                val existingUser = userDao.getUserByEmail(email)
                if (existingUser != null) {
                    return@withContext Result.failure(Exception("User with this email already exists"))
                }

                // Create new user with default values
                val user = User(
                    name = name,
                    email = email,
                    password = password
                )
                
                // Insert user into database
                userDao.insertUser(user)
                
                // Return success with user
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // log in a user
    suspend fun loginUser(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val user = userDao.authenticateUser(email, password)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Invalid email or password"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // update user's level and xp
    suspend fun updateUserProgress(uid: String, level: Int, xp: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                userDao.updateUserProgress(uid, level, xp)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // change user's theme
    suspend fun updateUserTheme(uid: String, theme: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                userDao.updateUserTheme(uid, theme)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // toggle notifications
    suspend fun updateNotificationSettings(uid: String, enabled: Boolean): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                userDao.updateNotificationSettings(uid, enabled)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // change user's currency
    suspend fun updateUserCurrency(uid: String, currency: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                userDao.updateUserCurrency(uid, currency)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // get user by their id
    suspend fun getUserByUid(uid: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val user = userDao.getUserByUid(uid)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 