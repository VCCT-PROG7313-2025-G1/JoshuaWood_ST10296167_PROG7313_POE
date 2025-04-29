# UI Layer Documentation

This directory contains all the UI-related components of the Rand application, following the MVVM architecture pattern with Jetpack Compose and Navigation components.

## Directory Structure

```
ui/
├── auth/                # Authentication related screens
├── categories/          # Category management screens
├── dashboard/           # Main dashboard and overview
├── goals/              # Financial goals screens
├── expenses/           # Expense tracking screens
├── common/             # Shared UI components
├── photo/              # Photo handling and processing
├── profile/            # User profile screens
├── settings/           # App settings screens
├── savings/            # Savings tracking screens
├── budget/             # Budget management screens
└── MainActivity.kt     # Main application activity
```

## Components

### Main Activity
- `MainActivity.kt`: Central activity that:
  - Handles navigation setup
  - Manages authentication state
  - Controls screen flow based on user login status
  - Implements navigation between major app sections

### Feature Modules

#### Authentication (`auth/`)
- Handles user registration and login
- Manages authentication state
- Controls access to protected features

#### Dashboard (`dashboard/`)
- Main app overview
- Financial summaries
- Quick access to key features

#### Categories (`categories/`)
- Category management
- Category creation and editing
- Category organization

#### Goals (`goals/`)
- Financial goal tracking
- Goal progress visualization
- Goal management

#### Expenses (`expenses/`)
- Expense tracking
- Transaction history
- Expense categorization

#### Profile (`profile/`)
- User profile management
- Personal information
- Account settings

#### Settings (`settings/`)
- App configuration
- User preferences
- System settings

#### Budget (`budget/`)
- Budget planning
- Budget tracking
- Budget analysis

#### Savings (`savings/`)
- Savings tracking
- Savings goals
- Savings analysis

#### Common (`common/`)
- Shared UI components
- Reusable widgets
- Common utilities

#### Photo (`photo/`)
- Receipt scanning
- Photo processing
- Image management

## Architecture

The UI layer follows these key principles:

1. **MVVM Architecture**
   - ViewModels handle business logic
   - Views (Fragments/Composables) handle UI
   - Data binding for UI updates

2. **Navigation**
   - Single Activity architecture
   - Navigation component for screen management
   - Deep linking support

3. **State Management**
   - ViewModels for state persistence
   - LiveData/StateFlow for reactive updates
   - Coroutines for async operations

## Best Practices

1. **UI Components**
   - Use Jetpack Compose for modern UI
   - Follow Material Design guidelines
   - Implement responsive layouts

2. **Navigation**
   - Use safe args for type-safe navigation
   - Handle back stack appropriately
   - Implement proper deep linking

3. **State Management**
   - Keep UI state in ViewModels
   - Use unidirectional data flow
   - Handle configuration changes properly

4. **Performance**
   - Implement lazy loading
   - Optimize image loading
   - Use appropriate coroutine scopes

## Usage Examples

1. **Navigation Setup**:
```kotlin
val navController = findNavController()
navController.navigate(R.id.destination)
```

2. **ViewModel Usage**:
```kotlin
val viewModel: MyViewModel by viewModels()
viewModel.data.observe(viewLifecycleOwner) { data ->
    // Update UI
}
```

3. **Composable Function**:
```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel) {
    val data by viewModel.data.collectAsState()
    // UI implementation
}
``` 