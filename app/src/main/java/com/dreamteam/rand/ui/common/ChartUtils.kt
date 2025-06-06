package com.dreamteam.rand.ui.common

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis

/**
 * Utility class for chart styling that ensures proper appearance in both light and dark modes
 */
object ChartUtils {

    /**
     * Applies theme-aware styling to a BarChart
     */
    fun applyThemeAwareStyling(context: Context, chart: BarChart) {
        val isDarkMode = isDarkModeActive(context)
        val textColor = if (isDarkMode) Color.WHITE else Color.BLACK
        
        // Apply text color to chart components
        chart.xAxis.textColor = textColor
        chart.axisLeft.textColor = textColor
        chart.axisRight.textColor = textColor
        chart.legend.textColor = textColor
        chart.description.textColor = textColor
        
        // Set value text color
        chart.data?.let { data ->
            data.dataSets.forEach { dataSet ->
                dataSet.valueTextColor = textColor
            }
        }
        
        // Apply theme-aware styling to limit lines
        applyThemeAwareStylingToLimitLines(context, chart.axisLeft.limitLines)
    }
    
    /**
     * Applies theme-aware styling to a LineChart
     */
    fun applyThemeAwareStyling(context: Context, chart: LineChart) {
        val isDarkMode = isDarkModeActive(context)
        val textColor = if (isDarkMode) Color.WHITE else Color.BLACK
        
        // Apply text color to chart components
        chart.xAxis.textColor = textColor
        chart.axisLeft.textColor = textColor
        chart.axisRight.textColor = textColor
        chart.legend.textColor = textColor
        chart.description.textColor = textColor
        
        // Set value text color
        chart.data?.let { data ->
            data.dataSets.forEach { dataSet ->
                dataSet.valueTextColor = textColor
            }
        }
        
        // Apply theme-aware styling to limit lines
        applyThemeAwareStylingToLimitLines(context, chart.axisLeft.limitLines)
    }
    
    /**
     * Apply theme-aware styling to axis components
     */
    fun applyThemeAwareStyling(context: Context, xAxis: XAxis, yAxis: YAxis) {
        val isDarkMode = isDarkModeActive(context)
        val textColor = if (isDarkMode) Color.WHITE else Color.BLACK
        
        xAxis.textColor = textColor
        yAxis.textColor = textColor
        
        // Apply theme-aware styling to limit lines
        applyThemeAwareStylingToLimitLines(context, yAxis.limitLines)
    }
    
    /**
     * Apply theme-aware styling to limit lines
     */
    fun applyThemeAwareStylingToLimitLines(context: Context, limitLines: List<LimitLine>) {
        val isDarkMode = isDarkModeActive(context)
        val textColor = if (isDarkMode) Color.WHITE else Color.BLACK
        
        limitLines.forEach { limitLine ->
            limitLine.textColor = textColor
        }
    }
    
    /**
     * Checks if dark mode is currently active
     */
    private fun isDarkModeActive(context: Context): Boolean {
        return context.resources.configuration.uiMode and 
               Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
} 