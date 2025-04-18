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
import com.dreamteam.rand.data.entity.TransactionType
import com.dreamteam.rand.data.repository.CategoryRepository
import com.dreamteam.rand.databinding.FragmentCategoriesBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import com.google.android.material.tabs.TabLayout

class CategoriesFragment : Fragment() {
    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var categoryAdapter: CategoryAdapter
    private val categoryViewModel: CategoryViewModel by viewModels {
        val database = RandDatabase.getDatabase(requireContext())
        val repository = CategoryRepository(database.categoryDao())
        CategoryViewModel.Factory(repository)
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
            // You can implement this later by passing the category ID
        }
        
        binding.categoriesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
    }

    private fun setupClickListeners() {
        binding.addCategoryFab.setOnClickListener {
            findNavController().navigate(R.id.action_categories_to_addCategory)
        }
        
        // Add click listener for the empty state add button
        binding.addFirstCategoryBtn.setOnClickListener {
            findNavController().navigate(R.id.action_categories_to_addCategory)
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
                
                // Update category count badge
                binding.categoryCountText.text = categories.size.toString()
                
                // Update the adapter
                categoryAdapter.submitList(categories)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 