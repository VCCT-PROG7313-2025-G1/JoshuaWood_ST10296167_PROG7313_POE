package com.dreamteam.rand.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val userId: String,
    val month: Long,
    val totalAmount: Double,
    val createdAt: Long,
    val updatedAt: Long
)

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
    val budgetId: Long,
    val categoryId: Long,
    val amount: Double,
    val spent: Double
) 