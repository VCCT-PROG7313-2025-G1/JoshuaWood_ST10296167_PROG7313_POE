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

// handles user authentication with animations and validation
class SignInFragment : Fragment() {
    private val TAG = "SignInFragment"
    
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Setting up sign in view")
        setupAnimations()
        setupClickListeners()
        observeViewModel()
    }

    // sets up a sequence of fade-in and translation animations
    private fun setupAnimations() {
        Log.d(TAG, "Setting up animations")
        // Define animation parameters
        val fadeDuration = 500L // Duration of each fade-in
        val translationDistance = -20f // Slight upward translation
        val staggerDelay = 200L // Delay between each element

        // List of views to animate with their respective start delays
        val viewsToAnimate = listOf(
            binding.logoImageView to 0L, // Initial delay is 0
            binding.titleTextView to staggerDelay, // Delay for the first view
            binding.cardView to staggerDelay * 2, // Delay for the second view
            binding.emailLayout to staggerDelay * 3, // Delay for the third view ... etc
            binding.passwordLayout to staggerDelay * 4,
            binding.signInButton to staggerDelay * 5,
            binding.forgotPasswordText to staggerDelay * 6,
            binding.signUpPromptLayout to staggerDelay * 7
        )

        // Apply fade-in and translation animation to each view
        viewsToAnimate.forEach { (view, delay) ->
            view.apply {
                alpha = 0f // Initial alpha
                translationY = translationDistance // Initial translation
                animate() // Start the animation
                    .alpha(1f)
                    .translationY(0f) // Fade-in and translation
                    .setDuration(fadeDuration) // Duration of the animation
                    .setStartDelay(delay)
                    .start() // Start the animation
            }
        }
    }

    // sets up click listeners for sign in button and navigation options
    private fun setupClickListeners() {
        Log.d(TAG, "Setting up click listeners")
        binding.signInButton.setOnClickListener {
            Log.d(TAG, "Sign in button clicked")
            if (validateInput()) {
                val email = binding.emailInput.text.toString().trim()
                val password = binding.passwordInput.text.toString().trim()

                Log.d(TAG, "Starting login process for user: $email")
                // Mark login as in progress
                isLoginInProgress = true

                userViewModel.loginUser(email, password)

                // Show loading state
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

    // observes ViewModel for login results and errors
    private fun observeViewModel() {
        Log.d(TAG, "Setting up ViewModel observers")
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null && isLoginInProgress) {
                Log.d(TAG, "Login successful for user: ${user.email}")
                // Reset flag first to prevent navigation loops
                isLoginInProgress = false

                try {
                    Log.d(TAG, "Navigating to dashboard")
                    findNavController().navigate(R.id.action_signIn_to_dashboard)
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation failed after login: ${e.message}")
                    Snackbar.make(binding.root, "Login successful but navigation failed", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        userViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Log.e(TAG, "Login error: $it")
                isLoginInProgress = false
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                // Reset loading state
                binding.signInButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    // validates user input before login
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