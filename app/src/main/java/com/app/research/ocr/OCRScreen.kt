package com.app.research.ocr

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.app.research.camoverlaypointsmapping.Graphic
import com.app.research.camoverlaypointsmapping.GraphicOverlay
import com.app.research.camoverlaypointsmapping.RectGraphic
import com.app.research.ui.isPreviewMode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import timber.log.Timber
import java.io.File


@Composable
fun OCRScreen(modifier: Modifier = Modifier) {
    OCRContent(modifier)
}

@Composable
fun OCRContent(
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }

    val graphics = remember { mutableStateListOf<Graphic>() }
    var imageWidth by remember { mutableIntStateOf(0) }
    var imageHeight by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // Preview + overlay share the same 3:4 (portrait) box so their sizes match exactly.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .align(Alignment.Center)
        ) {
            if (!isPreviewMode)
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FIT_CENTER
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        }

                        val analyzer = YourImageAnalyzer { rects, w, h ->
                            graphics.clear()
                            graphics.addAll(rects)
                            imageWidth = w
                            imageHeight = h
                        }
                        val resolutionSelector = ResolutionSelector.Builder()
                            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                            .build()
                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setResolutionSelector(resolutionSelector)
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build().also {
                                it.setAnalyzer(ContextCompat.getMainExecutor(ctx), analyzer)
                            }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = androidx.camera.core.Preview.Builder()
                                .setResolutionSelector(resolutionSelector)
                                .build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalyzer,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                Timber.e(e)
                            }
                        }, ContextCompat.getMainExecutor(context))

                        previewView
                    },
                )

            GraphicOverlay(
                modifier = Modifier.fillMaxSize(),
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                graphics = graphics
            )
        }

        Box(
            modifier = Modifier
                .padding(bottom = 18.dp)
                .navigationBarsPadding()
                .background(
                    color = Color.White.copy(.7f),
                    shape = CircleShape
                )
                .align(Alignment.BottomCenter)
                .size(78.dp)
                .clickable {
                    captureAndOcr(imageCapture, context) { savedPath, recognized ->
                        Timber.d("OCR text saved: $savedPath\n$recognized")
                        Toast.makeText(
                            context,
                            if (savedPath != null) "Saved: $savedPath" else "Save failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        )
    }
}


private fun captureAndOcr(
    imageCapture: ImageCapture,
    context: Context,
    onDone: (savedPath: String?, recognizedText: String) -> Unit,
) {
    val jpeg = File(context.cacheDir, "ocr_capture_${System.currentTimeMillis()}.jpg")
    val options = ImageCapture.OutputFileOptions.Builder(jpeg).build()
    imageCapture.takePicture(
        options,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                runOcr(context, Uri.fromFile(jpeg)) { text ->
                    val savedPath = saveTextFile(context, text)
                    onDone(savedPath, text)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Timber.e(exception, "Capture failed")
                onDone(null, "")
            }
        }
    )
}

private fun runOcr(context: Context, imageUri: Uri, onText: (String) -> Unit) {
    val image = try {
        InputImage.fromFilePath(context, imageUri)
    } catch (e: Exception) {
        Timber.e(e, "InputImage.fromFilePath failed")
        onText("")
        return
    }
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            Timber.d("Captured OCR text:\n${visionText.text}")
            onText(visionText.text)
        }
        .addOnFailureListener { e ->
            Timber.e(e, "OCR failed")
            onText("")
        }
}

/**
 * Writes `text` to a .txt file the user can open from the Files / Downloads app.
 * - API 29+: MediaStore Downloads, no permissions.
 * - Older: app-scoped external storage (Android/data/<pkg>/files/Documents).
 */
private fun saveTextFile(context: Context, text: String): String? {
    val filename = "OCR_${System.currentTimeMillis()}.txt"
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, filename)
                put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver
                .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
            context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
            "Downloads/$filename"
        } else {
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: return null
            val file = File(dir, filename).apply { writeText(text) }
            file.absolutePath
        }
    } catch (e: Exception) {
        Timber.e(e, "saveTextFile failed")
        null
    }
}


private class YourImageAnalyzer(
    private val onResult: (List<RectGraphic>, Int, Int) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val rotation = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage, rotation)

        // ML Kit bounding boxes are in the rotated image's coordinate space
        val (imgW, imgH) = if (rotation == 90 || rotation == 270)
            imageProxy.height to imageProxy.width
        else
            imageProxy.width to imageProxy.height

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val rects = mutableListOf<RectGraphic>()
                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        for (element in line.elements) {
                            val b = element.boundingBox ?: continue
                            rects += RectGraphic(
                                left = b.left.toFloat(),
                                top = b.top.toFloat(),
                                right = b.right.toFloat(),
                                bottom = b.bottom.toFloat(),
                                color = Color.Green,
                                strokeWidth = 4f,
                                label = element.text
                            )
                        }
                    }
                }
                onResult(rects, imgW, imgH)
            }
            .addOnFailureListener { Timber.e(it) }
            .addOnCompleteListener { imageProxy.close() }
    }
}

@Preview
@Composable
private fun OCRScreenPreview() {
    OCRScreen()
}
