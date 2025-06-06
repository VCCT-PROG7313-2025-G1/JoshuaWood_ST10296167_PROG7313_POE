package com.dreamteam.rand.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.FragmentProfileBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        observeUserData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadAchievements(userId: String, userLevel: Int){
        viewLifecycleOwner.lifecycleScope.launch {
            val data = userViewModel.getAchievementData(userId)

            val expenseCount = (data[0] as? Int) ?: 0
            val categoryCount = (data[1] as? Int) ?: 0
            val goalCount = (data[2] as? Int) ?: 0
            val totalExpenses = (data[3]?.toInt()) ?: 0

            val achievements = listOf(
                Achievement("Penny Pusher", "Add 10 expenses", minOf(expenseCount, 10), 10, expenseCount >= 10),
                Achievement("Label Lover", "Add 5 categories", minOf(categoryCount, 5), 5, categoryCount >= 5),
                Achievement("Goal Digger", "Add 5 goals", minOf(goalCount, 5), 5, goalCount >= 5),

                Achievement("Receipt Rascal", "Add 50 expenses", minOf(expenseCount, 50), 50, expenseCount >= 50),
                Achievement("Category Connoisseur", "Add 10 categories", minOf(categoryCount, 10), 10, categoryCount >= 10),
                Achievement("Goal Goblin", "Add 10 goals", minOf(goalCount, 10), 10, goalCount >= 10),

                Achievement("Rookie", "Reach level 10", minOf(userLevel, 10), 10, userLevel >= 10),
                Achievement("Financial Wizard", "Reach level 25", minOf(userLevel, 25), 25, userLevel >= 25),
                Achievement("Master Budgeter", "Reach level 50", minOf(userLevel, 50), 50, userLevel >= 50),

                Achievement("Suspicious Spender", "Spend R1000", minOf(totalExpenses, 1000), 1000, userLevel >= 1000),
                Achievement("Money Manager", "Spend R10 000", minOf(totalExpenses, 10000), 10000, userLevel >= 10000),
                Achievement("Bank Breaker", "Spend R100 000", minOf(totalExpenses, 100000), 100000, userLevel >= 100000),
            )
            setupAchievements(achievements)
        }
    }

    private fun setupAchievements(achievements: List<Achievement>) {
        val achievementsContainer = view?.findViewById<LinearLayout>(R.id.achievementsContainer)
        achievementsContainer?.removeAllViews()
        achievements.forEach { achievement ->
            val achievementView = createAchievementView(achievement)
            achievementsContainer?.addView(achievementView)
        }
    }

    private fun createAchievementView(achievement: Achievement): View {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.item_achievement, null)

        view.findViewById<TextView>(R.id.achievementName).text = achievement.name
        view.findViewById<TextView>(R.id.achievementDescription).text = achievement.description
        view.findViewById<TextView>(R.id.progressCount).text = "${achievement.current}/${achievement.target}"

        val progressBar = view.findViewById<ProgressBar>(R.id.achievementProgress)
        progressBar.max = achievement.target
        progressBar.progress = achievement.current

        val completionIcon = view.findViewById<ImageView>(R.id.completionIcon)
        completionIcon.visibility = if (achievement.isCompleted) View.VISIBLE else View.INVISIBLE

        return view
    }

    private fun observeUserData() {
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                // Navigation will be handled by the MainActivity observer
                return@observe
            }

            val currentLevelXP = user.xp % 100  // XP progress within current level (0-99)
            val currentLevel = user.level

            // Set user data
            binding.levelBadgeText.text = "Lvl.$currentLevel"
            binding.nameText.text = user.name
            binding.emailText.text = user.email
            binding.currentLevelText.text = "Lvl.$currentLevel"
            binding.nextLevelText.text = "Lvl.${currentLevel + 1}"
            binding.progressText.text = "$currentLevelXP/100 XP"

            // used chatgpt to help work out how to make certain text bold
            val formattedXpText = "<b>$currentLevelXP</b>/100 XP"
            binding.progressText.text = HtmlCompat.fromHtml(formattedXpText, HtmlCompat.FROM_HTML_MODE_LEGACY)

            // Update progress bar
            binding.xpProgressBar.progress = currentLevelXP

            // Apply staggered fade-in animation to text views
            applyStaggeredFadeInAnimation()

            loadAchievements(user.uid, currentLevel)
        }
    }

    // made use of ChatGPT and Grok to help make the fade in animation for the Profile screen.
    private fun applyStaggeredFadeInAnimation() {
        // List of views to animate
        val viewsToAnimate = listOf(
            binding.nameText,
            binding.emailText,
            binding.levelBadgeText,
            binding.progressText
        )

        viewsToAnimate.forEachIndexed { index, view ->
            val fadeIn = AlphaAnimation(0f, 1f).apply {
                duration = 600 // Duration of the fade-in effect (in milliseconds)
                startOffset = (index * 290).toLong() // Stagger delay (290ms per view)
                fillAfter = true // Keep the view visible after animation
            }
            view.startAnimation(fadeIn)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// data class for displaying achievements
data class Achievement(
    val name: String,
    val description: String,
    val current: Int,
    val target: Int,
    val isCompleted: Boolean
)