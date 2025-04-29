package com.dreamteam.rand.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.FragmentSignInBinding
import com.google.android.material.snackbar.Snackbar

class SignInFragment : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
    private var isLoginInProgress = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAnimations()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupAnimations() {
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

    private fun setupClickListeners() {
        binding.signInButton.setOnClickListener {
            if (validateInput()) {
                val email = binding.emailInput.text.toString().trim()
                val password = binding.passwordInput.text.toString().trim()

                // Mark login as in progress
                isLoginInProgress = true

                userViewModel.loginUser(email, password)

                // Show loading state
                binding.signInButton.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
            }
        }

        binding.forgotPasswordText.setOnClickListener {
            findNavController().navigate(R.id.action_signIn_to_forgotPassword)
        }

        binding.signUpText.setOnClickListener {
            findNavController().navigate(R.id.action_signIn_to_signUp)
        }
    }

    private fun observeViewModel() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null && isLoginInProgress) {
                // Reset flag first to prevent navigation loops
                isLoginInProgress = false

                try {
                    findNavController().navigate(R.id.action_signIn_to_dashboard)
                } catch (e: Exception) {
                    Snackbar.make(binding.root, "Login successful but navigation failed", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        userViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                isLoginInProgress = false
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                // Reset loading state
                binding.signInButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        val email = binding.emailInput.text.toString().trim()
        if (email.isEmpty()) {
            binding.emailLayout.error = getString(R.string.error_email_required)
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        val password = binding.passwordInput.text.toString().trim()
        if (password.isEmpty()) {
            binding.passwordLayout.error = getString(R.string.error_password_required)
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