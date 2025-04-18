package com.dreamteam.rand.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
        observeUserViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeUserViewModel() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                // Navigation will be handled by the MainActivity observer
                return@observe
            }
            
            // Update settings UI with user preferences
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 