package com.app.research.camoverlaypointsmapping

import MultiPermissionHandler
import android.Manifest
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.app.research.R
import com.app.research.singlescreen_r_d.skaifitness.FullSizeCenterBox
import com.app.research.singlescreen_r_d.skaifitness.HStack
import com.app.research.ui.theme.ResearchAndDevelopmentTheme
import com.app.research.ui.theme.white
import com.ml.android.scanner.DocumentScannerAnalyzer
import com.ml.android.scanner.DocumentScannerConfig
import com.ml.android.scanner.models.DetectedDocument
import timber.log.Timber

@Composable
fun CamPointsMappingScreen(modifier: Modifier = Modifier) {

    val requiredPermissions = listOf(
        Manifest.permission.CAMERA
    )

    MultiPermissionHandler(
        permissions = requiredPermissions,
        requiredPermission = requiredPermissions
    ) { onPermissionGranted ->

        if (!onPermissionGranted.contains(requiredPermissions[0])) {
            return@MultiPermissionHandler
        }
    }


    // Shared state for document detection and capture
    var isDocumentDetected by remember { mutableStateOf(false) }
    var captureCallback: (() -> Unit)? by remember { mutableStateOf(null) }

    Scaffold(
        modifier = modifier,
        containerColor = white,
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(
                onCaptureClick = { captureCallback?.invoke() },
                isDocumentDetected = isDocumentDetected
            )
        }
    ) { innerPadding ->
        CamPointsMappingOverlayContent(
            modifier = Modifier,
            onDocumentDetected = { isDocumentDetected = it },
            onCaptureReady = { callback -> captureCallback = callback }
        )
    }
}


@Composable
fun TopBar(
    modifier: Modifier = Modifier
) {
    HStack(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.Black.copy(alpha = 0.5f))
            .statusBarsPadding()
    ) {
        Text(
            "Cam Points Mapping",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = white,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    onCaptureClick: () -> Unit = {},
    isDocumentDetected: Boolean = false
) {
    HStack(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(16.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.Absolute.SpaceBetween
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier.size(84.dp)
        )

        CaptureButton(
            enabled = isDocumentDetected,
            onClick = onCaptureClick,
        )

        Spacer(
            modifier = Modifier.size(84.dp)
        )
    }

}

@Composable
fun CamPointsMappingOverlayContent(
    modifier: Modifier = Modifier,
    onDocumentDetected: (Boolean) -> Unit = {},
    onCaptureReady: ((() -> Unit) -> Unit) = {}
) {
    CameraPreview(
        modifier = modifier,
        onDocumentDetected = onDocumentDetected,
        onCaptureReady = onCaptureReady
    )
}


@Composable
fun CameraPreview(
    modifier: Modifier,
    onDocumentDetected: (Boolean) -> Unit = {},
    onCaptureReady: ((() -> Unit) -> Unit) = {}
) {

    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State to hold graphics to draw (document polygon overlay)
    var graphics by remember { mutableStateOf<List<Graphic>>(emptyList()) }
    val executor = remember { ContextCompat.getMainExecutor(ctx) }

    // Camera image dimensions (will be set when camera initializes)
    var imageWidth by remember { mutableIntStateOf(0) }
    var imageHeight by remember { mutableIntStateOf(0) }

    // State for document scanner
    var detectedDocument by remember { mutableStateOf<DetectedDocument?>(null) }
    var scannerAnalyzer by remember { mutableStateOf<DocumentScannerAnalyzer?>(null) }

    // Document scanner configuration
    val scannerConfig = remember {
        DocumentScannerConfig(
            autoCapture = false, // Manual capture only
            strokeColor = Color.Green.toArgb(),
            fillAlpha = 0.2f
        )
    }

    val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    // Create DocumentScannerAnalyzer
    val analyzer = remember {
        DocumentScannerAnalyzer(
            config = scannerConfig,
            onDocumentDetected = { document, _ ->
                // Update detected document state
                detectedDocument = document
                onDocumentDetected(document != null && document.isValid)

                // Convert DetectedDocument to PolygonGraphic
                if (document != null && document.isValid) {
                    val polygonPoints = document.points.map { point ->
                        Pair(point.x.toFloat(), point.y.toFloat())
                    }

                    graphics = listOf(
                        PolygonGraphic(
                            points = polygonPoints,
                            color = Color.Green,
                            strokeWidth = 8f,
                            isClosed = true
                        )
                    )

                    // Update image dimensions from document
                    if (imageWidth <= 0) {
                        imageWidth = document.frameWidth
                        imageHeight = document.frameHeight
                    }
                } else {
                    graphics = emptyList()
                }
            },
            onDocumentCaptured = { bitmap: Bitmap ->
                // Handle captured document bitmap
                Timber.d("Document captured: ${bitmap.width}x${bitmap.height}")
                // You can save the bitmap or show it in a preview screen here
            }
        ).also { scannerAnalyzer = it }
    }

    // Expose capture function to parent
    LaunchedEffect(Unit) {
        onCaptureReady {
            scannerAnalyzer?.triggerManualCapture()
        }
    }

    imageAnalysis.setAnalyzer(executor, analyzer)

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(ctx) }

    // track which lens is selected (default back)
    val chosenLensFacing by remember { mutableIntStateOf(LENS_FACING_BACK) }
    // boolean to tell overlays whether the image is flipped (true for front camera)
    val isImageFlipped = (chosenLensFacing == CameraSelector.LENS_FACING_FRONT)


    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {

        // camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    /*setRenderEffect(
                        android.graphics.RenderEffect.createBlurEffect(
                            26f, 26f,
                            android.graphics.Shader.TileMode.CLAMP
                        )
                    )*/

                    scaleType = PreviewView.ScaleType.FIT_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                cameraProviderFuture.addListener({

                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                    Timber.d("Preview Width ${previewView.width} Height ${previewView.height} : scale type : ${previewView.scaleType}")
                    // use the chosen lens facing value
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(chosenLensFacing)
                        .build()

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis,
                    )
                }, executor)

                previewView
            },
            modifier = Modifier
                .fillMaxSize()
        )

        // Document scanner graphic overlay
        GraphicOverlay(
            modifier = Modifier.fillMaxSize(),
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            isImageFlipped = isImageFlipped,
            graphics = graphics,
        )
    }

    // Clean up camera when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.get().unbindAll()
        }
    }
}

@Composable
fun CaptureButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(if (enabled) Color.Gray.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.3f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Inner circle
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (enabled) Color.White else Color.LightGray)
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    device = "spec:width=1080px,height=2340px,dpi=440,navigation=buttons",
    showSystemUi = true, showBackground = false
)
@Composable
private fun CameraOverlayPrev() {
    ResearchAndDevelopmentTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = white,
            topBar = { TopBar() },
            bottomBar = {
                BottomBar()
            }
        ) { innerPadding ->
            FullSizeCenterBox(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red)
                    .padding(paddingValues = innerPadding)
            ) {
                Text(text = "Camera Preview")
            }
        }
    }
}
