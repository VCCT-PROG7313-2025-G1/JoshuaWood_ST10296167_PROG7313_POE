package com.dreamteam.rand.ui.dashboard

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.FragmentDashboardBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import com.google.android.material.navigation.NavigationView
import java.text.NumberFormat
import java.util.Locale
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat

class DashboardFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by viewModels()
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault())

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
        observeUserData()
        setupTransactionsList()
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
            R.id.nav_budget -> {
                findNavController().navigate(R.id.action_dashboard_to_budget)
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
                userViewModel.logoutUser()
                findNavController().navigate(R.id.action_dashboard_to_welcome)
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setupClickListeners() {
        binding.addTransactionFab.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addTransaction)
        }
    }

    private fun observeUserData() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                findNavController().navigate(R.id.action_dashboard_to_welcome)
                return@observe
            }

            // Update welcome message
            binding.welcomeText.text = getString(R.string.welcome_message, user.name)

            // Update navigation drawer header
            binding.navigationView.getHeaderView(0).apply {
                findViewById<TextView>(R.id.navHeaderName).text = user.name
                findViewById<TextView>(R.id.navHeaderEmail).text = user.email
            }

            // TODO: Update balance and other user-specific data
            updateBalance(5000.00) // Placeholder amount
            updateBudgetProgress(650.00, 1000.00) // Placeholder budget values
        }
    }

    private fun setupTransactionsList() {
        binding.transactionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            // TODO: Set adapter when implemented
        }

        // TODO: Observe transactions and update UI
        // For now, show the no transactions message
        binding.noTransactionsText.visibility = View.VISIBLE
        binding.transactionsRecyclerView.visibility = View.GONE
    }

    private fun updateBalance(amount: Double) {
        val formattedAmount = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            .format(amount)
        binding.balanceText.text = formattedAmount
    }

    private fun updateBudgetProgress(spent: Double, total: Double) {
        val progress = ((spent / total) * 100).toInt()
        binding.budgetProgress.progress = progress
        
        val formattedSpent = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            .format(spent)
        val formattedTotal = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            .format(total)
        binding.budgetText.text = getString(R.string.budget_progress, formattedSpent, formattedTotal)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 