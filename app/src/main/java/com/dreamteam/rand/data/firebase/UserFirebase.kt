package com.dreamteam.rand.data.firebase

import android.util.Log
import com.dreamteam.rand.data.entity.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class UserFirebase {
    private val TAG = "UserFirebase"
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun getUserByUid(uid: String): User? {
        return try {
            val document = usersCollection.document(uid).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return try {
            val documents = usersCollection.whereEqualTo("email", email).get().await()
            documents.documents.firstOrNull()?.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun authenticateUser(email: String, password: String): User? {
        return try {
            Log.d(TAG, "Attempting to authenticate user with email: $email")
            val documents = usersCollection
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .await()
            
            Log.d(TAG, "Query completed. Found ${documents.size()} matching documents")
            
            if (documents.isEmpty) {
                Log.d(TAG, "No matching user found")
                null
            } else {
                val user = documents.documents.firstOrNull()?.toObject(User::class.java)
                if (user != null) {
                    Log.d(TAG, "Successfully found and mapped user object")
                } else {
                    Log.d(TAG, "Failed to map document to User object")
                }
                user
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during authentication: ${e.message}", e)
            null
        }
    }

    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val registration = usersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            
            val users = snapshot?.documents?.mapNotNull { 
                it.toObject(User::class.java) 
            } ?: emptyList()
            
            trySend(users)
        }
        
        awaitClose { registration.remove() }
    }

    suspend fun insertUser(user: User): Boolean {
        return try {
            usersCollection.document(user.uid).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUser(user: User): Boolean {
        return try {
            usersCollection.document(user.uid).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteUser(user: User): Boolean {
        return try {
            usersCollection.document(user.uid).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUserProgress(uid: String, level: Int, xp: Int): Boolean {
        return try {
            usersCollection.document(uid).update(
                mapOf(
                    "level" to level,
                    "xp" to xp
                )
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUserTheme(uid: String, theme: String): Boolean {
        return try {
            usersCollection.document(uid).update("theme", theme).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateNotificationSettings(uid: String, enabled: Boolean): Boolean {
        return try {
            usersCollection.document(uid).update("notificationsEnabled", enabled).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUserCurrency(uid: String, currency: String): Boolean {
        return try {
            usersCollection.document(uid).update("currency", currency).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}