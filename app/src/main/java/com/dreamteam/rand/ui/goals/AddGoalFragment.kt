package com.dreamteam.rand.ui.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.data.RandDatabase
import com.dreamteam.rand.data.repository.GoalRepository
import com.dreamteam.rand.databinding.FragmentAddGoalBinding
import com.dreamteam.rand.ui.auth.UserViewModel

class AddGoalFragment : Fragment() {

    private var _binding: FragmentAddGoalBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()
    private val goalViewModel: GoalViewModel by viewModels {
        val database = RandDatabase.getDatabase(requireContext())
        val repository = GoalRepository(database.goalDao())
        GoalViewModel.Factory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}