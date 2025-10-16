package com.app.research.good_gps.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import androidx.core.graphics.createBitmap


fun cloudBitmapGenerator(
    width: Int,
    height: Int,
    points: List<Point>,
): Bitmap {

    val bounds = calculateBound(points)
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)

    // Fill with white background
    canvas.drawColor(Color.WHITE)

    // Create paint with proper settings
    val paint = Paint().apply {
        color = Color.WHITE
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    // Create expanded path to ensure drawing outside the original path
    val expandedPath = createExpandedPath(points, bounds.center)

    // Calculate stroke widths based on bounds
    val boundsWidth = bounds.right.x - bounds.left.x
    val boundsHeight = bounds.bottom.y - bounds.top.y
    val maxDimension = maxOf(boundsWidth, boundsHeight)

    val baseStrokeWidth = maxDimension * 0.1f  // 10% of max dimension
    val maxStrokeWidth = maxDimension * 0.3f   // 30% of max dimension

    // Progressive stroke drawing
    val iterations = 100 // More iterations = smoother fade
    for (progress in iterations downTo 1) {
        val normalizedProgress = progress.toFloat() / iterations

        // Calculate alpha: fade from opaque to transparent
        val alpha = lerp(0f, 255f, normalizedProgress).toInt()

        // Calculate stroke width: thinner to thicker
        val strokeWidth = (normalizedProgress * maxStrokeWidth) + baseStrokeWidth
        // Apply settings and draw
        paint.alpha = alpha
        paint.strokeWidth = strokeWidth

        canvas.drawPath(expandedPath, paint)
    }

    return bitmap
}

// Create an expanded path that goes outside the original points
private fun createExpandedPath(points: List<Point>, center: Point): Path {
    val path = Path()
    val expansionFactor = 1.2f  // Expand by 20%

    points.forEachIndexed { index, point ->
        // Expand each point outward from center
        val expandedX = center.x + (point.x - center.x) * expansionFactor
        val expandedY = center.y + (point.y - center.y) * expansionFactor

        if (index == 0) {
            path.moveTo(expandedX, expandedY)
        } else {
            path.lineTo(expandedX, expandedY)
        }
    }

    return path
}

// Helper function for linear interpolation
private fun lerp(start: Float, end: Float, progress: Float): Float {
    return start + (end - start) * progress
}

private fun calculateBound(points: List<Point>): Bounds {
    val top = points.minByOrNull { it.y } ?: Point(0, 0)
    val bottom = points.maxByOrNull { it.y } ?: Point(0, 0)
    val left = points.minByOrNull { it.x } ?: Point(0, 0)
    val right = points.maxByOrNull { it.x } ?: Point(0, 0)
    val midHorizontal = Point((left.x + right.x) / 2, (left.y + right.y) / 2)
    val midVertical = Point((top.x + bottom.x) / 2, (top.y + bottom.y) / 2)
    val center = Point((left.x + right.x) / 2, (top.y + bottom.y) / 2)
    return Bounds(top, left, right, bottom, midHorizontal, midVertical, center)
}

private data class Bounds(
    val top: Point,
    val left: Point,
    val right: Point,
    val bottom: Point,
    val midHorizontal: Point,
    val midVertical: Point,
    val center: Point,
)