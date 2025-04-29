package com.dreamteam.rand.ui.photo

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.databinding.FragmentPhotoBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// handles camera operations and photo capture for expense receipts
class PhotoFragment : Fragment() {
    private val TAG = "PhotoFragment"
    
    private var _binding: FragmentPhotoBinding? = null
    private val binding get() = _binding!!
    // Camera lifecycle and feature control
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    // Handles image capture
    private lateinit var imgCaptureExecutor: ExecutorService
    // Reference to the latest saved file path
    private var latestPhotoFile: File? = null
    // Request for camera permission
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { permissionGranted->
        if (permissionGranted) {
            Log.d(TAG, "Camera permission granted, starting camera")
            // Launch camera
            startCamera()
        } else {
            Log.w(TAG, "Camera permission denied")
            Toast.makeText(requireContext(), "Cannot take a photo without camera permissions", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Creating photo view")
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        imgCaptureExecutor = Executors.newSingleThreadExecutor()
        Log.d(TAG, "Initialized image capture executor")

        // Launch permission request
        Log.d(TAG, "Requesting camera permission")
        requestCameraPermission.launch(android.Manifest.permission.CAMERA)

        // Take photo when FAB is clicked
        binding.takePhotoActionButton.setOnClickListener(){
            Log.d(TAG, "Take photo button clicked")
            takePhoto()
        }

        // Save photo to expense
        binding.savePhotoButton.setOnClickListener {
            Log.d(TAG, "Save photo button clicked")
            latestPhotoFile?.let { file ->
                Log.d(TAG, "Saving photo with path: ${file.absolutePath}")
                // Send result back using Fragment Result API
                setFragmentResult("photoResult", bundleOf("photoPath" to file.absolutePath))
                
                Toast.makeText(requireContext(), "Photo saved", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Navigating back after saving photo")
                findNavController().navigateUp()
            } ?: run {
                Log.w(TAG, "Attempted to save but no photo was taken")
                Toast.makeText(requireContext(), "No photo to save", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "Setting up photo view")
        setupToolbar()
    }

    private fun setupToolbar() {
        Log.d(TAG, "Setting up toolbar")
        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Toolbar back button clicked")
            findNavController().navigateUp()
        }
    }

    // Starts camera and binds preview and captured image to PhotoFragment lifecycle
    private fun startCamera() {
        Log.d(TAG, "Starting camera setup")
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        Log.d(TAG, "Using back camera")
        
        cameraProviderFuture.addListener(
            {
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(binding.imgUserPhoto.surfaceProvider)
                    }
                    imageCapture = ImageCapture.Builder().build()
                    Log.d(TAG, "Created camera preview and image capture")
                    
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                        Log.d(TAG, "Successfully bound camera to lifecycle")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to bind camera use cases: ${e.message}", e)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get camera provider: ${e.message}", e)
                }
            }, ContextCompat.getMainExecutor(requireContext()))
    }

    // Takes photo and saves file reference
    private fun takePhoto() {
        Log.d(TAG, "Preparing to take photo")
        val photoFile = File(
            requireActivity().externalMediaDirs.first(),
            "ExpensePhoto_${System.currentTimeMillis()}.jpg"
        )
        Log.d(TAG, "Created photo file at: ${photoFile.absolutePath}")

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        Log.d(TAG, "Configured output file options")

        imageCapture?.takePicture(outputFileOptions, imgCaptureExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo successfully saved to: ${photoFile.absolutePath}")
                    latestPhotoFile = photoFile // Save reference
                    requireActivity().runOnUiThread {
                        // Preview taken photo
                        binding.imgSavedPhoto.setImageURI(Uri.fromFile(photoFile))
                        binding.imgSavedPhoto.visibility = View.VISIBLE
                        binding.savePhotoButton.visibility = View.VISIBLE
                        Log.d(TAG, "Updated UI with saved photo preview")
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Failed to take photo: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    override fun onDestroyView() {
        Log.d(TAG, "Destroying photo view")
        super.onDestroyView()
        _binding = null
        imgCaptureExecutor.shutdown()
        Log.d(TAG, "Shut down image capture executor")
    }
}