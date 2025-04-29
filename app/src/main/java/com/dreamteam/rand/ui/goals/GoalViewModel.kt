package com.dreamteam.rand.ui.goals

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
import java.util.Locale
import java.util.Calendar
import java.util.Date

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

    // get all goals for a user
    fun getGoals(userId: String): LiveData<List<Goal>> {
        return repository.getGoals(userId).asLiveData<List<Goal>>()
    }

    // get goals filtered by month and year
    fun getGoalsByMonthAndYear(userId: String, month: Int, year: Int): LiveData<List<Goal>> {
        return repository.getGoalsByMonthAndYear(userId, month, year)
    }

    // get all goals ordered by year and month
    fun getAllGoalsOrdered(userId: String): LiveData<List<Goal>> {
        return repository.getAllGoalsOrdered(userId)
    }

    // get the total amount saved for goals
    fun fetchTotalSaved(userId: String) {
        viewModelScope.launch {
            // get all goals and sum up their current spent amounts
            val goals = repository.getGoals(userId).asLiveData<List<Goal>>().value ?: emptyList()
            val total = goals.sumOf { it.currentSpent }
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            _totalSaved.postValue(currencyFormatter.format(total))
        }
    }

    // save a new goal
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
            
            // update total saved
            fetchTotalSaved(userId)
        }
    }

    // update an existing goal
    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            repository.insertGoal(goal)
            _saveSuccess.value = true
            
            // update total saved
            fetchTotalSaved(goal.userId)
        }
    }

    // delete a goal
    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
            
            // update total saved
            fetchTotalSaved(goal.userId)
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