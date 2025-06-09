package com.dreamteam.rand.data.firebase

import android.util.Log
import com.dreamteam.rand.data.entity.Category
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class CategoryFirebase {
    private val TAG = "CategoryFirebase"
    private val db = FirebaseFirestore.getInstance()
    // get firestore categories collection
    val categoriesCollection = db.collection("categories")

    // AI DECLARATION:
    // used Claude to provide some assistance in constructing the methods
    // and how to interact wih firebase

    // get all categories for a specific user from firestore
    fun getAllCategories(userId: String): Flow<List<Category>> = flow {
        Log.d(TAG, "Getting categories for userId=$userId from Firestore")
        val snapshot = categoriesCollection
            .whereEqualTo("userId", userId)
            .orderBy("id", Query.Direction.ASCENDING)
            .get(Source.SERVER)
            .await()

        val categories = snapshot.documents.mapNotNull { it.toObject(Category::class.java) }
        Log.d(TAG, "Retrieved ${categories.size} categories from Firestore")
        emit(categories)
    }.catch { e ->
        Log.e(TAG, "Error getting categories: ${e.message}", e)
        // if retrieving from firestore fails, try the local cache
        try {
            val cacheSnapshot = categoriesCollection
                .whereEqualTo("userId", userId)
                .orderBy("id", Query.Direction.ASCENDING)
                .get(Source.CACHE)
                .await()

            val categories = cacheSnapshot.documents.mapNotNull {
                it.toObject(Category::class.java)
            }

            Log.d(TAG, "Retrieved ${categories.size} categories from cache")
            emit(categories)
        } catch (e2: Exception) {
            Log.e(TAG, "Cache retrieval also failed: ${e2.message}", e2)
            emit(emptyList())
        }
    }

    // insert a category into firestore
    suspend fun insertCategory(category: Category): Long {
        return try {
            Log.d(TAG, "Inserting category in Firebase: $category")
            
            // get the latest ID used for a category
            val maxIdDoc = categoriesCollection
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            // use this ID to determine the next ID to use
            val nextId = if (!maxIdDoc.isEmpty) {
                val maxCategory = maxIdDoc.documents[0].toObject(Category::class.java)
                (maxCategory?.id ?: 0) + 1
            } else {
                1L // start with and ID of 1 if no categories exist
            }

            Log.d(TAG, "Generated next ID: $nextId")
            
            // create a new category with the next ID
            val categoryWithId = category.copy(id = nextId)
            
            // save the category to Firebase
            Log.d(TAG, "Saving category to Firebase: $categoryWithId")
            val docRef = categoriesCollection.document()
            docRef.set(categoryWithId).await()
            
            Log.d(TAG, "Successfully inserted category with ID: $nextId")
            nextId
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting category: ${e.message}", e)
            -1L
        }
    }

    // update a category with new information on firestore
    suspend fun updateCategory(category: Category): Boolean {
        return try {
            Log.d(TAG, "Updating category: ${category.id}")
            val documents = categoriesCollection
                .whereEqualTo("id", category.id)
                .get()
                .await()
            val document = documents.documents.firstOrNull()
            if (document != null) {
                document.reference.set(category).await()
                Log.d(TAG, "Successfully updated category: ${category.id}")
                true
            } else {
                Log.w(TAG, "No category found to update with ID: ${category.id}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating category: ${e.message}", e)
            false
        }
    }

    // delete a category from firestore
    suspend fun deleteCategory(category: Category): Boolean {
        return try {
            Log.d(TAG, "Deleting category: ${category.id}")
            val documents = categoriesCollection
                .whereEqualTo("id", category.id)
                .get()
                .await()
            val document = documents.documents.firstOrNull()
            if (document != null) {
                document.reference.delete().await()
                Log.d(TAG, "Successfully deleted category: ${category.id}")
                true
            } else {
                Log.w(TAG, "No category found to delete with ID: ${category.id}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting category: ${e.message}", e)
            false
        }
    }
}