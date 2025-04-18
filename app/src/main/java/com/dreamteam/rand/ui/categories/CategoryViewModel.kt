package com.dreamteam.rand.ui.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dreamteam.rand.data.entity.Category
import com.dreamteam.rand.data.entity.TransactionType
import com.dreamteam.rand.data.repository.CategoryRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {
    private val _selectedType = MutableLiveData<TransactionType>(TransactionType.EXPENSE)
    val selectedType: LiveData<TransactionType> = _selectedType

    private val _selectedColor = MutableLiveData<String>("#FF5252") // Default red color
    val selectedColor: LiveData<String> = _selectedColor

    private val _selectedIcon = MutableLiveData<String>("ic_shopping") // Default shopping icon
    val selectedIcon: LiveData<String> = _selectedIcon

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    fun setSelectedType(type: TransactionType) {
        _selectedType.value = type
    }

    fun setSelectedColor(color: String) {
        _selectedColor.value = color
    }

    fun setSelectedIcon(icon: String) {
        _selectedIcon.value = icon
    }

    fun getCategories(userId: String) = repository.getCategories(userId).asLiveData()

    fun getCategoriesByType(userId: String, type: TransactionType) = 
        repository.getCategoriesByType(userId, type).asLiveData()

    fun saveCategory(userId: String, name: String, isDefault: Boolean = false) {
        viewModelScope.launch {
            val category = Category(
                userId = userId,
                name = name,
                type = _selectedType.value ?: TransactionType.EXPENSE,
                budget = null,
                color = _selectedColor.value ?: "#FF5252",
                icon = _selectedIcon.value ?: "ic_shopping",
                isDefault = isDefault,
                createdAt = Date().time
            )
            
            val result = repository.insertCategory(category)
            _saveSuccess.postValue(result > 0)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
            _saveSuccess.postValue(true)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun resetSaveStatus() {
        _saveSuccess.value = null
    }

    class Factory(private val repository: CategoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
                return CategoryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 