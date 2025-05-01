# Data Layer Documentation

This directory contains all the data-related components of the Rand application, following clean architecture principles with Room Database for local storage.

## Directory Structure

```
data/
├── dao/                # Data Access Objects
├── entity/            # Database entities
├── repository/        # Repository implementations
└── RandDatabase.kt    # Main database class
```

## Components

### Entities
- `User`: User profile and preferences
- `Transaction`: Financial transactions
- `Category`: Transaction categories
- `Goal`: Financial goals

### DAOs
- `UserDao`: User-related database operations
- `TransactionDao`: Transaction-related database operations
- `CategoryDao`: Category-related database operations
- `GoalDao`: Goal-related database operations

## Architecture

The data layer follows these key principles:

1. **Room Database**
   - SQLite abstraction layer
   - Type-safe queries
   - Reactive updates with Flow

2. **Repository Pattern**
   - Abstracts data sources
   - Handles business logic
   - Provides clean API for ViewModels

3. **Entity Relationships**
   - Foreign key constraints
   - Cascading operations
   - Indexed queries

## Best Practices

1. **Database Operations**
   - Use suspend functions for async operations
   - Implement proper error handling
   - Follow transaction best practices

2. **Data Access**
   - Use Flow for reactive updates
   - Implement proper indexing
   - Optimize query performance

3. **Entity Design**
   - Use proper data types
   - Implement relationships
   - Follow naming conventions

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

