package com.dreamteam.rand.ui.expenses

import android.graphics.Color
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

// this adapter shows a list of expenses in a recyclerview
// each expense shows its amount, description, date, category, and receipt if it has one
class ExpenseAdapter(
    private val onItemClick: (Transaction) -> Unit,
    private val categoryMap: MutableMap<Long, Category> = mutableMapOf()
) : ListAdapter<Transaction, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    // ai declaration: here we used gpt to help with the expense view binding 
    // and dynamic category lookup for displaying transactions
    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick, categoryMap, onReceiptClick)
    }

    // handle clicks on receipt images
    private var onReceiptClick: ((String) -> Unit)? = null

    // ai declaration: here we used claude to create the receipt image handling
    // with click listeners for viewing receipt photos
    fun setOnReceiptClickListener(listener: (String) -> Unit) {
        onReceiptClick = listener
    }

    // update the map of categories that we use to show category details
    fun updateCategoryMap(categories: List<Category>) {
        android.util.Log.d("ExpenseAdapter", "Updating category map with ${categories.size} categories")
        
        // Log current map content
        android.util.Log.d("ExpenseAdapter", "Current map has ${categoryMap.size} entries")
        
        // clear and repopulate the map
        categoryMap.clear()
        categories.forEach { category -> 
            android.util.Log.d("ExpenseAdapter", "Adding category to map: ${category.name}, ID: ${category.id}")
            categoryMap[category.id] = category
        }
        
        // Log new map content
        android.util.Log.d("ExpenseAdapter", "Updated map now has ${categoryMap.size} entries")
        
        notifyDataSetChanged()
    }

    // create a new view for an expense
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    // this class holds all the views for a single expense
    class ExpenseViewHolder(private val binding: ItemExpenseBinding) : RecyclerView.ViewHolder(binding.root) {
        
        // fill in all the expense details
        fun bind(expense: Transaction, onItemClick: (Transaction) -> Unit, categoryMap: Map<Long, Category>, onReceiptClick: ((String) -> Unit)?) {
            val context = binding.root.context
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            
            // show how much was spent
            binding.expenseAmount.text = currencyFormatter.format(expense.amount)
            
            // show what it was spent on
            binding.expenseDescription.text = expense.description
            
            // show when it was spent
            binding.expenseDate.text = dateFormat.format(Date(expense.date))
            
            // hide receipt stuff by default
            binding.receiptImageIcon.visibility = View.GONE
            binding.viewReceiptButton.visibility = View.GONE
            
            // show category details if we have them
            expense.categoryId?.let { id ->
                android.util.Log.d("ExpenseAdapter", "Expense has category ID: $id, looking for category in map with ${categoryMap.size} items")
                categoryMap[id]?.let { category ->
                    android.util.Log.d("ExpenseAdapter", "Found category: ${category.name}")
                    binding.categoryName.text = category.name
                    binding.categoryName.visibility = View.VISIBLE
                    
                    // try to show the category's color
                    try {
                        binding.categoryColorIndicator.setBackgroundColor(Color.parseColor(category.color))
                    } catch (e: Exception) {
                        binding.categoryColorIndicator.setBackgroundColor(context.getColor(R.color.primary))
                    }
                    
                    // try to show the category's icon
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
            
            // show receipt stuff if we have a receipt
            expense.receiptUri?.let { uri ->
                binding.receiptImageIcon.visibility = View.VISIBLE
                binding.viewReceiptButton.visibility = View.VISIBLE
                
                // handle clicks on the receipt
                val receiptClickListener = View.OnClickListener {
                    onReceiptClick?.invoke(uri)
                }
                
                binding.receiptImageIcon.setOnClickListener(receiptClickListener)
                binding.viewReceiptButton.setOnClickListener(receiptClickListener)
            }
            
            // handle clicks on the whole expense item
            binding.root.setOnClickListener {
                onItemClick(expense)
            }
        }
    }

    // this class helps the recyclerview figure out what changed in the list
    class ExpenseDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        // check if two items are the same expense
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        // check if the expense's details changed
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
} 