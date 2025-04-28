package com.dreamteam.rand.ui.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
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

class ExpensesFragment : Fragment() {
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
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        
        // Initialize with "All Categories" by default
        selectedCategoryId = null
        
        updateClearAllButtonVisibility()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(
            onItemClick = { expense ->
                // Navigate to edit expense screen (when implemented)
                // findNavController().navigate(R.id.action_expenses_to_editExpense)
            }
        )
        
        // Set receipt click listener
        expenseAdapter.setOnReceiptClickListener { receiptUri ->
            showReceiptImage(receiptUri)
        }
        
        binding.expensesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expenseAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.addExpenseFab.setOnClickListener {
            findNavController().navigate(R.id.action_expenses_to_addExpense)
        }
        
        binding.dateRangeField.setOnClickListener {
            showDateRangePicker()
        }
        
        // Handle clear button click for date range
        binding.dateRangeLayout.setEndIconOnClickListener {
            clearDateRange()
        }
        
        // Handle clear button click for category filter
        binding.categoryFilterLayout.setEndIconOnClickListener {
            clearCategoryFilter()
        }
        
        // Handle clear all filters button
        val clearAllButton = view?.findViewById<Button>(R.id.clearAllFiltersButton)
        clearAllButton?.setOnClickListener {
            clearAllFilters()
        }
    }
    
    private fun setupCategoryDropdown(categories: List<Category>) {
        categoryAdapter = CategoryDropdownAdapter(
            requireContext(),
            R.layout.item_dropdown_category,
            categories
        )
        
        val dropdownField = binding.categoryFilterField as AutoCompleteTextView
        dropdownField.setAdapter(categoryAdapter)
        
        // Set "All Categories" as the default selection
        dropdownField.setText(categoryAdapter.getItem(0)?.name, false)
        
        // Set threshold to 0 to show all options without typing
        dropdownField.threshold = 0
        
        // Use OnTouchListener instead of OnClickListener to prevent immediate dismissal
        dropdownField.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                if (!dropdownField.isPopupShowing) {
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
            
            // Update the field text to show the selected category name
            dropdownField.setText(selectedCategory?.name, false)
            
            binding.categoryFilterLayout.isEndIconVisible = selectedCategoryId != null
            updateClearAllButtonVisibility()
            loadExpensesWithFilters()
        }
    }
    
    private fun showDateRangePicker() {
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
            updateDateRangeText()
            loadExpensesWithFilters()
        }

        dateRangePicker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }
    
    private fun updateDateRangeText() {
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
    
    private fun clearDateRange() {
        startDate = null
        endDate = null
        binding.dateRangeField.setText("")
        binding.dateRangeLayout.isEndIconVisible = false
        updateClearAllButtonVisibility()
        loadExpensesWithFilters()
    }
    
    private fun clearCategoryFilter() {
        selectedCategoryId = null
        binding.categoryFilterField.setText("")
        binding.categoryFilterLayout.isEndIconVisible = false
        updateClearAllButtonVisibility()
        loadExpensesWithFilters()
    }
    
    private fun clearAllFilters() {
        clearDateRange()
        clearCategoryFilter()
        updateClearAllButtonVisibility()
    }
    
    private fun updateClearAllButtonVisibility() {
        // Show the clear all filters button if any filter is active
        val anyFilterActive = startDate != null || selectedCategoryId != null
        
        // Use findViewById instead of binding to get the button
        val clearAllButton = view?.findViewById<Button>(R.id.clearAllFiltersButton)
        clearAllButton?.visibility = if (anyFilterActive) View.VISIBLE else View.GONE
    }
    
    private fun loadExpensesWithFilters() {
        userViewModel.currentUser.value?.let { user ->
            if (selectedCategoryId != null) {
                // Filter by category and date range if both are selected
                expenseViewModel.getExpensesByCategoryAndDateRange(
                    userId = user.uid,
                    categoryId = selectedCategoryId!!,
                    startDate = startDate,
                    endDate = endDate
                ).observe(viewLifecycleOwner) { expenses ->
                    updateExpensesList(expenses)
                }
                
                // Update totals for this category and date range
                expenseViewModel.fetchTotalExpensesByCategoryAndDateRange(
                    userId = user.uid,
                    categoryId = selectedCategoryId!!,
                    startDate = startDate,
                    endDate = endDate
                )
            } else {
                // Just filter by date range
                expenseViewModel.getExpensesByDateRange(
                    userId = user.uid,
                    startDate = startDate,
                    endDate = endDate
                ).observe(viewLifecycleOwner) { expenses ->
                    updateExpensesList(expenses)
                }
                
                // Update total expenses for the selected date range
                expenseViewModel.fetchTotalExpensesByDateRange(user.uid, startDate, endDate)
            }
        }
    }
    
    private fun updateExpensesList(expenses: List<com.dreamteam.rand.data.entity.Transaction>) {
        android.util.Log.d("ExpensesFragment", "Loaded ${expenses.size} expenses")
        
        // Update expense count badge
        try {
            val countView = view?.findViewById<TextView>(R.id.expenseCount)
            countView?.text = "${expenses.size} items"
            countView?.visibility = if (expenses.isEmpty()) View.GONE else View.VISIBLE
        } catch (e: Exception) {
            android.util.Log.e("ExpensesFragment", "Error updating expense count: ${e.message}")
        }
        
        if (expenses.isEmpty()) {
            // Show empty state
            binding.emptyStateContainer.visibility = View.VISIBLE
            binding.expensesRecyclerView.visibility = View.GONE
        } else {
            // Show expenses
            binding.emptyStateContainer.visibility = View.GONE
            binding.expensesRecyclerView.visibility = View.VISIBLE
            
            // Update the adapter with expenses
            expenseAdapter.submitList(expenses)
        }
    }
    
    private fun observeViewModel() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                findNavController().navigate(R.id.action_dashboard_to_welcome)
                return@observe
            }
            
            // Load categories for the adapter
            categoryViewModel.getCategoriesByType(user.uid, TransactionType.EXPENSE)
                .observe(viewLifecycleOwner) { categories ->
                    android.util.Log.d("ExpensesFragment", "Loaded ${categories.size} categories")
                    if (categories.isNotEmpty()) {
                        // Log some category information for debugging
                        categories.forEach { category ->
                            android.util.Log.d("ExpensesFragment", "Category: ${category.name}, ID: ${category.id}")
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
            binding.totalExpensesValue.text = total.toString()
        }
    }

    private fun showReceiptImage(receiptUri: String) {
        try {
            // Create and show the image viewer dialog
            val imageViewerDialog = com.dreamteam.rand.ui.common.ImageViewerDialog.newInstance(receiptUri)
            imageViewerDialog.show(parentFragmentManager, "ImageViewerDialog")
        } catch (e: Exception) {
            android.widget.Toast.makeText(
                requireContext(),
                "Error opening receipt: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 