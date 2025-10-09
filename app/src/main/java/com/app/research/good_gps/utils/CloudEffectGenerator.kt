package com.app.research.good_gps.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.Log
import androidx.core.graphics.createBitmap
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * CloudEffectGenerator - Creates the white overlay with faded edges for golf course maps
 *
 * This class generates a "cloud" bitmap that creates the floating effect where the golf course
 * appears to be cut out from a white background with smooth, natural-looking edges.
 */
class CloudEffectGenerator {

    companion object {
        private const val TAG = "CloudEffectGenerator"

        // Constants for stroke width calculation (in meters)
        private const val BASE_STROKE_WIDTH_METERS = 50.0
        private const val MAX_STROKE_WIDTH_METERS = 75.0

        // Fade iteration constants
        private const val FADE_ITERATIONS = 100
        private const val FADE_STEP = 1.0f

        // Scaling factor for final bitmap
        private const val FINAL_SCALE_FACTOR = 0.125f

        // Small offset to ensure proper rendering
        private const val PATH_OFFSET = 5
    }

    /**
     * Data class representing a coordinate point
     */
    data class Coordinate(
        val latitude: Double,
        val longitude: Double
    )

    /**
     * Data class representing a pixel point on screen
     */
    data class PixelPoint(
        val x: Int,
        val y: Int
    )

    /**
     * Data class for aerial image request parameters
     */
    data class AerialImageRequest(
        val metersPerPixel: Double,
        val topLeft: Coordinate,
        val rotation: Double,
        val size: SizeD,
        val holeParams: HoleParams
    )

    /**
     * Data class for hole parameters
     */
    data class HoleParams(
        val segments: List<Coordinate>
    )

    /**
     * Data class for size dimensions
     */
    data class SizeD(
        val width: Double,
        val height: Double
    )

    /**
     * Coordinate translator for converting GPS coordinates to screen pixels
     */
    class CoordinateTranslator(
        private val topLeft: Coordinate,
        private val metersPerPixel: Float,
        private val direction: Float
    ) {
        fun getPointForCoord(coord: Coordinate): PixelPoint {
            // Convert GPS coordinate to screen pixel
            val distance = calculateDistance(topLeft, coord)
            val bearing = calculateBearing(topLeft, coord)

            val x = (distance * sin(Math.toRadians(bearing)) / metersPerPixel).toInt()
            val y = (distance * cos(Math.toRadians(bearing)) / metersPerPixel).toInt()

            return PixelPoint(x, y)
        }

        private fun calculateDistance(point1: Coordinate, point2: Coordinate): Double {
            val earthRadius = 6371000.0 // Earth radius in meters
            val lat1Rad = Math.toRadians(point1.latitude)
            val lat2Rad = Math.toRadians(point2.latitude)
            val deltaLatRad = Math.toRadians(point2.latitude - point1.latitude)
            val deltaLonRad = Math.toRadians(point2.longitude - point1.longitude)

            val a =
                sin(deltaLatRad / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltaLonRad / 2).pow(
                    2
                )
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            return earthRadius * c
        }

        private fun calculateBearing(point1: Coordinate, point2: Coordinate): Double {
            val lat1Rad = Math.toRadians(point1.latitude)
            val lat2Rad = Math.toRadians(point2.latitude)
            val deltaLonRad = Math.toRadians(point2.longitude - point1.longitude)

            val y = sin(deltaLonRad) * cos(lat2Rad)
            val x = cos(lat1Rad) * sin(lat2Rad) - sin(lat1Rad) * cos(lat2Rad) * cos(deltaLonRad)

            return Math.toDegrees(atan2(y, x))
        }
    }

