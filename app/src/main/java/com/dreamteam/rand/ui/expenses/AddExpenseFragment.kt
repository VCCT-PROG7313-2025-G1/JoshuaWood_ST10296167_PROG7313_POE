package com.dreamteam.rand.ui.expenses

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.data.RandDatabase
import com.dreamteam.rand.data.entity.TransactionType
import com.dreamteam.rand.data.repository.CategoryRepository
import com.dreamteam.rand.data.repository.ExpenseRepository
import com.dreamteam.rand.databinding.FragmentAddExpenseBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import com.dreamteam.rand.ui.categories.CategoryViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddExpenseFragment : Fragment() {
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    
    private val userViewModel: UserViewModel by activityViewModels()
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
    
    private var selectedPhotoPath: String? = null
    private val categoryIdMap = mutableMapOf<String, Long>()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupDatePicker()
        setupAmountInput()
        setupCategoryDropdown()
        setupPhotoSection()
        setupSaveButton()
        observeViewModel()
        setupPhotoResultListener()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupDatePicker() {
        // Set initial date to today
        binding.dateInput.setText(dateFormat.format(Date()))
        
        binding.dateInput.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    
                    binding.dateInput.setText(dateFormat.format(calendar.time))
                    expenseViewModel.setSelectedDate(calendar.timeInMillis)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            
            datePickerDialog.show()
        }
    }
    
    private fun setupAmountInput() {
        binding.amountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty() && s.toString() != ".") {
                    try {
                        val amount = s.toString().toDouble()
                        binding.amountPreview.text = amount.toString()
                    } catch (e: NumberFormatException) {
                        binding.amountPreview.text = "0.00"
                    }
                } else {
                    binding.amountPreview.text = "0.00"
                }
            }
        })
    }
    
    private fun setupCategoryDropdown() {
        userViewModel.currentUser.value?.let { user ->
            categoryViewModel.getCategoriesByType(user.uid, TransactionType.EXPENSE)
                .observe(viewLifecycleOwner) { categories ->
                    if (categories.isNotEmpty()) {
                        val categoryNames = categories.map { it.name }.toTypedArray()
                        
                        // Create a map of category names to IDs
                        categoryIdMap.clear()
                        categories.forEach { category ->
                            categoryIdMap[category.name] = category.id
                            android.util.Log.d("AddExpenseFragment", "Category in map: ${category.name}, ID: ${category.id}")
                        }
                        
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            categoryNames
                        )
                        
                        binding.categoryInput.setAdapter(adapter)
                        
                        // Set default category if available
                        if (categoryNames.isNotEmpty()) {
                            val defaultCategory = categoryNames[0]
                            binding.categoryInput.setText(defaultCategory, false)
                            // Set the selected category ID for the first item
                            val id = categoryIdMap[defaultCategory]
                            android.util.Log.d("AddExpenseFragment", "Setting default category: $defaultCategory with ID: $id")
                            if (id != null) {
                                expenseViewModel.setSelectedCategory(id)
                            }
                        }
                    }
                }
        }
        
        // Listen for category selection changes - THIS IS CRITICAL
        binding.categoryInput.setOnItemClickListener { _, _, position, _ ->
            val selectedCategory = binding.categoryInput.adapter.getItem(position).toString()
            val id = categoryIdMap[selectedCategory]
            android.util.Log.d("AddExpenseFragment", "User selected category: $selectedCategory with ID: $id")
            if (id != null) {
                expenseViewModel.setSelectedCategory(id)
            }
        }
    }
    
    private fun setupPhotoSection() {
        binding.attachPhotoButton.setOnClickListener {
            findNavController().navigate(R.id.action_addExpense_to_photo)
        }
        
        binding.removePhotoButton.setOnClickListener {
            selectedPhotoPath = null
            expenseViewModel.setPhotoUri(null)
            updatePhotoPreview()
        }
    }
    
    private fun updatePhotoPreview() {
        if (selectedPhotoPath != null) {
            try {
                binding.photoPreview.setImageURI(Uri.parse(selectedPhotoPath))
                binding.photoPreviewContainer.visibility = View.VISIBLE
                binding.attachPhotoButton.visibility = View.GONE
                binding.removePhotoButton.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.photoPreviewContainer.visibility = View.GONE
                binding.attachPhotoButton.visibility = View.VISIBLE
                binding.removePhotoButton.visibility = View.GONE
            }
        } else {
            binding.photoPreviewContainer.visibility = View.GONE
            binding.attachPhotoButton.visibility = View.VISIBLE
            binding.removePhotoButton.visibility = View.GONE
        }
    }
    
    private fun setupPhotoResultListener() {
        // Listen for result from photo fragment
        setFragmentResultListener("photoResult") { _, bundle ->
            val photoPath = bundle.getString("photoPath")
            selectedPhotoPath = photoPath
            expenseViewModel.setPhotoUri(photoPath)
            updatePhotoPreview()
        }
    }
    
    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            // Validate inputs
            if (!validateInputs()) {
                return@setOnClickListener
            }
            
            val amount = binding.amountInput.text.toString().toDouble()
            val description = binding.descriptionInput.text.toString().trim()
            
            // Make sure we have the correct category ID by getting it directly from the map
            val categoryName = binding.categoryInput.text.toString()
            val categoryId = categoryIdMap[categoryName]
            
            android.util.Log.d("AddExpenseFragment", "Saving expense with category: $categoryName, ID: $categoryId")
            
            // Force update the selected category in the viewmodel to ensure it's correct
            if (categoryId != null) {
                expenseViewModel.setSelectedCategory(categoryId)
                android.util.Log.d("AddExpenseFragment", "Explicitly setting category ID to: $categoryId before saving")
            } else {
                android.util.Log.e("AddExpenseFragment", "Could not find category ID for: $categoryName")
                Toast.makeText(requireContext(), "Error: Could not determine category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            userViewModel.currentUser.value?.let { user ->
                expenseViewModel.saveExpense(user.uid, amount, description)
            } ?: run {
                Toast.makeText(requireContext(), "Please sign in to add expenses", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        // Validate amount
        val amountStr = binding.amountInput.text.toString()
        if (amountStr.isEmpty() || amountStr == "." || amountStr.toDoubleOrNull() == null || amountStr.toDoubleOrNull() == 0.0) {
            binding.amountLayout.error = "Please enter a valid amount"
            isValid = false
        } else {
            binding.amountLayout.error = null
        }
        
        // Validate description
        val description = binding.descriptionInput.text.toString().trim()
        if (description.isEmpty()) {
            binding.descriptionLayout.error = "Please enter a description"
            isValid = false
        } else {
            binding.descriptionLayout.error = null
        }
        
        // Validate category
        val category = binding.categoryInput.text.toString()
        if (category.isEmpty() || !categoryIdMap.containsKey(category)) {
            binding.categoryLayout.error = "Please select a valid category"
            isValid = false
        } else {
            binding.categoryLayout.error = null
        }
        
        // Validate date
        val dateStr = binding.dateInput.text.toString()
        try {
            dateFormat.parse(dateStr)
            binding.dateLayout.error = null
        } catch (e: ParseException) {
            binding.dateLayout.error = "Please enter a valid date"
            isValid = false
        }
        
        return isValid
    }
    
    private fun observeViewModel() {
        expenseViewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                Toast.makeText(requireContext(), "Expense saved successfully", Toast.LENGTH_SHORT).show()
               // expenseViewModel.resetSaveStatus()
                findNavController().navigateUp()
            } else if (success == false) {
                Toast.makeText(requireContext(), "Failed to save expense", Toast.LENGTH_SHORT).show()
              //  expenseViewModel.resetSaveStatus()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 