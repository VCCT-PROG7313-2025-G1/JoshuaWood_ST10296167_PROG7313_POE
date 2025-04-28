package com.dreamteam.rand.ui.common

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.dreamteam.rand.R
import com.dreamteam.rand.databinding.DialogImageViewerBinding

class ImageViewerDialog : DialogFragment() {
    private var _binding: DialogImageViewerBinding? = null
    private val binding get() = _binding!!
    
    private var imageUri: String? = null
    
    companion object {
        private const val ARG_IMAGE_URI = "image_uri"
        
        fun newInstance(imageUri: String): ImageViewerDialog {
            val fragment = ImageViewerDialog()
            val args = Bundle()
            args.putString(ARG_IMAGE_URI, imageUri)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        
        arguments?.let {
            imageUri = it.getString(ARG_IMAGE_URI)
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogImageViewerBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set close button click listener
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        
        // Load the image
        try {
            imageUri?.let { uri ->
                binding.imageView.setImageURI(Uri.parse(uri))
            } ?: run {
                showError("No image URI provided")
                dismiss()
            }
        } catch (e: Exception) {
            showError("Failed to load image: ${e.message}")
            dismiss()
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 