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

    init {
        // Pass viewModelScope to repository
        repository.coroutineScope = viewModelScope
    }

    // update the selected category type
    fun setSelectedType(type: TransactionType) {
        _selectedType.value = type
    }

    // update the selected color
    fun setSelectedColor(color: String) {
        android.util.Log.d("CategoryViewModel", "Setting color to: $color")
        // Ensure color is in proper format (#RRGGBB or #AARRGGBB)
        val formattedColor = if (color.startsWith("#")) color else "#$color"
        _selectedColor.value = formattedColor
        android.util.Log.d("CategoryViewModel", "Color set to: ${_selectedColor.value}")
    }

    // update the selected icon
    fun setSelectedIcon(icon: String) {
        _selectedIcon.value = icon
    }

    // get all categories for a user
    fun getCategories(userId: String) = repository.getCategories(userId).asLiveData()

    // ai declaration: here we used claude to design the reactive data flow
    // for category type filtering and synchronization
    fun getCategoriesByType(userId: String, type: TransactionType): LiveData<List<Category>> {
        return repository.getCategoriesByType(userId, type).asLiveData()
    }

    // ai declaration: here we used gpt to implement the category saving system
    // with coroutines and repository integration and Firebase support
    fun saveCategory(userId: String, name: String, isDefault: Boolean = false) {
        viewModelScope.launch {
            val type = _selectedType.value ?: TransactionType.EXPENSE
            val color = _selectedColor.value ?: "#FF5252"
            val icon = _selectedIcon.value ?: "ic_shopping"
            
            // Enhanced logging before saving category
            android.util.Log.d("CategoryViewModel", "==================== CATEGORY DETAILS ====================")
            android.util.Log.d("CategoryViewModel", "Saving new category for user: $userId")
            android.util.Log.d("CategoryViewModel", "Name: $name")
            android.util.Log.d("CategoryViewModel", "Type: $type")
            android.util.Log.d("CategoryViewModel", "Color: $color")
            android.util.Log.d("CategoryViewModel", "Icon: $icon")
            android.util.Log.d("CategoryViewModel", "Is Default: $isDefault")
            android.util.Log.d("CategoryViewModel", "=======================================================")
            
            // Validate color format
            val finalColor = if (color.startsWith("#")) color else "#$color"
            
            val category = Category(
                userId = userId,
                name = name,
                type = type,
                budget = null,
                color = finalColor,
                icon = icon,
                isDefault = isDefault,
                createdAt = Date().time
            )
            
            val result = repository.insertCategory(category)
            
            // Log the result
            if (result > 0) {
                android.util.Log.d("CategoryViewModel", "✅ Category saved successfully with ID: $result")
            } else {
                android.util.Log.e("CategoryViewModel", "❌ Failed to save category")
            }
            
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