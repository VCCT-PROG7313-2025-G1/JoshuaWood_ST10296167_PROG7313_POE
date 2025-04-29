package com.dreamteam.rand.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// tracks user achievements and rewards
@Entity(
    tableName = "achievements",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,           // who earned this
    val type: String,             // achievement type
    val unlockedAt: Long,         // when it was earned
    val progress: Int,            // progress towards completion
    val description: String,      // what it's for
    val icon: String,             // achievement icon
    val xpReward: Int             // experience points earned
) 