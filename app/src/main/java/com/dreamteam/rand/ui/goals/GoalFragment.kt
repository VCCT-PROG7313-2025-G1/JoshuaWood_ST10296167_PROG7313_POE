package com.dreamteam.rand.ui.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dreamteam.rand.R
import com.dreamteam.rand.data.RandDatabase
import com.dreamteam.rand.data.repository.GoalRepository
import com.dreamteam.rand.databinding.FragmentGoalBinding
import com.dreamteam.rand.ui.auth.UserViewModel

class GoalFragment : Fragment() {
    private var _binding: FragmentGoalBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var goalAdapter: GoalAdapter

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
        _binding = FragmentGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        goalAdapter = GoalAdapter { goal ->
            // Edit goal functionality will be implemented later
        }

        binding.goalsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = goalAdapter
        }
    }

    private fun setupClickListeners() {
        binding.addGoalFab.setOnClickListener {
            findNavController().navigate(R.id.action_goal_to_addGoal)
        }

        binding.addFirstGoalBtn.setOnClickListener {
            findNavController().navigate(R.id.action_goal_to_addGoal)
        }
    }

    private fun observeViewModel() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                findNavController().navigate(R.id.action_dashboard_to_welcome)
                return@observe
            }

            // Load all goals ordered by year and month
            loadGoals(user.uid)
        }
    }

    private fun loadGoals(userId: String) {
        goalViewModel.getAllGoalsOrdered(userId).observe(viewLifecycleOwner) { goals ->
            if (goals.isEmpty()) {
                binding.emptyStateContainer.visibility = View.VISIBLE
                binding.goalsContainer.visibility = View.GONE
                binding.headerSection.visibility = View.GONE
            } else {
                binding.emptyStateContainer.visibility = View.GONE
                binding.goalsContainer.visibility = View.VISIBLE
                binding.headerSection.visibility = View.VISIBLE

                // Update goal count badge
                binding.goalCountText.text = goals.size.toString()

                // Update the adapter
                goalAdapter.submitList(goals)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}