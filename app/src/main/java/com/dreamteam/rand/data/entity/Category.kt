package com.dreamteam.rand.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName

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
    val userId: String = "",           // who owns this category
    val name: String = "",             // category name
    val type: TransactionType = TransactionType.EXPENSE,    // income or expense
    val budget: Double? = null,          // budget limit (optional)
    val color: String = "#FF5252",       // ui color
    val icon: String = "ic_shopping",    // category icon
    @get:PropertyName("default")
    @set:PropertyName("default")
    var isDefault: Boolean = false,      // is it a system category
    val createdAt: Long = System.currentTimeMillis()    // when category was created
) {
    // No-argument constructor required for Firestore
    constructor() : this(
        id = 0,
        userId = "",
        name = "",
        type = TransactionType.EXPENSE,
        budget = null,
        color = "#FF5252",
        icon = "ic_shopping",
        isDefault = false,
        createdAt = System.currentTimeMillis()
    )
}