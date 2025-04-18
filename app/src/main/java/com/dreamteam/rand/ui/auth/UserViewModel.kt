package com.dreamteam.rand.ui.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dreamteam.rand.data.RandDatabase
import com.dreamteam.rand.data.entity.User
import com.dreamteam.rand.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository: UserRepository
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    private val sharedPreferences = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    init {
        val userDao = RandDatabase.getDatabase(application).userDao()
        userRepository = UserRepository(userDao)
        
        // Check if user is already logged in
        checkForSavedUser()
    }
    
    private fun checkForSavedUser() {
        val savedUserId = sharedPreferences.getString("user_id", null)
        val savedEmail = sharedPreferences.getString("user_email", null)
        
        if (savedUserId != null && savedEmail != null) {
            viewModelScope.launch {
                try {
                    val result = userRepository.getUserByUid(savedUserId)
                    result.onSuccess { user ->
                        _currentUser.value = user
                    }.onFailure { exception ->
                        // If there's an issue loading the user, log the error but don't clear preferences
                        _error.value = "Failed to load user data: ${exception.message}"
                    }
                } catch (e: Exception) {
                    // Only clear preferences if there's a serious issue like database corruption
                    _error.value = "Error: ${e.message}"
                }
            }
        }
    }

    fun registerUser(name: String, email: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.registerUser(name, email, password)
            result.onSuccess { user ->
                _currentUser.value = user
                saveUserToPreferences(user)
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.loginUser(email, password)
            result.onSuccess { user ->
                _currentUser.value = user
                saveUserToPreferences(user)
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }
    
    private fun saveUserToPreferences(user: User) {
        sharedPreferences.edit().apply {
            putString("user_id", user.uid)
            putString("user_email", user.email)
            apply()
        }
    }
    
    private fun clearUserPreferences() {
        sharedPreferences.edit().apply {
            remove("user_id")
            remove("user_email")
            apply()
        }
    }

    fun updateUserProgress(level: Int, xp: Int) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val result = userRepository.updateUserProgress(user.uid, level, xp)
                result.onFailure { exception ->
                    _error.value = exception.message
                }
            }
        }
    }

    fun updateUserTheme(theme: String) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val result = userRepository.updateUserTheme(user.uid, theme)
                result.onFailure { exception ->
                    _error.value = exception.message
                }
            }
        }
    }

    fun updateNotificationSettings(enabled: Boolean) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val result = userRepository.updateNotificationSettings(user.uid, enabled)
                result.onFailure { exception ->
                    _error.value = exception.message
                }
            }
        }
    }

    fun updateUserCurrency(currency: String) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val result = userRepository.updateUserCurrency(user.uid, currency)
                result.onFailure { exception ->
                    _error.value = exception.message
                }
            }
        }
    }

    fun refreshUser() {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val result = userRepository.getUserByUid(user.uid)
                result.onSuccess { updatedUser ->
                    _currentUser.value = updatedUser
                }.onFailure { exception ->
                    _error.value = exception.message
                }
            }
        }
    }

    fun logoutUser() {
        _currentUser.value = null
        clearUserPreferences()
    }
} 