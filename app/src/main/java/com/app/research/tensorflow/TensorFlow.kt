package com.app.research.tensorflow

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun TensorFlow(modifier: Modifier = Modifier) {
    TensorFlowContent(modifier)
}

@Composable
fun TensorFlowContent(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var detector by remember { mutableStateOf<Detector?>(null) }
    var boundingBoxes by remember { mutableStateOf<List<BoundingBox>>(emptyList()) }
    var inferenceTime by remember { mutableLongStateOf(0L) }
    var isDetecting by remember { mutableStateOf(false) }
    
    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Timber.tag("TensorFlow").d("Camera permission granted")
        } else {
            Timber.tag("TensorFlow").d("Camera permission denied")
        }
    }
    
    // Check camera permission on first composition
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Initialize detector
    LaunchedEffect(context) {
        Timber.tag("TensorFlow").d("Initializing detector...")
        detector = Detector(
            context = context,
            detectorListener = object : Detector.DetectorListener {
                override fun onEmptyDetect() {
                    Timber.tag("TensorFlow").d("No objects detected")
                    boundingBoxes = emptyList()
                    isDetecting = false
                }
                
                override fun onDetect(detectedBoxes: List<BoundingBox>, time: Long) {
                    Timber.tag("TensorFlow").d("Detected ${detectedBoxes.size} objects in ${time}ms")
                    boundingBoxes = detectedBoxes
                    inferenceTime = time
                    isDetecting = false
                }
            }
        )
        detector?.setup()
        Timber.tag("TensorFlow").d("Detector setup completed")
    }
    
    // Cleanup detector on dispose
    DisposableEffect(Unit) {
        onDispose {
            detector?.clear()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Header with inference time
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Object Detection",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${inferenceTime}ms",
                    color = if (isDetecting) Color.Yellow else Color.Green,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Camera preview with overlay
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onImageCaptured = { bitmap ->
                    if (!isDetecting) {
                        Timber.tag("TensorFlow").d("Processing camera frame: ${bitmap.width}x${bitmap.height}")
                        isDetecting = true
                        detector?.detect(bitmap)
                    }
                }
            )
            
            // Bounding box overlay
            BoundingBoxOverlay(
                modifier = Modifier.fillMaxSize(),
                boundingBoxes = boundingBoxes
            )
        }
        
        // Detection status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f))
        ) {
            Text(
                text = if (boundingBoxes.isEmpty()) {
                    "No objects detected"
                } else {
                    "Detected ${boundingBoxes.size} object(s)"
                },
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var preview by remember { mutableStateOf<Preview?>(null) }
    var imageAnalyzer by remember { mutableStateOf<ImageAnalysis?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }


    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            previewView.scaleType = PreviewView.ScaleType.FIT_CENTER
            
            // Initialize camera
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(
                    context = ctx,
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    cameraProvider = cameraProvider!!,
                    cameraExecutor = cameraExecutor,
                    onImageCaptured = onImageCaptured,
                    onPreviewReady = { previewInstance ->
                        preview = previewInstance
                    },
                    onImageAnalyzerReady = { analyzer ->
                        imageAnalyzer = analyzer
                    },
                    onCameraReady = { cameraInstance ->
                        camera = cameraInstance
                    }
                )
            }, ContextCompat.getMainExecutor(ctx))
            
            previewView
        },
        modifier = modifier
    )
}

private fun bindCameraUseCases(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    cameraProvider: ProcessCameraProvider,
    cameraExecutor: ExecutorService,
    onImageCaptured: (Bitmap) -> Unit,
    onPreviewReady: (Preview) -> Unit,
    onImageAnalyzerReady: (ImageAnalysis) -> Unit,
    onCameraReady: (Camera) -> Unit
) {
    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
        .build()
    
    val preview = Preview.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
        .build()
    
    val imageAnalyzer = ImageAnalysis.Builder()
        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .build()
    
    imageAnalyzer.setAnalyzer(cameraExecutor) { imageProxy ->
        val bitmapBuffer = createBitmap(imageProxy.width, imageProxy.height)
        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
        imageProxy.close()
        
        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
        }
        
        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
            matrix, true
        )
        
        onImageCaptured(rotatedBitmap)
    }
    
    cameraProvider.unbindAll()
    
    try {
        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalyzer
        )

        preview.surfaceProvider = previewView.surfaceProvider
        
        onPreviewReady(preview)
        onImageAnalyzerReady(imageAnalyzer)
        onCameraReady(camera)
        
    } catch (exc: Exception) {
        Timber.tag("TensorFlow").e(exc, "Use case binding failed")
    }
}

@Composable
private fun BoundingBoxOverlay(
    modifier: Modifier = Modifier,
    boundingBoxes: List<BoundingBox>
) {
    val textMeasurer = rememberTextMeasurer()
    
    Canvas(
        modifier = modifier.clipToBounds()
    ) {
        boundingBoxes.forEach { box ->
            drawBoundingBox(box, textMeasurer)
        }
    }
}

private fun DrawScope.drawBoundingBox(
    box: BoundingBox,
    textMeasurer: TextMeasurer
) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    
    // Convert normalized coordinates to canvas coordinates
    val left = box.x1 * canvasWidth
    val top = box.y1 * canvasHeight
    val right = box.x2 * canvasWidth
    val bottom = box.y2 * canvasHeight
    
    // Draw bounding box rectangle
    drawRect(
        color = Color.Red,
        topLeft = androidx.compose.ui.geometry.Offset(left, top),
        size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
    )
    
    // Draw label background and text
    val labelText = "${box.clsName} (${(box.cnf * 100).toInt()}%)"
    val textStyle = androidx.compose.ui.text.TextStyle(
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
    )
    
    val textLayoutResult = textMeasurer.measure(
        text = androidx.compose.ui.text.AnnotatedString(labelText),
        style = textStyle,
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Clip
    )
    
    val textWidth = textLayoutResult.size.width
    val textHeight = textLayoutResult.size.height
    val padding = 8f
    
    // Draw label background rectangle
    drawRect(
        color = Color.Red,
        topLeft = androidx.compose.ui.geometry.Offset(left, top - textHeight - padding),
        size = androidx.compose.ui.geometry.Size(textWidth + padding * 2, textHeight + padding)
    )
    
    // Draw label text
    drawText(
        textMeasurer = textMeasurer,
        text = androidx.compose.ui.text.AnnotatedString(labelText),
        style = textStyle,
        topLeft = androidx.compose.ui.geometry.Offset(left + padding, top - padding),
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Clip
    )
}

