package com.dreamteam.rand.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.ActivityMainBinding
import com.dreamteam.rand.ui.auth.UserViewModel

// this is the main activity that handles the whole app's navigation and auth state
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    
    // grab the shared viewmodel that knows if someone's logged in
    private val userViewModel: UserViewModel by viewModels()

    private var syncObserver: Observer<Boolean>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set up the navigation system
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // tell the app which screens are at the top level
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.welcomeFragment, R.id.dashboardFragment)
        )

        // watch for when someone logs in or out
        userViewModel.currentUser.observe(this) { user ->
            val currentDest = navController.currentDestination ?: return@observe
            Log.d("MainActivity", "Auth state changed: user=${user?.name}, at=${currentDest.label}")
            
            if (user != null) {
                // they're logged in, make sure they're not stuck on login screens
                handleLoggedInUser(currentDest)
            } else {
                // they're logged out, send them to the login screens
                handleLoggedOutUser(currentDest)
            }
        }
    }
    
    // handle what happens when someone logs in
    private fun handleLoggedInUser(currentDest: NavDestination) {
        val currentDestId = currentDest.id
        
        // if they're already at the dashboard, we're good
        if (currentDestId == R.id.dashboardFragment) {
            Log.d("MainActivity", "User already at dashboard, no navigation needed")
            return
        }
        
        // figure out which login screen they're on and send them to the right place
        when (currentDestId) {
            R.id.signInFragment -> {
                if (syncObserver != null) {
                    userViewModel.syncCompleted.removeObserver(syncObserver!!)
                    syncObserver = null
                }
                syncObserver = Observer<Boolean> { isSynced ->
                    if (isSynced == true) {
                        Log.d("MainActivity", "Sync complete. Navigating from signIn to dashboard")
                        userViewModel.clearSyncFlag() // prevent double nav
                        userViewModel.syncCompleted.removeObserver(syncObserver!!)
                        syncObserver = null
                        try {
                            navController.navigate(R.id.action_signIn_to_dashboard)
                        } catch (e: Exception) {
                            Log.e("MainActivity", "Navigation error: ${e.message}")
                            try {
                                navController.navigate(R.id.dashboardFragment)
                            } catch (e2: Exception) {
                                Log.e("MainActivity", "Fallback navigation error: ${e2.message}")
                            }
                        }
                    }
                }
                userViewModel.syncCompleted.observe(this, syncObserver!!)
            }
            R.id.signUpFragment -> {
                try {
                    Log.d("MainActivity", "Navigating from signUp to dashboard")
                    navController.navigate(R.id.action_signUp_to_dashboard)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Navigation error from signUp to dashboard: ${e.message}")
                    // If navigation fails, try a safer approach with a simple navigate call
                    try {
                        navController.navigate(R.id.dashboardFragment)
                    } catch (e2: Exception) {
                        Log.e("MainActivity", "Critical navigation error: ${e2.message}")
                    }
                }
            }
            R.id.welcomeFragment -> {
                try {
                    Log.d("MainActivity", "Navigating from welcome to signIn")
                    navController.navigate(R.id.action_welcome_to_signIn)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Navigation error from welcome to signIn: ${e.message}")
                }
            }
        }
    }
    
    // handle what happens when someone logs out
    private fun handleLoggedOutUser(currentDest: NavDestination) {
        val currentDestId = currentDest.id
        
        // if they're already on a login screen, we're good
        if (currentDestId == R.id.welcomeFragment || 
            currentDestId == R.id.signInFragment || 
            currentDestId == R.id.signUpFragment) {
            return
        }
        
        // figure out where they are and send them to the welcome screen
        try {
            when (currentDestId) {
                R.id.dashboardFragment -> {
                    navController.navigate(R.id.action_dashboard_to_welcome)
                }
                R.id.settingsFragment -> {
                    navController.navigate(R.id.action_settings_to_welcome)
                }
                R.id.profileFragment -> {
                    navController.navigate(R.id.action_profile_to_welcome)
                }
                else -> {
                    // if we don't know where they are, just go to welcome
                    navController.navigate(R.id.welcomeFragment)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Logout navigation error: ${e.message}")
            try {
                // if all else fails, clear everything and go to welcome
                navController.popBackStack()
                navController.navigate(R.id.welcomeFragment)
            } catch (e: Exception) {
                Log.e("MainActivity", "Critical navigation error: ${e.message}")
            }
        }
    }

    // handle the back button in the toolbar
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        syncObserver?.also {
            userViewModel.syncCompleted.removeObserver(it)
        }
        super.onDestroy()
    }
} 