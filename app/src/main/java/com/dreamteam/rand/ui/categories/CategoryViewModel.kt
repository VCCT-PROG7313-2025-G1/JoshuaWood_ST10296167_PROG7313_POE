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

// this viewmodel handles all the category-related data and operations
// it keeps track of what's selected and manages saving/updating categories
class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {
    // keep track of what type of category is selected (expense or income)
    private val _selectedType = MutableLiveData<TransactionType>(TransactionType.EXPENSE)
    val selectedType: LiveData<TransactionType> = _selectedType

    // keep track of what color is selected for the category
    private val _selectedColor = MutableLiveData<String>("#FF5252") // Default red color
    val selectedColor: LiveData<String> = _selectedColor

    // keep track of what icon is selected for the category
    private val _selectedIcon = MutableLiveData<String>("ic_shopping") // Default shopping icon
    val selectedIcon: LiveData<String> = _selectedIcon

    // let the UI know if saving was successful
    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    // update the selected category type
    fun setSelectedType(type: TransactionType) {
        _selectedType.value = type
    }

    // update the selected color
    fun setSelectedColor(color: String) {
        _selectedColor.value = color
    }

    // update the selected icon
    fun setSelectedIcon(icon: String) {
        _selectedIcon.value = icon
    }

    // get all categories for a user
    fun getCategories(userId: String) = repository.getCategories(userId).asLiveData()

    // get categories filtered by type (expense or income)
    fun getCategoriesByType(userId: String, type: TransactionType) = 
        repository.getCategoriesByType(userId, type).asLiveData()

    // save a new category with the current selections
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

    // update an existing category
    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
            _saveSuccess.postValue(true)
        }
    }

    // delete a category
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // reset the save status after showing success/failure
    fun resetSaveStatus() {
        _saveSuccess.value = false
    }

    // factory class to create the viewmodel with its dependencies
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