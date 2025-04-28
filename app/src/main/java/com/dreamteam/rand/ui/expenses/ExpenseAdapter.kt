package com.dreamteam.rand.ui.expenses

import android.graphics.Color
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

    // Add a click handler for receipt images
    private var onReceiptClick: ((String) -> Unit)? = null

    fun setOnReceiptClickListener(listener: (String) -> Unit) {
        onReceiptClick = listener
    }

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
        holder.bind(getItem(position), onItemClick, categoryMap, onReceiptClick)
    }

    class ExpenseViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(expense: Transaction, onItemClick: (Transaction) -> Unit, categoryMap: Map<Long, Category>, onReceiptClick: ((String) -> Unit)?) {
            val context = binding.root.context
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            
            // Set expense amount
            binding.expenseAmount.text = currencyFormatter.format(expense.amount)
            
            // Set expense description
            binding.expenseDescription.text = expense.description
            
            // Set expense date
            binding.expenseDate.text = dateFormat.format(Date(expense.date))
            
            // By default, hide receipt elements
            binding.receiptImageIcon.visibility = View.GONE
            binding.viewReceiptButton.visibility = View.GONE
            
            // Set category details if available
            expense.categoryId?.let { id ->
                android.util.Log.d("ExpenseAdapter", "Expense has category ID: $id, looking for category in map with ${categoryMap.size} items")
                categoryMap[id]?.let { category ->
                    android.util.Log.d("ExpenseAdapter", "Found category: ${category.name}")
                    binding.categoryName.text = category.name
                    binding.categoryName.visibility = View.VISIBLE
                    
                    // Try to set category color indicator
                    try {
                        binding.categoryColorIndicator.setBackgroundColor(Color.parseColor(category.color))
                    } catch (e: Exception) {
                        binding.categoryColorIndicator.setBackgroundColor(context.getColor(R.color.primary))
                    }
                    
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
                    binding.categoryColorIndicator.setBackgroundColor(context.getColor(R.color.grey))
                }
            } ?: run {
                android.util.Log.d("ExpenseAdapter", "Expense has no category ID")
                binding.categoryName.visibility = View.GONE
                binding.categoryIcon.visibility = View.GONE
                binding.categoryColorIndicator.setBackgroundColor(context.getColor(R.color.grey))
            }
            
            // Show receipt icon and button if receipt is available
            expense.receiptUri?.let { uri ->
                binding.receiptImageIcon.visibility = View.VISIBLE
                binding.viewReceiptButton.visibility = View.VISIBLE
                
                // Set click listener for receipt view
                val receiptClickListener = View.OnClickListener {
                    onReceiptClick?.invoke(uri)
                }
                
                binding.receiptImageIcon.setOnClickListener(receiptClickListener)
                binding.viewReceiptButton.setOnClickListener(receiptClickListener)
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