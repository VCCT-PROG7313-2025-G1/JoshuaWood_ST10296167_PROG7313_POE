package com.dreamteam.rand

import android.app.Application
import androidx.room.Room
import com.dreamteam.rand.data.RandDatabase

class RandApplication : Application() {
    lateinit var database: RandDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            RandDatabase::class.java,
            "rand_database"
        ).build()
    }
} 