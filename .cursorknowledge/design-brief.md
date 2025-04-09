# Rand - Personal Budget Tracker App

## Implementation Priorities
1. Core Authentication & User Management
2. Basic Transaction Management
3. Category System
4. Budget Management
5. Data Visualization
6. Savings Goals
7. Gamification
8. Notifications

## Data Models

### User
```json
{
  "uid": "string",
  "email": "string",
  "name": "string",
  "level": "number",
  "xp": "number",
  "createdAt": "timestamp",
  "preferences": {
    "theme": "string",
    "notifications": "boolean",
    "currency": "string"
  }
}
```

### Transaction
```json
{
  "id": "string",
  "userId": "string",
  "amount": "number",
  "type": "expense|income",
  "category": "string",
  "description": "string",
  "date": "timestamp",
  "receiptUrl": "string?",
  "createdAt": "timestamp"
}
```

### Category
```json
{
  "id": "string",
  "userId": "string",
  "name": "string",
  "type": "expense|income",
  "budget": "number?",
  "color": "string",
  "icon": "string"
}
```

### SavingsGoal
```json
{
  "id": "string",
  "userId": "string",
  "name": "string",
  "targetAmount": "number",
  "currentAmount": "number",
  "deadline": "timestamp?",
  "createdAt": "timestamp"
}
```

### Achievement
```json
{
  "id": "string",
  "userId": "string",
  "type": "string",
  "unlockedAt": "timestamp",
  "progress": "number"
}
```

## Core Features

### 1. User Authentication
- Email/password registration and login
- Password recovery via OTP
- Profile management
- Implementation: Firebase Authentication
- Security: JWT tokens, password hashing

### 2. Transaction Management
- Add/edit/delete transactions
- Fields: amount, date, category, description, optional receipt photo
- Support for both expenses and income
- Quick transaction entry
- Transaction history with filtering
- Implementation: Firestore CRUD operations
- Storage: Firebase Storage for receipts

### 3. Category System
- Predefined categories (rent, food, etc.)
- Custom category creation/editing/deletion
- Category-based budget allocation
- Implementation: Firestore collections
- Default categories on user creation

### 4. Budget Management
- Monthly budget setting
- Real-time budget tracking
- Category-wise budget allocation
- Budget alerts and notifications
- Implementation: Firestore triggers for calculations
- Real-time updates using Firebase listeners

### 5. Data Visualization
- Monthly spending vs budget dashboard
- Balance over time (line/bar graph)
- Category-wise expenditure (pie chart)
- Date range filtering for all views
- Implementation: Charts.js or D3.js
- Data aggregation using Firestore queries

### 6. Savings Goals
- Create multiple savings goals
- Track progress towards goals
- Transfer funds between spending and savings
- Goal completion notifications
- Implementation: Firestore transactions for fund transfers
- Progress calculation triggers

### 7. Gamification
- Achievement badges for:
  - Weekly goals
  - Monthly goals
  - Long-term goals (6 months)
- Progress tracking
- XP system
- Implementation: Firestore triggers for achievement checks
- XP calculation based on user actions

### 8. Notifications
- Daily expense logging reminders
- Budget threshold alerts
- Achievement unlocks
- Savings goal progress
- Implementation: Firebase Cloud Messaging
- Scheduled notifications using Cloud Functions

## Technical Stack

### Frontend
- React Native
- Redux for state management
- React Navigation
- UI Components: React Native Paper
- Charts: React Native Chart Kit
- Forms: Formik + Yup

### Backend
- Firebase
  - Authentication
  - Firestore
  - Storage
  - Cloud Functions
  - Cloud Messaging

### Development Tools
- TypeScript
- ESLint
- Prettier
- Jest for testing
- GitHub Actions for CI/CD

## UI Screens

### Authentication Flow
1. Welcome Screen
   - Sign In button
   - Sign Up link
   - Implementation: React Native Paper components
   - Navigation: React Navigation stack

2. Sign In Screen
   - Email/password fields
   - Forgot Password link
   - Form validation
   - Error handling

3. Sign Up Screen
   - Name, email, password fields
   - Password strength indicator
   - Terms acceptance

4. Forgot Password Screen
   - Email input
   - OTP verification
   - New password setup
   - Success/error states

### Main App Flow
1. Dashboard
   - User level display
   - Spending overview
   - Recent transactions
   - Quick add transaction button
   - Implementation: Custom hooks for data fetching
   - Real-time updates

2. Transaction Management
   - Add transaction form
   - Transaction history
   - Filtering options
   - Implementation: Formik forms
   - Image picker for receipts

3. Budget Settings
   - Overall budget
   - Category budgets
   - Add/edit categories
   - Implementation: Slider components
   - Color picker for categories

4. Profile & Settings
   - User information
   - Achievement badges
   - XP log
   - App preferences
   - Notification settings
   - Theme customization
   - Implementation: Settings persistence
   - Theme context

5. Savings
   - Total balance
   - Spending balance
   - Savings goals list
   - Goal progress tracking
   - Add/delete goals
   - Implementation: Progress bars
   - Animated transitions

