package com.dreamteam.rand.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goals",
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
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val name: String,
    val monthYear: Long,
    val minAmount: Double,
    val maxAmount: Double,
    val currentSpent: Double = 0.0,
    val spendingStatus: GoalSpendingStatus = GoalSpendingStatus.ON_TRACK,
    val color: String,
    val createdAt: Long
)

enum class GoalSpendingStatus {
    BELOW_MINIMUM,
    ON_TRACK,
    OVER_BUDGET
}