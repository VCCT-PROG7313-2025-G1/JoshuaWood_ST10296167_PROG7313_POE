package com.dreamteam.rand.ui.categories

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.GradientDrawable

// this fragment lets you create a new category
// you can pick a name, color, and icon for your category
class AddCategoryFragment : Fragment() {
    private val TAG = "AddCategoryFragment"

    // binding to access the views
    private var _binding: FragmentAddCategoryBinding? = null
    private val binding get() = _binding!!

    // grab the current user and category viewmodel
    private val userViewModel: UserViewModel by activityViewModels()
    private val categoryViewModel: CategoryViewModel by viewModels {
        val database = RandDatabase.getDatabase(requireContext())
        val repository = CategoryRepository(database.categoryDao())
        CategoryViewModel.Factory(repository)
    }

    // keep track of what the user picked
    private var selectedColor = "#FF5252" // start with red
    private var selectedIconName = "ic_food" // start with food icon

    // map of icon names to their drawable resources
    private val iconResourceMap = mapOf(
        "ic_food" to R.drawable.ic_food,
        "ic_shopping" to R.drawable.ic_shopping,
        "ic_transport" to R.drawable.ic_transport,
        "ic_health" to R.drawable.ic_health,
        "ic_entertainment" to R.drawable.ic_entertainment
    )

    // create the view for this fragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Creating add category view")
        _binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    // setup everything after the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Setting up add category view")
        // set up all the ui stuff
        setupToolbar()
        categoryViewModel.setSelectedType(TransactionType.EXPENSE)
        // ensure the initial color is set in the ViewModel
        categoryViewModel.setSelectedColor(selectedColor)
        categoryViewModel.setSelectedIcon(selectedIconName)
        setupPreview()
        setupColorOptions()
        setupIconOptions()
        setupNameInput()
        setupSaveButton()
        observeViewModel()
        setupStaggeredFadeInAnimation()
        
