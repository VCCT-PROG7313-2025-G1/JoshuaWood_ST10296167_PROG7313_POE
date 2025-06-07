package com.dreamteam.rand.ui.goals

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.data.RandDatabase
import com.dreamteam.rand.data.repository.GoalRepository
import com.dreamteam.rand.databinding.FragmentAddGoalBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import com.dreamteam.rand.data.firebase.GoalFirebase

// this fragment lets you add a new spending goal
// you can set a name, amount range, month/year, and pick a color
class AddGoalFragment : Fragment() {
    private val TAG = "AddGoalFragment"
    private var isSaveInProgress = false
    private val goalXP = 15

    // binding to access all the views
    private var _binding: FragmentAddGoalBinding? = null
    private val binding get() = _binding!!

    // viewmodels to handle user data and goals
    private val userViewModel: UserViewModel by activityViewModels()
    private val goalViewModel: GoalViewModel by viewModels {
        val database = RandDatabase.getDatabase(requireContext())
        val goalDao = database.goalDao()
        val goalFirebase = GoalFirebase()
        val repository = GoalRepository(goalDao)
        GoalViewModel.Factory(repository)
    }

    // keep track of what the user picked
    private var selectedColor = "#FF5252"
    private var selectedMonth = 0
    private var selectedYear = 0

