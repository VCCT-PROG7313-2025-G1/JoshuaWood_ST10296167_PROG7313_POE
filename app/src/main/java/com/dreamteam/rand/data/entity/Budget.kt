package com.dreamteam.rand.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// monthly budget table - tracks total budget for each month
// ai note: could predict future budgets based on spending trends
@Entity(
    tableName = "budgets",
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
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,           // who owns this budget
    val month: Long,              // target month
    val totalAmount: Double,      // total budget amount
    val createdAt: Long,          // when budget was created
    val updatedAt: Long           // last update time
)

// tracks budget amounts for each category
// ai note: could suggest category splits based on past spending
@Entity(
    tableName = "budget_categories",
    foreignKeys = [
        ForeignKey(
            entity = Budget::class,
            parentColumns = ["id"],
            childColumns = ["budgetId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("budgetId"), Index("categoryId")]
)
data class BudgetCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val budgetId: Long,           // parent budget
    val categoryId: Long,         // spending category
    val amount: Double,           // budgeted amount
    val spent: Double             // amount spent so far
) 