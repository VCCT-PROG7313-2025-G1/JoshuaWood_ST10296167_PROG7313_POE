package com.dreamteam.rand.ui.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dreamteam.rand.R
import com.dreamteam.rand.data.RandDatabase
import com.dreamteam.rand.data.entity.Category
import com.dreamteam.rand.data.entity.TransactionType
import com.dreamteam.rand.data.repository.CategoryRepository
import com.dreamteam.rand.data.repository.ExpenseRepository
import com.dreamteam.rand.databinding.FragmentExpensesBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import com.dreamteam.rand.ui.categories.CategoryViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*
import android.animation.AnimatorSet
import android.animation.ObjectAnimator

// displays and manages the list of expenses with filtering capabilities
class ExpensesFragment : Fragment() {
    private val TAG = "ExpensesFragment"

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var categoryAdapter: CategoryDropdownAdapter

    private var startDate: Long? = null
    private var endDate: Long? = null
    private var selectedCategoryId: Long? = null
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    private val categoryViewModel: CategoryViewModel by viewModels {
        val database = RandDatabase.getDatabase(requireContext())
        val repository = CategoryRepository(database.categoryDao())
        CategoryViewModel.Factory(repository)
    }

    private val expenseViewModel: ExpenseViewModel by viewModels {
        val database = RandDatabase.getDatabase(requireContext())
        val repository = ExpenseRepository(database.transactionDao())
        ExpenseViewModel.Factory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Creating expenses view")
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Setting up expenses view with savedInstanceState: ${savedInstanceState != null}")
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()

        // Initialize with "All Categories" by default
        selectedCategoryId = null
        Log.d(TAG, "Initialized with default category selection: null")

        updateClearAllButtonVisibility()
        observeViewModel()
        setupStaggeredFadeInAnimation()
    }


    // Made use of ChatGPT and Grok to make the fade in animation for the Expense screen.
    // The Staggered fade-in animation fades in and slides up views sequentially.
    // Each view starts with alpha=0 and translationY=50, then animates to alpha=1 (600ms) and translationY=0 (500ms) with a 290ms delay between views.
    // Using Grok and ChatGPT it helped me to set the up the fade in animation and create the animator to create the transition effects.
    // The alpha keyword sets the opacity of the view to 0 making the view invisible.
    // The translationY keyword sets the position of the view.
    // The animator set method manages the animation and determines how the animations flows
    // val animators = viewsToAnimate.mapIndexed { index, view ->, this line maps each view to their animation
    // val fadeAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f), this line creates the fade in animation
    // val slideAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 50f, 0f), this line creates the slide up animation
    // I also asked ChatGPT and Grok how i could change the fade in duration to make it look better and more pleasing to watch.
    // So it suggested to set the duration to different values for each view.
    // Each duration is measured in milliseconds.
    // Then after setting the durations they are combined to create the animation
    // The startDelay keyword is used to control the delay between animations.
    // The duration of the startDelay is also measured in milliseconds
    // With the use of ChatGPT and Grok I learnt how to create the fade in animation for the Expense Screen and how to create the animator to create the transition effects.

    private fun setupStaggeredFadeInAnimation() {
        // Determine which view to animate: RecyclerView or empty state
        val contentView = if (binding.expensesRecyclerView.visibility == View.VISIBLE) {
            binding.expensesRecyclerView
        } else {
            binding.emptyStateContainer
        }

        // List of views to animate: summary card, filters card, list header, content, FAB
        val viewsToAnimate = listOf(
            binding.totalExpensesValue.parent.parent as View, // CardView for monthly summary
            binding.dateRangeLayout.parent.parent as View, // CardView for filters
            binding.expenseCount.parent as View, // LinearLayout for list header
            contentView,
            binding.addExpenseFab
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
                startDelay = (index * 290).toLong() // Stagger by 150ms per view
            }
        }

