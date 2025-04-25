package com.dreamteam.rand.ui.categories

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.data.RandDatabase
import com.dreamteam.rand.data.entity.TransactionType
import com.dreamteam.rand.data.repository.CategoryRepository
import com.dreamteam.rand.databinding.FragmentAddCategoryBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import com.google.android.material.card.MaterialCardView

class AddCategoryFragment : Fragment() {
    private var _binding: FragmentAddCategoryBinding? = null
    private val binding get() = _binding!!
    
    private val userViewModel: UserViewModel by activityViewModels()
    private val categoryViewModel: CategoryViewModel by viewModels {
        val database = RandDatabase.getDatabase(requireContext())
        val repository = CategoryRepository(database.categoryDao())
        CategoryViewModel.Factory(repository)
    }
    
    private var selectedColor = "#FF5252" // Default red color
    private var selectedIconName = "ic_food" // Default food icon
    
    // Define icon resource IDs
    private val iconResourceMap = mapOf(
        "ic_food" to R.drawable.ic_food,
        "ic_shopping" to R.drawable.ic_shopping,
        "ic_transport" to R.drawable.ic_transport,
        "ic_health" to R.drawable.ic_health,
        "ic_entertainment" to R.drawable.ic_entertainment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        categoryViewModel.setSelectedType(TransactionType.EXPENSE)
        setupPreview()
        setupColorOptions()
        setupIconOptions()
        setupNameInput()
        setupSaveButton()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupPreview() {
        // Set default values for the preview
        binding.previewCategoryName.text = "Food"
        binding.previewIconContainer.setBackgroundColor(Color.parseColor("#FF5252"))
        binding.previewIconImage.setImageResource(R.drawable.ic_food)
    }
    
    private fun setupNameInput() {
        binding.categoryNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString().trim()
                // Update preview with the entered name
                binding.previewCategoryName.text = if (text.isEmpty()) "Category" else text
            }
        })
    }

    private fun setupColorOptions() {
        // Color views
        val colorViews = listOf(
            binding.color1,
            binding.color2,
            binding.color3,
            binding.color4,
            binding.color5
        )
        
        // Selected indicators
        val colorSelectedIndicators = listOf(
            binding.color1Selected,
            binding.color2Selected,
            binding.color3Selected,
            binding.color4Selected,
            binding.color5Selected
        )
        
        val colors = listOf(
            "#FF5252", // Red
            "#448AFF", // Blue
            "#66BB6A", // Green
            "#FFC107", // Yellow
            "#9C27B0"  // Purple
        )
        
        // Set initial selection
        selectColor(0, colors[0], colorSelectedIndicators)
        
        // Set up click listeners for color options
        colorViews.forEachIndexed { index, colorView ->
            colorView.setOnClickListener {
                selectColor(index, colors[index], colorSelectedIndicators)
            }
        }
    }
    
    private fun selectColor(index: Int, color: String, indicators: List<ImageView>) {
        // Update the selected color
        selectedColor = color
        
        // Update check mark visibility
        indicators.forEachIndexed { i, indicator ->
            indicator.visibility = if (i == index) View.VISIBLE else View.GONE
        }
        
        // Update preview background - use backgroundTintList instead of setBackgroundColor
        binding.previewIconContainer.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
        
        // Update selected color in ViewModel
        categoryViewModel.setSelectedColor(color)
    }

    private fun setupIconOptions() {
        // Icon cards
        val iconCards = listOf(
            binding.icon1Card,
            binding.icon2Card,
            binding.icon3Card,
            binding.icon4Card,
            binding.icon5Card
        )
        
        // Icon views
        val iconViews = listOf(
            binding.icon1,
            binding.icon2,
            binding.icon3,
            binding.icon4,
            binding.icon5
        )
        
        val iconNames = listOf(
            "ic_food",
            "ic_shopping",
            "ic_transport",
            "ic_health",
            "ic_entertainment"
        )
        
        // Set initial selection
        selectIcon(0, iconNames[0], iconCards, iconViews)
        
        // Set up click listeners for icon options
        iconCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                selectIcon(index, iconNames[index], iconCards, iconViews)
            }
        }
    }
    
    private fun selectIcon(index: Int, iconName: String, cards: List<MaterialCardView>, icons: List<ImageView>) {
        // Update the selected icon
        selectedIconName = iconName
        
        // Update the card stroke and icon tint
        cards.forEachIndexed { i, card ->
            if (i == index) {
                card.strokeWidth = resources.getDimensionPixelSize(R.dimen.card_selected_stroke_width)
                card.strokeColor = ContextCompat.getColor(requireContext(), R.color.primary)
                icons[i].setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary))
            } else {
                card.strokeWidth = 0
                icons[i].setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey))
            }
        }
        
        // Update preview icon
        val resourceId = iconResourceMap[iconName] ?: R.drawable.ic_food
        binding.previewIconImage.setImageResource(resourceId)
        
        // Update selected icon in ViewModel
        categoryViewModel.setSelectedIcon(iconName)
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener {
            val categoryName = binding.categoryNameInput.text.toString().trim()
            
            if (categoryName.isEmpty()) {
                binding.categoryNameLayout.error = "Category name is required"
                return@setOnClickListener
            } else {
                binding.categoryNameLayout.error = null
            }
            
            userViewModel.currentUser.value?.let { user ->
                categoryViewModel.saveCategory(user.uid, categoryName)
            } ?: run {
                Toast.makeText(requireContext(), "Please sign in to add categories", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        categoryViewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                Toast.makeText(requireContext(), "Category saved successfully", Toast.LENGTH_SHORT).show()
              //  categoryViewModel.resetSaveStatus()
                findNavController().navigateUp()
            } else if (success == false) {
                Toast.makeText(requireContext(), "Failed to save category", Toast.LENGTH_SHORT).show()
             //   categoryViewModel.resetSaveStatus()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 