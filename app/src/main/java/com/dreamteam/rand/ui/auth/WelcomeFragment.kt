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

// this is the first screen people see when they open the app
class WelcomeFragment : Fragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    // grab the shared viewmodel that knows if someone's logged in
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
        // set up the buttons and make everything fade in nicely
        setupClickListeners()
        animateViewsStaggered()
    }

    // set up what happens when they click the buttons
    private fun setupClickListeners() {
        binding.signInButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_signIn)
        }

        binding.signUpButton.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_signUp)
        }
    }

    // I used ChatGPT and Grok to help me create the fade in animations for the Welcome Fragment class
    // the animateViewsStaggered function creates a staggered fade-in effect for UI elements
    // it creates a list of views to animate, a 500ms fade duration, a 150ms delay between each animation
    // it sets the alpha to 0f and the translationY to 40f to make the view invisible and set for the fade in effect.
    // Using ChatGPT and Grok I learnt how to create the fade in transition for the application.

    // make everything fade in one after another for a nice effect
    private fun animateViewsStaggered() {
        // list of things to animate
        val viewsToAnimate = listOf(
            binding.logoImageView,
            binding.taglineTextView,
            binding.signInButton,
            binding.signUpButton
        )

        // how long each animation takes and how long to wait between them
        val duration = 500L
        val delayBetweenItems = 150L

        // make each thing fade in and slide up
        viewsToAnimate.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 40f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(index * delayBetweenItems)
                .setDuration(duration)
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
