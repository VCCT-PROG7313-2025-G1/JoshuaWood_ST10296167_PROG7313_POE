package com.dreamteam.rand.ui.goals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dreamteam.rand.data.entity.Goal
import com.dreamteam.rand.data.entity.GoalSpendingStatus
import com.dreamteam.rand.data.repository.GoalRepository
import kotlinx.coroutines.launch
import java.util.Date

class GoalViewModel(private val repository: GoalRepository) : ViewModel() {
    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    fun getGoals(userId: String) = repository.getGoals(userId).asLiveData()

    fun getGoalsByMonth(userId: String, monthYear: Long) = 
        repository.getGoalsByMonth(userId, monthYear).asLiveData()

    suspend fun getGoal(id: Long) = repository.getGoal(id)

    fun saveGoal(
        userId: String, 
        name: String, 
        monthYear: Long,
        minAmount: Double,
        maxAmount: Double,
        color: String
    ) {
        viewModelScope.launch {
            val goal = Goal(
                userId = userId,
                name = name,
                monthYear = monthYear,
                minAmount = minAmount,
                maxAmount = maxAmount,
                currentSpent = 0.0,
                spendingStatus = GoalSpendingStatus.ON_TRACK,
                color = color,
                createdAt = Date().time
            )
            
            val result = repository.insertGoal(goal)
            _saveSuccess.postValue(result > 0)
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun updateSpentAmount(goalId: Long, newSpentAmount: Double) {
        viewModelScope.launch {
            repository.updateSpentAmount(goalId, newSpentAmount)
        }
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