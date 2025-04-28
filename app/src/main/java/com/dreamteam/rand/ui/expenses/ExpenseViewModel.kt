package com.dreamteam.rand.ui.expenses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dreamteam.rand.data.entity.Transaction
import com.dreamteam.rand.data.entity.TransactionType
import com.dreamteam.rand.data.repository.ExpenseRepository
import kotlinx.coroutines.launch
import java.util.Date
import java.text.NumberFormat
import java.util.Locale

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {
    private val _selectedCategoryId = MutableLiveData<Long>()
    val selectedCategoryId: LiveData<Long> = _selectedCategoryId

    private val _selectedDate = MutableLiveData<Long>(Date().time)
    val selectedDate: LiveData<Long> = _selectedDate

    private val _photoUri = MutableLiveData<String>()
    val photoUri: LiveData<String> = _photoUri

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _totalMonthlyExpenses = MutableLiveData<Double>(0.0)
    val totalMonthlyExpenses: LiveData<Double> = _totalMonthlyExpenses

    private val _totalExpenses = MutableLiveData<String>()
    val totalExpenses: LiveData<String> = _totalExpenses

    // Directly track the category ID to ensure it doesn't get lost
    private var directCategoryId: Long? = null

    fun setSelectedCategory(categoryId: Long) {
        android.util.Log.d("ExpenseViewModel", "Setting selected category ID: $categoryId")
        _selectedCategoryId.value = categoryId
        directCategoryId = categoryId  // Also store in direct variable
    }

    fun setSelectedDate(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    fun setPhotoUri(uri: String?) {
        _photoUri.value = uri ?: ""
    }

    fun getExpenses(userId: String) = repository.getExpenses(userId).asLiveData()

    fun getExpensesByCategory(userId: String, categoryId: Long) = 
        repository.getExpensesByCategory(userId, categoryId).asLiveData()

    fun getExpensesByCategoryAndDateRange(
        userId: String,
        categoryId: Long,
        startDate: Long?,
        endDate: Long?
    ): LiveData<List<Transaction>> {
        return repository.getExpensesByCategoryAndDateRange(
            userId = userId,
            categoryId = categoryId,
            startDate = startDate,
            endDate = endDate
        ).asLiveData()
    }

    fun getExpensesByDateRange(
        userId: String,
        startDate: Long?,
        endDate: Long?
    ): LiveData<List<Transaction>> {
        return repository.getExpensesByDateRange(userId, startDate, endDate).asLiveData()
    }

    fun getExpensesByMonthAndYear(
        userId: String,
        month: Int,
        year: Int
    ): LiveData<List<Transaction>> {
        return repository.getExpensesByMonthAndYear(userId, month, year).asLiveData()
    }

    fun fetchTotalExpensesByDateRange(userId: String, startDate: Long?, endDate: Long?) {
        viewModelScope.launch {
            val total = repository.getTotalExpensesByDateRange(userId, startDate, endDate)
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            _totalExpenses.value = currencyFormatter.format(total)
        }
    }

    fun fetchTotalExpensesByCategoryAndDateRange(
        userId: String,
        categoryId: Long,
        startDate: Long?,
        endDate: Long?
    ) {
        viewModelScope.launch {
            val total = repository.getTotalExpensesByCategoryAndDateRange(
                userId = userId,
                categoryId = categoryId,
                startDate = startDate,
                endDate = endDate
            )
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            _totalExpenses.value = currencyFormatter.format(total)
        }
    }

    fun saveExpense(userId: String, amount: Double, description: String) {
        viewModelScope.launch {
            // Use direct category ID if available, otherwise fall back to LiveData value
            val categoryIdToUse = directCategoryId ?: _selectedCategoryId.value
            
            android.util.Log.d("ExpenseViewModel", "Creating expense with direct category ID: $directCategoryId, LiveData value: ${_selectedCategoryId.value}")
            android.util.Log.d("ExpenseViewModel", "Final category ID being used: $categoryIdToUse")
            
            val receiptUri = _photoUri.value
            val date = _selectedDate.value ?: Date().time
            
            val result = repository.insertExpense(
                userId = userId,
                amount = amount,
                description = description,
                categoryId = categoryIdToUse,
                receiptUri = receiptUri
            )
            _saveSuccess.postValue(result > 0)
            
            // Update monthly total
            fetchTotalMonthlyExpenses(userId)
            
            // Reset the direct category ID after saving
            directCategoryId = null
        }
    }

    fun updateExpense(expense: Transaction) {
        viewModelScope.launch {
            repository.updateExpense(expense)
            _saveSuccess.postValue(true)
            
            // Update monthly total
            fetchTotalMonthlyExpenses(expense.userId)
        }
    }

    fun deleteExpense(expense: Transaction) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            
            // Update monthly total
            fetchTotalMonthlyExpenses(expense.userId)
        }
    }

    fun fetchTotalMonthlyExpenses(userId: String) {
        viewModelScope.launch {
            val total = repository.getTotalExpensesForMonth(userId)
            _totalMonthlyExpenses.postValue(total)
        }
    }

    fun resetSaveStatus() {
        _saveSuccess.value = false
    }

    class Factory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
                return ExpenseViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}