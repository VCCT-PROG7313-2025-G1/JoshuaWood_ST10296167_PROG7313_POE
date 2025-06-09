package com.dreamteam.rand.data.repository

import android.util.Log
import com.dreamteam.rand.data.dao.UserDao
import com.dreamteam.rand.data.entity.User
import com.dreamteam.rand.data.firebase.UserFirebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// handles all user-related operations
class UserRepository(
    private val userDao: UserDao,
    private val userFirebase: UserFirebase = UserFirebase()
) {
    private val TAG = "UserRepository"

    // register a new user in both firestore and local cache
    suspend fun registerUser(name: String, email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // first check if the user exists in firestore
                val existingFirestoreUser = userFirebase.getUserByEmail(email)
                if (existingFirestoreUser != null) {
                    return@withContext Result.failure(Exception("User with this email already exists"))
                }

                val user = User(
                    name = name,
                    email = email,
                    password = password
                )
                
                // save user to firestore first
                val firestoreSuccess = userFirebase.insertUser(user)
                if (!firestoreSuccess) {
                    return@withContext Result.failure(Exception("Failed to register user in Firebase"))
                }
                
                // then cache the user in room
                userDao.upsertUser(user)
                
                Result.success(user)
            } catch (e: Exception) {
                Log.e(TAG, "Registration error", e)
                Result.failure(e)
            }
        }
    }

    // log in a user
    suspend fun loginUser(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting login process for email: $email")
                
                // try local cache first
                Log.d(TAG, "Checking local cache...")
                val localUser = userDao.authenticateUser(email, password)
                if (localUser != null) {
                    Log.d(TAG, "Found user in local cache")
                    return@withContext Result.success(localUser)
                }

                // ff not in cache, try firestore
                Log.d(TAG, "Checking Firebase...")
                val firestoreUser = userFirebase.authenticateUser(email, password)
                if (firestoreUser != null) {
                    Log.d(TAG, "Found user in Firebase, updating cache")
                    userDao.upsertUser(firestoreUser)
                    return@withContext Result.success(firestoreUser)
                }

                Log.d(TAG, "User not found in either cache or Firebase")
                Result.failure(Exception("Invalid email or password"))
            } catch (e: Exception) {
                Log.e(TAG, "Login error: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    // get user by their id
    suspend fun getUserByUid(uid: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // check local cache first
                val localUser = userDao.getUserByUid(uid)
                if (localUser != null) {
                    return@withContext Result.success(localUser)
                }

                // if not in cache, get from firestore and cache it
                val firestoreUser = userFirebase.getUserByUid(uid)
                if (firestoreUser != null) {
                    userDao.insertUser(firestoreUser)
                    Result.success(firestoreUser)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // update a users level and xp
    suspend fun updateUserProgress(uid: String, level: Int, xp: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // update firestore data
                val firestoreSuccess = userFirebase.updateUserProgress(uid, level, xp)
                if (!firestoreSuccess) {
                    Log.w(TAG, "Failed to update progress in Firebase")
                }
                // then update local cache
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
                // update local cache first
                userDao.updateUserTheme(uid, theme)
                
                // then update Firebase
                val firestoreSuccess = userFirebase.updateUserTheme(uid, theme)
                if (!firestoreSuccess) {
                    Log.w(TAG, "Failed to update theme in Firebase")
                }
                
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
                // update local cache first
                userDao.updateNotificationSettings(uid, enabled)
                
                // then update Firebase
                val firestoreSuccess = userFirebase.updateNotificationSettings(uid, enabled)
                if (!firestoreSuccess) {
                    Log.w(TAG, "Failed to update notification settings in Firebase")
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    // update user's profile picture
    suspend fun updateUserProfilePicture(uid: String, profilePictureUri: String?): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // update local cache first
                userDao.updateUserProfilePicture(uid, profilePictureUri)
                
                // then update Firebase
                val firestoreSuccess = userFirebase.updateUserProfilePicture(uid, profilePictureUri)
                if (!firestoreSuccess) {
                    Log.w(TAG, "Failed to update profile picture in Firebase")
                }
                
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}