        // initialize the preview name
        binding.previewCategoryName.text = "Category"
    }

    // used chatgpt and grok to create the add category animations
    // creates a smooth entrance effect for the category items
    // each element fades in and slides up with a 290ms delay between them
     private fun setupStaggeredFadeInAnimation() {
        // List of views to animate: name input, preview card, color picker, icon picker, save button
        val viewsToAnimate = listOf(
            binding.categoryNameLayout,
            binding.previewIconContainer.parent.parent as View, // CardView containing preview
            binding.colorOptionsLayout,
            binding.iconOptionsLayout,
            binding.saveButton
        )

        val animatorSet = AnimatorSet()
        val animators = viewsToAnimate.mapIndexed { index, view ->
            // Initialize view state
            view.alpha = 0f // Start with alpha at 0, this makes the view invisible
            view.translationY = 50f

            // Used chat to help structure the animation for the fade in
            // Create fade-in animator
            val fadeAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
            fadeAnimator.duration = 600 // Duration for fade-in

            // Create slide-up animator
            val slideAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 50f, 0f)
            slideAnimator.duration = 500 // Duration for slide-up

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

    // set up the back button in the toolbar
    private fun setupToolbar() {
        Log.d(TAG, "Setting up toolbar")
        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Toolbar back button clicked")
            findNavController().navigateUp()
        }
    }

    // Update the preview icon container with the selected color
    private fun updatePreviewColor() {
        try {
            Log.d(TAG, "Updating preview with color: $selectedColor")
            
            // Remove any existing background tint to avoid conflicts
            binding.previewIconContainer.backgroundTintList = null
            
            // Create a new drawable for the preview container
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.OVAL
            shape.setColor(Color.parseColor(selectedColor))
            binding.previewIconContainer.background = shape
            
            Log.d(TAG, "Preview color updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating preview color: ${e.message}")
        }
    }

    // set up the preview card with the default color and icon
    private fun setupPreview() {
        Log.d(TAG, "Setting up category preview")
        
        try {
            // Update the preview color
            updatePreviewColor()
            
            // start with the default icon
            binding.previewIconImage.setImageResource(iconResourceMap[selectedIconName] ?: R.drawable.ic_food)
            
            Log.d(TAG, "Preview setup complete with color: $selectedColor")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up preview: ${e.message}")
        }
    }

    // set up the color picker buttons
    private fun setupColorOptions() {
        Log.d(TAG, "Setting up color options")
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
                try {
                    Log.d(TAG, "Color selected: ${colors[index]}")
                    // hide all other indicators
                    colorViews.forEach { (_, ind) -> ind.visibility = View.GONE }
                    indicator.visibility = View.VISIBLE
                    selectedColor = colors[index]

                    // Update the preview with the selected color
                    updatePreviewColor()
                    
                    // Update the ViewModel with the selected color
                    categoryViewModel.setSelectedColor(selectedColor)
                    
                    Log.d(TAG, "Color applied: ${colors[index]}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error applying color: ${e.message}")
                }
            }
        }
    }

    // set up the icon picker buttons
    private fun setupIconOptions() {
        Log.d(TAG, "Setting up icon options")
        // get all the icon cards and their images
        val iconCards = listOf(
            binding.icon1Card,
            binding.icon2Card,
            binding.icon3Card,
            binding.icon4Card,
            binding.icon5Card
        )

        val iconViews = listOf(
            binding.icon1,
            binding.icon2,
            binding.icon3,
            binding.icon4,
            binding.icon5
        )

        // the icons they can pick from
        val iconNames = listOf(
            "ic_food",
            "ic_shopping",
            "ic_transport",
            "ic_health",
            "ic_entertainment"
        )

        // start with the first icon selected
        selectIcon(0, iconNames[0], iconCards, iconViews)

        // when they click an icon, select it
        iconCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                Log.d(TAG, "Icon selected: ${iconNames[index]}")
                selectIcon(index, iconNames[index], iconCards, iconViews)
            }
        }
    }

    // handle when an icon is selected
    private fun selectIcon(index: Int, iconName: String, cards: List<MaterialCardView>, icons: List<ImageView>) {
        Log.d(TAG, "Selecting icon: $iconName")
        // remember which icon they picked
        selectedIconName = iconName

        // update the card borders and icon colors
        cards.forEachIndexed { i, card ->
            if (i == index) {
                // highlight the selected icon
                card.strokeWidth = resources.getDimensionPixelSize(R.dimen.card_selected_stroke_width)
                card.strokeColor = ContextCompat.getColor(requireContext(), R.color.primary)
                icons[i].setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary))
            } else {
                // unhighlight the others
                card.strokeWidth = 0
                icons[i].setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey))
            }
        }

        // update the preview with the new icon
        val resourceId = iconResourceMap[iconName] ?: R.drawable.ic_food
        binding.previewIconImage.setImageResource(resourceId)

        // tell the viewmodel which icon was picked
        categoryViewModel.setSelectedIcon(iconName)
    }

    // set up the name input field
    private fun setupNameInput() {
        Log.d(TAG, "Setting up name input")
        binding.categoryNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                Log.d(TAG, "Category name changed: ${s?.toString()}")
                // clear any error message when they type
                binding.categoryNameLayout.error = null
                
                // Update the preview category name
                binding.previewCategoryName.text = s?.toString()?.takeIf { it.isNotEmpty() } ?: "Category"
            }
        })
    }

    // set up the save button with validation
    private fun setupSaveButton() {
        Log.d(TAG, "Setting up save button")
        binding.saveButton.setOnClickListener {
            val categoryName = binding.categoryNameInput.text.toString().trim()

            // Log detailed information about the category being created
            Log.d(TAG, "------------- Creating New Category -------------")
            Log.d(TAG, "Name: $categoryName")
            Log.d(TAG, "Color: $selectedColor")
            Log.d(TAG, "Icon: $selectedIconName")
            Log.d(TAG, "Type: ${categoryViewModel.selectedType.value}")
            Log.d(TAG, "------------------------------------------------")

            Log.d(TAG, "Attempting to save category - Name: $categoryName, Icon: $selectedIconName, Color: $selectedColor")

            // check if they entered a name
            if (categoryName.isEmpty()) {
                Log.d(TAG, "Validation failed: Empty category name")
                binding.categoryNameLayout.error = "Category name is required"
                return@setOnClickListener
            } else {
                binding.categoryNameLayout.error = null
            }

            // save the category if we have a user
            userViewModel.currentUser.value?.let { user ->
                Log.d(TAG, "Saving category for user: ${user.uid}")
                categoryViewModel.saveCategory(user.uid, categoryName)
            } ?: run {
                Log.w(TAG, "Cannot save category: No user logged in")
                Toast.makeText(requireContext(), "Please sign in to add categories", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // watch for when the category gets saved
    private fun observeViewModel() {
        Log.d(TAG, "Setting up ViewModel observers")
        categoryViewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            when (success) {
                true -> {
                    Log.d(TAG, "✅ Category saved successfully!")
                    Toast.makeText(requireContext(), "Category saved successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                false -> {
                    Log.e(TAG, "❌ Failed to save category")
                    Toast.makeText(requireContext(), "Failed to save category", Toast.LENGTH_SHORT).show()
                }
                null -> {
                    // still saving, do nothing
                }
            }
        }
    }

    // clean up when the view is destroyed
    override fun onDestroyView() {
        Log.d(TAG, "Destroying add category view")
        super.onDestroyView()
        _binding = null
    }
}