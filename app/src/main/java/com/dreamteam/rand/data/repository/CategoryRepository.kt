package com.dreamteam.rand.data.repository

import com.dreamteam.rand.data.dao.CategoryDao
import com.dreamteam.rand.data.entity.Category
import com.dreamteam.rand.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// handles all category-related operations
class CategoryRepository(private val categoryDao: CategoryDao) {
    // get all categories for a user
    fun getCategories(userId: String): Flow<List<Category>> {
        return categoryDao.getCategories(userId)
    }
    
    // get categories of a specific type (income/expense)
    fun getCategoriesByType(userId: String, type: TransactionType): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(userId, type.toString())
    }
    
    // get a specific category
    suspend fun getCategory(id: Long): Category? {
        return categoryDao.getCategory(id)
    }
    
    // add a new category
    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }
    
    // update a category
    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }
    
    // remove a category
    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }
    
    // get system default categories
    fun getDefaultCategories(userId: String): Flow<List<Category>> {
        return categoryDao.getDefaultCategories(userId)
    }
} 