package com.dreamteam.rand.ui.dashboard

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dreamteam.rand.R
import com.dreamteam.rand.data.RandDatabase
import com.dreamteam.rand.data.entity.Transaction
import com.dreamteam.rand.data.entity.TransactionType
import com.dreamteam.rand.data.repository.CategoryRepository
import com.dreamteam.rand.data.repository.ExpenseRepository
import com.dreamteam.rand.databinding.FragmentDashboardBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import com.dreamteam.rand.ui.categories.CategoryViewModel
import com.dreamteam.rand.ui.expenses.ExpenseAdapter
import com.dreamteam.rand.ui.expenses.ExpenseViewModel
import com.google.android.material.navigation.NavigationView
import java.text.NumberFormat
import java.util.Locale
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupNavigationDrawer()
        setupClickListeners()
        setupExpensesList()
        setupExpensesChart()
        observeUserData()
        setupStaggeredFadeInAnimation()
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
            fadeAnimator.duration = 600

            // Create slide-up animator
            val slideAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 50f, 0f) //
            slideAnimator.duration = 500

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

                    // Only show the most recent 5 expenses
                    val recentExpenses = expenses.sortedByDescending { it.date }.take(5)
                    android.util.Log.d("DashboardFragment", "Showing ${recentExpenses.size} recent expenses")

                    // Log category IDs of recent expenses
                    recentExpenses.forEach { expense ->
                        android.util.Log.d("DashboardFragment", "Recent expense: ${expense.description}, Category ID: ${expense.categoryId}")
                    }

                    expenseAdapter.submitList(recentExpenses)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}