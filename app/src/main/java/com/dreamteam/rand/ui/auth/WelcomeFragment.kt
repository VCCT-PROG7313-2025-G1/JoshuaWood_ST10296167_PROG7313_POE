package com.dreamteam.rand.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        animateViewsStaggered() // Call to start the staggered animation
    }

    private fun setupClickListeners() {
        binding.signInButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_signIn)
        }

        binding.signUpButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_signUp)
        }
    }
// staggered fade in animation that is created when the onViewCreated method is called
    // displays the logo, app name, tagline, and two buttons in a slow sequential sequence for a transition effect
    private fun animateViewsStaggered() { // Staggered animation
        val viewsToAnimate = listOf(
            binding.logoImageView,
            binding.taglineTextView,
            binding.signInButton,
            binding.signUpButton
        )

        val duration = 500L // Duration of each animation
        val delayBetweenItems = 150L // Delay between each item

        viewsToAnimate.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 40f // Initial translation
            view.animate()
                .alpha(1f)
                .translationY(0f) // Fade-in and translation
                .setStartDelay(index * delayBetweenItems) // Delay based on position
                .setDuration(duration)
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
