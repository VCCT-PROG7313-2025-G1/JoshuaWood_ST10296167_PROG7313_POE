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

// this adapter shows a list of categories in a recyclerview
// each category shows its name, how much was spent, its color, and an icon
class CategoryAdapter(private val onEditClick: (Category) -> Unit) :
    ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    // keep track of how much was spent in each category
    private val categoryTotals = mutableMapOf<Long, Double>()

    // update how much was spent in a category and refresh its display
    fun updateCategoryTotal(categoryId: Long, total: Double) {
        categoryTotals[categoryId] = total
        // Find the position of the category and notify the change
        currentList.indexOfFirst { it.id == categoryId }.takeIf { it != -1 }?.let { position ->
            notifyItemChanged(position)
        }
    }

    // create a new view for a category
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    // fill in the category info when it's shown
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category, categoryTotals[category.id] ?: 0.0)
    }

    // this class holds all the views for a single category
    inner class CategoryViewHolder(val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Helper method to set category color properly
        private fun setColorIndicator(colorHex: String) {
            try {
                // Ensure color has # prefix
                val safeColorHex = if (colorHex.startsWith("#")) colorHex else "#$colorHex"
                android.util.Log.d("CategoryAdapter", "Setting color indicator: $safeColorHex")
                
                val colorInt = Color.parseColor(safeColorHex)
                val drawable = binding.root.context.getDrawable(com.dreamteam.rand.R.drawable.circle_shape)?.mutate()
                drawable?.let {
                    if (it is android.graphics.drawable.GradientDrawable) {
                        it.setColor(colorInt)
                        binding.categoryColorIndicator.background = it
                    } else {
                        binding.categoryColorIndicator.setBackgroundColor(colorInt)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CategoryAdapter", "Error setting color: ${e.message}")
                binding.categoryColorIndicator.setBackgroundColor(Color.GRAY)
            }
        }

        // fill in all the category details
        fun bind(category: Category, totalSpent: Double) {
            android.util.Log.d("CategoryAdapter", "Binding category: ${category.name}, ID: ${category.id}, Color: ${category.color}")
            binding.categoryName.text = category.name
            
            // show the amount spent in south african rand
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            binding.totalSpentAmount.text = currencyFormatter.format(totalSpent)

            // set the category's color
            setColorIndicator(category.color)

            // set the category's icon
            val resources = binding.root.context.resources
            val packageName = binding.root.context.packageName
            val iconResId = resources.getIdentifier(
                category.icon, "drawable", packageName
            )

            if (iconResId != 0) {
                android.util.Log.d("CategoryAdapter", "Setting category icon: ${category.icon}")
                binding.categoryIcon.setImageResource(iconResId)
            } else {
                android.util.Log.w("CategoryAdapter", "Icon not found: ${category.icon}")
            }
        }
    }

    // this class helps the recyclerview figure out what changed in the list
    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        // check if two items are the same category
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        // check if the category's details changed
        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem && 
                   oldItem.name == newItem.name &&
                   oldItem.color == newItem.color &&
                   oldItem.icon == newItem.icon
        }
    }
}