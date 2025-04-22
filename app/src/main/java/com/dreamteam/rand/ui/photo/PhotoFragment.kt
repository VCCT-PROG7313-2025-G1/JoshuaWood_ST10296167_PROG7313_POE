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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dreamteam.rand.databinding.FragmentExpensesBinding
import com.dreamteam.rand.databinding.FragmentPhotoBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PhotoFragment : Fragment() {
    private var _binding: FragmentPhotoBinding? = null
    private val binding get() = _binding!!
    // Camera lifecycle and feature control
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    // Handles image capture
    private lateinit var  imgCaptureExecutor: ExecutorService
    // Reference to the latest saved file path
    private var latestPhotoFile: File? = null
    // Request for camera permission
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { permissionGranted->
        if (permissionGranted) {
            // Launch camera
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Cannot take a photo without camera permissions", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        // Launch permission request
        requestCameraPermission.launch(android.Manifest.permission.CAMERA)

        // Take photo when FAB is clicked
        binding.takePhotoActionButton.setOnClickListener(){
            takePhoto()
        }

        // Save photo to expense
        binding.savePhotoButton.setOnClickListener {
            latestPhotoFile?.let { file ->
                // TODO:
                // Example: send the path back via FragmentResult API or Navigation args later
                Toast.makeText(requireContext(), "Photo saved: ${file.absolutePath}", Toast.LENGTH_SHORT).show()

                // This is where you would return to the add expense screen
                // and send the file path, e.g.:
                // findNavController().previousBackStackEntry?.savedStateHandle?.set("photoPath", file.absolutePath)
                // findNavController().navigateUp()
            } ?: run {
                Toast.makeText(requireContext(), "No photo to save", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    // Starts camera and binds preview and captured image to PhotoFragment lifecycle
    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.imgUserPhoto.surfaceProvider)
                }
                imageCapture = ImageCapture.Builder().build()
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                } catch (e: Exception) {
                    Log.d("PhotoFragment", "Use case binding failure")
                }
            }, ContextCompat.getMainExecutor(requireContext()))
    }

    // Takes photo and saves file reference
    private fun takePhoto() {
        val photoFile = File(
            requireActivity().externalMediaDirs.first(),
            "ExpensePhoto_${System.currentTimeMillis()}.jpg"
        )

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(outputFileOptions, imgCaptureExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    latestPhotoFile = photoFile // Save reference
                    requireActivity().runOnUiThread {
                        // Preview taken photo
                        binding.imgSavedPhoto.setImageURI(Uri.fromFile(photoFile))
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("PhotoFragment", "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}