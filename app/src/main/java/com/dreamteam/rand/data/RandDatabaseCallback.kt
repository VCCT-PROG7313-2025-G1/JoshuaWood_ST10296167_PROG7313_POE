package com.dreamteam.rand.data

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// handles database setup and initialization
class RandDatabaseCallback : RoomDatabase.Callback() {
    // called when database is first created
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Initialize default data
    }

    // called when database is opened
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        // Perform any necessary migrations or updates
    }
}
