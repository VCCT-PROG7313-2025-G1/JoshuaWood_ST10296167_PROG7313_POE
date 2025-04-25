//package com.dreamteam.rand.ui.auth
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.util.Patterns
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.fragment.app.activityViewModels
//import androidx.navigation.fragment.findNavController
//import com.dreamteam.rand.R
//import com.dreamteam.rand.databinding.FragmentSignUpBinding
//import com.google.android.material.snackbar.Snackbar
//
//class SignUpFragment : Fragment() {
//
//    private var _binding: FragmentSignUpBinding? = null
//    private val binding get() = _binding!!
//    private val userViewModel: UserViewModel by activityViewModels()
//    private var isRegistrationInProgress = false
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        setupClickListeners()
//        observeViewModel()
//    }
//
//    private fun setupClickListeners() {
//        binding.signUpButton.setOnClickListener {
//            if (validateInput()) {
//                val name = binding.nameInput.text.toString().trim()
//                val email = binding.emailInput.text.toString().trim()
//                val password = binding.passwordInput.text.toString().trim()
//
//                // Mark registration as in progress
//                isRegistrationInProgress = true
//
//                userViewModel.registerUser(name, email, password)
//
//                // Show loading state
//                binding.signUpButton.isEnabled = false
//                binding.progressBar.visibility = View.VISIBLE
//            }
//        }
//
//        binding.signInText.setOnClickListener {
//            findNavController().navigateUp()
//        }
//    }
//
//    private fun observeViewModel() {
//        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
//            if (user != null && isRegistrationInProgress) {
//                // Reset flag first to prevent navigation loops
//                isRegistrationInProgress = false
//
//                try {
//                    findNavController().navigate(R.id.action_signUp_to_dashboard)
//                } catch (e: Exception) {
//                    Snackbar.make(binding.root, "Registration successful but navigation failed", Snackbar.LENGTH_LONG).show()
//                }
//            }
//        }
//
//        userViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
//            errorMessage?.let {
//                isRegistrationInProgress = false
//                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
//                // Reset loading state
//                binding.signUpButton.isEnabled = true
//                binding.progressBar.visibility = View.GONE
//            }
//        }
//    }
//
//    private fun validateInput(): Boolean {
//        var isValid = true
//
//        val name = binding.nameInput.text.toString().trim()
//        if (name.isEmpty()) {
//            binding.nameLayout.error = getString(R.string.error_name_required)
//            isValid = false
//        } else {
//            binding.nameLayout.error = null
//        }
//
//        val email = binding.emailInput.text.toString().trim()
//        if (email.isEmpty()) {
//            binding.emailLayout.error = getString(R.string.error_email_required)
//            isValid = false
//        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            binding.emailLayout.error = getString(R.string.error_invalid_email)
//            isValid = false
//        } else {
//            binding.emailLayout.error = null
//        }
//
//        val password = binding.passwordInput.text.toString().trim()
//        if (password.isEmpty()) {
//            binding.passwordLayout.error = getString(R.string.error_password_required)
//            isValid = false
//        } else if (password.length < 6) {
//            binding.passwordLayout.error = getString(R.string.error_password_too_short)
//            isValid = false
//        } else {
//            binding.passwordLayout.error = null
//        }
//
//        return isValid
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}

package com.dreamteam.rand.ui.auth

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Patterns
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.FragmentSignUpBinding
import com.google.android.material.snackbar.Snackbar

// SignUpFragment handles the user registration UI and logic, including input validation,
// registration with a ViewModel, navigation, and button animations.
class SignUpFragment : Fragment() {

    // View binding for the sign-up fragment layout, nullable to prevent memory leaks
    private var _binding: FragmentSignUpBinding? = null
    // Non-nullable binding getter for safe access after initialization
    private val binding get() = _binding!!
    // Shared ViewModel to handle user registration logic and state
    private val userViewModel: UserViewModel by activityViewModels()
    // Flag to track if a registration is in progress to prevent navigation loops
    private var isRegistrationInProgress = false

