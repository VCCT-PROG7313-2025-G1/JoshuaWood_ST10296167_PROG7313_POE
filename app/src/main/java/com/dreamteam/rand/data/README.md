# Data Layer Documentation

This directory contains all the data-related components of the Rand application, following a clean architecture pattern with Room Database as the local storage solution.

## Directory Structure

```
data/
├── dao/                 # Data Access Objects (DAOs) for database operations
├── entity/             # Room database entities
├── repository/         # Repository classes that handle data operations
├── AppDatabase.kt      # Database configuration and setup
├── Converters.kt       # Type converters for Room database
├── RandDatabase.kt     # Main database class
└── RandDatabaseCallback.kt  # Database callback for initialization
```

## Components

### Database
- `RandDatabase.kt`: Main database class that:
  - Defines all entities
  - Provides access to DAOs
  - Handles database versioning (currently version 3)
  - Implements singleton pattern for database instance

### Entities
Located in `entity/` directory:
- `User`: User account information
- `Transaction`: Financial transactions (income/expenses)
- `Category`: Transaction categories
- `Goal`: Financial goals
- `Achievement`: User achievements
- `Budget`: Budget plans
- `BudgetCategory`: Budget category mappings

### Data Access Objects (DAOs)
Located in `dao/` directory:
- `UserDao`: User-related database operations
- `TransactionDao`: Transaction-related database operations
- `CategoryDao`: Category-related database operations
- `GoalDao`: Goal-related database operations
- `AchievementDao`: Achievement-related database operations
- `BudgetDao`: Budget-related database operations

### Repositories
Located in `repository/` directory:
- `UserRepository`: Handles user operations (registration, login, settings)
- `ExpenseRepository`: Manages expense-related operations
- `GoalRepository`: Manages goal-related operations
- `CategoryRepository`: Manages category-related operations

## Usage

1. **Database Access**:
```kotlin
val db = RandDatabase.getDatabase(context)
```

2. **Repository Access**:
```kotlin
val userRepository = UserRepository(db.userDao())
val expenseRepository = ExpenseRepository(db.transactionDao())
```

## Rules

1. Always use repositories for data operations, not DAOs directly
2. Perform database operations in coroutine context
3. Use Flow for reactive data streams
4. Handle errors appropriately in repository layer
5. Keep database version updated when schema changes

