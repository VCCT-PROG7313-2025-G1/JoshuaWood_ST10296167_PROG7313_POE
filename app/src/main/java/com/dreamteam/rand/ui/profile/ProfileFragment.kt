package com.dreamteam.rand.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.FragmentProfileBinding
import com.dreamteam.rand.ui.auth.UserViewModel

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by viewModels()

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
                findNavController().navigate(R.id.action_profile_to_welcome)
                return@observe
            }

            binding.nameText.text = user.name
            binding.emailText.text = user.email
            binding.levelText.text = "Level ${user.level}"
            binding.xpText.text = "${user.xp} XP"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 