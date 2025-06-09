
<p align="left">
  <img width="192" height="192" src="https://github.com/user-attachments/assets/21d3c20e-4e36-492d-9f03-9149f8195c3b">
</p>

# Rand


A budget tracking application developed by The Dream Team for PROG7313 at Varsity College Cape Town.

**Team Members:**
- [Joshua Wood](https://github.com/joshuawood13)
- [Abdul Davids](https://github.com/abduldavids)
- [Nicholas Phillips](https://github.com/nicholasphillipsST10263496)

Check out our Youtube video here:

[![Play](https://img.youtube.com/vi/hqsQ-MpwyQk/0.jpg)](https://www.youtube.com/watch?v=hqsQ-MpwyQk)

## Overview

Rand is a budget tracking application designed for sane people. It allows users to easily add, track, and analyze expenses, set monthly budget goals, and add categories. We want users of Rand to feel secure in their financial decisions, that's why our app gives users detailed expense graphs so that users can easily analyze and manage their current expenses. If that all seems like too much for you then don't sweat\! Ask Randy, your personal AI budget assistant, to analyze your recent expenses for you and receive advice on how to better optimize your spending. 

When using Rand, we want your experience to be as convenient as possible, that's why all your data will be stored in the cloud, so that you can easily access your account from any device. All you need to do is create an account and then you can log in from anywhere. Additionally, our clean and clutter-free UI design with smooth animations and access to multiple themes makes navigating and using Rand simple and user-friendly.

So if you're looking to start tracking and managing your expenses and budget in a way that's smart, simple, and actually enjoyable, Rand is the app for you.

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

**Welcome Screen**

---

The welcome screen serves as the user's first point of contact upon launching the application. It has been thoughtfully designed to provide a seamless and engaging introduction to the platform. Key features include:

* **Design**: A sleek, modern interface enhanced by a gradient-shift background that aligns with the application's overall aesthetic and branding.

* **Branding**: Prominently features the Rand logo accompanied by the tagline *“Take control of your finances”*, reinforcing the application's core mission.

* **User Onboarding Options**:  
  Displayed in a clean, card-style layout to ensure clarity and ease of access:

* **Sign In** – for returning users

* **Create Account** – for new users

* **Animations**: Subtle transitions and animations contribute to a premium and responsive user experience, enhancing interactivity without distraction.

* **User Experience**: The minimalist design keeps the interface intuitive and focused, reducing visual clutter and maintaining user engagement from the outset.

---

**Sign-In Screen**

---

The sign-in screen is displayed to users after selecting the "Sign In" option from the Welcome Screen. It continues the application's cohesive design and user-friendly approach. Key elements include:

* **Visual Consistency**: The screen maintains the same branding, including the Rand logo and gradient background, ensuring a seamless transition from the Welcome Screen.

* **User Greeting**: A greeting message, *“Welcome Back”*, is prominently displayed to create a personalized and inviting experience for returning users.

* **Login Form**: Clearly labeled **Email** and **Password** input fields enhance usability and a password visibility toggle, represented by an eye icon, allows users to view or hide their password for convenience and accuracy.

* **Error Handling**:  
  In the event of an unsuccessful login attempt, a descriptive error message is displayed, clearly communicating the reason for the failure and guiding the user toward resolution.

* **Alternate Navigation**:  
  For users without an existing account, a **“Sign Up”** link is provided. This link features a smooth transition to the registration page, promoting easy navigation and a fluid user experience.

---

**Create Account Screen**

---

The Create Account screen is presented to users either via redirection from the Sign-In screen or directly from the Welcome Screen. It continues the application's consistent design and intuitive layout. Key features include:

* **Visual Consistency**:  
  This screen retains the gradient background and branding elements, including the Rand logo, to ensure a cohesive user experience across all screens.

* **Header Display**:  
  A clear and prominent *“Create Account”* header immediately informs users of the screen's purpose.

* **Registration Form**:  
  The form includes the following well-labeled input fields:  
- **Full Name**  
- **Email Address**  
- **Secure Password**

* **User Actions**:  
- A bold and visually distinct **“Create Account”** button allows users to complete the registration process.  
- A **“Sign In”** link is also available for users who already have an account, offering a smooth redirection to the Sign-In screen.

* **Account Confirmation**:  
   Upon successful account creation, a confirmation message is displayed. The application then automatically transitions the user to the dashboard, ensuring a smooth experience.

---

**Dashboard Screen**

---

Upon successfully creating an account or signing in, users are directed to the **Dashboard**, the central hub of the application where they can monitor and manage their financial activity. The screen offers an intuitive and interactive layout designed to support ease of use and insight-driven engagement. Key features include:

* **Navigation**:  
  Positioned at the top-left corner of the banner is a **burger menu**. This menu provides access to the main sections of the application, enabling smooth and efficient navigation.

* **Welcome Message**:  
  Just below the banner, a centered **card view** displays a personalized welcome message along with the **total amount of money spent**, providing users with a quick financial overview.

* **Expense Graph**:  
  An **interactive expense graph** offers users a dynamic visualization of their spending trends over time. This feature enables users to explore a detailed and comprehensive analysis of their financial habits.

* **Randy’s Analysis**:  
  A newly introduced, **interactive feature** that delivers personalized insights and analytics based on the user’s spending behavior, offering actionable recommendations for better financial decision-making.

* **Recent Expenses**:  
  This section showcases the user’s **latest recorded transactions**, displayed in an organized list. A **“View All”** link allows users to access the complete history of their expenses.

* **Floating Action Button (FAB)**:  
  Located at the bottom-right corner of the screen, the FAB enables users to **quickly add a new expense**, enhancing workflow efficiency.

* **Design & Layout**:  
  The dashboard maintains the application’s **signature gradient banner** at the top while introducing a **clean white background** for the main content area. The overall layout is structured, clear, and visually balanced, ensuring a professional and user-friendly experience.

---

**Expense Screen**

---

This screen allows the user to view, filter, manage, and create their expenses.

After interacting with the FAB button on the Dashboard screen or using the burger menu to navigate to the Expenses screen, the user is directed to the **Add Expense** screen.

In this screen, the user is shown a **monthly expenses card view** that displays the total amount of money spent during the month.

Beneath this, a **filter card view** is available, allowing the user to search for a specific expense by entering the **date** of the expense and/or the **category**.

Following the filter section is the **“All Expenses”** title, indicating the start of the expenses list.

All expenses are displayed below, showing the **date of the expense**, the **amount**, the **category**, and the **name of the expense**.

At the bottom-right corner, a **FAB button** allows the user to create a new expense.

After clicking on the FAB button, the user is directed to the **Add Expense** screen.

Below the banner on this screen, the user interacts with input fields to create a new expense. The user must enter:

* an **amount**,

* a **description**,

* a **category**, and

* the **date** the expense was made.

Optionally, the user can **take a photo of the receipt/expense** for improved record keeping.

After entering the information, the user can interact with the **Save Expense** button to save the expense.

Once the expense is saved, the user is redirected to the **main Expense screen** to view their recently added expense.

The updated screen now displays:

* the **expense amount**,

* a **bar graph** showing the expense amount compared to other categories,

* a **filter** to find a specific expense, and

* an **All Expenses** card that shows all expenses made.

After interacting with the **Detail** button on the graph, the user is directed to the **Expense Analysis** screen.

---

**Expense Analysis**

---

This screen presents the user with **four main card views** that provide a comprehensive breakdown and analysis of their expenses.

* The **filter option** allows the user to filter the data by **time frame** and **category**.

* The **Expense by Category** card compares the expense against all other expenses made within the specified time frame.

* The **Expense Trends** card displays the user’s spending habits based on the expenses made.

* The **Budget vs Spending** card compares the user’s predefined budget against their actual expenses.

---

**Category Screen**

---

The Category screen displays all the user-created categories and provides functionality for filtering existing categories and creating new ones.

When the user navigates to the Category screen, they are presented with a **banner** that matches the application’s **gradient background theme**, along with a header labeled **"Categories."**

Beneath the banner, a title labeled **“Your Categories”** is displayed, accompanied by a **dynamic counter** that reflects the total number of categories the user has created.

Next to this is a **filter system** that allows users to filter their expense amounts per category based on a user-defined **date range**.

Below this section is a **list** of all categories the user has created. Each category entry displays:

* the **icon** of the category,

* the **category name**, and

* the **total money spent** on that category.

In the **bottom-right corner** of the screen is a **New Category** FAB button.

Upon clicking this button, the user is directed to the **Add Category** screen.

The Add Category screen maintains the same **banner theme** and presents a **Category Preview** card, which shows the user how the category will appear. The screen also includes input fields that the user must interact with to create a new category.

To create a category, the user must:

* enter a **category name**,

* select a **colour** from the category colour selection, and

* choose a **corresponding category icon**.

Once the user has made these selections, they must interact with the **Save Category** button to complete the process.

After saving, the newly created category will appear in the **Category screen** list.

---

**Goals Screen**

---

This screen displays all the goals the user has created, listing them in a clear and organized manner.

When the user navigates to this screen, they are presented with the **same banner theme** used throughout the application, along with the screen title **"Goals."**

Below the title is a section labeled **“Your Goals”**, accompanied by a **counter** that shows the total number of goals currently listed on the screen.

Under this section are **card views** that represent the listed goals.  
Each card view displays the following:

* the **goal name**,

* the **date**, and

* the **minimum and maximum amount** of spending money set for the goal.

In the **banner of each goal card view**, opposite the goal name, there is a **trash can icon**. When clicked, this icon allows the user to **delete a goal**.

Deleting a goal removes it from the list of goals shown on the screen.

In the **bottom-right corner** of the screen, there is a **FAB button** labeled **“Add Goal,”** which directs the user to the **Add Goal** screen.

The **Add Goal screen** maintains the same banner theme and includes the title **“Add Goal”**, followed by input fields the user must complete to create a new goal.

To create a goal, the user must:

* enter a **goal name**,

* select a **month and year** for the goal,

* define the **minimum and maximum spending amount**, and

* select a **goal colour** to help visually identify the goal in the list.

Once all the necessary information is entered, the user must click the **Save Goal** button to save the new goal.

Upon saving, the user is **redirected back** to the **Goals screen**, where the newly created goal is displayed.

---

**Photo Screen**

---

When a user is directed to the **Add Photo** section of the application, they are presented with a **camera and photo interface**.  
 Below this interface is a **“Save Photo”** button, which the user can interact with to **save their recently captured photo**.

---

**AI Insights**

---

This application features an **AI Insight** tool designed to provide an in-depth analysis of the user's **monthly expense tracking**.

The AI feature, named **Randy**, helps **break down the spending patterns and frequency** of expenses, delivering a detailed and insightful analysis.

After the user selects **“Generate”**, Randy begins analyzing the expense data. During this process, the **card view radiates in colour**, indicating the analysis is in progress.

Once complete, Randy presents a **thorough and comprehensive breakdown** of the user's expenses, offering clear insights into their **spending habits** and assisting them in **achieving their budgeting goals**.

---

**Profile Screen**

---

The Profile screen introduces **gamification elements** to enhance user engagement within the application.

On this screen, users are presented with **three main card views**:

* **User Profile Card** – Displays the user’s **level, profile icon, and name**, with their **email address** shown underneath.

* **Progress Bar Card** – Visually represents the user’s current **level progress**, indicating the **experience points (XP)** earned and the amount needed to reach the **next level**.

* **Achievements Card** – Designed to **motivate and engage** users by encouraging them to complete listed tasks to earn achievements such as **“Penny Pusher”** and **“Label Lover.”** These achievements serve as **rewards** for completing specific in-app actions.

Each badge includes a **dynamic progress bar** that updates in real-time based on the user’s interaction with the app, **tracking their progress** toward earning each achievement.

---

**Settings Screen**

---

The **Settings** screen allows users to **customize their application preferences** to enhance their experience.

This screen contains **one interactive card view**:

* **Theme** – Enables users to **switch between Light Mode, Dark Mode, or System Default**. Once a theme is selected, the application's **appearance updates instantly** to reflect the chosen preference.

These settings help ensure the application is tailored to the user’s personal style.

## Design Choices and How We Use GitHub

### **How the App is Built**

**App Structure (MVVM)**
The app uses a simple pattern where each screen has three parts: what you see (the UI), what manages the data (ViewModel), and where the data comes from (Repository). This makes it easier to test and change things later without breaking everything else.

The flow looks like this: Screen → ViewModel → Repository → Database/Firebase

**Data Storage**
We use two places to store data. The main storage is a local database on your phone (Room), which means the app works even without internet. We also backup everything to Firebase in the cloud, so you don't lose your data if something happens to your phone.

The database is set up so each user has their own expenses, categories, and goals. Categories connect to expenses, so you can see how much you spend in each area.

### **Design Choices**

**Look and Feel**
The app follows Google's Material Design, which means it looks familiar and works well with other Android apps. It supports both light and dark themes, and we made sure buttons and text are big enough for everyone to use easily.

**Getting Around**
The main sections (like Dashboard, Expenses, Goals) are easy to reach. The profile and settings are in a side menu that slides out. This keeps the main screen clean while still giving you quick access to everything.

**Profile Pictures**
When you add a profile picture, we don't store the actual image in the database (that would make it slow). Instead, we save where the image is stored on your phone and use a library called Glide to load it quickly. If something goes wrong, it just shows a default picture.

### **Keeping Your Data Safe**

**Local First, Cloud Backup**
Your data lives on your phone first, so the app is always fast. Everything gets backed up to Firebase automatically when you have internet. This means you can use the app on the subway, and it will sync when you get back online.

**Security**
We only ask for the minimum information we need. Your data is encrypted and we use Firebase's built-in security. For profile pictures, we use Android's secure file system so other apps can't access them.

---

## 🐙 **How We Use GitHub**

### **Project Organization**
The code is organized in a clear folder structure. The main app code lives in the `app` folder, while GitHub Actions (our automation) goes in `.github/workflows`. We also have folders for documentation and build scripts. The `cloudflare-worker.js` file contains a script for generating the AI suggestions for the app.

### **Working with Code Changes**
We use different branches for different purposes. The `main` branch has the live app code that users see. We work on new features in separate `feat-` branches, then open a pull request to merge them into the `main` branch.

This way, we can work on multiple features at the same time without breaking the main app. We also have rules that require at least one other person to review code changes before they get merged.

### **Automation (GitHub Actions)**

We have a workflow that automatically builds and tests the app every time someone wants to merge code into the main branch. The tests have to pass before GitHub will let the code get merged. This prevents broken code from reaching users. You can see the workflow in the `.github/workflows/android.yml` file.

---


## License

This project is licensed under the terms of the [LICENSE](LICENSE) file included in the repository.

--- 

# Part 3

# New Features

When it came to integrating new features into our app we had to make some changes from the features initially outlined in our design document. We decided to stick with **real-time dynamic analytics**, but altered how we went about providing this, deciding to implement **personalized AI insights** onto the dashboard where users can easily receive financial advice personalized to their spending habits. Additionally we also implemented an **expense trends graph** where users can easily view and analyze their daily expenses. We also decided to make changes to our rewards feature outlined in the design document, deciding to instead implement a **user** **level and achievement system** feature for each user which provides a much needed gamification element to the app. Finally, we implemented **dark mode**, an additional feature not included in our design document, which allows users to choose between light and dark themes based on their personal preference.

## **AI Spending Insights**

---

With **AI Spending Insights**, your finances drastically improve. This feature uses AI to analyze your spending habits, detect trends, and highlight unusual spikes, all to give you easy-to-understand, personalized advice that feels like it came from a financial coach.

**Why It's Useful**

* **Smarter recommendations, less effort**  
  You’ll get helpful, specific suggestions based on *your* habits. Providing a personalised experience. 

* **Feels like a financial coach**  
  Insights are personalized and human, not robotic. You’re nudged, not nagged.

* **Builds trust with transparency**  
  When users see the app recognizing real patterns in their behavior and offering real value, it builds confidence and loyalty.

* **Clear, not cluttered**  
  No more digging through graphs or charts. You get straightforward advice in natural language, so you always know where you stand.

# **Expense Trends**

---

**Visualize your spending at a glance.**  
Our new **Expense Trends** graph helps users to stay on top of their daily spending by turning raw data into easy-to-read visuals. With just a glance, you can see how your expenses change day to day, track your habits, and spot unusual spikes.

**Why It's Useful** 

* **Patterns made simple**  
  Instead of scrolling through endless lists, users can quickly identify trends in their spending.

* **Better decisions, faster**  
  Seeing your habits visually helps you understand where your money goes, so you can make changes with confidence 

* **Stay consistent**  
  Daily trends encourage users to keep an eye on their habits, leading to better long-term financial behavior.

# **User Level and Achievements**

---

**Finance just got a whole lot more fun.**  
With our new **User Level and Achievements** system, every action you take, whether it’s logging an expense, setting a goal, or organizing your categories, helps you earn experience points (XP) and level up. It’s like leveling up in a game. Grow from a rookie to a master budgeter\!

**Why It's Useful**

* **Progress with purpose**  
  Users love seeing visible progress. That’s why we give you a level, XP bar, and unlockable achievements to keep you motivated.

* **Positive feedback loop**  
  Rewarding users with XP, gives users regular motivation to keep going.

* **Discover more of the app**  
  Achievements push users to explore things like budgeting or goal-setting they might’ve skipped.

# **Dark Mode**

---

**Comfort and customization, your way.**  
**Dark Mode** gives users the freedom to choose between light or dark themes based on their preferences and environment.

**Why It's Useful**

* **Reduces eye strain**  
  Perfect for low-light environments and night time use 

* **Modern and sleek**  
  Adds a polished, professional look that users appreciate

* **User-friendly**   
  A visually comfortable experience tailored to your viewing preference.



