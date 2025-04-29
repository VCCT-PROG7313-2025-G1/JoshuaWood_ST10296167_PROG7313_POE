package com.dreamteam.rand.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// spending categories table - organizes transactions
@Entity(
    tableName = "categories",
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
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,           // who owns this category
    val name: String,             // category name
    val type: TransactionType,    // income or expense
    val budget: Double?,          // budget limit (optional)
    val color: String,            // ui color
    val icon: String,             // category icon
    val isDefault: Boolean,       // is it a system category
    val createdAt: Long           // when category was created
) 