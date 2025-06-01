package com.dreamteam.rand.data.repository

import android.util.Log
import com.dreamteam.rand.data.dao.CategoryDao
import com.dreamteam.rand.data.entity.Category
import com.dreamteam.rand.data.entity.TransactionType
import com.dreamteam.rand.data.firebase.CategoryFirebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

// handles all category-related operations
class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val categoryFirebase: CategoryFirebase = CategoryFirebase()
) {
    private val TAG = "CategoryRepository"
    private val syncedUsers = mutableSetOf<String>() // Track which users we've synced for

    // get all categories for a user
    fun getCategories(userId: String): Flow<List<Category>> {
        Log.d(TAG, "Getting categories for user: $userId")
        return categoryDao.getCategories(userId)
    }
    
    // get categories of a specific type (income/expense)
    fun getCategoriesByType(userId: String, type: TransactionType): Flow<List<Category>> {
        Log.d(TAG, "Getting categories for user: $userId, type: $type")
        
        // Only sync from Firebase if we haven't synced for this user yet
        if (!syncedUsers.contains(userId)) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    // Get all categories for this user from Firebase
                    val documents = categoryFirebase.categoriesCollection
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("type", type.toString())
                        .get()
                        .await()
                    
                    val categories = documents.documents.mapNotNull { 
                        it.toObject(Category::class.java) 
                    }
                    
                    Log.d(TAG, "Got ${categories.size} categories from Firebase for user $userId and type $type")
                    
                    // Cache them in Room
                    try {
                        categories.forEach { category ->
                            Log.d(TAG, "Caching category: ${category.name}")
                            categoryDao.insertCategory(category)
                        }
                        // Mark this user as synced after successful caching
                        syncedUsers.add(userId)
                    } catch (e: Exception) {
                        Log.d(TAG, "Some categories already cached")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing categories from Firebase", e)
                }
            }
        } else {
            Log.d(TAG, "Using cached categories for user: $userId")
        }
        
        // Return the Flow from Room which will update as cache is populated
        return categoryDao.getCategoriesByType(userId, type.toString())
    }
    
    // get a specific category
    suspend fun getCategory(id: Long): Category? {
        return categoryDao.getCategory(id)
    }
    
    // add a new category
    suspend fun insertCategory(category: Category): Long = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Inserting category in repository: $category")
            
            // First insert into Firebase and get the generated ID
            val firestoreId = categoryFirebase.insertCategory(category)
            if (firestoreId <= 0) {
                Log.e(TAG, "Failed to insert category in Firebase")
                return@withContext -1L
            }
            
            // Create a new category with the Firebase ID
            val categoryWithId = category.copy(id = firestoreId)
            
            // Then cache in Room with the same ID
            val roomResult = categoryDao.insertCategory(categoryWithId)
            if (roomResult <= 0) {
                Log.e(TAG, "Failed to cache category in Room")
                return@withContext -1L
            }
            
            Log.d(TAG, "Category inserted successfully with ID: $firestoreId")
            
            // Clear the sync flag for this user to force a refresh after adding a new category
            syncedUsers.remove(category.userId)
            
            return@withContext firestoreId
            
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting category", e)
            return@withContext -1L
        }
    }
    
    // update a category
    suspend fun updateCategory(category: Category) = withContext(Dispatchers.IO) {
        try {
            // Update Firebase first
            val firestoreSuccess = categoryFirebase.updateCategory(category)
            if (!firestoreSuccess) {
                Log.w(TAG, "Failed to update category in Firebase")
            }
            
            // Then update local cache
            categoryDao.updateCategory(category)
            
            // Clear the sync flag for this user to force a refresh after updating
            syncedUsers.remove(category.userId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating category", e)
            throw e
        }
    }
    
    // remove a category
    suspend fun deleteCategory(category: Category) = withContext(Dispatchers.IO) {
        try {
            // Delete from Firebase first
            val firestoreSuccess = categoryFirebase.deleteCategory(category)
            if (!firestoreSuccess) {
                Log.w(TAG, "Failed to delete category from Firebase")
            }
            
            // Then remove from local cache
            categoryDao.deleteCategory(category)
            
            // Clear the sync flag for this user to force a refresh after deleting
            syncedUsers.remove(category.userId)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting category", e)
            throw e
        }
    }
    
    // get system default categories
    fun getDefaultCategories(userId: String): Flow<List<Category>> {
        return categoryDao.getDefaultCategories(userId)
    }
    
    // Force a resync with Firebase for a user
    suspend fun forceSync(userId: String) {
        syncedUsers.remove(userId)
    }
}