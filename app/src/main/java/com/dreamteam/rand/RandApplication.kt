package com.dreamteam.rand

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.Room
import com.dreamteam.rand.data.RandDatabase
import com.google.firebase.FirebaseApp

class RandApplication : Application() {
    lateinit var database: RandDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        
        // Set instance
        instance = this
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Room database
        database = Room.databaseBuilder(
            applicationContext,
            RandDatabase::class.java,
            "rand_database"
        ).build()

        // Initialize theme from shared preferences
        initializeTheme()
    }

    private fun initializeTheme() {
        val sharedPrefs = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val themeMode = sharedPrefs.getString("theme_mode", "system") ?: "system"
        
        when (themeMode) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    companion object {
        lateinit var instance: RandApplication
            private set
    }
}