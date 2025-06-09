package com.dreamteam.rand.ui.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.data.RandDatabase
import com.dreamteam.rand.data.entity.TransactionType
import com.dreamteam.rand.data.entity.Goal
import com.dreamteam.rand.data.entity.GoalSpendingStatus
import com.dreamteam.rand.data.repository.CategoryRepository
import com.dreamteam.rand.data.repository.ExpenseRepository
import com.dreamteam.rand.data.repository.GoalRepository
import com.dreamteam.rand.databinding.FragmentExpenseAnalysisBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import com.dreamteam.rand.ui.categories.CategoryViewModel
import com.dreamteam.rand.ui.goals.GoalViewModel
import com.dreamteam.rand.ui.common.ChartUtils
import com.dreamteam.rand.ui.common.ViewUtils
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.Calendar
import java.util.Date

class ExpenseAnalysisFragment : Fragment() {

    private var _binding: FragmentExpenseAnalysisBinding? = null
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
    
    private val goalViewModel: GoalViewModel by viewModels {
        val database = RandDatabase.getDatabase(requireContext())
        val repository = GoalRepository(database.goalDao())
        GoalViewModel.Factory(repository)
    }

    // Filter options
    private val timeFrames = arrayOf("Last 7 days", "Last 30 days", "Last 3 months", "Last 6 months", "Last year")
    private var selectedTimeFrame = 1 // Default to Last 30 days
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup back button
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        setupTimeFrameSpinner()
        setupCategoryFilter()
        setupChart()
        setupTrendChart()
        loadData()
    }
    
    private fun setupTimeFrameSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            timeFrames
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        binding.timeFrameSpinner.adapter = adapter
        binding.timeFrameSpinner.setSelection(selectedTimeFrame)
        binding.timeFrameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTimeFrame = position
                loadData()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    private fun setupCategoryFilter() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                categoryViewModel.getCategoriesByType(user.uid, TransactionType.EXPENSE)
                    .observe(viewLifecycleOwner) { categories ->
                        // Clear existing chips
                        binding.categoryChipGroup.removeAllViews()
                        
                        // Add "All Categories" chip
                        val allCategoriesChip = com.google.android.material.chip.Chip(requireContext()).apply {
                            text = getString(R.string.all_categories)
                            isCheckable = true
                            isChecked = true
                            tag = "all"
                            chipBackgroundColor = getColorStateList("#6200EE") // Primary color for all categories
                            setTextColor(android.graphics.Color.WHITE)
                        }
                        binding.categoryChipGroup.addView(allCategoriesChip)
                        
                        // Make the chip group single selection
                        binding.categoryChipGroup.isSingleSelection = true
                        
                        // Add a chip for each category
                        categories.forEach { category ->
                            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                                text = category.name
                                isCheckable = true
                                tag = category.id
                                chipBackgroundColor = getColorStateList(category.color)
                                setTextColor(android.graphics.Color.WHITE)
                            }
                            binding.categoryChipGroup.addView(chip)
                        }
                        
                        // Setup chip selection listener with proper handling for single selection
                        binding.categoryChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
                            if (checkedIds.isEmpty()) {
                                // If no chip is selected, select "All Categories" chip
                                allCategoriesChip.isChecked = true
                                loadData()
                            } else {
                                // Get the selected chip
                                val selectedChip = group.findViewById<com.google.android.material.chip.Chip>(checkedIds[0])
                                if (selectedChip.tag == "all") {
                                    // If "All Categories" is selected, load all data
                                    loadData()
                                } else {
                                    // Otherwise, filter by the selected category
                                    val categoryId = selectedChip.tag as Long
                                    loadDataByCategory(categoryId)
                                }
                            }
                        }
                    }
            }
        }
    }
    
    private fun getColorStateList(colorString: String): android.content.res.ColorStateList {
        val color = try {
            android.graphics.Color.parseColor(colorString)
        } catch (e: Exception) {
            android.graphics.Color.parseColor("#FF5252") // Default color
        }
        
        return android.content.res.ColorStateList.valueOf(color)
    }
    
    private fun setupChart() {
        val chart = binding.expensesChart
        
        // Reset the chart
        chart.clear()
        chart.fitScreen()
        
        // Customize the chart
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.setPinchZoom(false)
        chart.setScaleEnabled(false)
        chart.setExtraOffsets(10f, 10f, 10f, 10f) // Add padding around the chart
        
        // Customize X axis
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setCenterAxisLabels(false) // Don't center labels to prevent overlap
        xAxis.setDrawGridLines(false)
        
        // Customize left Y axis
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.spaceTop = 35f
        leftAxis.axisMinimum = 0f
        leftAxis.setDrawLimitLinesBehindData(false) // Draw limit lines on top of data
        
        // Customize right Y axis
        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
        
        // Apply theme-aware styling
        ChartUtils.applyThemeAwareStyling(requireContext(), chart)
        
        // Set empty data initially
        chart.data = BarData()
        chart.setFitBars(true)
        chart.invalidate()
    }
    
    private fun setupTrendChart() {
        val chart = binding.trendChart
        
        // Reset the chart
        chart.clear()
        chart.fitScreen()
        
        // Customize the chart
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setPinchZoom(false)
        chart.setScaleEnabled(true)
        chart.legend.isEnabled = true
        chart.setExtraOffsets(10f, 10f, 10f, 10f) // Add padding around the chart
        
        // Customize X axis
        val xAxis = chart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        
        // Customize left Y axis
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMinimum = 0f
        leftAxis.setDrawLimitLinesBehindData(false) // Draw limit lines on top of data
        
        // Customize right Y axis
        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
        
        // Apply theme-aware styling
        ChartUtils.applyThemeAwareStyling(requireContext(), chart)
        
        // Set empty data initially
        chart.data = com.github.mikephil.charting.data.LineData()
        chart.invalidate()
    }
    
    private fun loadData() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val startDate = getStartDateForTimeFrame(selectedTimeFrame)
                val endDate = System.currentTimeMillis()
                
                expenseViewModel.getExpensesByDateRange(user.uid, startDate, endDate)
                    .observe(viewLifecycleOwner) { expenses ->
                        if (expenses.isNotEmpty()) {
                            categoryViewModel.getCategoriesByType(user.uid, TransactionType.EXPENSE)
                                .observe(viewLifecycleOwner) { categories ->
                                    updateChart(expenses, categories)
                                    updateTrendChart(expenses)
                                    updateBudgetComparison(expenses, categories)
                                }
                        } else {
                            // Show empty state
                            binding.emptyStateLayout.visibility = View.VISIBLE
                            binding.expensesChart.visibility = View.GONE
                            binding.trendChart.visibility = View.GONE
                            binding.budgetComparisonRecyclerView.visibility = View.GONE
                        }
                    }
            }
        }
    }
    
    private fun loadDataByCategory(categoryId: Long) {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val startDate = getStartDateForTimeFrame(selectedTimeFrame)
                val endDate = System.currentTimeMillis()
                
                expenseViewModel.getExpensesByCategoryAndDateRange(
                    userId = user.uid,
                    categoryId = categoryId,
                    startDate = startDate,
                    endDate = endDate
                ).observe(viewLifecycleOwner) { expenses ->
                    if (expenses.isNotEmpty()) {
                        categoryViewModel.getCategoriesByType(user.uid, TransactionType.EXPENSE)
                            .observe(viewLifecycleOwner) { categories ->
                                // Filter categories to only show the selected one
                                val selectedCategory = categories.find { it.id == categoryId }
                                if (selectedCategory != null) {
                                    updateChart(expenses, listOf(selectedCategory))
                                    updateTrendChart(expenses)
                                    updateBudgetComparison(expenses, listOf(selectedCategory))
                                }
                            }
                    } else {
                        // Show empty state
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.expensesChart.visibility = View.GONE
                        binding.trendChart.visibility = View.GONE
                        binding.budgetComparisonRecyclerView.visibility = View.GONE
                    }
                }
            }
        }
    }
    
    private fun updateChart(expenses: List<com.dreamteam.rand.data.entity.Transaction>, 
                           categories: List<com.dreamteam.rand.data.entity.Category>) {
        
        if (expenses.isEmpty() || categories.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.expensesChart.visibility = View.GONE
            return
        }
        
        binding.emptyStateLayout.visibility = View.GONE
        binding.expensesChart.visibility = View.VISIBLE
        
        // Group expenses by category
        val expensesByCategory = expenses.groupBy { it.categoryId }
        
        // Create bar entries and category labels
        val entries = ArrayList<BarEntry>()
        val categoryLabels = ArrayList<String>()
        val categoryColors = ArrayList<Int>()
        
        // Only add categories with expenses
        val categoriesWithExpenses = categories.filter { category ->
            val categoryExpenses = expensesByCategory[category.id] ?: emptyList()
            categoryExpenses.isNotEmpty() && categoryExpenses.sumOf { it.amount } > 0
        }
        
        categoriesWithExpenses.forEachIndexed { index, category ->
            val categoryExpenses = expensesByCategory[category.id] ?: emptyList()
            val totalAmount = categoryExpenses.sumOf { it.amount }
            
            if (totalAmount > 0) {
                entries.add(BarEntry(index.toFloat(), totalAmount.toFloat()))
                categoryLabels.add(category.name)
                
                // Parse the category color
                try {
                    categoryColors.add(android.graphics.Color.parseColor(category.color))
                } catch (e: Exception) {
                    categoryColors.add(android.graphics.Color.parseColor("#FF5252")) // Default color
                }
            }
        }
        
        if (entries.isEmpty()) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.expensesChart.visibility = View.GONE
            return
        }
        
        // Get chart reference
        val chart = binding.expensesChart
        
        // Reset the chart completely
        chart.clear()
        chart.fitScreen()
        
        // Basic chart configuration
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setDrawBarShadow(false)
        chart.setDrawValueAboveBar(true)
        chart.setPinchZoom(false)
        chart.setScaleEnabled(false)
        
        // Configure X-axis first
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setCenterAxisLabels(false) // Changed to false to prevent overlap
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = IndexAxisValueFormatter(categoryLabels)
        xAxis.labelCount = categoryLabels.size
        
        // Configure Y-axis
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.spaceTop = 35f
        leftAxis.axisMinimum = 0f
        
        // Clear previous limit lines
        leftAxis.removeAllLimitLines()
        
        // Disable right axis
        chart.axisRight.isEnabled = false
        
        // Get current month and year for goals
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1  // Calendar months are 0-based
        val currentYear = calendar.get(Calendar.YEAR)
        
        // Fetch goals for the current month/year
        userViewModel.currentUser.value?.let { user ->
            goalViewModel.getGoalsByMonthAndYear(user.uid, currentMonth, currentYear)
                .observe(viewLifecycleOwner) { goals ->
                    if (goals.isNotEmpty()) {
                        addGoalReferencesToChart(goals, chart, leftAxis, entries)
                    }
                }
        }
        
        // Create dataset with custom formatting
        val dataSet = BarDataSet(entries, "Expenses by Category")
        dataSet.colors = categoryColors
        dataSet.valueTextSize = 10f
        dataSet.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value > 1000) {
                    java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "ZA"))
                        .format(value).replace(".00", "")
                } else {
                    java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "ZA"))
                        .format(value)
                }
            }
        }
        
        // Create and configure bar data
        val data = BarData(dataSet)
        data.barWidth = 0.6f
        
        // Apply data to chart
        chart.data = data
        
        // Apply theme-aware styling
        ChartUtils.applyThemeAwareStyling(requireContext(), chart)
        
        // Refresh the chart
        chart.notifyDataSetChanged()
        chart.invalidate()
    }
    
    private fun addGoalReferencesToChart(
        goals: List<Goal>,
        chart: BarChart,
        leftAxis: com.github.mikephil.charting.components.YAxis,
        entries: List<BarEntry>
    ) {
        var maxGoalLimit = 0f
        
        // Add reference lines for min and max goal values
        goals.forEach { goal ->
            // Min spending target line
            val minLimitLine = com.github.mikephil.charting.components.LimitLine(
                goal.minAmount.toFloat(),
                "Min Target: ${goal.name}"
            )
            minLimitLine.lineWidth = 1.5f
            minLimitLine.lineColor = android.graphics.Color.parseColor("#4CAF50") // Green
            minLimitLine.enableDashedLine(10f, 5f, 0f)
            minLimitLine.labelPosition = com.github.mikephil.charting.components.LimitLine.LimitLabelPosition.RIGHT_BOTTOM
            minLimitLine.textSize = 10f
            minLimitLine.yOffset = 8f
            leftAxis.addLimitLine(minLimitLine)
            
            // Max spending limit line
            val maxLimitLine = com.github.mikephil.charting.components.LimitLine(
                goal.maxAmount.toFloat(),
                "Max Limit: ${goal.name}"
            )
            maxLimitLine.lineWidth = 1.5f
            maxLimitLine.lineColor = android.graphics.Color.RED
            maxLimitLine.enableDashedLine(10f, 5f, 0f)
            maxLimitLine.labelPosition = com.github.mikephil.charting.components.LimitLine.LimitLabelPosition.RIGHT_TOP
            maxLimitLine.textSize = 10f
            maxLimitLine.yOffset = 8f
            leftAxis.addLimitLine(maxLimitLine)
            
            // Track maximum value for axis scaling
            if (goal.maxAmount.toFloat() > maxGoalLimit) {
                maxGoalLimit = goal.maxAmount.toFloat()
            }
        }
        
        // Apply theme-aware styling to limit lines
        ChartUtils.applyThemeAwareStylingToLimitLines(requireContext(), leftAxis.limitLines)
        
        // Set axis maximum to accommodate both data and goal limit lines
        val maxValue = entries.maxOfOrNull { it.y } ?: 0f
        leftAxis.axisMaximum = maxOf(maxValue * 1.2f, maxGoalLimit * 1.2f)
        
        // Ensure limit lines are drawn on top of the data
        leftAxis.setDrawLimitLinesBehindData(false)
        
        // Refresh the chart
        chart.notifyDataSetChanged()
        chart.invalidate()
    }
    
    private fun getCustomColors(categories: List<com.dreamteam.rand.data.entity.Category>): List<Int> {
        // Use category colors if possible, otherwise default to a predefined set
        val colors = categories.map {
            try {
                android.graphics.Color.parseColor(it.color)
            } catch (e: Exception) {
                ColorTemplate.MATERIAL_COLORS[0]
            }
        }
        
        return if (colors.isEmpty()) {
            ColorTemplate.MATERIAL_COLORS.toList()
        } else {
            colors
        }
    }
    
    private fun getStartDateForTimeFrame(timeFrame: Int): Long {
        val calendar = Calendar.getInstance()
        when (timeFrame) {
            0 -> calendar.add(Calendar.DAY_OF_YEAR, -7) // Last 7 days
            1 -> calendar.add(Calendar.DAY_OF_YEAR, -30) // Last 30 days
            2 -> calendar.add(Calendar.MONTH, -3) // Last 3 months
            3 -> calendar.add(Calendar.MONTH, -6) // Last 6 months
            4 -> calendar.add(Calendar.YEAR, -1) // Last year
        }
        return calendar.timeInMillis
    }
    
    private fun updateTrendChart(expenses: List<com.dreamteam.rand.data.entity.Transaction>) {
        if (expenses.isEmpty()) {
            binding.trendChart.visibility = View.GONE
            return
        }
        
        binding.trendChart.visibility = View.VISIBLE
        
        // Group expenses by day
        val calendar = Calendar.getInstance()
        val expensesByDay = expenses.groupBy { expense ->
            calendar.timeInMillis = expense.date
            // Reset time part to get just the day
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }
        
        // Sort days chronologically
        val sortedDays = expensesByDay.keys.sorted()
        
        // Create entries for the line chart
        val entries = ArrayList<com.github.mikephil.charting.data.Entry>()
        val dayLabels = ArrayList<String>()
        val dateFormatter = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
        
        // Add entries for each day
        sortedDays.forEachIndexed { index, day ->
            val dailyExpenses = expensesByDay[day] ?: emptyList()
            val totalAmount = dailyExpenses.sumOf { it.amount }
            
            entries.add(com.github.mikephil.charting.data.Entry(index.toFloat(), totalAmount.toFloat()))
            dayLabels.add(dateFormatter.format(java.util.Date(day)))
        }
        
        // Get chart reference and reset it
        val chart = binding.trendChart
        chart.clear()
        chart.fitScreen()
        
        // Configure chart basics
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setPinchZoom(false)
        chart.setScaleEnabled(true)
        chart.legend.isEnabled = true
        
        // Configure X axis
        val xAxis = chart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(dayLabels)
        xAxis.labelCount = minOf(7, dayLabels.size) // Show at most 7 labels to avoid crowding
        
        // Configure Y axis
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMinimum = 0f
        
        // Disable right axis
        chart.axisRight.isEnabled = false
        
        // Clear previous limit lines
        leftAxis.removeAllLimitLines()
        
        // Create dataset
        val dataSet = com.github.mikephil.charting.data.LineDataSet(entries, "Daily Expenses")
        dataSet.color = android.graphics.Color.parseColor("#4CAF50")
        dataSet.lineWidth = 2f
        dataSet.setCircleColor(android.graphics.Color.parseColor("#4CAF50"))
        dataSet.circleRadius = 4f
        dataSet.setDrawCircleHole(false)
        dataSet.valueTextSize = 10f
        dataSet.mode = com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER
        dataSet.setDrawFilled(true)
        dataSet.fillColor = android.graphics.Color.parseColor("#4CAF50")
        dataSet.fillAlpha = 50
        
        // Get current month and year for goals
        val currentMonth = calendar.get(Calendar.MONTH) + 1  // Calendar months are 0-based
        val currentYear = calendar.get(Calendar.YEAR)
        
        // Fetch goals for daily reference lines
        userViewModel.currentUser.value?.let { user ->
            goalViewModel.getGoalsByMonthAndYear(user.uid, currentMonth, currentYear)
                .observe(viewLifecycleOwner) { goals ->
                    if (goals.isNotEmpty()) {
                        addGoalReferencesToTrendChart(goals, chart, leftAxis, entries, dayLabels.size)
                    }
                }
        }
        
        // Ensure limit lines are drawn on top of the data
        leftAxis.setDrawLimitLinesBehindData(false)
        
        // Create line data with the dataset
        val lineData = com.github.mikephil.charting.data.LineData(dataSet)
        
        // Set the data
        chart.data = lineData
        
        // Apply theme-aware styling
        ChartUtils.applyThemeAwareStyling(requireContext(), chart)
        
        // Add animation and refresh
        chart.animateX(500)
        chart.invalidate()
    }
    
    private fun addGoalReferencesToTrendChart(
        goals: List<Goal>,
        chart: com.github.mikephil.charting.charts.LineChart,
        leftAxis: com.github.mikephil.charting.components.YAxis,
        entries: List<com.github.mikephil.charting.data.Entry>,
        daysInPeriod: Int
    ) {
        // Get the most relevant goal (e.g., first one or one with highest priority)
        val primaryGoal = goals.firstOrNull() ?: return
        
        // Calculate daily average spending targets
        val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        val dailyMinTarget = primaryGoal.minAmount / daysInMonth
        val dailyMaxLimit = primaryGoal.maxAmount / daysInMonth
        
        // Add min daily target reference line
        val minLimitLine = com.github.mikephil.charting.components.LimitLine(
            dailyMinTarget.toFloat(),
            "Min Daily Target"
        )
        minLimitLine.lineWidth = 1.5f
        minLimitLine.lineColor = android.graphics.Color.parseColor("#4CAF50") // Green
        minLimitLine.enableDashedLine(10f, 5f, 0f)
        minLimitLine.labelPosition = com.github.mikephil.charting.components.LimitLine.LimitLabelPosition.RIGHT_BOTTOM
        minLimitLine.textSize = 10f
        minLimitLine.yOffset = 8f
        leftAxis.addLimitLine(minLimitLine)
        
        // Add max daily limit reference line
        val maxLimitLine = com.github.mikephil.charting.components.LimitLine(
            dailyMaxLimit.toFloat(),
            "Max Daily Limit"
        )
        maxLimitLine.lineWidth = 1.5f
        maxLimitLine.lineColor = android.graphics.Color.RED
        maxLimitLine.enableDashedLine(10f, 5f, 0f)
        maxLimitLine.labelPosition = com.github.mikephil.charting.components.LimitLine.LimitLabelPosition.RIGHT_TOP
        maxLimitLine.textSize = 10f
        maxLimitLine.yOffset = 8f
        leftAxis.addLimitLine(maxLimitLine)
        
        // Apply theme-aware styling to limit lines
        ChartUtils.applyThemeAwareStylingToLimitLines(requireContext(), leftAxis.limitLines)
        
        // Update axis to accommodate the limit lines
        val maxExpense = entries.maxOfOrNull { it.y } ?: 0f
        leftAxis.axisMaximum = maxOf(maxExpense * 1.2f, dailyMaxLimit.toFloat() * 1.2f)
        
        // Refresh chart with new limits
        chart.notifyDataSetChanged()
        chart.invalidate()
    }
    
    private fun updateBudgetComparison(
        expenses: List<com.dreamteam.rand.data.entity.Transaction>,
        categories: List<com.dreamteam.rand.data.entity.Category>
    ) {
        // Ensure we have the recycler view setup
        if (binding.budgetComparisonRecyclerView.adapter == null) {
            binding.budgetComparisonRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
            binding.budgetComparisonRecyclerView.adapter = BudgetComparisonAdapter()
        }
        
        // Get current month and year for goals
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1  // Calendar months are 0-based
        val currentYear = calendar.get(Calendar.YEAR)
        
        // Group expenses by category
        val expensesByCategory = expenses.groupBy { it.categoryId }
        
        // Create budget comparison items from categories
        val categoryBudgetItems = categories
            .filter { category -> category.budget != null && category.budget!! > 0 }
            .map { category ->
                val categoryExpenses = expensesByCategory[category.id] ?: emptyList()
                val totalSpent = categoryExpenses.sumOf { it.amount }
                val budget = category.budget ?: 0.0
                
                BudgetComparisonItem(
                    categoryName = category.name,
                    categoryColor = category.color,
                    spent = totalSpent,
                    budget = budget,
                    progress = if (budget > 0) (totalSpent / budget * 100).toInt().coerceAtMost(100) else 0
                )
            }
        
        // Fetch goals for the budget comparison
        userViewModel.currentUser.value?.let { user ->
            goalViewModel.getGoalsByMonthAndYear(user.uid, currentMonth, currentYear)
                .observe(viewLifecycleOwner) { goals ->
                    // Create goal budget items
                    val goalBudgetItems = goals.map { goal ->
                        val totalSpent = goal.currentSpent
                        val maxBudget = goal.maxAmount
                        
                        BudgetComparisonItem(
                            categoryName = "${goal.name} (Goal)",
                            categoryColor = goal.color,
                            spent = totalSpent,
                            budget = maxBudget,
                            progress = if (maxBudget > 0) (totalSpent / maxBudget * 100).toInt().coerceAtMost(100) else 0,
                            isGoal = true
                        )
                    }
                    
                    // Combine category and goal budget items and sort by progress
                    val allBudgetItems = (categoryBudgetItems + goalBudgetItems)
                        .sortedByDescending { it.progress }
                    
                    // Update the adapter with new data
                    (binding.budgetComparisonRecyclerView.adapter as BudgetComparisonAdapter).submitList(allBudgetItems)
                    
                    // Show/hide based on whether we have budget data
                    if (allBudgetItems.isNotEmpty()) {
                        binding.budgetComparisonRecyclerView.visibility = View.VISIBLE
                    } else {
                        binding.budgetComparisonRecyclerView.visibility = View.GONE
                    }
                }
        }
    }
    
    // Budget comparison data class
    data class BudgetComparisonItem(
        val categoryName: String,
        val categoryColor: String,
        val spent: Double,
        val budget: Double,
        val progress: Int,
        val isGoal: Boolean = false
    )
    
    // Budget comparison adapter
    inner class BudgetComparisonAdapter : 
        androidx.recyclerview.widget.ListAdapter<BudgetComparisonItem, BudgetComparisonViewHolder>(
            object : androidx.recyclerview.widget.DiffUtil.ItemCallback<BudgetComparisonItem>() {
                override fun areItemsTheSame(oldItem: BudgetComparisonItem, newItem: BudgetComparisonItem): Boolean {
                    return oldItem.categoryName == newItem.categoryName
                }
                
                override fun areContentsTheSame(oldItem: BudgetComparisonItem, newItem: BudgetComparisonItem): Boolean {
                    return oldItem == newItem
                }
            }
        ) {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetComparisonViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = com.dreamteam.rand.databinding.ItemBudgetComparisonBinding.inflate(inflater, parent, false)
            return BudgetComparisonViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: BudgetComparisonViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }
    
    // Budget comparison view holder
    inner class BudgetComparisonViewHolder(
        private val binding: com.dreamteam.rand.databinding.ItemBudgetComparisonBinding
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: BudgetComparisonItem) {
            binding.categoryNameText.text = item.categoryName
            
            val currencyFormatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "ZA"))
            binding.spentText.text = currencyFormatter.format(item.spent)
            binding.budgetText.text = currencyFormatter.format(item.budget)
            
            binding.progressBar.progress = item.progress
            binding.progressBar.progressTintList = getColorStateList(item.categoryColor)
            
            // Set progress text
            binding.progressText.text = "${item.progress}%"
            
            // Set color indicator
            binding.colorIndicator.setBackgroundColor(
                try {
                    android.graphics.Color.parseColor(item.categoryColor)
                } catch (e: Exception) {
                    android.graphics.Color.parseColor("#FF5252") // Default color
                }
            )
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 