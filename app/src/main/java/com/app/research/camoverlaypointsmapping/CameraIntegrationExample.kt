package com.app.research.camoverlaypointsmapping

import android.graphics.PointF
import android.graphics.RectF
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Example: How to integrate GraphicOverlay with CameraX
 * 
 * This file demonstrates how to use the GraphicOverlay with real camera data
 * and ML model outputs (like face detection, object detection, etc.)
 */

/**
 * Example state holder for camera overlay
 */
data class CameraOverlayState(
    val imageWidth: Int = 1280,
    val imageHeight: Int = 720,
    val isImageFlipped: Boolean = false,
    val graphics: List<Graphic> = emptyList()
)

/**
 * Example: Camera screen with overlay
 * 
 * In a real implementation:
 * 1. Replace the Box with your CameraX PreviewView
 * 2. Get actual image dimensions from camera
 * 3. Process camera frames with ML model
 * 4. Convert ML results to Graphic objects
 * 5. Update the graphics list
 */
@Composable
fun CameraWithOverlayExample(modifier: Modifier = Modifier) {
    var overlayState by remember { mutableStateOf(CameraOverlayState()) }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Step 1: Camera Preview
        // In real app, use CameraX PreviewView here
        // AndroidView(factory = { context ->
        //     PreviewView(context).apply {
        //         // Setup camera
        //     }
        // })
        
        // Step 2: Graphics Overlay on top
        GraphicOverlay(
            modifier = Modifier.fillMaxSize(),
            imageWidth = overlayState.imageWidth,
            imageHeight = overlayState.imageHeight,
            isImageFlipped = overlayState.isImageFlipped,
            graphics = overlayState.graphics
        )
    }
    
    // Step 3: Process camera frames and update graphics
    // In real app, this would be in your ImageAnalysis.Analyzer
    // Example:
    // LaunchedEffect(Unit) {
    //     processCameraFrames { mlResults ->
    //         overlayState = overlayState.copy(
    //             graphics = convertMLResultsToGraphics(mlResults)
    //         )
    //     }
    // }
}

/**
 * Example: Convert ML Kit Face Detection results to Graphics
 */
fun convertFaceDetectionToGraphics(
    faces: List<FaceDetectionResult>,
    imageWidth: Int,
    imageHeight: Int
): List<Graphic> {
    val graphics = mutableListOf<Graphic>()
    
    faces.forEach { face ->
        // Add face bounding box
        graphics.add(
            RectGraphic(
                left = face.boundingBox.left,
                top = face.boundingBox.top,
                right = face.boundingBox.right,
                bottom = face.boundingBox.bottom,
                color = Color.Green,
                strokeWidth = 5f,
                label = "Face ${face.confidence.toInt()}%"
            )
        )
        
        // Add facial landmarks
        face.landmarks.forEach { landmark ->
            graphics.add(
                PointGraphic(
                    x = landmark.position.x,
                    y = landmark.position.y,
                    radius = 8f,
                    color = Color.Red,
                    label = landmark.type
                )
            )
        }
    }
    
    return graphics
}

/**
 * Example: Convert TensorFlow Lite Object Detection results to Graphics
 */
fun convertObjectDetectionToGraphics(
    detections: List<ObjectDetectionResult>
): List<Graphic> {
    val graphics = mutableListOf<Graphic>()
    
    detections.forEach { detection ->
        // Add bounding box
        graphics.add(
            RectGraphic(
                left = detection.boundingBox.left,
                top = detection.boundingBox.top,
                right = detection.boundingBox.right,
                bottom = detection.boundingBox.bottom,
                color = Color.Blue,
                strokeWidth = 4f,
                label = "${detection.label} ${detection.confidence.toInt()}%"
            )
        )
    }
    
    return graphics
}

/**
 * Example: Convert Pose Detection results to Graphics
 */
fun convertPoseDetectionToGraphics(
    keypoints: List<PoseKeypoint>
): List<Graphic> {
    val graphics = mutableListOf<Graphic>()
    
    // Draw keypoints
    keypoints.forEach { keypoint ->
        graphics.add(
            PointGraphic(
                x = keypoint.position.x,
                y = keypoint.position.y,
                radius = 10f,
                color = Color.Cyan,
                label = keypoint.name
            )
        )
    }
    
    // Draw skeleton lines (connect keypoints)
    val connections = getPoseConnections()
    connections.forEach { (start, end) ->
        val startPoint = keypoints.find { it.name == start }
        val endPoint = keypoints.find { it.name == end }
        
        if (startPoint != null && endPoint != null) {
            graphics.add(
                LineGraphic(
                    startX = startPoint.position.x,
                    startY = startPoint.position.y,
                    endX = endPoint.position.x,
                    endY = endPoint.position.y,
                    color = Color.Green,
                    strokeWidth = 4f
                )
            )
        }
    }
    
    return graphics
}

