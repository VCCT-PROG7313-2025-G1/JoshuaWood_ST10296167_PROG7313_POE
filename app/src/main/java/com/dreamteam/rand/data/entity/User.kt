package com.dreamteam.rand.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

// used chatgpt to help design the user entity structure
// helped with field organization and default values
// assisted with indexing strategy for email uniqueness
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey
    val uid: String = UUID.randomUUID().toString(),  // unique user id
    val email: String = "",                         // login email
    val name: String = "",                         // display name
    val password: String = "",                     // hashed password
    val level: Int = 1,                             // user's level
    val xp: Int = 0,                                // experience points
    val theme: String = "system",                   // ui theme preference
    val notificationsEnabled: Boolean = true,       // notification settings
    val currency: String = "USD",                   // preferred currency
    val createdAt: Long = System.currentTimeMillis() // account creation time
) {
    // No-argument constructor required for Firestore
    constructor() : this(
        uid = UUID.randomUUID().toString(),
        email = "",
        name = "",
        password = "",
        level = 1,
        xp = 0,
        theme = "system",
        notificationsEnabled = true,
        currency = "USD",
        createdAt = System.currentTimeMillis()
    )
}