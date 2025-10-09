package com.app.research.good_gps.utils

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
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
    canvas.drawBitmap(blurredMask, 0f, 0f, clearPaint)

    mask.recycle()
    blurredMask.recycle()

    return result
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