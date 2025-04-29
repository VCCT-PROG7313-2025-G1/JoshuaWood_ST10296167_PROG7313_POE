package com.dreamteam.rand.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// tracks monthly spending goals for users
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
    val userId: String,           // who owns this goal
    val name: String,             // goal name
    val month: Int,               // target month (1-12)
    val year: Int,                // target year
    val minAmount: Double,        // minimum spending target
    val maxAmount: Double,        // maximum spending limit
    val currentSpent: Double = 0.0, // how much spent so far
    val spendingStatus: GoalSpendingStatus = if (currentSpent < minAmount) GoalSpendingStatus.BELOW_MINIMUM 
        else if (currentSpent > maxAmount) GoalSpendingStatus.OVER_BUDGET 
        else GoalSpendingStatus.ON_TRACK,
    val color: String,            // ui color for the goal
    val createdAt: Long           // when goal was created
)

// tracks if spending is under, over, or within target range
enum class GoalSpendingStatus {
    BELOW_MINIMUM,  // spending too low
    ON_TRACK,       // spending just right
    OVER_BUDGET     // spending too high
}