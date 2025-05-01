
<p align="left">
  <img width="192" height="192" src="https://github.com/user-attachments/assets/21d3c20e-4e36-492d-9f03-9149f8195c3b">
</p>

# Rand


A budget tracking application developed by The Dream Team for PROG7313 at Varsity College Cape Town.

**Team Members:**
- [Joshua Wood](https://github.com/)
- [Abdul Davids](https://github.com/abduldavids)
- [Nicholas Phillips](https://github.com/)

Check out our Youtube video here:

[![Play](https://img.youtube.com/vi/HluchMIJ9Hc/0.jpg)](https://www.youtube.com/watch?v=HluchMIJ9Hc)

## Overview

Rand is a budget tracking application that allows users to track their expenses, set goals, and manage their categories. It implements a local database to store user data and a camera interface to capture photos of receipts. We've also implemented a really nice UI/UX and smooth animations to make the app more user-friendly, and we have login and profile management features.

## Features

- Expense tracking
- Budget management
- Goal setting
- Photo capture
- User authentication
- Category management


## Technology Stack

- **Language**: Kotlin
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 35
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room
- **UI Components**: Material Components


## Getting Started

### Prerequisites

- Android Studio Meerkat (2024.3.1) or newer
- JDK 17
- Android SDK 35

### Installation

1. Clone the repository
   ```
   git clone https://github.com/VCCT-PROG7313-2025-G1/JoshuaWood_ST10296167_PROG7313_POE.git
   ```
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run the application


## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── dreamteam/
│   │   │           └── rand/
│   │   │               ├── data/
│   │   │               │   ├── dao/            # Data Access Objects
│   │   │               │   ├── entity/         # Database entities
│   │   │               │   ├── repository/     # Repositories
│   │   │               │   ├── AppDatabase.kt  # Database configuration
│   │   │               │   └── RandDatabase.kt # Main database class
│   │   │               ├── ui/
│   │   │               │   ├── auth/           # Login and Register screens
│   │   │               │   ├── categories/     # Budget categories features
│   │   │               │   ├── common/         # Shared UI components
│   │   │               │   ├── dashboard/      # Main dashboard
│   │   │               │   ├── expenses/       # Expense tracking features
│   │   │               │   ├── goals/          # Financial goals features
│   │   │               │   ├── photo/          # Photo-related functionality
│   │   │               │   ├── profile/        # User profile
│   │   │               │   ├── settings/       # App settings
│   │   │               │   └── MainActivity.kt # Entry point activity
│   │   │               └── RandApplication.kt  # Application class
│   │   ├── res # Resource files
│   │   └── AndroidManifest.xml
│   └── test/               # Unit tests
└── build.gradle.kts        # App-level build configuration
```

The project follows the MVVM (Model-View-ViewModel) architecture pattern:
- **Model**: Data layer with Room database, entities, and repositories
- **View**: UI components, activities, and fragments
- **ViewModel**: Manages UI-related data, handles business logic

## User Flow

### WELCOME SCREEN
- The first screen the user is introduced to when opening the application
- The user is presented with the logo of the application, a tagline "take control of your finances" and a card view that holds buttons for a user to interact with.
- The screen also makes use of a gradient shift background for a more aesthetic theme
- The user can select the sign in button and be directed to the sign in screen or the create account screen

### SIGN IN SCREEN
- The user is presented with the logo and the welcome back tagline, and the card view with input text fields.
- The user must enter a valid email address and password before being able to proceed further in the application.
- After entering in their valid credentials a user must click the sign in button to verify their account. If a valid account is not found, access will be denied and the user will be informed that either the password or email address is wrong.
- The user can interact with the eye icon in the password text to view the password that is hidden.
- If the user does not have an account they can interact with the sign up text to be directed to the account screen to create an account.

### CREATE ACCOUNT SCREEN
- The user is presented with the same gradient background, logo and new tagline text "Create Account".
- The card view is centered in the middle of the screen with input text fields for a user's name, email address and password.
- Entering the user's required details will be used for the account creation.
- After inputting these details the user can interact with the create account button to create the user's account.
- If the user already has an account the user can interact with the sign in text to be directed to the sign in page.

### DASHBOARD
- After creating an account or signing in a user is then directed to the dashboard screen.
- The screen presents the user with a burger menu in the top left corner of the banner for the screen with the applications name next to it.
- Below it, at the top and in the center theres a card view that displays a welcome message to the user and a value for the amount of money spent.
- The card view below is currently just a placeholder that is there for the later implementation of the expense graph.
- Below that there is a title to the left that is titled Recent Expenses and to the right of that is a view all title.
- If the user interacts with the view all text they user will be presented will all their expenses made.
- Underneath the recent expenses and the view all text the user will be presented with a card view that shows the user their recent expenses.
- Below their expenses their is a fab button at the bottom right of the screen which allows the user to create a new expense.
- Clicking on this fab button will direct the user to the fab screen.

### EXPENSE SCREEN
- After interacting with the fab button on in the dashboard screen or the burger menu and navigating to the expenses screen the user is directed to the Add Expense screen.
- In this screen the user is shown a monthly expenses card view that shows the total amount of money the user has spent in the month.
- Underneath that is a filters card view the can filter through and search for a specific expense by entering the date of the expense and the category.
- Below that is the all expenses title that shows all recent expenses.
- All the expenses are listed below it with showing the date of the expense, the amount and the category as well as the name of the expense.
- Below that to the bottom right is a fab button that allows the user to create a new expense.
- After clicking on the fab button the user is directed to the add expense screen.
- Below the banner on the screen the user will interact with the input fields on the screen to create a new expense.
- A user must enter a amount, a description , a category and date that the expense was made and optionally be able to take a photo of the receipt/ expense for improved record keeping.
- After the user as entered the information in the input fields the user can interact with the save expense button to save the expense.
- After saving the expense the user will be redirected to the dashboard screen to see their recently made expense.

### CATEGORY SCREEN
- When the user navigates to the categories screen they are presented with a banner that matches the gradient background colour theme and a text that is called categories.
- Below that is text that is tilted "Your Categories" with a dynamic counter that counts the categories the user made.
- Below this is a list of all the categories the user has made.
- In the bottom right corner of the screen is the new category fab button.
- After clicking on this the user is directed to the add category screen.
- The add category has the same banner theme and shows the user a card view called category preview which shows the user how the category will work and input fields the user must interact with to create a new category.
- The user must enter a category name, select a colour from the category colour selection and a corresponding category icon.
- After selecting their colour, icon and naming their category the user must save the category by interacting with the save category button.
- After interacting with this button the user's category will be saved in listed in the category screen.

### GOALS
- When the user navigates to this screen they are presented with the same banner theme and the Goals title text for the screen.
- Underneath it is a text "Your Goals" with a counter next to it that shows all the goals listed in the goals screen.
- Below this text is the card views showing the listed goals.
- The card view shows the goal's name, date and minimum and maximum amount of spending money.
- In the banner opposite the name is a trash can icon that the user can click on to delete a goal.
- Deleting a goal will remove the goal from the list of goals the user has made that is presented to them when visiting this page.
- At the bottom right of this screen is a fab button called add goal which will direct the user to the add goal screen.
- The add goal screen follows the same banner theme and the Add Goal title that the user is presented to along with the input fields the user must interact with to create the goal.
- The user must enter a goal name, select and month and year for the goal, the minimum and maximum amount of spending money and select a goal colour which will be used to help a user to identify their goal in the list.
- After entering the information the user must then click the save goal button to save the goal and for it to be displayed in the goal screen.
- By clicking on this save goal button the user is also directed back to the goal screen

### PHOTO
- When a user is directed to the add photo section of the application they are presented with a camera and photo screen and below that a save photo button that a user can interact with to save their recently captured photos.



## License

This project is licensed under the terms of the LICENSE file included in the repository.



