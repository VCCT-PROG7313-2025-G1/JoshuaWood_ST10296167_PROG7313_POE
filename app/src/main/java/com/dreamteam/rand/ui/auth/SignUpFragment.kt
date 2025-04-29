package com.dreamteam.rand.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Patterns
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar
import android.animation.ObjectAnimator
import android.animation.AnimatorSet

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
    private var isRegistrationInProgress = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
        startFadeInAnimations()
    }

    private fun startFadeInAnimations() {
        // Create fade-in animators for each view
        val logoAnimator = ObjectAnimator.ofFloat(binding.logoImageView, "alpha", 0f, 1f).apply {
            duration = 500 // lasts 500 ms
            startDelay = 100 // starts after 100ms
        }


        // creates a fade in animation for the title text view
        val titleAnimator = ObjectAnimator.ofFloat(binding.titleTextView, "alpha", 0f, 1f).apply {
            duration = 500 // lasts 500 ms
            startDelay = 300 // starts after 300 ms
        }

        // Slower fade-in for text fields
        val nameAnimator = ObjectAnimator.ofFloat(binding.nameLayout, "alpha", 0f, 1f).apply {
            duration = 1000 // Slower duration
            startDelay = 500 // starts after 500 ms
        }

        val emailAnimator = ObjectAnimator.ofFloat(binding.emailLayout, "alpha", 0f, 1f).apply {
            duration = 1000 // Slower duration
            startDelay = 700 // starts after 700 ms
        }

        val passwordAnimator = ObjectAnimator.ofFloat(binding.passwordLayout, "alpha", 0f, 1f).apply {
            duration = 1000 // Slower duration
            startDelay = 900 // starts after 900 ms
        }

        val buttonAnimator = ObjectAnimator.ofFloat(binding.signUpButton, "alpha", 0f, 1f).apply {
            duration = 500 // short fade duration
            startDelay = 1100 // start after 1100ms
        }

        val signInLayoutAnimator = ObjectAnimator.ofFloat(binding.signInLayout, "alpha", 0f, 1f).apply {
            duration = 500 // short duration
            startDelay = 1300 // starts last in the sequence
        }

        // Play animations together
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
            start() // starts the animation sequence
        }
    }

    private fun setupClickListeners() {
        binding.signUpButton.setOnClickListener {
            if (validateInput()) {
                val name = binding.nameInput.text.toString().trim()
                val email = binding.emailInput.text.toString().trim()
                val password = binding.passwordInput.text.toString().trim()

                // Mark registration as in progress
                isRegistrationInProgress = true

                userViewModel.registerUser(name, email, password)

                // Show loading state
                binding.signUpButton.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
            }
        }

        binding.signInText.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null && isRegistrationInProgress) {
                // Reset flag first to prevent navigation loops
                isRegistrationInProgress = false

                try {
                    findNavController().navigate(R.id.action_signUp_to_dashboard)
                } catch (e: Exception) {
                    Snackbar.make(binding.root, "Registration successful but navigation failed", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        userViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                isRegistrationInProgress = false
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                // Reset loading state
                binding.signUpButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        val name = binding.nameInput.text.toString().trim()
        if (name.isEmpty()) {
            binding.nameLayout.error = getString(R.string.error_name_required)
            isValid = false
        } else {
            binding.nameLayout.error = null
        }

        val email = binding.emailInput.text.toString().trim()
        if (email.isEmpty()) {
            binding.emailLayout.error = getString(R.string.error_email_required)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        val password = binding.passwordInput.text.toString().trim()
        if (password.isEmpty()) {
            binding.passwordLayout.error = getString(R.string.error_password_required)
            isValid = false
        } else if (password.length < 6) {
            binding.passwordLayout.error = getString(R.string.error_password_too_short)
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}