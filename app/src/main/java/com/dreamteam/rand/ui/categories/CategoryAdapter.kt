package com.dreamteam.rand.ui.categories

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dreamteam.rand.data.entity.Category
import com.dreamteam.rand.databinding.ItemCategoryBinding

class CategoryAdapter(private val onEditClick: (Category) -> Unit) :
    ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.categoryName.text = category.name

            // Set color
            try {
                binding.categoryColorIndicator.setBackgroundColor(Color.parseColor(category.color))
            } catch (e: Exception) {
                // If color parsing fails, use a default color
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

            binding.editButton.setOnClickListener {
                onEditClick(category)
            }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
} 