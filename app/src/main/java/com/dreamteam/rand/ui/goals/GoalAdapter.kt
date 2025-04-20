package com.dreamteam.rand.ui.goals

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dreamteam.rand.R
import com.dreamteam.rand.data.entity.Goal
import com.dreamteam.rand.data.entity.GoalSpendingStatus
import com.dreamteam.rand.databinding.ItemGoalBinding
import java.text.SimpleDateFormat
import java.util.*

class GoalAdapter(private val onDeleteClick: (Goal) -> Unit) :
    ListAdapter<Goal, GoalAdapter.GoalViewHolder>(GoalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = ItemGoalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GoalViewHolder(private val binding: ItemGoalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(goal: Goal) {
            // Set goal name and header color
            binding.goalName.text = goal.name
            try {
                binding.goalHeader.setBackgroundColor(Color.parseColor(goal.color))
            } catch (e: Exception) {
                binding.goalHeader.setBackgroundColor(Color.GRAY)
            }

            // Set month and year
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH, goal.month - 1) // Calendar months are 0-based
            calendar.set(Calendar.YEAR, goal.year)
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            binding.monthYear.text = dateFormat.format(calendar.time)

            // Set amounts
            binding.currentAmount.text = String.format("R %.2f", goal.currentSpent)
            binding.minAmount.text = String.format("Min: R %.2f", goal.minAmount)
            binding.maxAmount.text = String.format("Max: R %.2f", goal.maxAmount)

            // Calculate progress
            val progress = when {
                goal.maxAmount <= goal.minAmount -> 0 // Prevent division by zero
                else -> ((goal.currentSpent - goal.minAmount) / (goal.maxAmount - goal.minAmount) * 100)
                    .coerceIn(0.0, 100.0)
                    .toInt()
            }
            binding.goalProgress.progress = progress

            // Set status chip style based on spending status
            val context = binding.root.context
            when (goal.spendingStatus) {
                GoalSpendingStatus.BELOW_MINIMUM -> {
                    binding.statusChip.text = "Below Minimum"
                    binding.statusChip.setTextColor(ContextCompat.getColor(context, R.color.progress_blue))
                }
                GoalSpendingStatus.ON_TRACK -> {
                    binding.statusChip.text = "On Track"
                    binding.statusChip.setTextColor(ContextCompat.getColor(context, R.color.progress_green))
                }
                GoalSpendingStatus.OVER_BUDGET -> {
                    binding.statusChip.text = "Over Budget"
                    binding.statusChip.setTextColor(ContextCompat.getColor(context, R.color.progress_red))
                }
            }

            // Set delete button click listener
            binding.editButton.setOnClickListener {
                onDeleteClick(goal)
            }
        }
    }

    private class GoalDiffCallback : DiffUtil.ItemCallback<Goal>() {
        override fun areItemsTheSame(oldItem: Goal, newItem: Goal): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Goal, newItem: Goal): Boolean {
            return oldItem == newItem
        }
    }
}