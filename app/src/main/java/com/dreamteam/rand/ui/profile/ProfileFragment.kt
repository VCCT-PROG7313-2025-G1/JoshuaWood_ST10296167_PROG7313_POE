package com.dreamteam.rand.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.FragmentProfileBinding
import com.dreamteam.rand.ui.auth.UserViewModel
import com.dreamteam.rand.ui.common.ViewUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()
    
    private var currentPhotoUri: Uri? = null
    
    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data ?: currentPhotoUri
            selectedImageUri?.let { uri ->
                loadProfileImage(uri)
                userViewModel.updateUserProfilePicture(uri.toString())
            }
        }
    }

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
        //ViewUtils.setToolbarGradient(this, binding.toolbar) to add a dark mode gradient to the banner
        setupToolbar()
        setupProfilePictureClick()
        observeUserData()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupProfilePictureClick() {
        binding.profilePictureContainer.setOnClickListener {
            showImagePickerDialog()
        }
    }
    
    private fun showImagePickerDialog() {
        val options = arrayOf("Camera", "Gallery")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Profile Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }
    
    private fun openCamera() {
        try {
            val photoFile = File.createTempFile("profile_pic", ".jpg", requireContext().cacheDir)
            currentPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )
            
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
            }
            imagePickerLauncher.launch(intent)
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Error creating camera file", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
    
    private fun loadProfileImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .transform(CircleCrop())
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .into(binding.profileImageView)
    }

    private fun loadAchievements(userId: String, userLevel: Int) {
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

                Achievement("Suspicious Spender", "Spend R1000", minOf(totalExpenses, 1000), 1000, totalExpenses >= 1000),
                Achievement("Money Manager", "Spend R10 000", minOf(totalExpenses, 10000), 10000, totalExpenses >= 10000),
                Achievement("Bank Breaker", "Spend R100 000", minOf(totalExpenses, 100000), 100000, totalExpenses >= 100000),
            )
            setupAchievements(achievements)
        }
    }

    private fun setupAchievements(achievements: List<Achievement>) {
        val achievementsContainer = view?.findViewById<LinearLayout>(R.id.achievementsContainer)
        achievementsContainer?.removeAllViews()

        achievements.forEachIndexed { index, achievement ->
            val achievementView = createAchievementView(achievement)

            // Apply staggered fade-in animation
            val fadeIn = AlphaAnimation(0f, 1f).apply {
                duration = 500
                startOffset = (index * 300).toLong()
                fillAfter = true
            }
            achievementView.startAnimation(fadeIn)

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
                return@observe
            }

            val currentLevelXP = user.xp % 100
            val currentLevel = user.level

            binding.levelBadgeText.text = "Lvl.$currentLevel"
            binding.nameText.text = user.name
            binding.emailText.text = user.email
            binding.currentLevelText.text = "Lvl.$currentLevel"
            binding.nextLevelText.text = "Lvl.${currentLevel + 1}"
            val formattedXpText = "<b>$currentLevelXP</b>/100 XP"
            binding.progressText.text = HtmlCompat.fromHtml(formattedXpText, HtmlCompat.FROM_HTML_MODE_LEGACY)

            binding.xpProgressBar.progress = currentLevelXP

            // Load profile picture if user has one
            user.profilePictureUri?.let { uri ->
                try {
                    loadProfileImage(Uri.parse(uri))
                } catch (e: Exception) {
                    // If there's an error loading the custom image, fall back to default
                    Glide.with(this@ProfileFragment)
                        .load(R.drawable.ic_profile)
                        .transform(CircleCrop())
                        .into(binding.profileImageView)
                }
            } ?: run {
                // Load default profile image
                Glide.with(this@ProfileFragment)
                    .load(R.drawable.ic_profile)
                    .transform(CircleCrop())
                    .into(binding.profileImageView)
            }

            applyStaggeredFadeInAnimation()

            loadAchievements(user.uid, currentLevel)
        }
    }

    private fun applyStaggeredFadeInAnimation() {
        val viewsToAnimate = listOf(
            binding.nameText,
            binding.emailText,
            binding.levelBadgeText,
            binding.progressText
        )

        viewsToAnimate.forEachIndexed { index, view ->
            val fadeIn = AlphaAnimation(0f, 1f).apply {
                duration = 500
                startOffset = (index * 300).toLong()
                fillAfter = true
            }
            view.startAnimation(fadeIn)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class Achievement(
    val name: String,
    val description: String,
    val current: Int,
    val target: Int,
    val isCompleted: Boolean
)
