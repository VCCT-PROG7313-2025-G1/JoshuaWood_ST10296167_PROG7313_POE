package com.dreamteam.rand.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.FragmentSettingsBinding
import com.dreamteam.rand.ui.auth.UserViewModel

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
        setupToolbar()
        setupThemeSettings(view)
        setupNotificationsSwitch()
        observeUserViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupThemeSettings(view: View) {
        // Get current theme setting from SharedPreferences
        val sharedPrefs = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val currentTheme = sharedPrefs.getString("theme_mode", "system")
        
        // Find all the radio buttons
        val lightRadio = view.findViewById<RadioButton>(R.id.lightThemeRadio)
        val darkRadio = view.findViewById<RadioButton>(R.id.darkThemeRadio)
        val systemRadio = view.findViewById<RadioButton>(R.id.systemThemeRadio)
        val radioGroup = view.findViewById<RadioGroup>(R.id.themeRadioGroup)
        
        // Set the appropriate radio button based on the current theme
        when (currentTheme) {
            "light" -> lightRadio.isChecked = true
            "dark" -> darkRadio.isChecked = true
            "system" -> systemRadio.isChecked = true
        }
        
        // Set up radio group listener
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val themeMode = when (checkedId) {
                R.id.lightThemeRadio -> "light"
                R.id.darkThemeRadio -> "dark"
                R.id.systemThemeRadio -> "system"
                else -> "system"
            }
            
            // Save preference to SharedPreferences
            sharedPrefs.edit().putString("theme_mode", themeMode).apply()
            
            // Update theme in the ViewModel (for Firebase/Room)
            userViewModel.updateUserTheme(themeMode)
            
            // Apply the theme change
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
            if (user == null) {
                // Navigation will be handled by the MainActivity observer
                return@observe
            }
            
            // Update UI with user preferences
            binding.notificationsSwitch.isChecked = user.notificationsEnabled
        }
    }
    
    private fun applyTheme(themeMode: String) {
        when (themeMode) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 