package com.dreamteam.rand.data.dao

import androidx.room.*
import com.dreamteam.rand.data.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getCategories(userId: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE userId = :userId AND type = :type")
    fun getCategoriesByType(userId: String, type: String): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategory(id: Long): Category?

    @Insert
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("""
        SELECT * FROM categories 
        WHERE userId = :userId 
        AND isDefault = 1
    """)
    fun getDefaultCategories(userId: String): Flow<List<Category>>
} 