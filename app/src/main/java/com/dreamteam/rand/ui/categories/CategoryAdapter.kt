package com.dreamteam.rand.ui.categories

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dreamteam.rand.data.entity.Category
import com.dreamteam.rand.databinding.ItemCategoryBinding
import java.text.NumberFormat
import java.util.Locale

class CategoryAdapter(private val onEditClick: (Category) -> Unit) :
    ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private val categoryTotals = mutableMapOf<Long, Double>()

    fun updateCategoryTotal(categoryId: Long, total: Double) {
        categoryTotals[categoryId] = total
        // Find the position of the category and notify the change
        currentList.indexOfFirst { it.id == categoryId }.takeIf { it != -1 }?.let { position ->
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category, categoryTotals[category.id] ?: 0.0)
        holder.binding.editButton.setOnClickListener {
            onEditClick(category)
        }
    }

    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category, totalSpent: Double) {
            binding.categoryName.text = category.name
            
            // Format currency amount
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            binding.totalSpentAmount.text = currencyFormatter.format(totalSpent)

            // Set color
            try {
                binding.categoryColorIndicator.setBackgroundColor(Color.parseColor(category.color))
            } catch (e: Exception) {
                binding.categoryColorIndicator.setBackgroundColor(Color.GRAY)
            }

            // Set icon
            val resources = binding.root.context.resources
            val packageName = binding.root.context.packageName
            val iconResId = resources.getIdentifier(
                category.icon, "drawable", packageName
            )

            if (iconResId != 0) {
                binding.categoryIcon.setImageResource(iconResId)
            }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem && 
                   oldItem.name == newItem.name &&
                   oldItem.color == newItem.color &&
                   oldItem.icon == newItem.icon
        }
    }
}