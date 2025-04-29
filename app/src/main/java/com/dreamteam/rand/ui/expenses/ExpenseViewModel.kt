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

// this viewmodel handles all the expense-related data and operations
// it keeps track of what's selected and manages saving/updating expenses
class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {
    // keep track of what category is selected for the expense
    private val _selectedCategoryId = MutableLiveData<Long>()
    val selectedCategoryId: LiveData<Long> = _selectedCategoryId

    // keep track of what date is selected for the expense
    private val _selectedDate = MutableLiveData<Long>(Date().time)
    val selectedDate: LiveData<Long> = _selectedDate

    // keep track of the receipt photo uri if one was taken
    private val _photoUri = MutableLiveData<String>()
    val photoUri: LiveData<String> = _photoUri

    // let the UI know if saving was successful
    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    // keep track of how much was spent this month
    private val _totalMonthlyExpenses = MutableLiveData<Double>(0.0)
    val totalMonthlyExpenses: LiveData<Double> = _totalMonthlyExpenses

    // keep track of the total expenses as a formatted string
    private val _totalExpenses = MutableLiveData<String>()
    val totalExpenses: LiveData<String> = _totalExpenses

    // directly track the category ID to make sure it doesn't get lost
    private var directCategoryId: Long? = null

    // update which category is selected
    fun setSelectedCategory(categoryId: Long) {
        android.util.Log.d("ExpenseViewModel", "Setting selected category ID: $categoryId")
        _selectedCategoryId.value = categoryId
        directCategoryId = categoryId  // Also store in direct variable
    }

    // update which date is selected
    fun setSelectedDate(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    // update the receipt photo uri
    fun setPhotoUri(uri: String?) {
        _photoUri.value = uri ?: ""
    }

    // get all expenses for a user
    fun getExpenses(userId: String) = repository.getExpenses(userId).asLiveData()

    // get expenses filtered by category
    fun getExpensesByCategory(userId: String, categoryId: Long) = 
        repository.getExpensesByCategory(userId, categoryId).asLiveData()

    // get expenses filtered by category and date range
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

    // get expenses filtered by date range
    fun getExpensesByDateRange(
        userId: String,
        startDate: Long?,
        endDate: Long?
    ): LiveData<List<Transaction>> {
        return repository.getExpensesByDateRange(userId, startDate, endDate).asLiveData()
    }

    // get expenses filtered by month and year
    fun getExpensesByMonthAndYear(
        userId: String,
        month: Int,
        year: Int
    ): LiveData<List<Transaction>> {
        return repository.getExpensesByMonthAndYear(userId, month, year).asLiveData()
    }

    // get the total amount spent in a date range
    fun fetchTotalExpensesByDateRange(userId: String, startDate: Long?, endDate: Long?) {
        viewModelScope.launch {
            val total = repository.getTotalExpensesByDateRange(userId, startDate, endDate)
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            _totalExpenses.value = currencyFormatter.format(total)
        }
    }

    // get the total amount spent in a category for a date range
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

    // save a new expense
    fun saveExpense(userId: String, amount: Double, description: String) {
        viewModelScope.launch {
            // use direct category ID if available, otherwise fall back to LiveData value
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
            
            // update monthly total
            fetchTotalMonthlyExpenses(userId)
            
            // reset the direct category ID after saving
            directCategoryId = null
        }
    }

    // update an existing expense
    fun updateExpense(expense: Transaction) {
        viewModelScope.launch {
            repository.updateExpense(expense)
            _saveSuccess.postValue(true)
            
            // update monthly total
            fetchTotalMonthlyExpenses(expense.userId)
        }
    }

    // delete an expense
    fun deleteExpense(expense: Transaction) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            
            // update monthly total
            fetchTotalMonthlyExpenses(expense.userId)
        }
    }

    // get the total amount spent this month
    fun fetchTotalMonthlyExpenses(userId: String) {
        viewModelScope.launch {
            val total = repository.getTotalExpensesForMonth(userId)
            _totalMonthlyExpenses.postValue(total)
        }
    }

    // reset the save status after showing success/failure
    fun resetSaveStatus() {
        _saveSuccess.value = false
    }

    // factory class to create the viewmodel with its dependencies
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