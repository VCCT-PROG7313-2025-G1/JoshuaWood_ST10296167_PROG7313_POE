package com.dreamteam.rand.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// used chatgpt to help design the transaction entity structure
// helped with foreign key relationships and indexing strategy
// assisted with enum design for transaction types

// transaction types - either money in or money out
enum class TransactionType {
    EXPENSE, INCOME
}

// main transaction table - tracks all money movements
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["uid"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("userId"),
        Index("categoryId"),
        Index("date")
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "", // Empty string for no-arg constructor
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long? = null,
    val description: String = "",
    val date: Long = 0,
    val receiptUri: String? = null,
    val createdAt: Long = 0
) {
    // No-argument constructor for Firebase
    constructor() : this(
        id = 0,
        userId = "",
        amount = 0.0,
        type = TransactionType.EXPENSE,
        categoryId = null,
        description = "",
        date = 0,
        receiptUri = null,
        createdAt = 0
    )
}