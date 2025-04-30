package com.dreamteam.rand.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.FragmentProfileBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import android.view.animation.AlphaAnimation

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        observeUserData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeUserData() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                // Navigation will be handled by the MainActivity observer
                return@observe
            }

            // Set user data
            binding.nameText.text = user.name
            binding.emailText.text = user.email
            binding.levelText.text = "Level ${user.level}"
            binding.xpText.text = "${user.xp} XP"

            // Apply staggered fade-in animation to text views
            applyStaggeredFadeInAnimation()
        }
    }

    // Made use of ChatGPT and Grok to make the fade in animation for the Profile screen.
    // val viewsToAnimate = listOf() is used to create a list of views to animate.
    //  viewToAnimate -iterates through views to apply fade-in animation with staggered delays
    // the val fadeIn = AlphaAnimation is used to create a fade-in animation from invisible (alpha 0) to fully visible (alpha 1)
    // the duration is set to 600 milliseconds (0.6 seconds)
    // the startOffset is used to control the delay between animations
    // the fillAfter = true is used to keep the view visible after the animation
    // With the use of ChatGPT and Grok I learnt how to create the fade in animation for the Profile Screen and how to create the animator to create the transition effects.

    private fun applyStaggeredFadeInAnimation() {
        // List of views to animate
        val viewsToAnimate = listOf(
            binding.nameText,
            binding.emailText,
            binding.levelText,
            binding.xpText
        )

        viewsToAnimate.forEachIndexed { index, view ->
            val fadeIn = AlphaAnimation(0f, 1f).apply {
                duration = 600 // Duration of the fade-in effect (in milliseconds)
                startOffset = (index * 290).toLong() // Stagger delay (290ms per view)
                fillAfter = true // Keep the view visible after animation
            }
            view.startAnimation(fadeIn)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}