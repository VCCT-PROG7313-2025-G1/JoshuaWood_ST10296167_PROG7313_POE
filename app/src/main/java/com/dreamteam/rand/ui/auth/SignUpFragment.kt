package com.dreamteam.rand.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Patterns
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar
import android.animation.ObjectAnimator
import android.animation.AnimatorSet

// lets people create a new account with their name, email, and password
class SignUpFragment : Fragment() {
    private val TAG = "SignUpFragment"
    
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    // grab the shared viewmodel that handles signing up
    private val userViewModel: UserViewModel by activityViewModels()
    // keep track of if we're already trying to sign up
    private var isRegistrationInProgress = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Creating sign up view")
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Setting up sign up view")
        // set up the buttons and make everything fade in nicely
        setupClickListeners()
        observeViewModel()
        startFadeInAnimations()
    }

    // make everything fade in one after another for a nice effect
    private fun startFadeInAnimations() {
        Log.d(TAG, "Starting fade-in animations")
        // make the logo fade in first
        val logoAnimator = ObjectAnimator.ofFloat(binding.logoImageView, "alpha", 0f, 1f).apply {
            duration = 500
            startDelay = 100
        }

        // then the title
        val titleAnimator = ObjectAnimator.ofFloat(binding.titleTextView, "alpha", 0f, 1f).apply {
            duration = 500
            startDelay = 300
        }

        // then the text fields one by one
        val nameAnimator = ObjectAnimator.ofFloat(binding.nameLayout, "alpha", 0f, 1f).apply {
            duration = 1000
            startDelay = 500
        }

        val emailAnimator = ObjectAnimator.ofFloat(binding.emailLayout, "alpha", 0f, 1f).apply {
            duration = 1000
            startDelay = 700
        }

        val passwordAnimator = ObjectAnimator.ofFloat(binding.passwordLayout, "alpha", 0f, 1f).apply {
            duration = 1000
            startDelay = 900
        }

        // then the sign up button
        val buttonAnimator = ObjectAnimator.ofFloat(binding.signUpButton, "alpha", 0f, 1f).apply {
            duration = 500
            startDelay = 1100
        }

        // and finally the sign in link
        val signInLayoutAnimator = ObjectAnimator.ofFloat(binding.signInLayout, "alpha", 0f, 1f).apply {
            duration = 500
            startDelay = 1300
        }

        // play all the animations together
        AnimatorSet().apply {
            playTogether(
                logoAnimator,
                titleAnimator,
                nameAnimator,
                emailAnimator,
                passwordAnimator,
                buttonAnimator,
                signInLayoutAnimator
            )
            start()
        }
    }

    // set up what happens when they click the buttons
    private fun setupClickListeners() {
        Log.d(TAG, "Setting up click listeners")
        binding.signUpButton.setOnClickListener {
            Log.d(TAG, "Sign up button clicked")
            // check if they filled everything out right
            if (validateInput()) {
                val name = binding.nameInput.text.toString().trim()
                val email = binding.emailInput.text.toString().trim()
                val password = binding.passwordInput.text.toString().trim()

                Log.d(TAG, "Starting registration process for user: $email")
                // remember we're trying to sign up
                isRegistrationInProgress = true

                // try to create their account
                userViewModel.registerUser(name, email, password)

                // show the loading spinner
                binding.signUpButton.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
            } else {
                Log.w(TAG, "Registration validation failed")
            }
        }

        binding.signInText.setOnClickListener {
            Log.d(TAG, "Navigating to sign in screen")
            findNavController().navigateUp()
        }
    }

    // watch for when they successfully sign up or if there's an error
    private fun observeViewModel() {
        Log.d(TAG, "Setting up ViewModel observers")
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null && isRegistrationInProgress) {
                Log.d(TAG, "Registration successful for user: ${user.email}")
                // reset the flag so we don't try to navigate again
                isRegistrationInProgress = false

                try {
                    Log.d(TAG, "Navigating to dashboard")
                    findNavController().navigate(R.id.action_signUp_to_dashboard)
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation failed after registration: ${e.message}")
                    Snackbar.make(binding.root, "Registration successful but navigation failed", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        userViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Log.e(TAG, "Registration error: $it")
                isRegistrationInProgress = false
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                // hide the loading spinner
                binding.signUpButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    // check if they filled out the form right
    private fun validateInput(): Boolean {
        Log.d(TAG, "Validating user input")
        var isValid = true

        val name = binding.nameInput.text.toString().trim()
        if (name.isEmpty()) {
            Log.w(TAG, "Name validation failed: empty")
            binding.nameLayout.error = getString(R.string.error_name_required)
            isValid = false
        } else {
            binding.nameLayout.error = null
        }

        val email = binding.emailInput.text.toString().trim()
        if (email.isEmpty()) {
            Log.w(TAG, "Email validation failed: empty")
            binding.emailLayout.error = getString(R.string.error_email_required)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.w(TAG, "Email validation failed: invalid format")
            binding.emailLayout.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        val password = binding.passwordInput.text.toString().trim()
        if (password.isEmpty()) {
            Log.w(TAG, "Password validation failed: empty")
            binding.passwordLayout.error = getString(R.string.error_password_required)
            isValid = false
        } else if (password.length < 6) {
            Log.w(TAG, "Password validation failed: too short")
            binding.passwordLayout.error = getString(R.string.error_password_too_short)
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        return isValid
    }

    override fun onDestroyView() {
        Log.d(TAG, "Destroying sign up view")
        super.onDestroyView()
        _binding = null
    }
}