/**
 * Example: Handle normalized coordinates from ML models
 * Many ML models output coordinates in normalized form (0.0 to 1.0)
 */
fun convertNormalizedDetectionToGraphics(
    normalizedRect: NormalizedRect,
    imageWidth: Int,
    imageHeight: Int,
    label: String
): RectGraphic {
    return RectGraphic(
        left = normalizedRect.left * imageWidth,
        top = normalizedRect.top * imageHeight,
        right = normalizedRect.right * imageWidth,
        bottom = normalizedRect.bottom * imageHeight,
        color = Color.Yellow,
        strokeWidth = 4f,
        label = label
    )
}

// Data classes for examples (replace with actual ML Kit or TFLite classes)
data class FaceDetectionResult(
    val boundingBox: RectF,
    val confidence: Float,
    val landmarks: List<FaceLandmark>
)

data class FaceLandmark(
    val type: String,
    val position: PointF
)

data class ObjectDetectionResult(
    val label: String,
    val confidence: Float,
    val boundingBox: RectF
)

data class PoseKeypoint(
    val name: String,
    val position: PointF,
    val confidence: Float
)

data class NormalizedRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

/**
 * Example pose skeleton connections
 */
fun getPoseConnections(): List<Pair<String, String>> {
    return listOf(
        "nose" to "left_eye",
        "nose" to "right_eye",
        "left_eye" to "left_ear",
        "right_eye" to "right_ear",
        "nose" to "left_shoulder",
        "nose" to "right_shoulder",
        "left_shoulder" to "left_elbow",
        "left_elbow" to "left_wrist",
        "right_shoulder" to "right_elbow",
        "right_elbow" to "right_wrist",
        "left_shoulder" to "left_hip",
        "right_shoulder" to "right_hip",
        "left_hip" to "right_hip",
        "left_hip" to "left_knee",
        "left_knee" to "left_ankle",
        "right_hip" to "right_knee",
        "right_knee" to "right_ankle"
    )
}

/**
 * INTEGRATION GUIDE:
 * 
 * 1. With CameraX:
 * ```kotlin
 * val imageAnalysis = ImageAnalysis.Builder()
 *     .setTargetResolution(Size(1280, 720))
 *     .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
 *     .build()
 * 
 * imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
 *     // Get image dimensions
 *     val imageWidth = imageProxy.width
 *     val imageHeight = imageProxy.height
 *     
 *     // Process with ML model
 *     val results = mlModel.detect(imageProxy)
 *     
 *     // Convert to graphics
 *     val graphics = convertMLResultsToGraphics(results)
 *     
 *     // Update UI
 *     overlayState = overlayState.copy(
 *         imageWidth = imageWidth,
 *         imageHeight = imageHeight,
 *         graphics = graphics
 *     )
 *     
 *     imageProxy.close()
 * }
 * ```
 * 
 * 2. With MLKit Face Detection:
 * ```kotlin
 * val detector = FaceDetection.getClient(options)
 * detector.process(image)
 *     .addOnSuccessListener { faces ->
 *         val graphics = faces.map { face ->
 *             RectGraphic(
 *                 left = face.boundingBox.left.toFloat(),
 *                 top = face.boundingBox.top.toFloat(),
 *                 right = face.boundingBox.right.toFloat(),
 *                 bottom = face.boundingBox.bottom.toFloat(),
 *                 color = Color.Green,
 *                 label = "Face"
 *             )
 *         }
 *         updateGraphics(graphics)
 *     }
 * ```
 * 
 * 3. With TensorFlow Lite:
 * ```kotlin
 * val results = detector.detect(bitmap)
 * val graphics = results.map { detection ->
 *     RectGraphic(
 *         left = detection.location.left,
 *         top = detection.location.top,
 *         right = detection.location.right,
 *         bottom = detection.location.bottom,
 *         color = Color.Blue,
 *         label = detection.label
 *     )
 * }
 * ```
 */
