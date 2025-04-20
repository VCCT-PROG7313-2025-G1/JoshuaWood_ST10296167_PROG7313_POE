package com.dreamteam.rand.ui.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.data.RandDatabase
import com.dreamteam.rand.data.repository.GoalRepository
import com.dreamteam.rand.databinding.FragmentAddGoalBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

class AddGoalFragment : Fragment() {
    private var _binding: FragmentAddGoalBinding? = null
    private val binding get() = _binding!!
    
    private val userViewModel: UserViewModel by activityViewModels()
    private val goalViewModel: GoalViewModel by viewModels {
        val database = RandDatabase.getDatabase(requireContext())
        val repository = GoalRepository(database.goalDao())
        GoalViewModel.Factory(repository)
    }
    
    private var selectedColor = "#FF5252"
    private var selectedMonth = 0
    private var selectedYear = 0
    
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
        setupColorSelection()
        setupMonthYearPicker()
        setupSaveButton()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupMonthYearPicker() {
        // Set up click listener for the month input field
        binding.monthInput.setOnClickListener {
            showMonthYearPicker()
        }
    }

    private fun showMonthYearPicker() {
        val calendar = Calendar.getInstance()
        
        // Set constraints to only allow users to select future dates
        val constraintsBuilder = CalendarConstraints.Builder()
            .setStart(calendar.timeInMillis)
        
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Month and Year")
            .setCalendarConstraints(constraintsBuilder.build())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = Calendar.getInstance()
            selectedDate.timeInMillis = selection
            
            // Extract month (1-12) and year
            selectedMonth = selectedDate.get(Calendar.MONTH) + 1 // Calendar months are 0-based
            selectedYear = selectedDate.get(Calendar.YEAR)
            
            // Format the date to show in the input field
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            binding.monthInput.setText(dateFormat.format(selectedDate.time))
        }

        datePicker.show(parentFragmentManager, "monthYearPicker")
    }

    private fun setupColorSelection() {
        val colorViews = listOf(
            binding.color1 to binding.color1Selected,
            binding.color2 to binding.color2Selected,
            binding.color3 to binding.color3Selected,
            binding.color4 to binding.color4Selected,
            binding.color5 to binding.color5Selected
        )

        val colors = listOf("#FF5252", "#448AFF", "#66BB6A", "#FFC107", "#9C27B0")

        // Set initial selection
        binding.color1Selected.visibility = View.VISIBLE

        colorViews.forEachIndexed { index, (colorView, indicator) ->
            colorView.setOnClickListener {
                // Update selection indicators
                colorViews.forEach { (_, ind) -> ind.visibility = View.GONE }
                indicator.visibility = View.VISIBLE
                selectedColor = colors[index]
            }
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val goalName = binding.goalNameInput.text.toString().trim()
            val minAmount = binding.minAmountInput.text.toString().toDoubleOrNull()
            val maxAmount = binding.maxAmountInput.text.toString().toDoubleOrNull()
            
            when {
                goalName.isEmpty() -> {
                    binding.goalNameLayout.error = "Goal name is required"
                    return@setOnClickListener
                }
                selectedMonth == 0 || selectedYear == 0 -> {
                    binding.monthLayout.error = "Please select a month and year"
                    return@setOnClickListener
                }
                minAmount == null -> {
                    binding.minAmountLayout.error = "Please enter a valid minimum amount"
                    return@setOnClickListener
                }
                maxAmount == null -> {
                    binding.maxAmountLayout.error = "Please enter a valid maximum amount"
                    return@setOnClickListener
                }
                maxAmount < minAmount -> {
                    binding.maxAmountLayout.error = "Maximum amount must be greater than minimum amount"
                    return@setOnClickListener
                }
                else -> {
                    // Clear any previous errors
                    binding.goalNameLayout.error = null
                    binding.monthLayout.error = null
                    binding.minAmountLayout.error = null
                    binding.maxAmountLayout.error = null
                    
                    userViewModel.currentUser.value?.let { user ->
                        goalViewModel.saveGoal(
                            userId = user.uid,
                            name = goalName,
                            month = selectedMonth,
                            year = selectedYear,
                            minAmount = minAmount,
                            maxAmount = maxAmount,
                            color = selectedColor
                        )
                    }
                }
            }
        }
    }

    private fun observeViewModel() {
        goalViewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                Toast.makeText(requireContext(), "Goal saved successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else if (success == false) {
                Toast.makeText(requireContext(), "Failed to save goal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}