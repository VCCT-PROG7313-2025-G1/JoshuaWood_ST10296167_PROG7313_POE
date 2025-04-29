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

// this adapter shows categories in a dropdown menu
// it adds an "All Categories" option at the top and shows each category's name and color
class CategoryDropdownAdapter(
    context: Context,
    resource: Int,
    private val categories: List<Category>
) : ArrayAdapter<Category>(context, resource, categories) {

    private val inflater = LayoutInflater.from(context)
    
    // add an "All Categories" option at the top of the list
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
    
    // combine the "All Categories" option with the real categories
    private val allItems = mutableListOf<Category>().apply {
        add(allCategoriesOption)
        addAll(categories)
    }

    // tell how many items are in the dropdown
    override fun getCount(): Int = allItems.size

    // get a category at a specific position
    override fun getItem(position: Int): Category? = allItems[position]

    // create the view for each item in the dropdown
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.item_dropdown_category, parent, false)
        
        val category = allItems[position]
        val categoryName = view.findViewById<TextView>(R.id.categoryName)
        val colorIndicator = view.findViewById<View>(R.id.categoryColorIndicator)
        
        categoryName.text = category.name
        
        // make "All Categories" stand out with bold text
        if (category.id == -1L) {
            categoryName.setTypeface(null, Typeface.BOLD)
        } else {
            categoryName.setTypeface(null, Typeface.NORMAL)
        }
        
        // show the category's color
        try {
            colorIndicator.setBackgroundColor(Color.parseColor(category.color))
        } catch (e: Exception) {
            colorIndicator.setBackgroundColor(Color.GRAY)
        }
        
        return view
    }

    // handle filtering the dropdown (not really used since we show all items)
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