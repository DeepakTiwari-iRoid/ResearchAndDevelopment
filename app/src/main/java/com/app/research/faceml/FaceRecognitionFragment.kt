package com.app.research.faceml

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.research.R
import com.app.research.databinding.FragmentFaceRecognitionBinding
import com.app.research.faceml.utils.MeshImageAnalyzer
import com.google.mlkit.vision.facemesh.FaceMeshDetection
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */

class FaceRecognitionFragment : Fragment() {
    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val STABILITY_THRESHOLD_MS = 3_000L
        val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }


    private var _binding: FragmentFaceRecognitionBinding? = null
    private val binding get() = _binding!!

    private lateinit var context: Context

    val defaultDetector = FaceMeshDetection.getClient(
        FaceMeshDetectorOptions.Builder()
            .setUseCase(FaceMeshDetectorOptions.BOUNDING_BOX_ONLY)
            .setUseCase(FaceMeshDetectorOptions.FACE_MESH)
            .build()
    )

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }

            if (!permissionGranted) {
                Toast.makeText(
                    requireActivity(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startCamera()
            }
        }

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService
    private var meshAnalyzer: MeshImageAnalyzer? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaceRecognitionBinding.inflate(inflater, container, false)
        context = requireContext()
        initial()
        return binding.root
    }

    private fun initial() {
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
        // Set up the listeners for take photo and video capture buttons
        /*binding.imageCaptureButton.setOnClickListener { takePhoto() }
        binding.videoCaptureButton.setOnClickListener { captureVideo() }*/

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Analyzer created when camera starts
    }


    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault())
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Timber.e("Photo capture failed: ${exc.message}")
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    Timber.d(msg)
                }
            }
        )
    }

    private fun captureVideo() {}

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireActivity())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = binding.viewFinder.surfaceProvider
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            val isFrontCamera = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
            val analyzerInstance = MeshImageAnalyzer(
                defaultDetector,
                binding.graphicOverlay,
                isFrontCamera,
                onSelfieCaptured = ::onSelfieCaptured,
                onStabilityProgress = { progress ->
                    updateProgressText(progress)
                },
                stabilityThresholdMillis = STABILITY_THRESHOLD_MS
            )
            meshAnalyzer = analyzerInstance

            val imageAnalyzerUseCase = ImageAnalysis.Builder()
                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysisUseCase ->
                    analysisUseCase.setAnalyzer(
                        cameraExecutor,
                        analyzerInstance
                    )
                }
            val imageCaptureUseCase = ImageCapture.Builder().build()
            imageCapture = imageCaptureUseCase

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview, imageCaptureUseCase, imageAnalyzerUseCase
                )

            } catch (exc: Exception) {
                Timber.e("Use case binding failed %s", exc.message)
            }

        }, ContextCompat.getMainExecutor(this.requireActivity()))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            context,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun updateProgressText(progress: Float) {
        binding.progressText.post {
            when {
                progress >= 1f -> {
                    binding.progressText.text = getString(R.string.capturing_selfie_message)
                }

                progress <= 0f -> {
                    binding.progressText.text = getString(R.string.hold_steady_message)
                }

                else -> {
                    val secondsRemaining =
                        (STABILITY_THRESHOLD_MS / 1000f) * (1 - progress)
                    binding.progressText.text = getString(
                        R.string.hold_steady_countdown_message,
                        String.format(
                            Locale.getDefault(),
                            "%.1f",
                            secondsRemaining.coerceAtLeast(0f)
                        )
                    )
                }
            }
        }
    }

    private fun onSelfieCaptured(bitmap: Bitmap) {
        binding.croppedPreview.post {
            binding.croppedPreview.setImageBitmap(bitmap)
            binding.progressText.text = getString(R.string.face_captured_message)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        meshAnalyzer = null
        cameraExecutor.shutdown()
        _binding = null
    }
}