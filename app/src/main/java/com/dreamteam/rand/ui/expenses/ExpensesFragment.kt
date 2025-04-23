package com.dreamteam.rand.ui.expenses

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
import com.dreamteam.rand.data.entity.TransactionType
import com.dreamteam.rand.data.repository.CategoryRepository
import com.dreamteam.rand.data.repository.ExpenseRepository
import com.dreamteam.rand.databinding.FragmentExpensesBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import com.dreamteam.rand.ui.categories.CategoryViewModel

class ExpensesFragment : Fragment() {
    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!
    
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var expenseAdapter: ExpenseAdapter
    
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
        
        binding.expensesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expenseAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.addExpenseFab.setOnClickListener {
            findNavController().navigate(R.id.action_expenses_to_addExpense)
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
                }
            
            // Load expenses
            expenseViewModel.getExpenses(user.uid).observe(viewLifecycleOwner) { expenses ->
                android.util.Log.d("ExpensesFragment", "Loaded ${expenses.size} expenses")
                
                // Log some expense information for debugging
                expenses.forEach { expense ->
                    android.util.Log.d("ExpensesFragment", "Expense: ${expense.description}, Category ID: ${expense.categoryId}")
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
            
            // Update total expenses
            expenseViewModel.fetchTotalMonthlyExpenses(user.uid)
        }
        
        // Observe monthly total
        expenseViewModel.totalMonthlyExpenses.observe(viewLifecycleOwner) { total ->
            binding.totalExpensesValue.text = total.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 