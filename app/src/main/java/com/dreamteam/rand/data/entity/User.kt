package com.dreamteam.rand.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

// user profile table - stores all user preferences and progress
// ai note: could be enhanced with personalized financial insights
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey
    val uid: String = UUID.randomUUID().toString(),  // unique user id
    val email: String,                               // login email
    val name: String,                                // display name
    val password: String,                            // hashed password
    val level: Int = 1,                             // user's level
    val xp: Int = 0,                                // experience points
    val theme: String = "system",                   // ui theme preference
    val notificationsEnabled: Boolean = true,       // notification settings
    val currency: String = "USD",                   // preferred currency
    val createdAt: Long = System.currentTimeMillis() // account creation time
) 