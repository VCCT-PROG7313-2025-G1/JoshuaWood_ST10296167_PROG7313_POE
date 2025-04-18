package com.dreamteam.rand.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.ActivityMainBinding
import com.dreamteam.rand.ui.auth.UserViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    
    // Initialize the shared UserViewModel
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Configure top-level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.welcomeFragment, R.id.dashboardFragment)
        )

        // Observe authentication state and navigate accordingly
        userViewModel.currentUser.observe(this) { user ->
            val currentDest = navController.currentDestination ?: return@observe
            Log.d("MainActivity", "Auth state changed: user=${user?.name}, at=${currentDest.label}")
            
            if (user != null) {
                // User is logged in - move to dashboard if on auth screens
                handleLoggedInUser(currentDest)
            } else {
                // User is logged out - move to auth screens if needed
                handleLoggedOutUser(currentDest)
            }
        }
    }
    
    private fun handleLoggedInUser(currentDest: NavDestination) {
        val currentDestId = currentDest.id
        
        // Don't do anything if we're already at the dashboard
        if (currentDestId == R.id.dashboardFragment) {
            return
        }
        
        // Only handle known auth screens - welcome, signIn, signUp
        when (currentDestId) {
            R.id.signInFragment -> {
                try {
                    navController.navigate(R.id.action_signIn_to_dashboard)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Navigation error: ${e.message}")
                }
            }
            R.id.signUpFragment -> {
                try {
                    navController.navigate(R.id.action_signUp_to_dashboard)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Navigation error: ${e.message}")
                }
            }
            R.id.welcomeFragment -> {
                try {
                    navController.navigate(R.id.action_welcome_to_signIn)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Navigation error: ${e.message}")
                }
            }
        }
    }
    
    private fun handleLoggedOutUser(currentDest: NavDestination) {
        val currentDestId = currentDest.id
        
        // Don't do anything if we're already at an auth screen
        if (currentDestId == R.id.welcomeFragment || 
            currentDestId == R.id.signInFragment || 
            currentDestId == R.id.signUpFragment) {
            return
        }
        
        // Handle logout based on current screen
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
                    // For any other screen, try to navigate directly to welcome
                    navController.navigate(R.id.welcomeFragment)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Logout navigation error: ${e.message}")
            try {
                // Last resort: clear back stack and go to welcome
                navController.popBackStack()
                navController.navigate(R.id.welcomeFragment)
            } catch (e: Exception) {
                Log.e("MainActivity", "Critical navigation error: ${e.message}")
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
} 