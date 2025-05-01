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
import android.util.Log
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
import android.animation.AnimatorSet
import android.animation.ObjectAnimator

// this fragment lets you add a new expense with all its details
// you can pick a category, date, amount, and attach a photo of the receipt
class AddExpenseFragment : Fragment() {
    private val TAG = "AddExpenseFragment"

    // binding to access all the views
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    // viewmodels to handle user data, categories, and expenses
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

    // keep track of the photo path and category IDs
    private var selectedPhotoPath: String? = null
    private val categoryIdMap = mutableMapOf<String, Long>()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    // create the view for adding an expense
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Creating add expense view")
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    // setup all the UI components after the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Setting up add expense view")
        setupToolbar()
        setupDatePicker()
        setupAmountInput()
        setupCategoryDropdown()
        setupPhotoSection()
        setupSaveButton()
        observeViewModel()
        setupPhotoResultListener()
        setupStaggeredFadeInAnimation()
    }

    // ai declaration: here we used chatgpt to create the staggered fade-in animations
    // that make form elements appear one after another for a smooth entrance effect
    private fun setupStaggeredFadeInAnimation() {
        // List of views to animate: amount section, description, category, date, photo section, save button
        val viewsToAnimate = listOf(
            binding.amountPreview, // Amount preview TextView
            binding.amountLayout, // Amount input TextInputLayout
            binding.descriptionLayout, // Description input
            binding.categoryLayout, // Category dropdown
            binding.dateLayout, // Date input
            binding.attachPhotoButton.parent as View, // FrameLayout containing photo button or preview
            binding.saveButton // Save button
        )

        val animatorSet = AnimatorSet()
        val animators = viewsToAnimate.mapIndexed { index, view ->
            // Initialize view state
            view.alpha = 0f // Start with alpha at 0, this makes the view invisible
            view.translationY = 50f // Start with a small translation to show from

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
            Log.d(TAG, "Navigating back")
            findNavController().navigateUp()
        }
    }

    // ai declaration: here we used gpt to implement date picker dialog integration
    // with proper date formatting and calendar selection
    private fun setupDatePicker() {
        Log.d(TAG, "Setting up date picker")
        // start with today's date
        binding.dateInput.setText(dateFormat.format(Date()))

        binding.dateInput.setOnClickListener {
            Log.d(TAG, "Opening date picker dialog")
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    binding.dateInput.setText(dateFormat.format(calendar.time))
                    expenseViewModel.setSelectedDate(calendar.timeInMillis)
                    Log.d(TAG, "Date selected: ${dateFormat.format(calendar.time)}")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.show()
        }
    }

    // setup the amount input with live preview of the number
    private fun setupAmountInput() {
        Log.d(TAG, "Setting up amount input")
        binding.amountInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty() && s.toString() != ".") {
                    try {
                        val amount = s.toString().toDouble()
                        binding.amountPreview.text = amount.toString()
                        Log.d(TAG, "Amount preview updated: $amount")
                    } catch (e: NumberFormatException) {
                        Log.w(TAG, "Invalid amount format: ${s.toString()}")
                        binding.amountPreview.text = "0.00"
                    }
                } else {
                    binding.amountPreview.text = "0.00"
                }
            }
        })
    }

    // setup the dropdown to pick which category the expense belongs to
    private fun setupCategoryDropdown() {
        Log.d(TAG, "Setting up category dropdown")
        
        // Get selected category ID directly from ViewModel
        val selectedCategoryId = expenseViewModel.selectedCategoryId.value
        Log.d(TAG, "Current selected category ID from ViewModel: $selectedCategoryId")
        
        userViewModel.currentUser.value?.let { user ->
            categoryViewModel.getCategoriesByType(user.uid, TransactionType.EXPENSE)
                .observe(viewLifecycleOwner) { categories ->
                    if (categories.isNotEmpty()) {
                        val categoryNames = categories.map { it.name }.toTypedArray()

                        // keep track of which category name goes with which ID
                        categoryIdMap.clear()
                        categories.forEach { category ->
                            categoryIdMap[category.name] = category.id
                            Log.d(TAG, "Category in map: ${category.name}, ID: ${category.id}")
                        }

                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            categoryNames
                        )

                        binding.categoryInput.setAdapter(adapter)

                        // Check if we should restore a previous category selection
                        if (selectedCategoryId != null) {
                            // Find the category name corresponding to the ID
                            val categoryNameToRestore = categories.find { it.id == selectedCategoryId }?.name
                            if (categoryNameToRestore != null && categoryNames.contains(categoryNameToRestore)) {
                                binding.categoryInput.setText(categoryNameToRestore, false)
                                Log.d(TAG, "Restored category from ViewModel ID: $selectedCategoryId, Name: $categoryNameToRestore")
                            } else {
                                // Selected category no longer exists, use default
                                val defaultCategory = categoryNames[0]
                                binding.categoryInput.setText(defaultCategory, false)
                                val id = categoryIdMap[defaultCategory]
                                Log.d(TAG, "Selected category no longer exists, setting default: $defaultCategory with ID: $id")
                                if (id != null) {
                                    expenseViewModel.setSelectedCategory(id)
                                }
                            }
                        } else if (categoryNames.isNotEmpty()) {
                            // No previous selection, use first category as default
                            val defaultCategory = categoryNames[0]
                            binding.categoryInput.setText(defaultCategory, false)
                            val id = categoryIdMap[defaultCategory]
                            Log.d(TAG, "Setting default category: $defaultCategory with ID: $id")
                            if (id != null) {
                                expenseViewModel.setSelectedCategory(id)
                            }
                        }
                    }
                }
        }

        // handle when user picks a different category
        binding.categoryInput.setOnItemClickListener { _, _, position, _ ->
            val selectedCategory = binding.categoryInput.adapter.getItem(position).toString()
            val id = categoryIdMap[selectedCategory]
            Log.d(TAG, "User selected category: $selectedCategory with ID: $id")
            if (id != null) {
                expenseViewModel.setSelectedCategory(id)
            }
        }
    }

    // ai declaration: here we used claude to design the photo attachment system
    // for handling receipt photos and image preview functionality
    private fun setupPhotoSection() {
        Log.d(TAG, "Setting up photo section")
        binding.attachPhotoButton.setOnClickListener {
            Log.d(TAG, "Navigating to photo screen")
            findNavController().navigate(R.id.action_addExpense_to_photo)
        }

        binding.removePhotoButton.setOnClickListener {
            Log.d(TAG, "Removing attached photo")
            selectedPhotoPath = null
            expenseViewModel.setPhotoUri(null)
            updatePhotoPreview()
        }
    }

    // update the photo preview when a photo is added or removed
    private fun updatePhotoPreview() {
        Log.d(TAG, "Updating photo preview")
        if (selectedPhotoPath != null) {
            try {
                binding.photoPreview.setImageURI(Uri.parse(selectedPhotoPath))
                binding.photoPreviewContainer.visibility = View.VISIBLE
                binding.attachPhotoButton.visibility = View.GONE
                binding.removePhotoButton.visibility = View.VISIBLE
                Log.d(TAG, "Photo preview updated successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating photo preview: ${e.message}")
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

    // listen for when a photo is taken or picked from gallery
    private fun setupPhotoResultListener() {
        Log.d(TAG, "Setting up photo result listener")
        setFragmentResultListener("photoResult") { _, bundle ->
            val photoPath = bundle.getString("photoPath")
            selectedPhotoPath = photoPath
            expenseViewModel.setPhotoUri(photoPath)
            Log.d(TAG, "Received photo path: $photoPath")
            updatePhotoPreview()
        }
    }

    // setup the save button to create the expense
    private fun setupSaveButton() {
        Log.d(TAG, "Setting up save button")
        binding.saveButton.setOnClickListener {
            Log.d(TAG, "Save button clicked")
            // check if all the required fields are filled
            if (!validateInputs()) {
                Log.w(TAG, "Input validation failed")
                return@setOnClickListener
            }

            val amount = binding.amountInput.text.toString().toDouble()
            val description = binding.descriptionInput.text.toString().trim()

            // Make sure we have the correct category ID by getting it directly from the map
            val categoryName = binding.categoryInput.text.toString()
            val categoryId = categoryIdMap[categoryName]

            // Log detailed information about the expense being created
            Log.d(TAG, "------------- Creating New Expense -------------")
            Log.d(TAG, "Description: $description")
            Log.d(TAG, "Amount: $amount")
            Log.d(TAG, "Category Name: $categoryName")
            Log.d(TAG, "Category ID: $categoryId")
            Log.d(TAG, "Date: ${binding.dateInput.text}")
            Log.d(TAG, "Has Receipt Image: ${selectedPhotoPath != null}")
            Log.d(TAG, "Receipt Path: $selectedPhotoPath")
            Log.d(TAG, "------------------------------------------------")

            Log.d(TAG, "Saving expense with category: $categoryName, ID: $categoryId")

            // Force update the selected category in the viewmodel to ensure it's correct
            if (categoryId != null) {
                expenseViewModel.setSelectedCategory(categoryId)
                Log.d(TAG, "Explicitly setting category ID to: $categoryId before saving")
            } else {
                Log.e(TAG, "Could not find category ID for: $categoryName")
                Toast.makeText(requireContext(), "Error: Could not determine category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userViewModel.currentUser.value?.let { user ->
                Log.d(TAG, "Saving expense for user: ${user.uid}")
                expenseViewModel.saveExpense(user.uid, amount, description)
            } ?: run {
                Log.w(TAG, "No user logged in")
                Toast.makeText(requireContext(), "Please sign in to add expenses", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // validates all input fields before saving
    private fun validateInputs(): Boolean {
        Log.d(TAG, "Validating inputs")
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
                Log.d(TAG, "✅ Expense saved successfully!")
                Toast.makeText(requireContext(), "Expense saved successfully", Toast.LENGTH_SHORT).show()
                // expenseViewModel.resetSaveStatus()
                findNavController().navigateUp()
            } else if (success == false) {
                Log.e(TAG, "❌ Failed to save expense")
                Toast.makeText(requireContext(), "Failed to save expense", Toast.LENGTH_SHORT).show()
                // expenseViewModel.resetSaveStatus()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}