package com.dreamteam.rand.ui.expenses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dreamteam.rand.data.RandDatabase
import com.dreamteam.rand.data.entity.Transaction
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
    private val _saveSuccess = MutableLiveData<Boolean?>()
    val saveSuccess: LiveData<Boolean?> = _saveSuccess

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
        android.util.Log.d("ExpenseViewModel", "Setting photo URI: $uri, currently selected category: ${_selectedCategoryId.value}")
        _photoUri.value = uri ?: ""
        // Note: We deliberately don't reset the category selection here
        android.util.Log.d("ExpenseViewModel", "Photo URI set, category ID remains: ${_selectedCategoryId.value}")
    }

    // get all expenses for a user
    fun getExpenses(userId: String): LiveData<List<Transaction>> {
        return repository.getExpenses(userId).asLiveData()
    }

    // get expenses filtered by category
    fun getExpensesByCategory(userId: String, categoryId: Long): LiveData<List<Transaction>> {
        return repository.getExpensesByCategory(userId, categoryId).asLiveData()
    }

    // ai declaration: here we used claude to design the category filtering system
    // with date range support for detailed expense analysis
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

    // ai declaration: here we used gpt to design the expense saving logic 
    // with proper error handling and async repository operations
    fun saveExpense(userId: String, amount: Double, description: String) {
        viewModelScope.launch {
            // use direct category ID if available, otherwise fall back to LiveData value
            val categoryIdToUse = directCategoryId ?: _selectedCategoryId.value
            
            android.util.Log.d("ExpenseViewModel", "Creating expense with direct category ID: $directCategoryId, LiveData value: ${_selectedCategoryId.value}")
            android.util.Log.d("ExpenseViewModel", "Final category ID being used: $categoryIdToUse")
            
            val receiptUri = _photoUri.value
            val date = _selectedDate.value ?: Date().time
            
            // Enhanced logging before saving expense
            android.util.Log.d("ExpenseViewModel", "==================== EXPENSE DETAILS ====================")
            android.util.Log.d("ExpenseViewModel", "Saving new expense for user: $userId")
            android.util.Log.d("ExpenseViewModel", "Description: $description")
            android.util.Log.d("ExpenseViewModel", "Amount: $amount")
            android.util.Log.d("ExpenseViewModel", "Category ID: $categoryIdToUse")
            android.util.Log.d("ExpenseViewModel", "Date: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(Date(date))}")
            android.util.Log.d("ExpenseViewModel", "Receipt Image: ${receiptUri ?: "None"}")
            android.util.Log.d("ExpenseViewModel", "=======================================================")
            
            val result = repository.insertExpense(
                userId = userId,
                amount = amount,
                description = description,
                categoryId = categoryIdToUse,
                receiptUri = receiptUri,
                date = date
            )
            _saveSuccess.postValue(result > 0)
            
            // Log the result
            if (result > 0) {
                android.util.Log.d("ExpenseViewModel", "✅ Expense saved successfully with ID: $result")
            } else {
                android.util.Log.e("ExpenseViewModel", "❌ Failed to save expense")
            }

            // update monthly total
            fetchTotalMonthlyExpenses(userId)
            
            // reset the direct category ID after saving
            directCategoryId = null
        }
    }

    // ai declaration: here we used chatgpt to implement date filtering for transactions
    // using coroutines and flow transformation
    fun fetchTotalMonthlyExpenses(userId: String) {
        viewModelScope.launch {
            val total = repository.getTotalExpensesForMonth(userId)
            _totalMonthlyExpenses.postValue(total)
        }
    }

    // reset the save status after showing success/failure
    fun resetSaveStatus() {
        _saveSuccess.value = null
    }

    // factory class to create the viewmodel with its dependencies
    class Factory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
        constructor(database: RandDatabase) : this(
            ExpenseRepository(
                transactionDao = database.transactionDao(),
                goalDao = database.goalDao()
            )
        )

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
                return ExpenseViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}