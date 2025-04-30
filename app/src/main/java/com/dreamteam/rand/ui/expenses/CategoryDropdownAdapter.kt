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
import android.view.animation.AlphaAnimation
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

        // Apply staggered fade-in animation
        applyFadeInAnimation(view, position)

        return view
    }

    // Made use of ChatGPT and Grok to make the fade in animation for the CategoryDropDownAdapter screen.
    // val viewsToAnimate = listOf() is used to create a list of views to animate.
    //  viewToAnimate -iterates through views to apply fade-in animation with staggered delays
    // the val fadeIn = AlphaAnimation is used to create a fade-in animation from invisible (alpha 0) to fully visible (alpha 1)
    // the duration is set to 600 milliseconds (0.6 seconds)
    // the startOffset is used to control the delay between animations
    // the fillAfter = true is used to keep the view visible after the animation
    // With the use of ChatGPT and Grok I learnt how to create the fade in animation for the CategoryDropDownAdapter Screen and how to create the animator to create the transition effects.

    private fun applyFadeInAnimation(view: View, position: Int) {
        // Create a fade-in animation (from 0 to 1 alpha)
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 600 // Duration of the fade-in effect (in milliseconds)
            startOffset = (position * 100).toLong() // Stagger delay (100ms per item)
            fillAfter = true // Keep the view visible after animation
        }

        // Start the animation on the view
        view.startAnimation(fadeIn)
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