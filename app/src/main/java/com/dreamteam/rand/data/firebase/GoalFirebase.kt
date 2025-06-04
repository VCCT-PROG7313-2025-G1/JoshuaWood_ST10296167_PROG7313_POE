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

    fun getAllGoals(): Flow<List<Goal>> = callbackFlow {
        Log.d(TAG, "Setting up goals listener")
        val registration = goalsCollection
            .orderBy("id", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to goals: ${error.message}", error)
                    try {
                        val cacheSnapshot = goalsCollection
                            .orderBy("id", Query.Direction.ASCENDING)
                            .get(Source.CACHE)
                            .result
                        val goals = cacheSnapshot?.documents?.mapNotNull { 
                            it.toObject(Goal::class.java) 
                        }?.filter { it.userId.isNotEmpty() } ?: emptyList()
                        Log.d(TAG, "Retrieved ${goals.size} goals from cache")
                        trySend(goals)
                    } catch (e: Exception) {
                        Log.e(TAG, "Cache retrieval also failed: ${e.message}", e)
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }
                
                val goals = snapshot?.documents?.mapNotNull { 
                    it.toObject(Goal::class.java) 
                }?.filter { it.userId.isNotEmpty() } ?: emptyList()
                
                Log.d(TAG, "Retrieved ${goals.size} goals from Firestore")
                goals.forEach { goal ->
                    Log.d(TAG, "Goal: ${goal.name}, ID: ${goal.id}, UserID: ${goal.userId}")
                }
                
                trySend(goals)
            }
        
        awaitClose { 
            Log.d(TAG, "Removing goals listener")
            registration.remove() 
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