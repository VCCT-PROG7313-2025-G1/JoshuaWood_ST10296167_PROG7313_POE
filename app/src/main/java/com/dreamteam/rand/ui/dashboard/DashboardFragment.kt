package com.dreamteam.rand.ui.dashboard

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dreamteam.rand.R
import com.dreamteam.rand.data.RandDatabase
import com.dreamteam.rand.data.entity.Transaction
import com.dreamteam.rand.data.entity.TransactionType
import com.dreamteam.rand.data.repository.AiRepository
import com.dreamteam.rand.data.repository.CategoryRepository
import com.dreamteam.rand.data.repository.ExpenseRepository
import com.dreamteam.rand.databinding.FragmentDashboardBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import com.dreamteam.rand.ui.categories.CategoryViewModel
import com.dreamteam.rand.ui.expenses.ExpenseAdapter
import com.dreamteam.rand.ui.expenses.ExpenseViewModel
import com.dreamteam.rand.ui.common.ChartUtils
import com.google.android.material.navigation.NavigationView
import java.text.NumberFormat
import java.util.Locale
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.net.Uri
import android.widget.ImageView
import androidx.core.view.forEach
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.dreamteam.rand.ui.common.ViewUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.XAxis
import java.util.Calendar

class DashboardFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var aiRepository: AiRepository
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    private var recentExpenses: List<Transaction> = emptyList()
    private var originalCardElevation: Float = 0f
    private var borderAnimation: android.animation.ValueAnimator? = null
    private var glowAnimation: android.graphics.drawable.AnimationDrawable? = null

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
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        aiRepository = AiRepository()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupNavigationDrawer()
        setupClickListeners()
        setupExpensesList()
        setupExpensesChart()
        setupAiCard()
        observeUserData()
        setupStaggeredFadeInAnimation()
    }
    
    private fun setupAiCard() {
        // Set initial placeholder text with a hardcoded string to avoid reference issues
        binding.aiSuggestionText.text = "Your AI-powered spending insights will appear here. Generate insights to get personalized financial advice based on your recent expenses."
    }

    // used chatgpt and grok to create the dashboard animations
    // creates a smooth entrance effect for the welcome card, graph, and transactions
    // each element fades in and slides up with a 290ms delay between them
    private fun setupStaggeredFadeInAnimation() {
        // List of views to animate: welcome card, graph card, transactions card, FAB
        val viewsToAnimate = listOf(
            binding.welcomeText.parent.parent as View, // Welcome CardView
            binding.transactionsRecyclerView.parent.parent.parent.parent as View, // Graph CardView
            binding.transactionsRecyclerView.parent.parent.parent as View, // Transactions CardView
            binding.addExpenseFab // FAB
        )

        val animatorSet = AnimatorSet()
        val animators = viewsToAnimate.mapIndexed { index, view ->
            // Initialize view state
            view.alpha = 0f // Start with alpha at 0, this makes the view invisible
            view.translationY = 50f // Start with translationY at 50, this moves the view up

            // Create fade-in animator
            val fadeAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f) // Fade in
            fadeAnimator.duration = 500

            // Create slide-up animator
            val slideAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 50f, 0f) //
            slideAnimator.duration = 400

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

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupNavigationDrawer() {
        binding.navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> {
                // Already on dashboard
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_transactions -> {
                findNavController().navigate(R.id.action_dashboard_to_transactions)
            }
            R.id.nav_categories -> {
                findNavController().navigate(R.id.action_dashboard_to_categories)
            }
            R.id.nav_savings -> {
                findNavController().navigate(R.id.action_dashboard_to_savings)
            }
            R.id.nav_profile -> {
                findNavController().navigate(R.id.action_dashboard_to_profile)
            }
            R.id.nav_settings -> {
                findNavController().navigate(R.id.action_dashboard_to_settings)
            }
            R.id.nav_logout -> {
                // Just log out, MainActivity will handle the navigation
                userViewModel.logoutUser()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setupClickListeners() {
        binding.addExpenseFab.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addTransaction)
        }

        binding.viewAllExpensesButton.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_transactions)
        }
        
        // Navigate to expense analysis screen when chart is clicked
        binding.chartClickOverlay.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_expenseAnalysis)
        }
        
        binding.viewDetailsText.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_expenseAnalysis)
        }
        
        // Set up AI insights button
        binding.generateInsightsButton.setOnClickListener {
            generateAiInsights()
        }
    }

    private fun setupExpensesList() {
        expenseAdapter = ExpenseAdapter({ expense ->
            // Navigate to edit expense when implemented
        })

        // Set receipt click listener
        expenseAdapter.setOnReceiptClickListener { receiptUri ->
            showReceiptImage(receiptUri)
        }

        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = expenseAdapter
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

    private fun setupExpensesChart() {
        val chart = binding.expensesChart
        
        // Configure chart appearance
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.setPinchZoom(false)
        chart.setScaleEnabled(false)
        chart.legend.isEnabled = false
        
        // Configure axes
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMinimum = 0f
        
        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
        
        // Apply theme-aware styling
        ChartUtils.applyThemeAwareStyling(requireContext(), chart)
        
        // Set empty data initially
        chart.data = BarData()
    }
    
    private fun loadExpensesChartData(userId: String) {
        // Get the current month's start and end dates
        val calendar = Calendar.getInstance()
        
        // End date is today
        val endDate = calendar.timeInMillis
        
        // Start date is 30 days ago
        calendar.add(Calendar.DAY_OF_YEAR, -30)
        val startDate = calendar.timeInMillis
        
        // Get expenses for the last 30 days
        expenseViewModel.getExpensesByDateRange(userId, startDate, endDate)
            .observe(viewLifecycleOwner) { expenses ->
                if (expenses.isEmpty()) {
                    binding.noChartDataText.visibility = View.VISIBLE
                    binding.expensesChart.visibility = View.GONE
                    return@observe
                }
                
                // Group expenses by category
                val expensesByCategory = expenses.groupBy { it.categoryId }
                
                // Get all categories for this user
                categoryViewModel.getCategoriesByType(userId, TransactionType.EXPENSE)
                    .observe(viewLifecycleOwner) { categories ->
                        updateExpensesChart(expensesByCategory, categories)
                    }
            }
    }
    
    private fun updateExpensesChart(
        expensesByCategory: Map<Long?, List<Transaction>>,
        categories: List<com.dreamteam.rand.data.entity.Category>
    ) {
        if (expensesByCategory.isEmpty() || categories.isEmpty()) {
            binding.noChartDataText.visibility = View.VISIBLE
            binding.expensesChart.visibility = View.GONE
            return
        }
        
        binding.noChartDataText.visibility = View.GONE
        binding.expensesChart.visibility = View.VISIBLE
        
        // Create bar entries and category labels for the chart
        val entries = ArrayList<BarEntry>()
        val categoryLabels = ArrayList<String>()
        
        // Filter to only the top 5 categories by amount
        val topCategories = categories.filter { category ->
            val categoryExpenses = expensesByCategory[category.id] ?: emptyList()
            categoryExpenses.isNotEmpty()
        }.sortedByDescending { category ->
            val categoryExpenses = expensesByCategory[category.id] ?: emptyList()
            categoryExpenses.sumOf { expense -> expense.amount }
        }.take(5)
        
        // Create chart entries for each category
        topCategories.forEachIndexed { index, category ->
            val categoryExpenses = expensesByCategory[category.id] ?: emptyList()
            val totalAmount = categoryExpenses.sumOf { expense -> expense.amount }
            
            if (totalAmount > 0) {
                entries.add(BarEntry(index.toFloat(), totalAmount.toFloat()))
                categoryLabels.add(category.name)
            }
        }
        
        // Create and customize the dataset
        val dataSet = BarDataSet(entries, "Categories")
        dataSet.colors = topCategories.map {
            try {
                android.graphics.Color.parseColor(it.color)
            } catch (e: Exception) {
                android.graphics.Color.parseColor("#FF5252") // Default color
            }
        }
        dataSet.valueTextSize = 10f
        
        // Create bar data with the dataset
        val data = BarData(dataSet)
        data.barWidth = 0.6f
        
        // Update chart with new data
        val chart = binding.expensesChart
        chart.data = data
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(categoryLabels)
        chart.xAxis.labelCount = categoryLabels.size
        
        // Apply theme-aware styling
        ChartUtils.applyThemeAwareStyling(requireContext(), chart)
        
        // Refresh the chart
        chart.invalidate()
    }

    private fun observeUserData() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                // Navigation will be handled by the MainActivity observer
                return@observe
            }

            // Update welcome message
            binding.welcomeText.text = getString(R.string.welcome_message, user.name)

            // Update navigation drawer header
            binding.navigationView.getHeaderView(0).apply {
                findViewById<TextView>(R.id.navHeaderName).text = user.name
                findViewById<TextView>(R.id.navHeaderEmail).text = user.email
                
                // Update profile picture in navigation header
                val navHeaderImage = findViewById<ImageView>(R.id.navHeaderImage)
                user.profilePictureUri?.let { uri ->
                    try {
                        Glide.with(this@DashboardFragment)
                            .load(Uri.parse(uri))
                            .transform(CircleCrop())
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .into(navHeaderImage)
                    } catch (e: Exception) {
                        // If there's an error loading the custom image, fall back to default
                        Glide.with(this@DashboardFragment)
                            .load(R.drawable.ic_profile)
                            .transform(CircleCrop())
                            .into(navHeaderImage)
                    }
                } ?: run {
                    // Load default profile image
                    Glide.with(this@DashboardFragment)
                        .load(R.drawable.ic_profile)
                        .transform(CircleCrop())
                        .into(navHeaderImage)
                }
            }

            // Get expenses and sync with Firebase
            expenseViewModel.getExpenses(user.uid).observe(viewLifecycleOwner) { expenses ->
                android.util.Log.d("DashboardFragment", "Loaded ${expenses.size} expenses for dashboard")
                if (expenses.isEmpty()) {
                    binding.noTransactionsText.visibility = View.VISIBLE
                    binding.transactionsRecyclerView.visibility = View.GONE
                } else {
                    binding.noTransactionsText.visibility = View.GONE
                    binding.transactionsRecyclerView.visibility = View.VISIBLE

                    // Get the most recent 10 expenses for AI analysis, but only show 5
                    recentExpenses = expenses.sortedByDescending { it.date }.take(10)
                    android.util.Log.d("DashboardFragment", "Showing ${recentExpenses.take(5).size} recent expenses")

                    // Log category IDs of recent expenses
                    recentExpenses.forEach { expense ->
                        android.util.Log.d("DashboardFragment", "Recent expense: ${expense.description}, Category ID: ${expense.categoryId}")
                    }

                    expenseAdapter.submitList(recentExpenses.take(5))
                }
                
                // Update total monthly expenses after syncing
                expenseViewModel.fetchTotalMonthlyExpenses(user.uid)
            }

            // Load categories for the expense adapter
            categoryViewModel.getCategoriesByType(user.uid, TransactionType.EXPENSE)
                .observe(viewLifecycleOwner) { categories ->
                    android.util.Log.d("DashboardFragment", "Loaded ${categories.size} categories for adapter")
                    expenseAdapter.updateCategoryMap(categories)
                }

            // Observe total monthly expenses
            expenseViewModel.totalMonthlyExpenses.observe(viewLifecycleOwner) { total ->
                updateBalance(total)
            }
            
            // Load chart data
            loadExpensesChartData(user.uid)
        }
    }

    private fun updateBalance(amount: Double) { // Update the balance text
        val formattedAmount = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            .format(amount)
        binding.balanceText.text = formattedAmount
    }

    private fun generateAiInsights() {
        if (recentExpenses.isEmpty()) {
            Toast.makeText(requireContext(), "No expenses available for AI analysis", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Hide loading bar completely - we'll use the glowing border instead
        binding.aiLoadingProgressBar.visibility = View.GONE
        
        // Update button state
        binding.generateInsightsButton.isEnabled = false
        binding.generateInsightsButton.text = "Analyzing..."
        binding.aiSuggestionText.text = ""
        
        // Apply Apple-style rainbow glowing animation to the container border
        startGlowingAnimation()
        
        // Get the last 10 expenses (or fewer if not available)
        val expensesToAnalyze = recentExpenses.take(10)
        
        coroutineScope.launch {
            try {
                android.util.Log.d("DashboardFragment", "Sending ${expensesToAnalyze.size} expenses to AI service")
                val result = aiRepository.getAiInsights(expensesToAnalyze)
                
                result.fold(
                    onSuccess = { insight ->
                        android.util.Log.d("DashboardFragment", "AI insight received: $insight")
                        
                        // Stream the text response
                        streamTextResponse(insight)
                    },
                    onFailure = { error ->
                        android.util.Log.e("DashboardFragment", "AI insight error: ${error.message}", error)
                        
                        // Stop glowing animation first
                        stopGlowingAnimation()
                        
                        // Then show error message
                        binding.aiSuggestionText.text = "Could not generate insights: ${error.message}"
                        Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        
                        // Reset the button state
                        resetGenerateButton()
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("DashboardFragment", "Exception calling AI service", e)
                
                // Stop glowing animation first
                stopGlowingAnimation()
                
                // Then show error message
                binding.aiSuggestionText.text = "Could not connect to AI service"
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                
                // Reset the button state
                resetGenerateButton()
            }
            // We don't need the finally block since we're not using the progress bar anymore
        }
    }
    
    private fun startGlowingAnimation() {
        // Show and animate the glow background behind the card
        startCardGlowAnimation()
        
        // Also keep a simple border animation on the text container
        startBorderAnimation()
    }
    
    private fun startCardGlowAnimation() {
        // Get reference to the glow background
        val glowBackground = binding.root.findViewById<View>(com.dreamteam.rand.R.id.cardGlowBackground)
        
        // Show the glow
        glowBackground.visibility = View.VISIBLE
        
        // Create a series of drawable backgrounds with different colors
        val glowDrawables = arrayOf(
            createGlowDrawable("#8036A1FF"), // Blue
            createGlowDrawable("#809669FF"), // Purple
            createGlowDrawable("#80FF6CFF"), // Pink
            createGlowDrawable("#80FF5A5A"), // Red
            createGlowDrawable("#80FF9C41"), // Orange
            createGlowDrawable("#80FFDC41"), // Yellow
            createGlowDrawable("#803FE66F"), // Green
            createGlowDrawable("#8036A1FF")  // Back to blue
        )
        
        // Create a frame animation with the drawables
        val animation = android.graphics.drawable.AnimationDrawable()
        
        // Add frames to animation with 400ms per frame
        glowDrawables.forEach { drawable ->
            animation.addFrame(drawable, 400)
        }
        
        // Set the animation as the background
        glowBackground.background = animation
        
        // Start the animation
        animation.setEnterFadeDuration(1000)
        animation.setExitFadeDuration(1000)
        animation.start()
        
        // Store animation for later use
        glowAnimation = animation
    }
    
    private fun createGlowDrawable(colorString: String): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 18f * resources.displayMetrics.density
            
            // Determine if we're in dark mode
            val isNightMode = (resources.configuration.uiMode and 
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
                    android.content.res.Configuration.UI_MODE_NIGHT_YES
            
            // Create a radial gradient from color to transparent
            val color = android.graphics.Color.parseColor(colorString)
            val transparent = android.graphics.Color.TRANSPARENT
            
            // In dark mode, make the colors slightly more vibrant
            val adjustedColor = if (isNightMode) {
                // Increase alpha for better visibility in dark mode
                val alpha = Math.min(255, android.graphics.Color.alpha(color) + 40)
                android.graphics.Color.argb(
                    alpha,
                    android.graphics.Color.red(color),
                    android.graphics.Color.green(color),
                    android.graphics.Color.blue(color)
                )
            } else {
                color
            }
            
            colors = intArrayOf(adjustedColor, transparent)
            gradientType = android.graphics.drawable.GradientDrawable.RADIAL_GRADIENT
            gradientRadius = 250f * resources.displayMetrics.density
        }
    }
    
    private fun startBorderAnimation() {
        // Get a reference to the text container
        val textContainer = binding.aiSuggestionText
        
        // Determine if we're in dark mode
        val isNightMode = (resources.configuration.uiMode and 
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        
        // Get appropriate background color based on theme
        val backgroundColor = if (isNightMode) {
            android.graphics.Color.parseColor("#121212") // Dark background
        } else {
            android.graphics.Color.WHITE
        }
        
        // Create a drawable for the border
        val borderDrawable = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 12f * resources.displayMetrics.density
            setColor(backgroundColor)
            setStroke(2, android.graphics.Color.parseColor("#3F51B5"))
        }
        
        // Set the background
        textContainer.background = borderDrawable
        
        // Create the color animation
        val colorAnimator = android.animation.ValueAnimator.ofFloat(0f, 1f)
        colorAnimator.duration = 3000
        colorAnimator.repeatCount = android.animation.ValueAnimator.INFINITE
        colorAnimator.repeatMode = android.animation.ValueAnimator.RESTART
        
        // Apple-style pastel rainbow colors
        val colors = intArrayOf(
            android.graphics.Color.parseColor("#FF36A1FF"), // Blue
            android.graphics.Color.parseColor("#FF9669FF"), // Purple
            android.graphics.Color.parseColor("#FFFF6CFF"), // Pink
            android.graphics.Color.parseColor("#FFFF5A5A"), // Red
            android.graphics.Color.parseColor("#FFFF9C41"), // Orange
            android.graphics.Color.parseColor("#FFFFDC41"), // Yellow
            android.graphics.Color.parseColor("#FF3FE66F"), // Green
            android.graphics.Color.parseColor("#FF36A1FF")  // Back to blue
        )
        
        colorAnimator.addUpdateListener { animator ->
            val fraction = animator.animatedValue as Float
            val colorIndex = (fraction * (colors.size - 1)).toInt()
            val nextColorIndex = Math.min(colorIndex + 1, colors.size - 1)
            
            val color = evaluateColor(
                fraction * (colors.size - 1) - colorIndex,
                colors[colorIndex],
                colors[nextColorIndex]
            )
            
            // Update border color
            borderDrawable.setStroke(2, color)
        }
        
        colorAnimator.start()
        borderAnimation = colorAnimator
    }
    
    private fun stopGlowingAnimation() {
        // Stop the color animation
        borderAnimation?.cancel()
        borderAnimation = null
        
        // Reset the text container background based on theme
        val isNightMode = (resources.configuration.uiMode and 
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
                android.content.res.Configuration.UI_MODE_NIGHT_YES
                
        val backgroundColor = if (isNightMode) {
            android.graphics.Color.parseColor("#121212") // Dark background
        } else {
            android.graphics.Color.WHITE
        }
        
        val defaultDrawable = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 12f * resources.displayMetrics.density
            setColor(backgroundColor)
        }
        binding.aiSuggestionText.background = defaultDrawable
        
        // Hide the glow background
        val glowBackground = binding.root.findViewById<View>(com.dreamteam.rand.R.id.cardGlowBackground)
        glowBackground?.visibility = View.GONE
        
        // Stop the glow animation if it's running
        if (glowAnimation != null) {
            glowAnimation?.stop()
            glowAnimation = null
        }
    }
    
    private fun evaluateColor(fraction: Float, startColor: Int, endColor: Int): Int {
        val startA = android.graphics.Color.alpha(startColor)
        val startR = android.graphics.Color.red(startColor)
        val startG = android.graphics.Color.green(startColor)
        val startB = android.graphics.Color.blue(startColor)
        
        val endA = android.graphics.Color.alpha(endColor)
        val endR = android.graphics.Color.red(endColor)
        val endG = android.graphics.Color.green(endColor)
        val endB = android.graphics.Color.blue(endColor)
        
        return android.graphics.Color.argb(
            startA + (fraction * (endA - startA)).toInt(),
            startR + (fraction * (endR - startR)).toInt(),
            startG + (fraction * (endG - startG)).toInt(),
            startB + (fraction * (endB - startB)).toInt()
        )
    }
    
    private fun streamTextResponse(text: String) {
        // Clear previous animation
        stopGlowingAnimation()
        
        // Stream the text character by character
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            binding.aiSuggestionText.text = ""
            
            // Stream the text with a natural typing effect
            // Characters come in faster in the middle and slower at sentence breaks
            val words = text.split(" ")
            
            for (wordIndex in words.indices) {
                val word = words[wordIndex]
                
                // Determine if this is the end of a sentence
                val isEndOfSentence = word.endsWith(".") || word.endsWith("!") || word.endsWith("?")
                
                // Stream each character in the word
                for (charIndex in word.indices) {
                    binding.aiSuggestionText.append(word[charIndex].toString())
                    
                    // Adjust timing based on position
                    val delay = when {
                        isEndOfSentence && charIndex == word.length - 1 -> 300L // Pause at end of sentences
                        charIndex == word.length - 1 -> 50L  // Small pause between words
                        else -> 15L // Quick typing within words
                    }
                    
                    kotlinx.coroutines.delay(delay)
                }
                
                // Add space between words (except at the end)
                if (wordIndex < words.size - 1) {
                    binding.aiSuggestionText.append(" ")
                }
            }
            
            // Reset button after streaming is complete
            resetGenerateButton()
        }
    }
    
    private fun showRegenerateButton() {
        // We won't use the FAB animation for now since it's causing binding issues
        // Instead, we'll just reset the main button
        resetGenerateButton()
    }
    
    private fun resetGenerateButton() {
        // Reset the main button
        binding.generateInsightsButton.visibility = View.VISIBLE
        binding.generateInsightsButton.alpha = 1f
        binding.generateInsightsButton.isEnabled = true
        binding.generateInsightsButton.text = "Generate"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        borderAnimation?.cancel()
        borderAnimation = null
        _binding = null
    }
}