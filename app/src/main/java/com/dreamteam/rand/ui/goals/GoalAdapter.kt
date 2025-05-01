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

// this adapter shows a list of goals in a recyclerview
// each goal shows its name, month/year, progress bars, and spending status
class GoalAdapter(private val onDeleteClick: (Goal) -> Unit) :
    ListAdapter<Goal, GoalAdapter.GoalViewHolder>(GoalDiffCallback()) {

    // keep track of goals and their current spending amounts
    // Claude suggested using mutableMapOf
    private val goals = mutableMapOf<Long, Goal>()

    // update how much has been spent on a goal
    fun updateGoalExpenses(updatedGoal: Goal) {
        goals[updatedGoal.id] = updatedGoal
        // find where this goal is in the list
        val position = currentList.indexOfFirst { it.id == updatedGoal.id }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    // create a new view for a goal
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = ItemGoalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GoalViewHolder(binding)
    }

    // fill in the goal info when it's shown
    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // this class holds all the views for a single goal
    inner class GoalViewHolder(private val binding: ItemGoalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // fill in all the goal details
        fun bind(goal: Goal) {
            // use the latest spending amount if we have it
            val currentGoal = goals[goal.id] ?: goal

            // show the goal name and color
            binding.goalName.text = currentGoal.name
            try {
                binding.goalHeader.setBackgroundColor(Color.parseColor(currentGoal.color))
            } catch (e: Exception) {
                binding.goalHeader.setBackgroundColor(Color.GRAY)
            }

            // show the month and year
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH, currentGoal.month - 1) // Calendar months are 0-based
            calendar.set(Calendar.YEAR, currentGoal.year)
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            binding.monthYear.text = dateFormat.format(calendar.time)

            // show all the amounts
            // formatting done by Claude
            binding.currentAmount.text = String.format("R %.2f", currentGoal.currentSpent)
            binding.minAmount.text = String.format("R %.2f", currentGoal.minAmount)
            binding.maxAmount.text = String.format("R %.2f", currentGoal.maxAmount)
            binding.minAmountLabel.text = String.format("R %.2f", currentGoal.minAmount)

            // figure out how far along we are to the minimum goal
            // used Claude to work out how to do this
            val minProgress = when {
                currentGoal.minAmount <= 0 -> 0
                else -> ((currentGoal.currentSpent / currentGoal.minAmount) * 100)
                    .coerceIn(0.0, 100.0)
                    .toInt()
            }
            binding.minGoalProgress.progress = minProgress

            // figure out how far along we are to the maximum goal
            // used Claude to work out how to do this
            val maxProgress = when {
                currentGoal.maxAmount <= currentGoal.minAmount -> 0
                currentGoal.currentSpent <= currentGoal.minAmount -> 0
                else -> (((currentGoal.currentSpent - currentGoal.minAmount) / (currentGoal.maxAmount - currentGoal.minAmount)) * 100)
                    .coerceIn(0.0, 100.0)
                    .toInt()
            }
            binding.maxGoalProgress.progress = maxProgress

            // show a user if they are spending too little, are on track, or too much
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

            // handle when the delete button is clicked
            binding.deleteButton.setOnClickListener {
                onDeleteClick(currentGoal)
            }
        }
    }

    // this class helps the recyclerview figure out what changed in the list
    private class GoalDiffCallback : DiffUtil.ItemCallback<Goal>() {
        // check if two items are the same goal
        override fun areItemsTheSame(oldItem: Goal, newItem: Goal): Boolean {
            return oldItem.id == newItem.id
        }

        // check if the goal's details changed
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