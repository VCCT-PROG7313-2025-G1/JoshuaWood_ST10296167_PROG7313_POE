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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first

// handles all category-related operations
class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val categoryFirebase: CategoryFirebase = CategoryFirebase()
) {
    private val TAG = "CategoryRepository"
    
    // Allow setting the coroutineScope from ViewModel
    lateinit var coroutineScope: CoroutineScope

    // Get all categories for a user - use local cache first
    fun getCategories(userId: String): Flow<List<Category>> {
        Log.d(TAG, "Getting categories for user: $userId from local cache")
        return categoryDao.getCategories(userId)
    }
    
    // Get categories by type - use local cache first
    fun getCategoriesByType(userId: String, type: TransactionType): Flow<List<Category>> {
        Log.d(TAG, "Getting categories for user: $userId, type: $type from local cache")
        return categoryDao.getCategoriesByType(userId, type.toString())
    }
    
    // Get a specific category - use local cache first
    suspend fun getCategory(id: Long): Category? {
        return categoryDao.getCategory(id)
    }
    
    // Insert a new category into both Firebase and local cache
    suspend fun insertCategory(category: Category): Long = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Inserting new category: ${category.name}")
            
            // Insert into Firebase first to get the ID
            val firestoreId = categoryFirebase.insertCategory(category)
            if (firestoreId <= 0) {
                Log.e(TAG, "Failed to insert category in Firebase")
                return@withContext -1L
            }
            
            // Create category with Firebase ID
            val categoryWithId = category.copy(id = firestoreId)
            
            // Cache in Room
            try {
                categoryDao.insertCategory(categoryWithId)
                Log.d(TAG, "Category cached successfully with ID: $firestoreId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cache category in Room", e)
            }
            
            return@withContext firestoreId
            
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting category", e)
            return@withContext -1L
        }
    }
    
    // Update both Firebase and local cache
    suspend fun updateCategory(category: Category) = withContext(Dispatchers.IO) {
        try {
            // Update Firebase first
            val firestoreSuccess = categoryFirebase.updateCategory(category)
            if (!firestoreSuccess) {
                Log.w(TAG, "Failed to update category in Firebase")
            }
            
            // Update local cache
            categoryDao.updateCategory(category)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating category", e)
            throw e
        }
    }
    
    // Delete from both Firebase and local cache
    suspend fun deleteCategory(category: Category) = withContext(Dispatchers.IO) {
        try {
            // Delete from Firebase first
            val firestoreSuccess = categoryFirebase.deleteCategory(category)
            if (!firestoreSuccess) {
                Log.w(TAG, "Failed to delete category from Firebase")
            }
            
            // Delete from local cache
            categoryDao.deleteCategory(category)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting category", e)
            throw e
        }
    }
    
    // Get system default categories from local cache
    fun getDefaultCategories(userId: String): Flow<List<Category>> {
        return categoryDao.getDefaultCategories(userId)
    }
}