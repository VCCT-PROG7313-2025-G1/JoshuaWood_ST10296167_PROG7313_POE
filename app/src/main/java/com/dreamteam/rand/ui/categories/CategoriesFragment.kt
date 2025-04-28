package com.dreamteam.rand.ui.categories

import android.os.Bundle
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

class CategoriesFragment : Fragment() {
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var categoryAdapter: CategoryAdapter
    
    private var startDate: Long? = null
    private var endDate: Long? = null
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
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        binding.dateRangeLayout.isEndIconVisible = false  // Set initial state
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            // Navigate to edit category screen (not implemented yet)
        }
        
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
    }

    private fun clearDateRange() {
        startDate = null
        endDate = null
        binding.dateRangeField.setText("")
        binding.dateRangeLayout.isEndIconVisible = false
        loadCategoriesWithDateRange()
    }

    private fun setupClickListeners() {
        binding.addCategoryFab.setOnClickListener {
            findNavController().navigate(R.id.action_categories_to_addCategory)
        }
        
        binding.addFirstCategoryBtn.setOnClickListener {
            findNavController().navigate(R.id.action_categories_to_addCategory)
        }

        binding.dateRangeField.setOnClickListener {
            showDateRangePicker()
        }

        // Handle clear button click
        binding.dateRangeLayout.setEndIconOnClickListener {
            clearDateRange()
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
            loadCategoriesWithDateRange()
        }

        dateRangePicker.show(parentFragmentManager, "DATE_RANGE_PICKER")
    }

    private fun updateDateRangeText() {
        if (startDate != null && endDate != null) {
            val startText = dateFormat.format(Date(startDate!!))
            val endText = dateFormat.format(Date(endDate!!))
            binding.dateRangeField.setText("$startText - $endText")
            binding.dateRangeLayout.isEndIconVisible = true
        } else {
            binding.dateRangeField.setText("")
            binding.dateRangeLayout.isEndIconVisible = false
        }
    }

    private fun observeViewModel() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                findNavController().navigate(R.id.action_dashboard_to_welcome)
                return@observe
            }

            // Always load expense categories
            loadCategories(user.uid, TransactionType.EXPENSE)
        }
    }

    private fun loadCategories(userId: String, type: TransactionType) {
        categoryViewModel.getCategoriesByType(userId, type).observe(viewLifecycleOwner) { categories ->
            if (categories.isEmpty()) {
                // Show empty state
                binding.emptyStateContainer.visibility = View.VISIBLE
                binding.categoriesRecyclerView.visibility = View.GONE
                binding.headerSection.visibility = View.GONE
            } else {
                // Show categories
                binding.emptyStateContainer.visibility = View.GONE
                binding.categoriesRecyclerView.visibility = View.VISIBLE
                binding.headerSection.visibility = View.VISIBLE
                
                binding.categoryCountText.text = categories.size.toString()
                loadCategoriesWithDateRange(categories, userId)
            }
        }
    }

    private fun loadCategoriesWithDateRange(categories: List<Category> = categoryAdapter.currentList, userId: String? = userViewModel.currentUser.value?.uid) {
        if (userId == null) return

        categories.forEach { category ->
            expenseViewModel.getExpensesByCategoryAndDateRange(
                userId = userId,
                categoryId = category.id,
                startDate = startDate,
                endDate = endDate
            ).observe(viewLifecycleOwner) { expenses ->
                val totalSpent = expenses.sumOf { it.amount }
                categoryAdapter.updateCategoryTotal(category.id, totalSpent)
            }
        }
        
        categoryAdapter.submitList(categories)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}