package com.dreamteam.rand.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.FragmentAuthBaseBinding
import com.dreamteam.rand.databinding.ContentForgotPasswordBinding

// lets people reset their password if they forgot it
class ForgotPasswordFragment : Fragment() {
    private var _binding: FragmentAuthBaseBinding? = null
    private val binding get() = _binding!!
    private lateinit var forgotPasswordBinding: ContentForgotPasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // set up the screen title
        binding.titleTextView.text = "Forgot Password"
        
        // add the forgot password form
        forgotPasswordBinding = ContentForgotPasswordBinding.inflate(layoutInflater, binding.contentContainer, true)
        
        // set up what happens when they click the buttons
        forgotPasswordBinding.resetPasswordButton.setOnClickListener {
            val email = forgotPasswordBinding.emailEditText.text.toString()
            
            // check if they entered an email
            if (email.isBlank()) {
                Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // TODO: Implement actual password reset
            Toast.makeText(requireContext(), "Password reset link sent to $email", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
        }
        
        // let them go back to sign in
        forgotPasswordBinding.backToSignInTextView.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 