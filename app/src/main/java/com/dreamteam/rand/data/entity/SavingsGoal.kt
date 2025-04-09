package com.dreamteam.rand.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class GoalStatus {
    ACTIVE, COMPLETED, CANCELLED
}

@Entity(
    tableName = "savings_goals",
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
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: Long?,
    val status: GoalStatus,
    val createdAt: Long
) 