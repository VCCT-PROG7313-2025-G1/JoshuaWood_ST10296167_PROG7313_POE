package com.dreamteam.rand.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey
    val uid: String = UUID.randomUUID().toString(),
    val email: String,
    val name: String,
    val password: String,
    val level: Int = 1,
    val xp: Int = 0,
    val theme: String = "system",
    val notificationsEnabled: Boolean = true,
    val currency: String = "USD",
    val createdAt: Long = System.currentTimeMillis()
) 