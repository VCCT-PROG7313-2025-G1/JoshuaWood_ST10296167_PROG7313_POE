package com.dreamteam.rand.ui.expenses

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dreamteam.rand.R
import com.dreamteam.rand.data.entity.Category
import com.dreamteam.rand.data.entity.Transaction
import com.dreamteam.rand.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseAdapter(
    private val onItemClick: (Transaction) -> Unit,
    private val categoryMap: MutableMap<Long, Category> = mutableMapOf()
) : ListAdapter<Transaction, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    fun updateCategoryMap(categories: List<Category>) {
        android.util.Log.d("ExpenseAdapter", "Updating category map with ${categories.size} categories")
        
        // Log current map content
        android.util.Log.d("ExpenseAdapter", "Current map has ${categoryMap.size} entries")
        
        // Clear and repopulate the map
        categoryMap.clear()
        categories.forEach { category -> 
            android.util.Log.d("ExpenseAdapter", "Adding category to map: ${category.name}, ID: ${category.id}")
            categoryMap[category.id] = category
        }
        
        // Log new map content
        android.util.Log.d("ExpenseAdapter", "Updated map now has ${categoryMap.size} entries")
        
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick, categoryMap)
    }

    class ExpenseViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(expense: Transaction, onItemClick: (Transaction) -> Unit, categoryMap: Map<Long, Category>) {
            val context = binding.root.context
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            
            // Set expense amount
            binding.expenseAmount.text = currencyFormatter.format(expense.amount)
            
            // Set expense description
            binding.expenseDescription.text = expense.description
            
            // Set expense date
            binding.expenseDate.text = dateFormat.format(Date(expense.date))
            
            // Set category details if available
            expense.categoryId?.let { id ->
                android.util.Log.d("ExpenseAdapter", "Expense has category ID: $id, looking for category in map with ${categoryMap.size} items")
                categoryMap[id]?.let { category ->
                    android.util.Log.d("ExpenseAdapter", "Found category: ${category.name}")
                    binding.categoryName.text = category.name
                    binding.categoryName.visibility = View.VISIBLE
                    
                    // Try to load icon resource
                    try {
                        val iconResourceId = context.resources.getIdentifier(
                            category.icon, "drawable", context.packageName
                        )
                        if (iconResourceId != 0) {
                            binding.categoryIcon.setImageResource(iconResourceId)
                            binding.categoryIcon.visibility = View.VISIBLE
                        } else {
                            binding.categoryIcon.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        binding.categoryIcon.visibility = View.GONE
                    }
                } ?: run {
                    android.util.Log.d("ExpenseAdapter", "Category ID $id not found in map")
                    binding.categoryName.visibility = View.GONE
                    binding.categoryIcon.visibility = View.GONE
                }
            } ?: run {
                android.util.Log.d("ExpenseAdapter", "Expense has no category ID")
                binding.categoryName.visibility = View.GONE
                binding.categoryIcon.visibility = View.GONE
            }
            
            // Show receipt icon if receipt is available
            binding.receiptIndicator.visibility = if (expense.receiptUri != null) View.VISIBLE else View.GONE
            
            // Show receipt thumbnail if available
            expense.receiptUri?.let { uri ->
                try {
                    binding.receiptThumbnail.setImageURI(Uri.parse(uri))
                    binding.receiptThumbnail.visibility = View.VISIBLE
                } catch (e: Exception) {
                    binding.receiptThumbnail.visibility = View.GONE
                }
            } ?: run {
                binding.receiptThumbnail.visibility = View.GONE
            }
            
            // Set click listener for the item
            binding.root.setOnClickListener {
                onItemClick(expense)
            }
        }
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
} 