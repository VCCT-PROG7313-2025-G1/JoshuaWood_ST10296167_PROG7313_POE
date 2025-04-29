package com.dreamteam.rand.data.dao

import androidx.room.*
import com.dreamteam.rand.data.entity.Category
import kotlinx.coroutines.flow.Flow

// handles all category database operations
@Dao
interface CategoryDao {
    // get all categories for a user
    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getCategories(userId: String): Flow<List<Category>>

    // get categories of a specific type (income/expense)
    @Query("SELECT * FROM categories WHERE userId = :userId AND type = :type")
    fun getCategoriesByType(userId: String, type: String): Flow<List<Category>>

    // get a specific category
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategory(id: Long): Category?

    // add a new category
    @Insert
    suspend fun insertCategory(category: Category): Long

    // update a category
    @Update
    suspend fun updateCategory(category: Category)

    // remove a category
    @Delete
    suspend fun deleteCategory(category: Category)

    // get system default categories
    @Query("""
        SELECT * FROM categories 
        WHERE userId = :userId 
        AND isDefault = 1
    """)
    fun getDefaultCategories(userId: String): Flow<List<Category>>
} 