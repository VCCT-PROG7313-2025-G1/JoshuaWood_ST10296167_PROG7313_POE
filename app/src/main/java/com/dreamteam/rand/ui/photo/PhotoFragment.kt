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
import com.dreamteam.rand.ui.common.ViewUtils
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// this fragment handles taking photos of expense receipts
// it uses the camera to take pictures and saves them for expenses
class PhotoFragment : Fragment() {
    private val TAG = "PhotoFragment"
    
    // binding to access all the views
    private var _binding: FragmentPhotoBinding? = null
    private val binding get() = _binding!!
    
    // stuff we need to control the camera
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    
    // thread to handle taking photos
    private lateinit var imgCaptureExecutor: ExecutorService
    
    // keep track of the last photo we took
    private var latestPhotoFile: File? = null
    
    // ask for permission to use the camera function from module manual
    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { permissionGranted->
        if (permissionGranted) {
            Log.d(TAG, "Camera permission granted, starting camera")
            // start the camera if they said yes
            startCamera()
        } else {
            Log.w(TAG, "Camera permission denied")
            Toast.makeText(requireContext(), "Cannot take a photo without camera permissions", Toast.LENGTH_SHORT).show()
        }
    }

    // create the view for taking photos
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "Creating photo view")
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        
        // start a thread to handle taking photos
        imgCaptureExecutor = Executors.newSingleThreadExecutor()
        Log.d(TAG, "Initialized image capture executor")

        // ask for permission to use the camera
        Log.d(TAG, "Requesting camera permission")
        requestCameraPermission.launch(android.Manifest.permission.CAMERA)

        // when they click the take photo button
        binding.takePhotoActionButton.setOnClickListener(){
            Log.d(TAG, "Take photo button clicked")
            takePhoto()
        }

        // when they click the save button, save the most recent photo file
        binding.savePhotoButton.setOnClickListener {
            Log.d(TAG, "Save photo button clicked")
            latestPhotoFile?.let { file ->
                Log.d(TAG, "Saving photo with path: ${file.absolutePath}")
                // send the photo path back to whoever asked for it
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

    // setup the view after it's created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //ViewUtils.setToolbarGradient(this, binding.toolbar) to add a dark mode gradient to the banner
        Log.d(TAG, "Setting up photo view")
        setupToolbar()
    }

    // setup the toolbar with back button
    private fun setupToolbar() {
        Log.d(TAG, "Setting up toolbar")
        binding.toolbar.setNavigationOnClickListener {
            Log.d(TAG, "Toolbar back button clicked")
            findNavController().navigateUp()
        }
    }

    // start up the camera and show the preview function from module manual with minor modifications
    private fun startCamera() {
        Log.d(TAG, "Starting camera setup")
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        Log.d(TAG, "Using back camera")
        
        cameraProviderFuture.addListener(
            {
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    // setup the preview so they can see what they're taking a picture of
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(binding.imgUserPhoto.surfaceProvider)
                    }
                    imageCapture = ImageCapture.Builder().build()
                    Log.d(TAG, "Created camera preview and image capture")
                    
                    try {
                        // connect everything together
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

    // used Claude to help modify the takePhoto() function from the module manual to
    // save the photo to local emulator storage and then use the file path to retrieve the image
    // take a picture and save it
    private fun takePhoto() {
        Log.d(TAG, "Preparing to take photo")
        // create a file to save the photo to
        val photoFile = File(
            requireActivity().externalMediaDirs.first(),
            "ExpensePhoto_${System.currentTimeMillis()}.jpg"
        )
        Log.d(TAG, "Created photo file at: ${photoFile.absolutePath}")

        // setup how we want to save the photo
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        Log.d(TAG, "Configured output file options")

        // take the picture
        imageCapture?.takePicture(outputFileOptions, imgCaptureExecutor,
            object : ImageCapture.OnImageSavedCallback {
                // when the photo is saved successfully
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo successfully saved to: ${photoFile.absolutePath}")
                    latestPhotoFile = photoFile // remember where we saved it
                    requireActivity().runOnUiThread {
                        // show them the photo they just took
                        binding.imgSavedPhoto.setImageURI(Uri.fromFile(photoFile))
                        binding.imgSavedPhoto.visibility = View.VISIBLE
                        binding.savePhotoButton.visibility = View.VISIBLE
                        Log.d(TAG, "Updated UI with saved photo preview")
                    }
                }

                // if something went wrong
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Failed to take photo: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // clean up when done
    override fun onDestroyView() {
        Log.d(TAG, "Destroying photo view")
        super.onDestroyView()
        _binding = null
        imgCaptureExecutor.shutdown()
        Log.d(TAG, "Shut down image capture executor")
    }
}