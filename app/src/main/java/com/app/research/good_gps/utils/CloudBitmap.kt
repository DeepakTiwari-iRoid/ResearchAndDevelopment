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
    val iterations = 100
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

/*
fun cloudBitmapGenerator(
    width: Int,
    height: Int,
    points: List<Point>,
): Bitmap {

    val bounds = calculateBound(points)
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLUE
        strokeWidth = 2f
        isAntiAlias = true
    }

    val step = 100

    for (i in 1 until step) {
        paint.strokeWidth *= 1.5f
        paint.alpha =255 - (i * (255 / step))
    }

    val center = bounds.center
    var expansionStep = 2f // how much to expand per iteration

    repeat(step) { iteration ->
        val path = Path() // fresh path each time
        points.firstOrNull()?.let { first ->
            // move first point outward from center
            val startX = first.x + (first.x - center.x) * (expansionStep / 100f)
            val startY = first.y + (first.y - center.y) * (expansionStep / 100f)
            path.moveTo(startX.toFloat(), startY.toFloat())
        }

        for (i in 1 until points.size) {
            val p = points[i]
            // shift each point outward from center proportionally
            val newX = p.x + (p.x - center.x) * (expansionStep / 100f)
            val newY = p.y + (p.y - center.y) * (expansionStep / 100f)
            path.lineTo(newX.toFloat(), newY.toFloat())
        }

        path.close()
        canvas.drawPath(path, paint)

        // Increment expansion for next iteration
        expansionStep *= 1.5f
    }
    return bitmap
}
*/


/*fun cloudBitmapGenerator(
    width: Int,
    height: Int,
    points: List<Point>,
    blurRadius: Float = 40f // controls softness of the fade
): Bitmap {
    // Main bitmap (will become white with transparent hole)
    val result = createBitmap(width, height)
    val canvas = Canvas(result)

    // Fill entire screen with white
    val bgPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Create polygon path
    val path = Path().apply {
        if (points.isNotEmpty()) {
            moveTo(points[0].x.toFloat(), points[0].y.toFloat())
            for (i in 1 until points.size) {
                lineTo(points[i].x.toFloat(), points[i].y.toFloat())
            }
            close()
        }
    }



    // Temporary alpha mask bitmap
    val mask = createBitmap(width, height, Bitmap.Config.ALPHA_8)
    val maskCanvas = Canvas(mask)

    // Fill mask polygon area (solid white = full alpha)
    val fillPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    maskCanvas.drawPath(path, fillPaint)

    // Blur the mask edges outward (soft fade)
    val blurredMask = mask.extractAlpha(
        Paint().apply {
            maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        },
        null
    )

    // Use blurred mask to clear transparent region (soft edges)
    val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        isAntiAlias = true
    }

    // Create gradient mask
    val gradientMask = createBitmap(width, height, Bitmap.Config.ALPHA_8)
    val gCanvas = Canvas(gradientMask)
    val gradientPaint = Paint().apply {
        shader = RadialGradient(
            width / 2f, height / 2f,
            width.toFloat() / 1.2f,
            Color.WHITE,
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
    }
    gCanvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)

// Combine blurred polygon mask + gradient mask
    val combinedMask = createBitmap(width, height, Bitmap.Config.ALPHA_8)
    val cCanvas = Canvas(combinedMask)
    val combinePaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY) }
    cCanvas.drawBitmap(blurredMask, 0f, 0f, null)
    cCanvas.drawBitmap(gradientMask, 0f, 0f, combinePaint)

// Use combined mask for DST_OUT clear
    canvas.drawBitmap(combinedMask, 0f, 0f, clearPaint)


    canvas.drawBitmap(blurredMask, 0f, 0f, clearPaint)

    mask.recycle()
    blurredMask.recycle()

    return result
}*/

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