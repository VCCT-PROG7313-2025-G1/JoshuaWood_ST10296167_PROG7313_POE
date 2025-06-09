package com.dreamteam.rand.ui.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.dreamteam.rand.databinding.FragmentCategoriesBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import com.dreamteam.rand.ui.expenses.ExpenseViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import com.dreamteam.rand.data.firebase.CategoryFirebase
import com.dreamteam.rand.ui.common.ViewUtils

// this fragment shows all the categories and how much was spent in each one
// you can filter by date range and see spending stats
class CategoriesFragment : Fragment() {
    private val TAG = "CategoriesFragment"

    // binding to access the views
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!

    // viewmodels to handle data
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var categoryAdapter: CategoryAdapter

    // date range for filtering
    private var startDate: Long? = null
    private var endDate: Long? = null
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    // setup the category viewmodel with its repository
    private val categoryViewModel: CategoryViewModel by viewModels {
        val database = RandDatabase.getDatabase(requireContext())
        val categoryDao = database.categoryDao()
        val categoryFirebase = CategoryFirebase()
        val repository = CategoryRepository(categoryDao, categoryFirebase)
        CategoryViewModel.Factory(repository)
    }

    // setup the expense viewmodel with its repository
    private val expenseViewModel: ExpenseViewModel by viewModels {
        val database = RandDatabase.getDatabase(requireContext())
        val repository = ExpenseRepository(database.transactionDao())
        ExpenseViewModel.Factory(repository)
    }

