package com.dreamteam.rand.data.firebase

import android.util.Log
import com.dreamteam.rand.data.entity.Goal
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GoalFirebase {
    private val TAG = "GoalFirebase"
    private val db = FirebaseFirestore.getInstance()
    val goalsCollection = db.collection("goals")

    suspend fun getGoalById(id: Long): Goal? {
        return try {
            Log.d(TAG, "Getting goal by ID: $id")
            val documents = goalsCollection
                .whereEqualTo("id", id)
                .get(Source.SERVER)
                .await()
            
            val goal = documents.documents.firstOrNull()?.toObject(Goal::class.java)
            if (goal != null) {
                Log.d(TAG, "Found goal: ${goal.name}")
            } else {
                Log.d(TAG, "No goal found with ID: $id")
            }
            goal
        } catch (e: Exception) {
            Log.e(TAG, "Error getting goal by ID: ${e.message}", e)
            try {
                Log.d(TAG, "Trying cache for goal ID: $id")
                val documents = goalsCollection
                    .whereEqualTo("id", id)
                    .get(Source.CACHE)
                    .await()
                documents.documents.firstOrNull()?.toObject(Goal::class.java)
            } catch (e2: Exception) {
                Log.e(TAG, "Cache attempt also failed: ${e2.message}", e2)
                null
            }
        }
    }

    fun getAllGoals(userId: String): Flow<List<Goal>> = flow {
        Log.d(TAG, "Getting goals for userId=$userId from Firestore")
        val snapshot = goalsCollection
            .whereEqualTo("userId", userId)
            .orderBy("id", Query.Direction.ASCENDING)
            .get(Source.SERVER)
            .await()

        val goals = snapshot.documents.mapNotNull { it.toObject(Goal::class.java) }
        Log.d(TAG, "Retrieved ${goals.size} goals from Firestore")
        emit(goals)
    }.catch { e ->
        Log.e(TAG, "Error getting goals: ${e.message}", e)
        // Try cache if server fails
        try {
            val cacheSnapshot = goalsCollection
                .whereEqualTo("userId", userId)
                .orderBy("id", Query.Direction.ASCENDING)
                .get(Source.CACHE)
                .await()

            val goals = cacheSnapshot.documents.mapNotNull {
                it.toObject(Goal::class.java)
            }

            Log.d(TAG, "Retrieved ${goals.size} goals from cache")
            emit(goals)
        } catch (e2: Exception) {
            Log.e(TAG, "Cache retrieval also failed: ${e2.message}", e2)
            emit(emptyList())
        }
    }

    suspend fun insertGoal(goal: Goal): Long {
        return try {
            Log.d(TAG, "Inserting goal in Firebase: $goal")
            
            // Get the current maximum ID
            val maxIdDoc = goalsCollection
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            val nextId = if (!maxIdDoc.isEmpty) {
                val maxGoal = maxIdDoc.documents[0].toObject(Goal::class.java)
                (maxGoal?.id ?: 0) + 1
            } else {
                1L // Start with 1 if no goals exist
            }

            Log.d(TAG, "Generated next ID: $nextId")
            
            // Create a new goal with the next ID
            val goalWithId = goal.copy(id = nextId)
            
            // Save to Firebase
            Log.d(TAG, "Saving goal to Firebase: $goalWithId")
            val docRef = goalsCollection.document()
            docRef.set(goalWithId).await()
            
            Log.d(TAG, "Successfully inserted goal with ID: $nextId")
            nextId
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting goal: ${e.message}", e)
            -1L
        }
    }

    suspend fun updateGoal(goal: Goal): Boolean {
        return try {
            Log.d(TAG, "Updating goal: ${goal.id}")
            val documents = goalsCollection
                .whereEqualTo("id", goal.id)
                .get()
                .await()
            val document = documents.documents.firstOrNull()
            if (document != null) {
                document.reference.set(goal).await()
                Log.d(TAG, "Successfully updated goal: ${goal.id}")
                true
            } else {
                Log.w(TAG, "No goal found to update with ID: ${goal.id}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating goal: ${e.message}", e)
            false
        }
    }

    suspend fun deleteGoal(goal: Goal): Boolean {
        return try {
            Log.d(TAG, "Deleting goal: ${goal.id}")
            val documents = goalsCollection
                .whereEqualTo("id", goal.id)
                .get()
                .await()
            val document = documents.documents.firstOrNull()
            if (document != null) {
                document.reference.delete().await()
                Log.d(TAG, "Successfully deleted goal: ${goal.id}")
                true
            } else {
                Log.w(TAG, "No goal found to delete with ID: ${goal.id}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting goal: ${e.message}", e)
            false
        }
    }
}