        // Play all animations together
        animatorSet.playTogether(animators.map { it })
        animatorSet.start()
    }

    // sets up the toolbar with navigation
    private fun setupToolbar() {
        Log.d(TAG, "Setting up toolbar")
        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Navigating back")
            findNavController().navigateUp()
        }
    }

    // sets up the recycler view for displaying expenses
    private fun setupRecyclerView() {
        Log.d(TAG, "Setting up recycler view with layout manager and adapter")
        expenseAdapter = ExpenseAdapter(
            onItemClick = { expense ->
                Log.d(TAG, "Expense clicked - ID: ${expense.id}, Amount: ${expense.amount}, Category: ${expense.categoryId}")
                // Navigate to edit expense screen (when implemented)
                // findNavController().navigate(R.id.action_expenses_to_editExpense)
            }
        )

        // Set receipt click listener
        expenseAdapter.setOnReceiptClickListener { receiptUri ->
            Log.d(TAG, "Receipt clicked - URI: $receiptUri")
            showReceiptImage(receiptUri)
        }

        binding.expensesRecyclerView.apply {
            Log.d(TAG, "Configuring RecyclerView with LinearLayoutManager")
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expenseAdapter
            Log.d(TAG, "RecyclerView setup complete")
        }
    }

    // sets up all click listeners for the fragment
    private fun setupClickListeners() {
        Log.d(TAG, "Setting up click listeners")
        binding.addExpenseFab.setOnClickListener {
            Log.d(TAG, "Add expense FAB clicked")
            findNavController().navigate(R.id.action_expenses_to_addExpense)
        }

        binding.dateRangeField.setOnClickListener {
            Log.d(TAG, "Date range field clicked")
            showDateRangePicker()
        }

        // Handle clear button click for date range
        binding.dateRangeLayout.setEndIconOnClickListener {
            Log.d(TAG, "Clearing date range")
            clearDateRange()
        }

        // Handle clear button click for category filter
        binding.categoryFilterLayout.setEndIconOnClickListener {
            Log.d(TAG, "Clearing category filter")
            clearCategoryFilter()
        }

        // Handle clear all filters button
        val clearAllButton = view?.findViewById<Button>(R.id.clearAllFiltersButton)
        clearAllButton?.setOnClickListener {
            Log.d(TAG, "Clearing all filters")
            clearAllFilters()
        }
    }

    // sets up the category dropdown with available categories
    private fun setupCategoryDropdown(categories: List<Category>) {
        Log.d(TAG, "Setting up category dropdown with ${categories.size} categories")
        if (categories.isEmpty()) {
            Log.w(TAG, "No categories available for dropdown")
            return
        }

        categoryAdapter = CategoryDropdownAdapter(
            requireContext(),
            R.layout.item_dropdown_category,
            categories
        )

        val dropdownField = binding.categoryFilterField as AutoCompleteTextView
        dropdownField.setAdapter(categoryAdapter)

        // Set "All Categories" as the default selection
        val defaultCategory = categoryAdapter.getItem(0)
        dropdownField.setText(defaultCategory?.name, false)
        Log.d(TAG, "Set default category: ${defaultCategory?.name}")

        // Set threshold to 0 to show all options without typing
        dropdownField.threshold = 0

        // Use OnTouchListener instead of OnClickListener to prevent immediate dismissal
        dropdownField.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                if (!dropdownField.isPopupShowing) {
                    Log.d(TAG, "Showing category dropdown")
                    dropdownField.showDropDown()
                }
            }
            // Let the view also handle the touch event
            v.performClick()
            true
        }

        dropdownField.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedCategory = categoryAdapter.getItem(position)
            selectedCategoryId = if (selectedCategory?.id == -1L) null else selectedCategory?.id

            Log.d(TAG, "Category selected - Name: ${selectedCategory?.name}, ID: $selectedCategoryId, Position: $position")

            // Update the field text to show the selected category name
            dropdownField.setText(selectedCategory?.name, false)

            binding.categoryFilterLayout.isEndIconVisible = selectedCategoryId != null
            updateClearAllButtonVisibility()
            loadExpensesWithFilters()
        }
    }

    // shows the date range picker dialog
    private fun showDateRangePicker() {
        Log.d(TAG, "Showing date range picker")
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Range")
            .setSelection(
                androidx.core.util.Pair(
                    startDate ?: MaterialDatePicker.todayInUtcMilliseconds(),
                    endDate ?: MaterialDatePicker.todayInUtcMilliseconds()
                )
            )
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            startDate = selection.first
            endDate = selection.second
            Log.d(TAG, "Date range selected: ${dateFormat.format(Date(startDate!!))} - ${dateFormat.format(Date(endDate!!))}")
            updateDateRangeText()
            loadExpensesWithFilters()
        }

        dateRangePicker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }

    // updates the date range text display
    private fun updateDateRangeText() {
        Log.d(TAG, "Updating date range text")
        if (startDate != null && endDate != null) {
            val startText = dateFormat.format(Date(startDate!!))
            val endText = dateFormat.format(Date(endDate!!))
            binding.dateRangeField.setText("$startText - $endText")
            binding.dateRangeLayout.isEndIconVisible = true
            updateClearAllButtonVisibility()
        } else {
            binding.dateRangeField.setText("")
            binding.dateRangeLayout.isEndIconVisible = false
        }
    }

    // clears the date range filter
    private fun clearDateRange(shouldReloadData: Boolean = true) {
        Log.d(TAG, "Clearing date range filter")
        startDate = null
        endDate = null
        binding.dateRangeField.setText("")
        binding.dateRangeLayout.isEndIconVisible = false
        updateClearAllButtonVisibility()
        if (shouldReloadData) {
            loadExpensesWithFilters()
        }
    }

    // clears the category filter
    private fun clearCategoryFilter(shouldReloadData: Boolean = true) {
        Log.d(TAG, "Clearing category filter")
        selectedCategoryId = null
        binding.categoryFilterField.setText("")
        binding.categoryFilterLayout.isEndIconVisible = false
        updateClearAllButtonVisibility()
        if (shouldReloadData) {
            loadExpensesWithFilters()
        }
    }

    // clears all active filters
    private fun clearAllFilters() {
        Log.d(TAG, "Clearing all filters")
        // Pass false to prevent individual reload
        clearDateRange(false)
        clearCategoryFilter(false)
        
        // Reset dropdown to default selection if needed
        try {
            // Get the default "All Categories" item which is always at position 0
            val defaultCategory = categoryAdapter.getItem(0)
            if (defaultCategory != null) {
                (binding.categoryFilterField as? AutoCompleteTextView)?.setText(defaultCategory.name, false)
                Log.d(TAG, "Reset category dropdown to: ${defaultCategory.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting category dropdown: ${e.message}", e)
        }
        
        updateClearAllButtonVisibility()
        // Single reload after clearing all filters
        loadExpensesWithFilters()
    }

    // updates the visibility of the clear all filters button
    private fun updateClearAllButtonVisibility() {
        Log.d(TAG, "Updating clear all button visibility")
        // Show the clear all filters button if any filter is active
        val anyFilterActive = startDate != null || selectedCategoryId != null

        // Use findViewById instead of binding to get the button
        val clearAllButton = view?.findViewById<Button>(R.id.clearAllFiltersButton)
        clearAllButton?.visibility = if (anyFilterActive) View.VISIBLE else View.GONE
    }

    // loads expenses based on current filters
    private fun loadExpensesWithFilters() {
        Log.d(TAG, "Loading expenses with filters - Category: $selectedCategoryId, Date Range: $startDate - $endDate")
        userViewModel.currentUser.value?.let { user ->
            Log.d(TAG, "Loading expenses for user: ${user.uid}")
            if (selectedCategoryId != null) {
                Log.d(TAG, "Filtering by category: $selectedCategoryId")
                // Filter by category and date range if both are selected
                expenseViewModel.getExpensesByCategoryAndDateRange(
                    userId = user.uid,
                    categoryId = selectedCategoryId!!,
                    startDate = startDate,
                    endDate = endDate
                ).observe(viewLifecycleOwner) { expenses ->
                    Log.d(TAG, "Loaded ${expenses.size} expenses for category $selectedCategoryId")
                    if (expenses.isEmpty()) {
                        Log.d(TAG, "No expenses found for the selected filters")
                    } else {
                        Log.d(TAG, "First expense: ID=${expenses.first().id}, Amount=${expenses.first().amount}")
                        Log.d(TAG, "Last expense: ID=${expenses.last().id}, Amount=${expenses.last().amount}")
                    }
                    updateExpensesList(expenses)
                }

                // Update totals for this category and date range
                Log.d(TAG, "Fetching total expenses for category $selectedCategoryId")
                expenseViewModel.fetchTotalExpensesByCategoryAndDateRange(
                    userId = user.uid,
                    categoryId = selectedCategoryId!!,
                    startDate = startDate,
                    endDate = endDate
                )
            } else {
                Log.d(TAG, "Loading all expenses (no category filter)")
                // Just filter by date range
                expenseViewModel.getExpensesByDateRange(
                    userId = user.uid,
                    startDate = startDate,
                    endDate = endDate
                ).observe(viewLifecycleOwner) { expenses ->
                    Log.d(TAG, "Loaded ${expenses.size} expenses for date range")
                    updateExpensesList(expenses)
                }

                // Update total expenses for the selected date range
                Log.d(TAG, "Fetching total expenses for date range")
                expenseViewModel.fetchTotalExpensesByDateRange(user.uid, startDate, endDate)
            }
        } ?: run {
            Log.w(TAG, "No user logged in, cannot load expenses")
        }
    }

    private fun updateExpensesList(expenses: List<com.dreamteam.rand.data.entity.Transaction>) {
        Log.d(TAG, "Updating expenses list with ${expenses.size} items")

        // Update expense count badge
        try {
            val countView = view?.findViewById<TextView>(R.id.expenseCount)
            countView?.text = "${expenses.size} items"
            countView?.visibility = if (expenses.isEmpty()) View.GONE else View.VISIBLE
            Log.d(TAG, "Updated expense count badge: ${expenses.size} items")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating expense count: ${e.message}", e)
        }

        if (expenses.isEmpty()) {
            Log.d(TAG, "Showing empty state")
            // Show empty state
            binding.emptyStateContainer.visibility = View.VISIBLE
            binding.expensesRecyclerView.visibility = View.GONE
        } else {
            Log.d(TAG, "Showing expenses list")
            // Show expenses
            binding.emptyStateContainer.visibility = View.GONE
            binding.expensesRecyclerView.visibility = View.VISIBLE

            // Update the adapter with expenses
            expenseAdapter.submitList(expenses)
            Log.d(TAG, "Submitted ${expenses.size} expenses to adapter")
        }
    }

    private fun observeViewModel() {
        Log.d(TAG, "Setting up ViewModel observers")
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                Log.w(TAG, "No user logged in, navigating to welcome screen")
                findNavController().navigate(R.id.action_dashboard_to_welcome)
                return@observe
            }

            Log.d(TAG, "User logged in: ${user.uid}")
            // Load categories for the adapter
            categoryViewModel.getCategoriesByType(user.uid, TransactionType.EXPENSE)
                .observe(viewLifecycleOwner) { categories ->
                    Log.d(TAG, "Loaded ${categories.size} categories for user ${user.uid}")
                    if (categories.isEmpty()) {
                        Log.w(TAG, "No categories found for user ${user.uid}")
                    } else {
                        categories.forEach { category ->
                            Log.d(TAG, "Category loaded - Name: ${category.name}, ID: ${category.id}, Type: ${category.type}")
                        }
                    }
                    expenseAdapter.updateCategoryMap(categories)
                    setupCategoryDropdown(categories)
                }

            // Load expenses with initial filters
            loadExpensesWithFilters()
        }

        // Observe total expenses
        expenseViewModel.totalExpenses.observe(viewLifecycleOwner) { total ->
            Log.d(TAG, "Total expenses updated: $total")
            binding.totalExpensesValue.text = total.toString()
        }
    }

    private fun showReceiptImage(receiptUri: String) {
        Log.d(TAG, "Attempting to show receipt image: $receiptUri")
        try {
            // Create and show the image viewer dialog
            val imageViewerDialog = com.dreamteam.rand.ui.common.ImageViewerDialog.newInstance(receiptUri)
            imageViewerDialog.show(parentFragmentManager, "ImageViewerDialog")
            Log.d(TAG, "Successfully showed receipt image dialog")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing receipt image: ${e.message}", e)
            android.widget.Toast.makeText(
                requireContext(),
                "Error opening receipt: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        Log.d(TAG, "Destroying view and cleaning up resources")
        super.onDestroyView()
        _binding = null
    }
}