    // create the view for this fragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Creating categories view")
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    // setup everything after the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Setting up categories view")
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        binding.dateRangeLayout.isEndIconVisible = false  // Set initial state
        observeViewModel()
        setupStaggeredFadeInAnimation()
    }

    // used chatgpt and grok to create the category list animations
    // creates a smooth entrance effect for the category list and filters
    // each element fades in and slides up with a 290ms delay between them
    private fun setupStaggeredFadeInAnimation() {
        // Determine which view to animate: RecyclerView or empty state
        val contentView = if (binding.categoriesRecyclerView.visibility == View.VISIBLE) {
            binding.categoriesRecyclerView
        } else {
            binding.emptyStateContainer
        }

        // List of views to animate: header section, content (RecyclerView or empty state), FAB
        val viewsToAnimate = listOf(
            binding.headerSection,
            contentView,
            binding.addCategoryFab
        )

        val animatorSet = AnimatorSet()
        val animators = viewsToAnimate.mapIndexed { index, view ->
            // Initialize view state
            view.alpha = 0f // Start with alpha at 0, this makes the view invisible
            view.translationY = 50f

            // Used chat to help structure the animation for the fade in
            // Create fade-in animator
            val fadeAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
            fadeAnimator.duration = 500 // Duration for fade-in

            // Create slide-up animator
            val slideAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 50f, 0f)
            slideAnimator.duration = 400 // Duration for slide-up

            // Combine fade and slide for each view
            AnimatorSet().apply {
                playTogether(fadeAnimator, slideAnimator)
                startDelay = (index * 300).toLong() // Stagger by 300ms per view
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

    // setup the list of categories
    private fun setupRecyclerView() {
        Log.d(TAG, "Setting up recycler view")
        categoryAdapter = CategoryAdapter { category ->
            Log.d(TAG, "Category clicked - ID: ${category.id}, Name: ${category.name}")
            // Navigate to edit category screen (not implemented yet)
        }

        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
        Log.d(TAG, "RecyclerView setup complete")
    }

    // clear the date range filter
    private fun clearDateRange() {
        Log.d(TAG, "Clearing date range filter")
        startDate = null
        endDate = null
        binding.dateRangeField.setText("")
        binding.dateRangeLayout.isEndIconVisible = false
        loadCategoriesWithDateRange()
    }

    // setup all the click listeners for buttons and fields
    private fun setupClickListeners() {
        Log.d(TAG, "Setting up click listeners")
        binding.addCategoryFab.setOnClickListener {
            Log.d(TAG, "Add category FAB clicked")
            findNavController().navigate(R.id.action_categories_to_addCategory)
        }

        binding.addFirstCategoryBtn.setOnClickListener {
            Log.d(TAG, "Add first category button clicked")
            findNavController().navigate(R.id.action_categories_to_addCategory)
        }

        binding.dateRangeField.setOnClickListener {
            Log.d(TAG, "Date range field clicked")
            showDateRangePicker()
        }

        // Handle clear button click
        binding.dateRangeLayout.setEndIconOnClickListener {
            Log.d(TAG, "Date range clear button clicked")
            clearDateRange()
        }
    }

    // ai declaration: here we used claude to implement the date range filtering system
    // for category expenses over specific time periods
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
            loadCategoriesWithDateRange()
        }

        dateRangePicker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }

    // update the text showing the selected date range
    private fun updateDateRangeText() {
        Log.d(TAG, "Updating date range text")
        if (startDate != null && endDate != null) {
            val startText = dateFormat.format(Date(startDate!!))
            val endText = dateFormat.format(Date(endDate!!))
            binding.dateRangeField.setText("$startText - $endText")
            binding.dateRangeLayout.isEndIconVisible = true
            Log.d(TAG, "Date range text updated: $startText - $endText")
        } else {
            binding.dateRangeField.setText("")
            binding.dateRangeLayout.isEndIconVisible = false
        }
    }

    // watch for changes in the viewmodels
    private fun observeViewModel() {
        Log.d(TAG, "Setting up ViewModel observers")
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                Log.w(TAG, "No user logged in, navigating to welcome screen")
                findNavController().navigate(R.id.action_dashboard_to_welcome)
                return@observe
            }

            Log.d(TAG, "User logged in: ${user.uid}")
            // Always load expense categories
            loadCategories(user.uid, TransactionType.EXPENSE)
        }
    }

    // load all categories for a user
    private fun loadCategories(userId: String, type: TransactionType) {
        Log.d(TAG, "Loading categories from local cache for user: $userId, type: $type")
        categoryViewModel.getCategoriesByType(userId, type).observe(viewLifecycleOwner) { categories ->
            Log.d(TAG, "Loaded ${categories.size} categories from cache")
            if (categories.isEmpty()) {
                Log.d(TAG, "No categories in cache, showing empty state")
                binding.emptyStateContainer.visibility = View.VISIBLE
                binding.categoriesRecyclerView.visibility = View.GONE
                binding.headerSection.visibility = View.GONE
            } else {
                Log.d(TAG, "Showing cached categories")
                binding.emptyStateContainer.visibility = View.GONE
                binding.categoriesRecyclerView.visibility = View.VISIBLE
                binding.headerSection.visibility = View.VISIBLE

                binding.categoryCountText.text = categories.size.toString()
                loadCategoriesWithDateRange(categories, userId)
            }
        }
    }

    private fun loadCategoriesWithDateRange(categories: List<Category> = categoryAdapter.currentList, userId: String? = userViewModel.currentUser.value?.uid) {
        if (userId == null) {
            Log.w(TAG, "Cannot load category totals: no user ID")
            return
        }

        Log.d(TAG, "Loading category totals for date range")
        categories.forEach { category ->
            Log.d(TAG, "Loading expenses for category: ${category.name} from local cache")
            expenseViewModel.getExpensesByCategoryAndDateRange(
                userId = userId,
                categoryId = category.id,
                startDate = startDate,
                endDate = endDate
            ).observe(viewLifecycleOwner) { expenses ->
                val totalSpent = expenses.sumOf { it.amount }
                Log.d(TAG, "Category ${category.name} total from cache: $totalSpent")
                categoryAdapter.updateCategoryTotal(category.id, totalSpent)
            }
        }

        categoryAdapter.submitList(categories)
    }

    // clean up when the view is destroyed
    override fun onDestroyView() {
        Log.d(TAG, "Destroying categories view")
        super.onDestroyView()
        _binding = null
    }
}