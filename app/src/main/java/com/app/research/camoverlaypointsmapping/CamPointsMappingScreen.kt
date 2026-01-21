package com.app.research.camoverlaypointsmapping

import MultiPermissionHandler
import android.Manifest
import android.content.Context
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.app.research.R
import com.app.research.singlescreen_r_d.skaifitness.FullSizeCenterBox
import com.app.research.singlescreen_r_d.skaifitness.HStack
import com.app.research.ui.theme.ResearchAndDevelopmentTheme
import com.app.research.ui.theme.black
import com.app.research.ui.theme.white

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


    Scaffold(
        modifier = modifier,
        containerColor = white,
        topBar = { TopBar() },
        bottomBar = {
            BottomBar()
        }
    ) { innerPadding ->
        CamPointsMappingOverlayContent()
    }
}


@Composable
fun TopBar(
    modifier: Modifier = Modifier
) {
    HStack(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .background(black.copy(alpha = 0.5f))
    ) {
        Text(
            "Cam Points Mapping",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = white,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
        )
    }
}

@Composable
fun BottomBar(
    modifier: Modifier = Modifier
) {
    HStack(
        modifier = modifier
            .fillMaxWidth()
            .background(black.copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.Absolute.SpaceBetween
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier.size(84.dp)
        )

        CaptureButton(
            enabled = false,
            onClick = {},
        )

        Spacer(
            modifier = Modifier.size(84.dp)
        )
    }

}

@Composable
fun CamPointsMappingOverlayContent(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State to hold graphics to draw (you can add graphics here later)
    var graphics by remember { mutableStateOf<List<Graphic>>(emptyList()) }


    // Camera image dimensions (will be set when camera initializes)
    var imageWidth by remember { mutableStateOf(0) }
    var imageHeight by remember { mutableStateOf(0) }

    val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()


    CameraPreview(
        imageAnalysis = imageAnalysis,
        context = context,
        lifecycleOwner = lifecycleOwner,
        graphics = graphics,
        imageWidth = imageWidth,
        imageHeight = imageHeight,
        modifier = modifier
    )
}

@Composable
fun CameraPreview(
    imageAnalysis: ImageAnalysis,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    graphics: List<Graphic>,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier
) {

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // track which lens is selected (default back)
    val chosenLensFacing = remember { mutableStateOf(LENS_FACING_BACK) }
    // boolean to tell overlays whether the image is flipped (true for front camera)
    val isImageFlipped = (chosenLensFacing.value == CameraSelector.LENS_FACING_FRONT)

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
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                val executor = ContextCompat.getMainExecutor(ctx)

                cameraProviderFuture.addListener({

                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                    // use the chosen lens facing value
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(chosenLensFacing.value)
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

        // Pose graphic overlay
        GraphicOverlay(
            modifier = Modifier.size(maxWidth, maxHeight),
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            isImageFlipped = isImageFlipped, // pass boolean now
            graphics = graphics
        )
    }

    // Clean up camera when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
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

@androidx.compose.ui.tooling.preview.Preview
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
