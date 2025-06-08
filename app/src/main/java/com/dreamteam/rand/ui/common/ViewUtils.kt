package com.dreamteam.rand.ui.common

import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.dreamteam.rand.R

object ViewUtils {
    
    /**
     * Sets a gradient background to the toolbar based on the current theme
     */
    fun setToolbarGradient(fragment: Fragment, toolbar: MaterialToolbar) {
        val context = fragment.requireContext()
        val isDarkMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        
        val gradientDrawable = GradientDrawable().apply {
            orientation = GradientDrawable.Orientation.LEFT_RIGHT
            if (isDarkMode) {
                // Dark mode gradient
                colors = intArrayOf(
                    ContextCompat.getColor(context, R.color.primary_dark),
                    ContextCompat.getColor(context, R.color.secondary_dark)
                )
            } else {
                // Light mode gradient
                colors = intArrayOf(
                    ContextCompat.getColor(context, R.color.primary),
                    ContextCompat.getColor(context, R.color.secondary)
                )
            }
        }
        
        toolbar.background = gradientDrawable
    }
} 