    // create the view for adding a goal
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Creating add goal view")
        _binding = FragmentAddGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    // setup all the UI components after the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Setting up add goal view")
        setupToolbar()
        setupColorSelection()
        setupMonthYearPicker()
        setupSaveButton()
        observeViewModel()
        setupStaggeredFadeInAnimation()
    }

    // used chatgpt and grok to create the add goal animations
    // creates a smooth entrance effect for the goal items
    // each element fades in and slides up with a 290ms delay between them
    private fun setupStaggeredFadeInAnimation() {
        // List of views to animate: input card, save button
        val viewsToAnimate = listOf(
            binding.goalNameLayout.parent.parent as View, // MaterialCardView containing all inputs
            binding.saveButton // Save button
        )

        val animatorSet = AnimatorSet()
        val animators = viewsToAnimate.mapIndexed { index, view ->
            // Initialize view state
            view.alpha = 0f // Start with alpha at 0, this makes the view invisible
            view.translationY = 50f //

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
                startDelay = (index * 290).toLong() // Stagger by 150ms per view
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

    // setup the month/year picker button
    private fun setupMonthYearPicker() {
        Log.d(TAG, "Setting up month/year picker")
        binding.monthInput.setOnClickListener {
            Log.d(TAG, "Month input clicked")
            showMonthYearPicker()
        }
    }

    // show a calendar to pick month and year
    private fun showMonthYearPicker() {
        Log.d(TAG, "Showing month/year picker")
        val calendar = Calendar.getInstance()

        // only let them pick future dates
        val constraintsBuilder = CalendarConstraints.Builder()
            .setStart(calendar.timeInMillis)

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Month and Year")
            .setCalendarConstraints(constraintsBuilder.build())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = Calendar.getInstance()
            selectedDate.timeInMillis = selection

            // get the month (1-12) and year they picked
            // ChatGPT suggested ignoring the day and just getting the month and year
            selectedMonth = selectedDate.get(Calendar.MONTH) + 1
            selectedYear = selectedDate.get(Calendar.YEAR)

            // show what they picked in the input field
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            binding.monthInput.setText(dateFormat.format(selectedDate.time))
            Log.d(TAG, "Selected month/year: $selectedMonth/$selectedYear")
        }

        datePicker.show(parentFragmentManager, "monthYearPicker")
    }

    // setup the color picker buttons
    private fun setupColorSelection() {
        Log.d(TAG, "Setting up color selection")
        // pair up each color button with its selection indicator
        val colorViews = listOf(
            binding.color1 to binding.color1Selected,
            binding.color2 to binding.color2Selected,
            binding.color3 to binding.color3Selected,
            binding.color4 to binding.color4Selected,
            binding.color5 to binding.color5Selected
        )

        // the colors they can pick from
        val colors = listOf("#FF5252", "#448AFF", "#66BB6A", "#FFC107", "#9C27B0")

        // start with the first color selected
        binding.color1Selected.visibility = View.VISIBLE

        // when they click a color, show its indicator and hide the others
        colorViews.forEachIndexed { index, (colorView, indicator) ->
            colorView.setOnClickListener {
                Log.d(TAG, "Color selected: ${colors[index]}")
                colorViews.forEach { (_, ind) -> ind.visibility = View.GONE }
                indicator.visibility = View.VISIBLE
                selectedColor = colors[index]
                goalViewModel.setSelectedColor(selectedColor)
            }
        }
    }

    // setup the save button with all the validation
    private fun setupSaveButton() {
        Log.d(TAG, "Setting up save button")
        binding.saveButton.setOnClickListener {
            // get what they typed in
            val goalName = binding.goalNameInput.text.toString().trim()
            val minAmount = binding.minAmountInput.text.toString().toDoubleOrNull()
            val maxAmount = binding.maxAmountInput.text.toString().toDoubleOrNull()

            Log.d(TAG, "Validating goal input - Name: $goalName, Min: $minAmount, Max: $maxAmount, Month: $selectedMonth, Year: $selectedYear")

            // check if everything is filled out right
            when {
                goalName.isEmpty() -> {
                    Log.d(TAG, "Validation failed: Empty goal name")
                    binding.goalNameLayout.error = "Goal name is required"
                    return@setOnClickListener
                }
                selectedMonth == 0 || selectedYear == 0 -> {
                    Log.d(TAG, "Validation failed: No month/year selected")
                    binding.monthLayout.error = "Please select a month and year"
                    return@setOnClickListener
                }
                minAmount == null -> {
                    Log.d(TAG, "Validation failed: Invalid minimum amount")
                    binding.minAmountLayout.error = "Please enter a valid minimum amount"
                    return@setOnClickListener
                }
                maxAmount == null -> {
                    Log.d(TAG, "Validation failed: Invalid maximum amount")
                    binding.maxAmountLayout.error = "Please enter a valid maximum amount"
                    return@setOnClickListener
                }
                maxAmount < minAmount -> {
                    Log.d(TAG, "Validation failed: Maximum less than minimum")
                    binding.maxAmountLayout.error = "Maximum amount must be greater than minimum"
                    return@setOnClickListener
                }
                else -> {
                    userViewModel.currentUser.value?.let { user ->
                        Log.d(TAG, "All validation passed, saving goal for user: ${user.uid}")
                        // show loading spinner
                        isSaveInProgress = true
                        binding.saveButton.isEnabled = false
                        binding.goalProgressBar.visibility = View.VISIBLE

                        goalViewModel.saveGoal(
                            userId = user.uid,
                            name = goalName,
                            month = selectedMonth,
                            year = selectedYear,
                            minAmount = minAmount,
                            maxAmount = maxAmount,
                            color = selectedColor
                        )
                    } ?: run {
                        Log.w(TAG, "Cannot save goal: No user logged in")
                        Toast.makeText(requireContext(), "Please sign in to add goals", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // watch for when the goal gets saved
    // ChatGPT suggested using requireContext() instead of context for fragment classes
    private fun observeViewModel() {
        Log.d(TAG, "Setting up ViewModel observers")
        goalViewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            when (success) {
                true -> {
                    Log.d(TAG, "✅ Goal saved successfully!")
                    Toast.makeText(requireContext(), "Goal saved successfully + ${goalXP}XP", Toast.LENGTH_SHORT).show()
                    // give user xp
                    userViewModel.updateUserProgress(goalXP)
                    goalViewModel.resetSaveStatus()

                    isSaveInProgress = false
                    binding.saveButton.isEnabled = true
                    binding.goalProgressBar.visibility = View.GONE

                    findNavController().navigateUp()
                }
                false -> {
                    Log.e(TAG, "❌ Failed to save goal")
                    Toast.makeText(requireContext(), "Failed to save goal", Toast.LENGTH_SHORT).show()
                    goalViewModel.resetSaveStatus()

                    isSaveInProgress = false
                    binding.saveButton.isEnabled = true
                    binding.goalProgressBar.visibility = View.GONE
                }
                null -> {
                    // still saving, do nothing
                }
            }
        }
    }

    // clean up when the view is destroyed
    override fun onDestroyView() {
        Log.d(TAG, "Destroying add goal view")
        super.onDestroyView()
        _binding = null
    }
}