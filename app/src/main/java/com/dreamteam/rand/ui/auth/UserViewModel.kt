package com.dreamteam.rand.ui.auth

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dreamteam.rand.data.RandDatabase
import com.dreamteam.rand.data.entity.User
import com.dreamteam.rand.data.repository.UserRepository
import com.dreamteam.rand.data.firebase.UserFirebase
import com.dreamteam.rand.data.repository.FirebaseRepository
import kotlinx.coroutines.launch

// handles all the user stuff like signing in, signing up, and keeping track of who's logged in
class UserViewModel(application: Application) : AndroidViewModel(application) {
    // grab the stuff we need to work with users
    private val userRepository: UserRepository
    private val firebaseRepository: FirebaseRepository
    // keep track of who's logged in
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    // keep track of any errors that happen
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // keep track of sync state
    private val _syncCompleted = MutableLiveData<Boolean>()
    val syncCompleted: LiveData<Boolean> get() = _syncCompleted
    
    // store some stuff on the device so we remember who's logged in
    private val sharedPreferences = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    init {
        val db = RandDatabase.getDatabase(application)
        val userDao = db.userDao()
        val transactionDao = db.transactionDao()
        val categoryDao = db.categoryDao()
        val goalDao = db.goalDao()
        val userFirebase = UserFirebase()
        userRepository = UserRepository(userDao, userFirebase)
        firebaseRepository = FirebaseRepository(userDao, transactionDao, categoryDao, goalDao)
        // check if someone was already logged in
        checkForSavedUser()
    }
    
    // see if we remember who was logged in last time
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
                        // if we can't load their info, just tell them there was an error
                        _error.value = "Failed to load user data: ${exception.message}"
                    }
                } catch (e: Exception) {
                    // only clear everything if something really bad happened
                    _error.value = "Error: ${e.message}"
                }
            }
        }
    }

    // create a new account for someone
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

    // let someone sign in to their account
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            // reset sync flag
            _syncCompleted.value = false

            val result = userRepository.loginUser(email, password)
            result.onSuccess { user ->
               // _currentUser.value = user
                saveUserToPreferences(user)

                try{
                    firebaseRepository.syncAllUserData(user.uid)
                    _currentUser.value = user
                    _syncCompleted.postValue(true)
                } catch (e: Exception){
                    Log.e("UserViewModel", "Sync failed: ${e.message}")
                    // Decide: do you want to proceed without sync or show error?
                    _error.value = "Data sync failed: ${e.message}"
                }
                // sync firebase data
//                firebaseRepository.syncAllUserData(user.uid)
//                _syncCompleted.postValue(true)
            }.onFailure { exception ->
                _error.value = exception.message
            }
        }
    }
    
    // remember who's logged in on the device
    private fun saveUserToPreferences(user: User) {
        sharedPreferences.edit().apply {
            putString("user_id", user.uid)
            putString("user_email", user.email)
            apply()
        }
    }
    
    // forget who was logged in
    private fun clearUserPreferences() {
        sharedPreferences.edit().apply {
            remove("user_id")
            remove("user_email")
            apply()
        }
    }

    // update how far they've gotten in the app
    fun updateUserProgress(increasedXP: Int) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val newXP = user.xp + increasedXP
                val newLvl = calculateLevel(newXP);
                val result = userRepository.updateUserProgress(user.uid, newLvl, newXP)
                result.onSuccess {
                    val updatedUser = user.copy(xp = newXP, level = newLvl)
                    _currentUser.value = updatedUser
                    Log.d("UserViewModel", "User progress updated: Level $newLvl, XP $newXP")
                }
                result.onFailure { exception ->
                    _error.value = exception.message
                }
            }
        }
    }

    private fun calculateLevel(xp: Int): Int{
        if(xp <= 0){
            return 1
        }
        return (xp/100) + 1
    }

    suspend fun getAchievementData(userId: String): List<Number?>{
        return firebaseRepository.getAchievementData(userId)
    }

    // change what the app looks like for them
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

    // turn notifications on or off
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

    // change what kind of money they see
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

    // get the latest info about them
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

    fun clearSyncFlag() {
        _syncCompleted.value = false
    }

    // sign them out
    fun logoutUser() {
        _currentUser.value = null
        clearUserPreferences()
    }
}