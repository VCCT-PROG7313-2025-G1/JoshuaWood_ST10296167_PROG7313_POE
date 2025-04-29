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

    private val goals = mutableMapOf<Long, Goal>()

    fun updateGoalExpenses(updatedGoal: Goal) {
        goals[updatedGoal.id] = updatedGoal
        // Find the position of the goal in the current list
        val position = currentList.indexOfFirst { it.id == updatedGoal.id }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

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
            // Use the updated goal from our map if available
            val currentGoal = goals[goal.id] ?: goal

            // Set goal name and header color
            binding.goalName.text = currentGoal.name
            try {
                binding.goalHeader.setBackgroundColor(Color.parseColor(currentGoal.color))
            } catch (e: Exception) {
                binding.goalHeader.setBackgroundColor(Color.GRAY)
            }

            // Set month and year
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH, currentGoal.month - 1) // Calendar months are 0-based
            calendar.set(Calendar.YEAR, currentGoal.year)
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            binding.monthYear.text = dateFormat.format(calendar.time)

            // Set amounts
            binding.currentAmount.text = String.format("R %.2f", currentGoal.currentSpent)
            binding.minAmount.text = String.format("R %.2f", currentGoal.minAmount)
            binding.maxAmount.text = String.format("R %.2f", currentGoal.maxAmount)
            binding.minAmountLabel.text = String.format("R %.2f", currentGoal.minAmount)

            // Calculate progress for minimum goal (0 to minAmount)
            val minProgress = when {
                currentGoal.minAmount <= 0 -> 0
                else -> ((currentGoal.currentSpent / currentGoal.minAmount) * 100)
                    .coerceIn(0.0, 100.0)
                    .toInt()
            }
            binding.minGoalProgress.progress = minProgress

            // Calculate progress for maximum goal (minAmount to maxAmount)
            val maxProgress = when {
                currentGoal.maxAmount <= currentGoal.minAmount -> 0
                currentGoal.currentSpent <= currentGoal.minAmount -> 0
                else -> (((currentGoal.currentSpent - currentGoal.minAmount) / (currentGoal.maxAmount - currentGoal.minAmount)) * 100)
                    .coerceIn(0.0, 100.0)
                    .toInt()
            }
            binding.maxGoalProgress.progress = maxProgress

            // Set status chip style based on spending status
            val context = binding.root.context
            when {
                currentGoal.currentSpent < currentGoal.minAmount -> {
                    binding.statusChip.text = "Below Minimum"
                    binding.statusChip.setTextColor(ContextCompat.getColor(context, R.color.progress_blue))
                }
                currentGoal.currentSpent <= currentGoal.maxAmount -> {
                    binding.statusChip.text = "On Track"
                    binding.statusChip.setTextColor(ContextCompat.getColor(context, R.color.progress_green))
                }
                else -> {
                    binding.statusChip.text = "Over Budget"
                    binding.statusChip.setTextColor(ContextCompat.getColor(context, R.color.progress_red))
                }
            }

            // Set delete button click listener
            binding.deleteButton.setOnClickListener {
                onDeleteClick(currentGoal)
            }
        }
    }

    private class GoalDiffCallback : DiffUtil.ItemCallback<Goal>() {
        override fun areItemsTheSame(oldItem: Goal, newItem: Goal): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Goal, newItem: Goal): Boolean {
            return oldItem.id == newItem.id &&
                   oldItem.name == newItem.name &&
                   oldItem.month == newItem.month &&
                   oldItem.year == newItem.year &&
                   oldItem.minAmount == newItem.minAmount &&
                   oldItem.maxAmount == newItem.maxAmount &&
                   oldItem.currentSpent == newItem.currentSpent &&
                   oldItem.color == newItem.color
        }
    }
}