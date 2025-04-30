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
        observeUserData()
        setupStaggeredFadeInAnimation()
    }

    // Made use of ChatGPT and Grok to make the fade in animation for the dashboard.
    // The Staggered fade-in animation fades in and slides up views (welcome card, graph card, transactions card, FAB) sequentially.
    // Each view starts with alpha=0 and translationY=50, then animates to alpha=1 (600ms) and translationY=0 (500ms) with a 290ms delay between views.
    // Using Grok and ChatGPT it helped me to set the up the fade in animation and create the animator to create the transition effects.
    // The alpha keyword sets the opacity of the view to 0 making the view invisible.
    // The translationY keyword sets the position of the view.
    // The animator set method manages the animation and determines how the animations flows
    // val animators = viewsToAnimate.mapIndexed { index, view ->, this line maps each view to their animation
    // val fadeAnimator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f), this line creates the fade in animation
    // val slideAnimator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 50f, 0f), this line creates the slide up animation
    // I also asked ChatGPT and Grok how i could change the fade in duration to make it look better and more pleasing to watch.
    // So it suggested to set the duration to different values for each view.
    // Each duration is measured in milliseconds.
    // Then after setting the durations they are combined to create the animation
    // The startDelay keyword is used to control the delay between animations.
    // The duration of the startDelay is also measured in milliseconds
    // With the use of ChatGPT and Grok I learnt how to create the fade in animation for the dashboard and how to create the animator to create the transition effects.

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

            // Load categories for the expense adapter
            categoryViewModel.getCategoriesByType(user.uid, TransactionType.EXPENSE)
                .observe(viewLifecycleOwner) { categories ->
                    android.util.Log.d("DashboardFragment", "Loaded ${categories.size} categories for adapter")
                    expenseAdapter.updateCategoryMap(categories)
                }

            // Load recent expenses
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
            }

            // Update total monthly expenses
            expenseViewModel.fetchTotalMonthlyExpenses(user.uid)
            expenseViewModel.totalMonthlyExpenses.observe(viewLifecycleOwner) { total ->
                updateBalance(total)
            }
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