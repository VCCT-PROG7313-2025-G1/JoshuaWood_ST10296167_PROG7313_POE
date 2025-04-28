package com.dreamteam.rand.ui.expenses

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.dreamteam.rand.R
import com.dreamteam.rand.data.entity.Category
import com.dreamteam.rand.data.entity.TransactionType

class CategoryDropdownAdapter(
    context: Context,
    resource: Int,
    private val categories: List<Category>
) : ArrayAdapter<Category>(context, resource, categories) {

    private val inflater = LayoutInflater.from(context)
    
    // Add an "All Categories" option
    private val allCategoriesOption = Category(
        id = -1,
        name = "All Categories",
        color = "#4CAF50", // More distinctive color for All Categories
        icon = "ic_category",
        type = TransactionType.EXPENSE,
        userId = "",
        isDefault = false,
        budget = 0.0,
        createdAt = System.currentTimeMillis()
    )
    
    private val allItems = mutableListOf<Category>().apply {
        add(allCategoriesOption)
        addAll(categories)
    }

    override fun getCount(): Int = allItems.size

    override fun getItem(position: Int): Category? = allItems[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.item_dropdown_category, parent, false)
        
        val category = allItems[position]
        val categoryName = view.findViewById<TextView>(R.id.categoryName)
        val colorIndicator = view.findViewById<View>(R.id.categoryColorIndicator)
        
        categoryName.text = category.name
        
        // Make "All Categories" stand out
        if (category.id == -1L) {
            categoryName.setTypeface(null, Typeface.BOLD)
        } else {
            categoryName.setTypeface(null, Typeface.NORMAL)
        }
        
        try {
            colorIndicator.setBackgroundColor(Color.parseColor(category.color))
        } catch (e: Exception) {
            colorIndicator.setBackgroundColor(Color.GRAY)
        }
        
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                results.values = allItems
                results.count = allItems.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notifyDataSetChanged()
            }
        }
    }
} 