    /**
     * Creates the cloud bitmap with faded edges
     *
     * @param courseId Unique identifier for the golf course
     * @param holeNumber Hole number
     * @param request Aerial image request parameters
     * @return Generated cloud bitmap
     */
    fun createCloud(
        courseId: String,
        holeNumber: Int,
        request: AerialImageRequest
    ): Bitmap? {
        try {
            Log.d(TAG, "Creating cloud effect for course: $courseId, hole: $holeNumber")

            // Calculate stroke widths based on map scale
            val baseStrokeWidth = (BASE_STROKE_WIDTH_METERS / request.metersPerPixel).toFloat()
            val maxStrokeWidth = (MAX_STROKE_WIDTH_METERS / request.metersPerPixel).toFloat()

            Log.d(TAG, "Stroke widths - Base: $baseStrokeWidth, Max: $maxStrokeWidth")

            // Create paint for drawing
            val paint = Paint().apply {
                color = Color.WHITE
                xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                style = Paint.Style.STROKE
            }

            // Create bitmap for drawing
            val bitmap = createBitmap(
                (request.size.width + PATH_OFFSET).toInt(),
                request.size.height.toInt(),
                Bitmap.Config.ALPHA_8
            )

            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE) // Fill with white

            // Create coordinate translator
            val translator = CoordinateTranslator(
                request.topLeft,
                request.metersPerPixel.toFloat(),
                (-request.rotation).toFloat()
            )

            // Create path from golf course segments
            val path = createPathFromSegments(request.holeParams.segments, translator)

            // Draw progressive strokes to create fade effect
            drawProgressiveStrokes(canvas, path, paint, baseStrokeWidth, maxStrokeWidth)

            // Create final scaled bitmap
            val finalBitmap = createScaledBitmap(bitmap, request.size)

            // Clean up
            bitmap.recycle()

            Log.d(TAG, "Cloud effect created successfully")
            return finalBitmap

        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Out of memory error creating cloud effect", e)
            System.gc()
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error creating cloud effect", e)
            return null
        }
    }

    /**
     * Creates a path from golf course segment coordinates
     */
    private fun createPathFromSegments(
        segments: List<Coordinate>,
        translator: CoordinateTranslator
    ): Path {
        val path = Path()

        segments.forEachIndexed { index, coordinate ->
            val pixelPoint = translator.getPointForCoord(coordinate)

            if (index == 0) {
                // Move to first point
                path.moveTo(
                    pixelPoint.x + PATH_OFFSET.toFloat(),
                    pixelPoint.y.toFloat()
                )
            } else {
                // Line to subsequent points
                path.lineTo(
                    pixelPoint.x + PATH_OFFSET.toFloat(),
                    pixelPoint.y.toFloat()
                )
            }
        }

        return path
    }

    /**
     * Draws progressive strokes to create the fade effect
     */
    private fun drawProgressiveStrokes(
        canvas: Canvas,
        path: Path,
        paint: Paint,
        baseStrokeWidth: Float,
        maxStrokeWidth: Float
    ) {
        Log.d(TAG, "Drawing progressive strokes...")

        // Draw 100 iterations with varying alpha and stroke width
        for (progress in FADE_ITERATIONS downTo 1) {
            val normalizedProgress = progress.toFloat() / FADE_ITERATIONS

            // Calculate alpha (fade from opaque to transparent)
            val alpha = lerp(0f, 255f, normalizedProgress).toInt()

            // Calculate stroke width (thinner to thicker)
            val strokeWidth = (normalizedProgress * maxStrokeWidth) + baseStrokeWidth

            // Apply settings and draw
            paint.alpha = alpha
            paint.strokeWidth = strokeWidth

            canvas.drawPath(path, paint)
        }

        Log.d(TAG, "Progressive strokes completed")
    }

    /**
     * Creates a scaled down version of the bitmap for performance
     */
    private fun createScaledBitmap(originalBitmap: Bitmap, size: SizeD): Bitmap {
        val scaledWidth = ceil(size.width * FINAL_SCALE_FACTOR).toInt()
        val scaledHeight = (size.height * FINAL_SCALE_FACTOR).toInt()

        val scaledBitmap = createBitmap(scaledWidth, scaledHeight)

        val canvas = Canvas(scaledBitmap)
        canvas.drawColor(Color.TRANSPARENT)

        val matrix = Matrix()
        matrix.postScale(FINAL_SCALE_FACTOR, FINAL_SCALE_FACTOR)

        val scaledPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawBitmap(originalBitmap, matrix, scaledPaint)

        return scaledBitmap
    }

    /**
     * Linear interpolation utility function
     */
    private fun lerp(start: Float, end: Float, progress: Float): Float {
        return start + (end - start) * progress
    }

    /**
     * Saves the cloud bitmap to database (placeholder for actual implementation)
     */
    fun saveCloudToDatabase(
        courseId: String,
        holeNumber: Int,
        cloudBitmap: Bitmap
    ): Boolean {
        return try {
            // Convert bitmap to byte array
            val outputStream = java.io.ByteArrayOutputStream()
            cloudBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val imageBytes = outputStream.toByteArray()

            // TODO: Save to database
            // Example: database.insertCloud(courseId, holeNumber, imageBytes)

            Log.d(TAG, "Cloud bitmap saved to database")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving cloud bitmap to database", e)
            false
        }
    }
}

/**
 * Extension function to create a cloud effect for a golf hole
 */
fun CloudEffectGenerator.createCloudForHole(
    courseId: String,
    holeNumber: Int,
    segments: List<CloudEffectGenerator.Coordinate>,
    metersPerPixel: Double,
    topLeft: CloudEffectGenerator.Coordinate,
    rotation: Double = 0.0,
    mapWidth: Double,
    mapHeight: Double
): Bitmap? {
    val request = CloudEffectGenerator.AerialImageRequest(
        metersPerPixel = metersPerPixel,
        topLeft = topLeft,
        rotation = rotation,
        size = CloudEffectGenerator.SizeD(mapWidth, mapHeight),
        holeParams = CloudEffectGenerator.HoleParams(segments)
    )

    return createCloud(courseId, holeNumber, request)
}

/**
 * Usage example
 */
class CloudEffectExample {

    fun createExampleCloudEffect() {
        val generator = CloudEffectGenerator()

        // Example golf hole segments (GPS coordinates)
        val segments = listOf(
            CloudEffectGenerator.Coordinate(40.7128, -74.0060), // Tee box
            CloudEffectGenerator.Coordinate(40.7130, -74.0058), // Fairway point 1
            CloudEffectGenerator.Coordinate(40.7132, -74.0056), // Fairway point 2
            CloudEffectGenerator.Coordinate(40.7134, -74.0054)  // Green
        )

        // Create cloud effect
        val cloudBitmap = generator.createCloudForHole(
            courseId = "example_course_123",
            holeNumber = 1,
            segments = segments,
            metersPerPixel = 0.5, // 0.5 meters per pixel
            topLeft = CloudEffectGenerator.Coordinate(40.7120, -74.0070),
            rotation = 0.0,
            mapWidth = 1000.0,
            mapHeight = 800.0
        )

        // Save to database
        cloudBitmap?.let { bitmap ->
            generator.saveCloudToDatabase("example_course_123", 1, bitmap)
        }
    }
}
