package com.app.research.faceml.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.facemesh.FaceMeshDetector
import timber.log.Timber
import java.io.ByteArrayOutputStream
import kotlin.math.hypot

class MeshImageAnalyzer(
    private val faceMeshDetector: FaceMeshDetector,
    private val graphicOverlay: GraphicOverlay,
    private val isImageFlipped: Boolean,
    private val onSelfieCaptured: (Bitmap) -> Unit,
    private val onStabilityProgress: ((Float) -> Unit)? = null,
    private val stabilityThresholdMillis: Long = 2_000L,
    private val movementThresholdPx: Float = 20f
) : ImageAnalysis.Analyzer {

    private var needUpdateOverlayImageSourceInfo = true
    private var previousImageWidth = 0
    private var previousImageHeight = 0
    private var lastCenterX: Float? = null
    private var lastCenterY: Float? = null
    private var stableStartTime: Long? = null
    private var captureTriggered = false

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val imageWidth =
            if (rotationDegrees == 0 || rotationDegrees == 180) imageProxy.width else imageProxy.height
        val imageHeight =
            if (rotationDegrees == 0 || rotationDegrees == 180) imageProxy.height else imageProxy.width

        if (needUpdateOverlayImageSourceInfo ||
            imageWidth != previousImageWidth ||
            imageHeight != previousImageHeight
        ) {
            graphicOverlay.setImageSourceInfo(
                imageWidth,
                imageHeight,
                isImageFlipped
            )
            needUpdateOverlayImageSourceInfo = false
            previousImageWidth = imageWidth
            previousImageHeight = imageHeight
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)
        faceMeshDetector
            .process(inputImage)
            .addOnSuccessListener { faceMeshes ->
                graphicOverlay.clear()
                if (faceMeshes.isEmpty()) {
                    resetStability()
                    captureTriggered = false
                } else {
                    val primaryFace = faceMeshes.first()
                    handleFaceStability(primaryFace.boundingBox, imageProxy)
                }
                faceMeshes.forEach { faceMesh ->
                    graphicOverlay.add(FaceMeshGraphic(graphicOverlay, faceMesh))
                }
                graphicOverlay.postInvalidate()
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Face mesh detection failed")
                resetStability()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun handleFaceStability(boundingBox: Rect, imageProxy: ImageProxy) {
        val centerX = boundingBox.exactCenterX()
        val centerY = boundingBox.exactCenterY()
        val lastX = lastCenterX
        val lastY = lastCenterY

        if (lastX == null || lastY == null) {
            lastCenterX = centerX
            lastCenterY = centerY
            stableStartTime = null
            onStabilityProgress?.invoke(0f)
            return
        }

        val movement = hypot(centerX - lastX, centerY - lastY)
        if (movement > movementThresholdPx) {
            stableStartTime = null
            onStabilityProgress?.invoke(0f)
        } else {

            val now = SystemClock.elapsedRealtime()

            if (stableStartTime == null) {
                stableStartTime = now
            }
            val elapsed = now - stableStartTime!!
            val progress = (elapsed.toFloat() / stabilityThresholdMillis).coerceIn(0f, 1f)
            onStabilityProgress?.invoke(progress)
            if (!captureTriggered && elapsed >= stabilityThresholdMillis) {
                captureTriggered = true
                captureSelfie(imageProxy, boundingBox)
            }
        }

        lastCenterX = centerX
        lastCenterY = centerY
    }

    private fun resetStability() {
        lastCenterX = null
        lastCenterY = null
        stableStartTime = null
        if (!captureTriggered) {
            onStabilityProgress?.invoke(0f)
        }
    }

    private fun captureSelfie(imageProxy: ImageProxy, boundingBox: Rect) {
        runCatching {
            val bitmap = imageProxy.toBitmap()
            val rotated = bitmap.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            val cropped = rotated.crop(boundingBox)
            onSelfieCaptured(cropped)
        }.onFailure {
            Timber.e(it, "Failed to capture cropped selfie")
        }
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun Bitmap.rotate(degrees: Float): Bitmap {
        if (degrees == 0f) return this
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private fun Bitmap.crop(rect: Rect): Bitmap {
        val safeRect = Rect(
            rect.left.coerceAtLeast(0),
            rect.top.coerceAtLeast(0),
            rect.right.coerceAtMost(width),
            rect.bottom.coerceAtMost(height)
        )
        if (safeRect.width() <= 0 || safeRect.height() <= 0) {
            return this
        }
        return Bitmap.createBitmap(
            this,
            safeRect.left,
            safeRect.top,
            safeRect.width(),
            safeRect.height()
        )
    }
}
