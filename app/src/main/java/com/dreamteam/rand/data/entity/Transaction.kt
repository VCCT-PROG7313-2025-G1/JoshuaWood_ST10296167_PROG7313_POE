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
    val userId: String,           // who made the transaction
    val amount: Double,           // how much money
    val type: TransactionType,    // in or out
    val categoryId: Long?,        // what kind of transaction
    val description: String,      // what was it for
    val date: Long,              // when did it happen
    val receiptUri: String?,     // proof of purchase
    val createdAt: Long          // when was it recorded
) 