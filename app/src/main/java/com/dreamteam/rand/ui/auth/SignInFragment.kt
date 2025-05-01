package com.dreamteam.rand.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.FragmentSignInBinding
import com.google.android.material.snackbar.Snackbar

// lets people sign in to their account with email and password
class SignInFragment : Fragment() {
    private val TAG = "SignInFragment"
    
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    // grab the shared viewmodel that handles signing in
    private val userViewModel: UserViewModel by activityViewModels()
    // keep track of if we're already trying to sign in
    private var isLoginInProgress = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Creating sign in view")
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    // I made used of ChatGPT and Grok to create the fade in animations for the Sign In Fragment class
    //  the onViewCreated function in this Android fragment initializes the sign-in screen
    // The setupAnimations function creates the fade in effect for UI elements
    // It defines a 500ms fade duration, a -20-pixel upward slide and a 200ms delay between each animation
    // Each animation is initalized at 0f alpha and 0f translationY to make the view invisible and set for the fade in effect.
    // Using ChatGPT and Grok it  helped me created the fade in transition for the application.

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Setting up sign in view")
        // make everything fade in nicely and set up the buttons
        setupAnimations()
        setupClickListeners()
        observeViewModel()
    }

    // made use of chatgpt and grok to help create the fade-in animations
    // the animations create a smooth entrance effect with staggered timing
    // each ui element fades in and slides up with a 200ms delay between them
    private fun setupAnimations() {
        Log.d(TAG, "Setting up animations")
        // how long each animation takes and how far things move
        val fadeDuration = 500L
        val translationDistance = -20f
        val staggerDelay = 200L

        // list of things to animate and when to start each one
        val viewsToAnimate = listOf(
            binding.logoImageView to 0L,
            binding.titleTextView to staggerDelay,
            binding.cardView to staggerDelay * 2,
            binding.emailLayout to staggerDelay * 3,
            binding.passwordLayout to staggerDelay * 4,
            binding.signInButton to staggerDelay * 5,
            binding.forgotPasswordText to staggerDelay * 6,
            binding.signUpPromptLayout to staggerDelay * 7
        )

        // make each thing fade in and slide up
        viewsToAnimate.forEach { (view, delay) ->
            view.apply {
                alpha = 0f
                translationY = translationDistance
                animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(fadeDuration)
                    .setStartDelay(delay)
                    .start()
            }
        }
    }

    // set up what happens when they click the buttons
    private fun setupClickListeners() {
        Log.d(TAG, "Setting up click listeners")
        binding.signInButton.setOnClickListener {
            Log.d(TAG, "Sign in button clicked")
            // check if they filled everything out right
            if (validateInput()) {
                val email = binding.emailInput.text.toString().trim()
                val password = binding.passwordInput.text.toString().trim()

                Log.d(TAG, "Starting login process for user: $email")
                // remember we're trying to sign in
                isLoginInProgress = true

                // try to sign them in
                userViewModel.loginUser(email, password)

                // show the loading spinner
                binding.signInButton.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
            } else {
                Log.w(TAG, "Login validation failed")
            }
        }

        binding.forgotPasswordText.setOnClickListener {
            Log.d(TAG, "Navigating to forgot password screen")
            findNavController().navigate(R.id.action_signIn_to_forgotPassword)
        }

        binding.signUpText.setOnClickListener {
            Log.d(TAG, "Navigating to sign up screen")
            findNavController().navigate(R.id.action_signIn_to_signUp)
        }
    }

    // watch for when they successfully sign in or if there's an error
    private fun observeViewModel() {
        Log.d(TAG, "Setting up ViewModel observers")
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null && isLoginInProgress) {
                Log.d(TAG, "Login successful for user: ${user.email}")
                // reset the flag so we don't try to navigate again
                isLoginInProgress = false

                // Ensure we're still on the SignInFragment before navigating
                if (!isAdded || view == null) {
                    Log.d(TAG, "Fragment no longer attached, skipping navigation")
                    return@observe
                }

                try {
                    // Check if we're on the sign-in fragment before navigating
                    val currentDestId = findNavController().currentDestination?.id
                    if (currentDestId == R.id.signInFragment) {
                        Log.d(TAG, "Navigating to dashboard")
                        findNavController().navigate(R.id.action_signIn_to_dashboard)
                    } else if (currentDestId == R.id.dashboardFragment) {
                        Log.d(TAG, "Already on dashboard, no navigation needed")
                    } else {
                        Log.d(TAG, "Not on signInFragment (current: $currentDestId), using safe navigation")
                        // If on an unexpected screen, navigate directly to dashboard
                        try {
                            findNavController().navigate(R.id.dashboardFragment)
                        } catch (e: Exception) {
                            Log.e(TAG, "Safe navigation failed: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation failed after login: ${e.message}")
                    if (isAdded && view != null) {
                        Snackbar.make(binding.root, "Login successful but navigation failed", Snackbar.LENGTH_LONG).show()
                    }
                }
                
                // Always make sure to hide the loading spinner
                if (isAdded && view != null) {
                    binding.signInButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        userViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Log.e(TAG, "Login error: $it")
                isLoginInProgress = false
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                // hide the loading spinner
                binding.signInButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    // check if they filled out the form right
    private fun validateInput(): Boolean {
        Log.d(TAG, "Validating user input")
        var isValid = true

        val email = binding.emailInput.text.toString().trim()
        if (email.isEmpty()) {
            Log.w(TAG, "Email validation failed: empty")
            binding.emailLayout.error = getString(R.string.error_email_required)
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        val password = binding.passwordInput.text.toString().trim()
        if (password.isEmpty()) {
            Log.w(TAG, "Password validation failed: empty")
            binding.passwordLayout.error = getString(R.string.error_password_required)
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        return isValid
    }

    override fun onDestroyView() {
        Log.d(TAG, "Destroying sign in view")
        super.onDestroyView()
        _binding = null
    }
}