package com.dreamteam.rand

import android.app.Application
import androidx.room.Room
import com.dreamteam.rand.data.RandDatabase
import com.google.firebase.FirebaseApp

class RandApplication : Application() {
    lateinit var database: RandDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Room database
        database = Room.databaseBuilder(
            applicationContext,
            RandDatabase::class.java,
            "rand_database"
        ).build()
    }
}