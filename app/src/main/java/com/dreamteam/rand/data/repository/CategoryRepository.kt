package com.dreamteam.rand.data.repository

import com.dreamteam.rand.data.dao.CategoryDao
import com.dreamteam.rand.data.entity.Category
import com.dreamteam.rand.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(private val categoryDao: CategoryDao) {
    
    fun getCategories(userId: String): Flow<List<Category>> {
        return categoryDao.getCategories(userId)
    }
    
    fun getCategoriesByType(userId: String, type: TransactionType): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(userId, type.toString())
    }
    
    suspend fun getCategory(id: Long): Category? {
        return categoryDao.getCategory(id)
    }
    
    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }
    
    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }
    
    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }
    
    fun getDefaultCategories(userId: String): Flow<List<Category>> {
        return categoryDao.getDefaultCategories(userId)
    }
} 