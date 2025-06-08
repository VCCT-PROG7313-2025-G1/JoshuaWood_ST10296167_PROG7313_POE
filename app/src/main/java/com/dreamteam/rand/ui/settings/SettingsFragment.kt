package com.dreamteam.rand.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.FragmentSettingsBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import com.dreamteam.rand.ui.common.ViewUtils

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply dynamic gradient to toolbar
       // ViewUtils.setToolbarGradient(this, binding.toolbar)

        setupToolbar()
        setupThemeSettings(view)
        setupNotificationsSwitch()
        observeUserViewModel()

        // Apply staggered fade-in
        setupStaggeredFadeInAnimation()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupThemeSettings(view: View) {
        val sharedPrefs = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val currentTheme = sharedPrefs.getString("theme_mode", "system")

        val lightRadio = view.findViewById<RadioButton>(R.id.lightThemeRadio)
        val darkRadio = view.findViewById<RadioButton>(R.id.darkThemeRadio)
        val systemRadio = view.findViewById<RadioButton>(R.id.systemThemeRadio)
        val radioGroup = view.findViewById<RadioGroup>(R.id.themeRadioGroup)

        when (currentTheme) {
            "light" -> lightRadio.isChecked = true
            "dark" -> darkRadio.isChecked = true
            "system" -> systemRadio.isChecked = true
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val themeMode = when (checkedId) {
                R.id.lightThemeRadio -> "light"
                R.id.darkThemeRadio -> "dark"
                R.id.systemThemeRadio -> "system"
                else -> "system"
            }

            sharedPrefs.edit().putString("theme_mode", themeMode).apply()
            userViewModel.updateUserTheme(themeMode)
            applyTheme(themeMode)
        }
    }

    private fun setupNotificationsSwitch() {
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            userViewModel.updateNotificationSettings(isChecked)
        }
    }

    private fun observeUserViewModel() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) return@observe
            binding.notificationsSwitch.isChecked = user.notificationsEnabled
        }
    }

    private fun applyTheme(themeMode: String) {
        when (themeMode) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        // Re-apply toolbar gradient after theme change
        ViewUtils.setToolbarGradient(this, binding.toolbar)
    }

    private fun setupStaggeredFadeInAnimation() {
        val rootViewGroup = binding.root as ViewGroup
        val animationDuration = 500L
        val delayBetweenItems = 400L

        for (i in 0 until rootViewGroup.childCount) {
            val child = rootViewGroup.getChildAt(i)
            child.alpha = 0f
            child.animate()
                .alpha(1f)
                .setStartDelay(i * delayBetweenItems)
                .setDuration(animationDuration)
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