    // Inflates the fragment's layout and returns the root view
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using view binding
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Sets up the fragment after the view is created, initializing click listeners and ViewModel observers
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set up click listeners for the sign-up button and sign-in text
        setupClickListeners()
        // Observe ViewModel LiveData for user registration state and errors
        observeViewModel()
    }

    // Configures click listeners for the sign-up button and sign-in text
    private fun setupClickListeners() {
        // Handle click on the "Sign Up" button
        binding.signUpButton.setOnClickListener {
            // Play a scale animation on the button for a subtle press effect
            animateButton()

            // Validate user input before proceeding with registration
            if (validateInput()) {
                // Extract trimmed input values
                val name = binding.nameInput.text.toString().trim()
                val email = binding.emailInput.text.toString().trim()
                val password = binding.passwordInput.text.toString().trim()

                // Mark registration as in progress to prevent multiple submissions
                isRegistrationInProgress = true

                // Call the ViewModel to register the user with the provided details
                userViewModel.registerUser(name, email, password)

                // Show loading state by disabling the button and showing the progress bar
                binding.signUpButton.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
            }
        }

        // Handle click on the "Sign In" text to navigate back to the sign-in screen
        binding.signInText.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    // Applies a scale animation to the "Sign Up" button to create a press effect
    private fun animateButton() {
        // Create a scale animation for the X-axis (scale down to 95% and back to 100%)
        val scaleX = ObjectAnimator.ofFloat(binding.signUpButton, "scaleX", 1f, 0.95f, 1f)
        // Create a scale animation for the Y-axis (scale down to 95% and back to 100%)
        val scaleY = ObjectAnimator.ofFloat(binding.signUpButton, "scaleY", 1f, 0.95f, 1f)
        // Set the animation duration to 200 milliseconds for a quick, subtle effect
        scaleX.duration = 200
        scaleY.duration = 200
        // Start the animations
        scaleX.start()
        scaleY.start()
    }

    // Observes the ViewModel's LiveData for user registration state and errors
    private fun observeViewModel() {
        // Observe the currentUser LiveData to check if registration was successful
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null && isRegistrationInProgress) {
                // Reset the flag to prevent navigation loops
                isRegistrationInProgress = false

                try {
                    // Navigate to the dashboard screen after successful registration
                    findNavController().navigate(R.id.action_signUp_to_dashboard)
                } catch (e: Exception) {
                    // Show a snackbar if navigation fails
                    Snackbar.make(binding.root, "Registration successful but navigation failed", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        // Observe the error LiveData to display any registration errors
        userViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                // Reset the registration flag
                isRegistrationInProgress = false
                // Show the error message in a snackbar
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                // Reset the loading state by enabling the button and hiding the progress bar
                binding.signUpButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    // Validates user input (name, email, and password) before registration
    private fun validateInput(): Boolean {
        var isValid = true

        // Validate name input
        val name = binding.nameInput.text.toString().trim()
        if (name.isEmpty()) {
            // Show an error if the name field is empty
            binding.nameLayout.error = getString(R.string.error_name_required)
            isValid = false
        } else {
            // Clear any existing error
            binding.nameLayout.error = null
        }

        // Validate email input
        val email = binding.emailInput.text.toString().trim()
        if (email.isEmpty()) {
            // Show an error if the email field is empty
            binding.emailLayout.error = getString(R.string.error_email_required)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Show an error if the email format is invalid
            binding.emailLayout.error = getString(R.string.error_invalid_email)
            isValid = false
        } else {
            // Clear any existing error
            binding.emailLayout.error = null
        }

        // Validate password input
        val password = binding.passwordInput.text.toString().trim()
        if (password.isEmpty()) {
            // Show an error if the password field is empty
            binding.passwordLayout.error = getString(R.string.error_password_required)
            isValid = false
        } else if (password.length < 6) {
            // Show an error if the password is too short (less than 6 characters)
            binding.passwordLayout.error = getString(R.string.error_password_too_short)
            isValid = false
        } else {
            // Clear any existing error
            binding.passwordLayout.error = null
        }

        // Return true if all inputs are valid, false otherwise
        return isValid
    }

    // Cleans up resources when the fragment's view is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        // Set binding to null to prevent memory leaks
        _binding = null
    }
}