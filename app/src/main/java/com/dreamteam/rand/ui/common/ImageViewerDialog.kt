package com.dreamteam.rand.ui.common

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.dreamteam.rand.databinding.DialogImageViewerBinding
import java.io.File

class ImageViewerDialog : DialogFragment() {
    private var _binding: DialogImageViewerBinding? = null
    private val binding get() = _binding!!
    
    private var imageUri: String? = null
    
    companion object {
        private const val ARG_IMAGE_URI = "image_uri"
        private const val TAG = "ImageViewerDialog"
        
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
            Log.d(TAG, "Received image URI: $imageUri")
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
        
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        
        try {
            imageUri?.let { uri ->
                Log.d(TAG, "Loading image from URI: $uri")
                
                // Try loading as a file first
                val file = File(uri)
                if (file.exists()) {
                    Log.d(TAG, "Loading from file: ${file.absolutePath}")
                    Glide.with(requireContext())
                        .load(file)
                        .into(binding.imageView)
                } else {
                    // Try loading as URI
                    Log.d(TAG, "Loading from URI")
                    Glide.with(requireContext())
                        .load(Uri.parse(uri))
                        .into(binding.imageView)
                }
            } ?: run {
                Log.e(TAG, "No image URI provided")
                showError("No image URI provided")
                dismiss()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image", e)
            showError("Failed to load image: ${e.message}")
            dismiss()
        }
    }
    
    private fun showError(message: String) {
        Log.e(TAG, message)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 