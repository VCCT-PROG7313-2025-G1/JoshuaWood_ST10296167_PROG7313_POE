package com.dreamteam.rand.ui.goals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dreamteam.rand.data.entity.Goal
import com.dreamteam.rand.data.entity.GoalSpendingStatus
import com.dreamteam.rand.data.repository.GoalRepository
import kotlinx.coroutines.launch

class GoalViewModel(
    private val repository: GoalRepository
) : ViewModel() {

    private val _saveSuccess = MutableLiveData<Boolean?>()
    val saveSuccess: LiveData<Boolean?> = _saveSuccess

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
                val currentSpent = 0.0
                val goal = Goal(
                    userId = userId,
                    name = name,
                    month = month,
                    year = year,
                    minAmount = minAmount,
                    maxAmount = maxAmount,
                    currentSpent = currentSpent,
                    spendingStatus = GoalSpendingStatus.BELOW_MINIMUM, // Initialize as BELOW_MINIMUM since currentSpent is 0
                    color = color,
                    createdAt = System.currentTimeMillis()
                )
                val goalId = repository.insertGoal(goal)
                _saveSuccess.value = goalId > 0
            } catch (e: Exception) {
                _saveSuccess.value = false
            }
        }
    }

    fun getGoalsByMonthAndYear(userId: String, month: Int, year: Int): LiveData<List<Goal>> {
        return repository.getGoalsByMonthAndYear(userId, month, year)
    }

    fun getAllGoalsOrdered(userId: String): LiveData<List<Goal>> {
        return repository.getAllGoalsOrdered(userId)
    }

    fun resetSaveStatus() {
        _saveSuccess.value = null
    }

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