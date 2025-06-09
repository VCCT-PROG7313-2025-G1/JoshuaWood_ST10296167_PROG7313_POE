package com.dreamteam.rand.ui.goals

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dreamteam.rand.data.entity.Goal
import com.dreamteam.rand.data.repository.GoalRepository
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

// this viewmodel handles all the goal-related data and operations
// it keeps track of goals, their progress, and manages saving/updating them
class GoalViewModel(private val repository: GoalRepository) : ViewModel() {
    // keep track of what month and year is selected for the goal
    private val _selectedMonth = MutableLiveData<Int>(Calendar.getInstance().get(Calendar.MONTH))
    val selectedMonth: LiveData<Int> = _selectedMonth

    private val _selectedYear = MutableLiveData<Int>(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: LiveData<Int> = _selectedYear

    // keep track of what color is selected for the goal
    private val _selectedColor = MutableLiveData<String>()
    val selectedColor: LiveData<String> = _selectedColor

    // let the UI know if saving was successful
    private val _saveSuccess = MutableLiveData<Boolean?>()
    val saveSuccess: LiveData<Boolean?> = _saveSuccess

    // keep track of the total amount saved for goals
    private val _totalSaved = MutableLiveData<String>()
    val totalSaved: LiveData<String> = _totalSaved

    init {
        // Pass viewModelScope to repository
        repository.coroutineScope = viewModelScope
    }

    // update which month is selected
    fun setSelectedMonth(month: Int) {
        _selectedMonth.value = month
    }

    // update which year is selected
    fun setSelectedYear(year: Int) {
        _selectedYear.value = year
    }

    // update which color is selected
    fun setSelectedColor(color: String) {
        _selectedColor.value = color
    }

    // get goals for a user - uses local cache first
    fun getGoals(userId: String) = repository.getGoals(userId).asLiveData()

    // get goals by month and year - uses local cache first
    fun getGoalsByMonthAndYear(userId: String, month: Int, year: Int) = 
        repository.getGoalsByMonthAndYear(userId, month, year)

    // get all goals ordered by date - uses local cache first
    fun getAllGoalsOrdered(userId: String): LiveData<List<Goal>> {
        return repository.getAllGoalsOrdered(userId)
    }

    // save a new goal to Firebase and cache
    fun saveGoal(
        userId: String,
        name: String,
        month: Int,
        year: Int,
        minAmount: Double,
        maxAmount: Double,
        color: String
    ) {
        viewModelScope.launch {
            try {
                Log.d("GoalViewModel", "Saving new goal: $name")
                val goal = Goal(
                    userId = userId,
                    name = name,
                    month = month,
                    year = year,
                    minAmount = minAmount,
                    maxAmount = maxAmount,
                    color = color,
                    currentSpent = 0.0,
                    createdAt = System.currentTimeMillis()
                )
                val result = repository.insertGoal(goal)
                _saveSuccess.postValue(result > 0)
                
                // update total saved if successful
                if (result > 0) {
                    fetchTotalSaved(userId)
                }
            } catch (e: Exception) {
                Log.e("GoalViewModel", "Error saving goal", e)
                _saveSuccess.postValue(false)
            }
        }
    }

    // update an existing goal's spending amount in Firebase and cache
    fun updateGoalSpending(goalId: Long, newAmount: Double) {
        viewModelScope.launch {
            try {
                repository.updateSpentAmount(goalId, newAmount)
            } catch (e: Exception) {
                Log.e("GoalViewModel", "Error updating goal spending", e)
            }
        }
    }

    // delete a goal from Firebase and cache
    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            try {
                repository.deleteGoal(goal)
                fetchTotalSaved(goal.userId)
            } catch (e: Exception) {
                Log.e("GoalViewModel", "Error deleting goal", e)
            }
        }
    }

    // get the total amount saved for goals from cache
    fun fetchTotalSaved(userId: String) {
        viewModelScope.launch {
            // get all goals and sum up their current spent amounts
            val goals = repository.getGoals(userId).asLiveData().value ?: emptyList()
            val total = goals.sumOf { it.currentSpent }
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            _totalSaved.postValue(currencyFormatter.format(total))
        }
    }

    // reset the save status after showing success/failure
    fun resetSaveStatus() {
        _saveSuccess.value = null
    }

    // factory class to create the viewmodel with its dependencies
    class Factory(private val repository: GoalRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GoalViewModel::class.java)) {
                return GoalViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}