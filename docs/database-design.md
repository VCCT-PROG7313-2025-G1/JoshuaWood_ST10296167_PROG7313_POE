# Rand - Database Design

## Overview
This document outlines the database design for the Rand budget tracking application using Room Database. Room is an SQLite object mapping library for Android that provides an abstraction layer over SQLite to allow fluent database access while harnessing the full power of SQLite.

## Database Structure

### Entities

#### 1. User
```kotlin
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val uid: String,
    val email: String,
    val name: String,
    val level: Int,
    val xp: Int,
    val createdAt: Long,
    val theme: String,
    val notificationsEnabled: Boolean,
    val currency: String
)
```

#### 2. Transaction
```kotlin
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
    val userId: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long?,
    val description: String,
    val date: Long,
    val receiptUri: String?,
    val createdAt: Long
)

enum class TransactionType {
    EXPENSE, INCOME
}
```

#### 3. Category
```kotlin
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
    val userId: String,
    val name: String,
    val type: TransactionType,
    val budget: Double?,
    val color: String,
    val icon: String,
    val isDefault: Boolean,
    val createdAt: Long
)
```

#### 4. SavingsGoal
```kotlin
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

enum class GoalStatus {
    ACTIVE, COMPLETED, CANCELLED
}
```

#### 5. Achievement
```kotlin
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
    val userId: String,
    val type: String,
    val unlockedAt: Long,
    val progress: Int,
    val description: String,
    val icon: String,
    val xpReward: Int
)
```

#### 6. Budget
```kotlin
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
```

## Data Access Objects (DAOs)

### UserDao
```kotlin
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUser(uid: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)
}
```

### TransactionDao
```kotlin
@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getTransactions(userId: String): Flow<List<Transaction>>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        ORDER BY date DESC
    """)
    fun getTransactionsByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<Transaction>>

    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}
```

### CategoryDao
```kotlin
@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getCategories(userId: String): Flow<List<Category>>

    @Insert
    suspend fun insertCategory(category: Category): Long

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)
}
```

### SavingsGoalDao
```kotlin
@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals WHERE userId = :userId")
    fun getSavingsGoals(userId: String): Flow<List<SavingsGoal>>

    @Insert
    suspend fun insertSavingsGoal(goal: SavingsGoal): Long

    @Update
    suspend fun updateSavingsGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteSavingsGoal(goal: SavingsGoal)
}
```

### AchievementDao
```kotlin
@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements WHERE userId = :userId")
    fun getAchievements(userId: String): Flow<List<Achievement>>

    @Insert
    suspend fun insertAchievement(achievement: Achievement): Long

    @Update
    suspend fun updateAchievement(achievement: Achievement)
}
```

### BudgetDao
```kotlin
@Dao
interface BudgetDao {
    @Query("""
        SELECT * FROM budgets 
        WHERE userId = :userId 
        AND month = :month
    """)
    fun getBudget(userId: String, month: Long): Flow<Budget?>

    @Insert
    suspend fun insertBudget(budget: Budget): Long

    @Update
    suspend fun updateBudget(budget: Budget)

    @Query("""
        SELECT * FROM budget_categories 
        WHERE budgetId = :budgetId
    """)
    fun getBudgetCategories(budgetId: Long): Flow<List<BudgetCategory>>

    @Insert
    suspend fun insertBudgetCategory(category: BudgetCategory)

    @Update
    suspend fun updateBudgetCategory(category: BudgetCategory)
}
```

## Database Class
```kotlin
@Database(
    entities = [
        User::class,
        Transaction::class,
        Category::class,
        SavingsGoal::class,
        Achievement::class,
        Budget::class,
        BudgetCategory::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class RandDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun achievementDao(): AchievementDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: RandDatabase? = null

        fun getDatabase(context: Context): RandDatabase {
            return INSTANCE ?: synchronized(self) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RandDatabase::class.java,
                    "rand_database"
                )
                .addCallback(RandDatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

## Type Converters
```kotlin
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }

    @TypeConverter
    fun fromGoalStatus(value: GoalStatus): String {
        return value.name
    }

    @TypeConverter
    fun toGoalStatus(value: String): GoalStatus {
        return GoalStatus.valueOf(value)
    }
}
```

## Database Callback
```kotlin
class RandDatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        // Initialize default data
        CoroutineScope(Dispatchers.IO).launch {
            // Create default categories
            // Set up initial user preferences
        }
    }

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        // Perform any necessary migrations or updates
    }
}
```

## Repository Pattern
```kotlin
class RandRepository(
    private val userDao: UserDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val achievementDao: AchievementDao,
    private val budgetDao: BudgetDao
) {
    // User operations
    suspend fun getUser(uid: String) = userDao.getUser(uid)
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)

    // Transaction operations
    fun getTransactions(userId: String) = transactionDao.getTransactions(userId)
    suspend fun insertTransaction(transaction: Transaction) = transactionDao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: Transaction) = transactionDao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.deleteTransaction(transaction)

    // Add other repository methods...
}
```

## Migration Strategy
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add migration logic here
    }
}
```
