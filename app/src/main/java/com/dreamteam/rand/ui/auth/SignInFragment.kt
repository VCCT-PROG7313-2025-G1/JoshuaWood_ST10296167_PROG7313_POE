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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Setting up sign in view")
        // make everything fade in nicely and set up the buttons
        setupAnimations()
        setupClickListeners()
        observeViewModel()
    }

    // make everything fade in one after another for a nice effect
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