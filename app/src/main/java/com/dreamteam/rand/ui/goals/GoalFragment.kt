package com.dreamteam.rand.ui.goals

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dreamteam.rand.R
import com.dreamteam.rand.data.entity.Goal
import com.dreamteam.rand.data.RandDatabase
import com.dreamteam.rand.data.repository.GoalRepository
import com.dreamteam.rand.databinding.FragmentGoalBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import com.dreamteam.rand.ui.expenses.ExpenseViewModel
import com.dreamteam.rand.data.repository.ExpenseRepository
import java.util.Calendar
import android.animation.AnimatorSet
import android.animation.ObjectAnimator

// this fragment shows all your spending goals
// you can see how much you've spent compared to your goals and add new ones
class GoalFragment : Fragment() {
    private val TAG = "GoalFragment"

    // binding to access all the views
    private var _binding: FragmentGoalBinding? = null
    private val binding get() = _binding!!

    // viewmodels to handle user data, goals, and expenses
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var goalAdapter: GoalAdapter

    private val goalViewModel: GoalViewModel by viewModels {
        val database = RandDatabase.getDatabase(requireContext())
        val repository = GoalRepository(database.goalDao())
        GoalViewModel.Factory(repository)
    }

    private val expenseViewModel: ExpenseViewModel by viewModels {
        val database = RandDatabase.getDatabase(requireContext())
        val repository = ExpenseRepository(database.transactionDao())
        ExpenseViewModel.Factory(repository)
    }

    // create the view for showing goals
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Creating goals view")
        _binding = FragmentGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    // setup all the UI components after the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Setting up goals view")
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        setupStaggeredFadeInAnimation()
    }

    private fun setupStaggeredFadeInAnimation() {
        // Determine which view to animate: RecyclerView or empty state
        val contentView = if (binding.goalsContainer.visibility == View.VISIBLE) {
            binding.goalsRecyclerView
        } else {
            binding.emptyStateContainer
        }

        // List of views to animate: header section, content (RecyclerView or empty state), FAB
        val viewsToAnimate = listOf(
            binding.headerSection, // Header with "Your Goals" and count
            contentView, // RecyclerView or empty state
            binding.addGoalFab // FAB
        )

        val animatorSet = AnimatorSet()
        val animators = viewsToAnimate.mapIndexed { index, view ->
            // Initialize view state
            view.alpha = 0f // Start with alpha at 0, this makes the view invisible
            view.translationY = 50f

            // Used chat to help structure the animation for the fade in
            // Create fade-in animator
            val fadeAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
            fadeAnimator.duration = 600 // Duration of the fade-in effect (in milliseconds)

            // Create slide-up animator
            val slideAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 50f, 0f)
            slideAnimator.duration = 500 // Duration of the slide-up effect (in milliseconds)

            // Combine fade and slide for each view
            AnimatorSet().apply {
                playTogether(fadeAnimator, slideAnimator)
                startDelay = (index * 290).toLong() // Stagger by 290ms per view
            }
        }

        // Play all animations together
        animatorSet.playTogether(animators.map { it })
        animatorSet.start()
    }

    // setup the toolbar with back button
    private fun setupToolbar() {
        Log.d(TAG, "Setting up toolbar")
        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Toolbar back button clicked")
            findNavController().navigateUp()
        }
    }

    // setup the list of goals
    private fun setupRecyclerView() {
        Log.d(TAG, "Setting up recycler view")
        goalAdapter = GoalAdapter { goal ->
            Log.d(TAG, "Goal clicked for deletion - ID: ${goal.id}, Name: ${goal.name}")
            showDeleteConfirmationDialog(goal)
        }

        binding.goalsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = goalAdapter
        }
        Log.d(TAG, "RecyclerView setup complete")
    }

    // ask the user if they really want to delete a goal
    private fun showDeleteConfirmationDialog(goal: Goal) {
        Log.d(TAG, "Showing delete confirmation dialog for goal: ${goal.name}")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete '${goal.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                Log.d(TAG, "User confirmed deletion of goal: ${goal.name}")
                goalViewModel.deleteGoal(goal)
            }
            .setNegativeButton("Cancel") { _, _ ->
                Log.d(TAG, "User cancelled deletion of goal: ${goal.name}")
            }
            .show()
    }

    // setup buttons to add new goals
    private fun setupClickListeners() {
        Log.d(TAG, "Setting up click listeners")
        binding.addGoalFab.setOnClickListener {
            Log.d(TAG, "Add goal FAB clicked")
            findNavController().navigate(R.id.action_goal_to_addGoal)
        }

        binding.addFirstGoalBtn.setOnClickListener {
            Log.d(TAG, "Add first goal button clicked")
            findNavController().navigate(R.id.action_goal_to_addGoal)
        }
    }

    // watch for changes in the user and goals
    private fun observeViewModel() {
        Log.d(TAG, "Setting up ViewModel observers")
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                Log.w(TAG, "No user logged in, navigating to welcome screen")
                findNavController().navigate(R.id.action_dashboard_to_welcome)
                return@observe
            }

            Log.d(TAG, "User logged in: ${user.uid}")
            // load all goals ordered by year and month
            loadGoals(user.uid)
        }
    }

    // load all goals and their spending amounts
    private fun loadGoals(userId: String) {
        Log.d(TAG, "Loading goals for user: $userId")
        goalViewModel.getAllGoalsOrdered(userId).observe(viewLifecycleOwner) { goals ->
            Log.d(TAG, "Loaded ${goals.size} goals")
            if (goals.isEmpty()) {
                Log.d(TAG, "No goals found, showing empty state")
                binding.emptyStateContainer.visibility = View.VISIBLE
                binding.goalsContainer.visibility = View.GONE
                binding.headerSection.visibility = View.GONE
            } else {
                Log.d(TAG, "Showing goals list")
                binding.emptyStateContainer.visibility = View.GONE
                binding.goalsContainer.visibility = View.VISIBLE
                binding.headerSection.visibility = View.VISIBLE

                // update how many goals you have
                binding.goalCountText.text = goals.size.toString()
                Log.d(TAG, "Goal count updated: ${goals.size}")

                // update each goal with how much was spent that month
                goals.forEach { goal ->
                    Log.d(TAG, "Loading expenses for goal: ${goal.name} (Month: ${goal.month}, Year: ${goal.year})")
                    expenseViewModel.getExpensesByMonthAndYear(
                        userId = userId,
                        month = goal.month,
                        year = goal.year
                    ).observe(viewLifecycleOwner) { expenses ->
                        val totalSpent = expenses.sumOf { it.amount }
                        Log.d(TAG, "Goal ${goal.name} total spent: $totalSpent (${expenses.size} expenses)")
                        goalAdapter.updateGoalExpenses(goal.copy(currentSpent = totalSpent))
                    }
                }

                // update the list of goals
                goalAdapter.submitList(goals)
                Log.d(TAG, "Submitted ${goals.size} goals to adapter")
            }
        }
    }

    // clean up when the view is destroyed
    override fun onDestroyView() {
        Log.d(TAG, "Destroying goals view")
        super.onDestroyView()
        _binding = null
    }
}