## Navigation
- Bottom navigation bar for main sections
- Side menu for additional options
- Back navigation for sub-screens
- Implementation: React Navigation
- Deep linking support

# PROG7313

	*POE Part 1:*  
**Planning & Design Document** 

---

Group members:

**Abdul Baari Davids** \- ST10267411  
**Joshua Wood** \- ST10296167  
**Nicholas Phillips** \- ST10263496  
**Sky Martin** \- ST10286905

**Table of Contents**

[**Introduction	2**](#introduction)

[**App Overview	3**](#app-overview)

[Overview \- Rand	3](#overview---rand)

[Innovative features	3](#innovative-features)

[**App Requirements	4**](#app-requirements)

[User Authentication	4](#user-authentication)

[Expense/Income Management	4](#expense/income-management)

[Category Management	4](#category-management)

[Budget Tracking	4](#budget-tracking)

[Data Visualization	4](#data-visualization)

[Gamification	4](#gamification)

[Notifications	5](#notifications)

[Data Storage	5](#data-storage)

[Saving tracking	5](#saving-tracking)

[Security	5](#security)

[**User Interface Design	6**](#user-interface-design)

[Mockups	6](#mockups)

[Welcome Screen	6](#welcome-screen)

[Sign In Screen	6](#sign-in-screen)

[Forgot Password Screen	7](#forgot-password-screen)

[Sign Up Screen	8](#sign-up-screen)

[Dashboard Screen	8](#dashboard-screen)

[Add Transaction Screen	9](#add-transaction-screen)

[Navigation Menu Screen	9](#navigation-menu-screen)

[Budget Settings Screen	10](#budget-settings-screen)

[Profile Screen	11](#profile-screen)

[Settings Screen	11](#settings-screen)

[Savings Screen	12](#savings-screen)

[User Navigation Diagram	13](#user-navigation-diagram)

[**Project Plan	19**](#project-plan)

[**Conclusion	19**](#conclusion)

[**Disclosure of AI Usage	26**](#disclosure-of-ai-usage)

# **Introduction** {#introduction}

Having completed our research, our group now has a thorough understanding of what is required for a high quality budget tracker app and what innovative features we could include in our own app. We will now move on to planning and fully designing our personal budget tracker app. 

First, we will create an app overview listing the innovative features that we are planning to include in our app. This will serve as the foundation for the rest of our design. We will then create a detailed list of all the requirements of our app, explaining exactly how we want the app to function. From these requirements we will then develop UI mockups and a user flow diagram in order to refine the design, layout and user experience of our app before development. Finally, we will create a complete project plan in the form of a Gantt chart that will allow us to effectively track and manage our progress over the rest of the project. 

# **App Overview** {#app-overview}

## **![][image1]**

## **Overview \- Rand** {#overview---rand}

Rand is a personal budgeting app designed to help users keep track of their expenses, income, manage budgets, and achieve their financial goals. With detailed analytics and customization, users can unlock deeper insights into their spending habits and quickly understand their savings progress. With our real-time notifications integration, Rand lets you know when it's time to log your daily expenses. Rand also features fun achievements and badges in order to reward reaching your goals and keep you motivated. 

## **Innovative Features** {#innovative-features}

**Real-time dynamic analytics \-** Rand goes beyond basic transaction tables and features interactive graphs that allow you to explore your finances over time. 

**Enhanced category customization \-** Rand enables you to fully customize spending categories to match your personal financial preference. 

**Goals setting \-** Rand helps you manage long-term financial goals. Saving for a new iPhone or a vacation? Create specific savings goals and let Rand's real-time notification system remind you daily to set aside funds, helping you achieve your financial dreams.

**Rewards \-** Finally made your books balance? With Rand Badges you can have something to show for your hard work, be it a one-week, four-week or even six-months goal, Rand keeps you motivated to keep saving.

# **App Requirements** {#app-requirements}

## **User Authentication** {#user-authentication}

Users should be able to register a new account as well as log in to an existing account. Users should also be able to change their account details and password.

## **Expense/Income Management** {#expense/income-management}

Users should be able to *quickly* add transaction entries with amount, date, category, description, and optional receipt photos. 

Users should also be able to edit or delete existing expense entries from a list of recent entries and view a detailed list of expenses, filterable by date or category.

## **Category Management** {#category-management}

Users should be able to create, edit, and delete custom transaction categories, there will also be predefined example categories such as rent, food etc.

## **Budget Tracking** {#budget-tracking}

Users should be able to set an overall monthly budget. The app will handle real-time calculation and display of remaining budget based on the transactions for the month.

## **Data Visualization** {#data-visualization}

On the home screen there'll be a display dashboard summarizing *monthly spending* vs budget. We'll also show a line/bar graph for balance over time.  
We'll also have a pie chart for seeing what *category* has more expenditure.  
All views are filterable by selected date ranges.

## **Gamification** {#gamification}

Users can unlock badges for reaching monthly/weekly goals and view progress indicators towards unlocking badges.

## **Notifications** {#notifications}

The app will send push notifications for daily expense logging reminders, as well as budget threshold alerts when approaching/exceeding limits.   
Achievement notifications will be pushed when users unlock badges.

## **Data Storage** {#data-storage}

On the backend we'll store expenses, budgets, categories, and achievements in Firebase Firestore and store receipt photos in Firebase Storage. This is to ensure scalability. 

## **Saving tracking** {#saving-tracking}

Users can set personal savings goals for different items (new phone, holiday etc.) and view their progress towards those goals.

## **Security** {#security}

In case a user forgets their password they can opt to send a OTP to login and change their password.

# **User Interface Design** {#user-interface-design}

## **Mockups**  {#mockups}

### **Welcome Screen** {#welcome-screen}

### 

#### **Purpose**

	The welcome screen is the first screen a user is presented when opening the application. The sign in button will direct a user to a sign in process where the user enters their credentials to access their account and proceed further with the application.  
The blue SIgn Up words direct the user to the Create Account screen where the user will enter their personal details such as name, surname, etc.

### **Sign In Screen** {#sign-in-screen}

#### 

#### **Purpose**

The sign in screen requires the user to enter their password and email to access their account and the app. The highlighted blue text will take the user to the forgot password screen, this screen will allow the user to enter the required details for the user to recover/ change their password if they forget.

### **Forgot Password Screen** {#forgot-password-screen}

### 

### 

### 

### 

### 

### 

#### **Purpose**

The forgot password screen displays a text field for a user to input their email. The user would then enter their email address for the code to be sent to. After receiving a code, the user can then enter the code and create a new password that will be set for the account.

### **Sign Up Screen** {#sign-up-screen}

#### 

#### **Purpose**

The sign up screen is where the user is directed after pressing the "Sign Up" blue text from the welcome screen.. On this screen, the user must fill in the empty fields: *name*, *email*, and *password* in order to successfully register an account with the system, allowing them to sign in to the app.

### **Dashboard Screen** {#dashboard-screen}

### 

#### **Purpose**

The dashboard screen is the first screen the user will be shown after successfully logging into their account. The dashboard will display the user's level, a pie chart illustrating their spending habits on certain categories, and display their recent expenses. The user will be able to interact with an add button, which will allow the user to add a transaction.

### **Add Transaction Screen** {#add-transaction-screen}


#### **Purpose**

The add transaction screen allows the user to add a transaction by filling in the fields such as *amount*, *description*, *category*, and the *date* with an option to upload/take a photo of the expense/receipt. These transactions can either be expenses or income. 

### **Navigation Menu Screen** {#navigation-menu-screen}

## 

#### **Purpose**

A user can find the navigation menu by clicking on the 3 lines in the top left corner of any screen. From this screen a user can navigate to the **dashboard**, **budget settings**, **profile**, and **savings** screens.

### **Budget Settings Screen** {#budget-settings-screen}



#### **Purpose**

The budget settings screen allows the user to set their budget. The overall tab will show the user their *monthly income* and *overall budget*. It will also show the amount set aside for other categories that the user can budget for such as groceries, entertainment, parking and cafeteria etc. The user will be able to interact with the add category button that will allow the user to create a new category that they can budget for. The user will also be able to save their new categories and be able to delete categories.

### **Profile Screen** {#profile-screen}

#### 

#### 

#### 

#### 

#### 

#### 

#### **Purpose**

The profile screen displays the user's *name*, *current level*, expandable *unlocked badges* earned through maintaining good spending habits and sticking to a budget, and an expandable *XP log*. Additionally, the user can access *app settings* by clicking the **settings** button.

### **Settings Screen** {#settings-screen}


#### **Purpose**

The settings screen allows the user to customize their *preferences*, *notifications*, *themes* and contact *support.*

### **Savings Screen** {#savings-screen}

#### **Purpose**

The purpose of the savings screen is to show the user detailed information about their savings and allow them to set and manage their savings goals.

The savings screen shows the user's *total balance* (including all savings goals and spending balance), *spending balance*, and *savings goals* with their respective accumulated *amounts*, *totals*, and *progress bars.*

Pressing the ""Plus" (+)" button on a goal allows the user to contribute an amount from their spending balance to the savings goal.

Pressing the **"Add Goal"** button allows the user to add a new savings goal.

Long pressing on a savings goal changes the add buttons to delete buttons, which when pressed will ask for confirmation and if confirmed delete the goal.

## **User Navigation Diagram** {#user-navigation-diagram}

### 

![][image2]  
![][image3]  
![][image4]  
![][image5]

# **Project Plan** {#project-plan}

# **Conclusion** {#conclusion}

In conclusion, our research has provided us with the knowledge needed to effectively plan and fully design our personal budget tracker app. By outlining innovative features in our app overview, detailing functionality through a requirements list, and refining the user experience with UI mockups and a user flow diagram. Along with creating a project plan Gantt chart for the entire project. We have created a solid foundation to start development, ensuring that our group knows exactly what to build and how we are going to build it before we begin implementing the app prototype.

