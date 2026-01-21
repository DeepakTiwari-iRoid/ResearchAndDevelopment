package com.app.research.camoverlaypointsmapping

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

/**
 * GraphicOverlay - A canvas overlay for drawing graphics with coordinate transformation
 * 
 * This overlay handles coordinate mapping between:
 * - Image coordinates (e.g., from camera/ML model)
 * - Screen coordinates (where to draw on screen)
 */
@Composable
fun GraphicOverlay(
    modifier: Modifier = Modifier,
    imageWidth: Int = 0,
    imageHeight: Int = 0,
    isImageFlipped: Boolean = false,
    graphics: List<Graphic> = emptyList()
) {

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Calculate scale factors for coordinate transformation
        val scaleX = if (imageWidth > 0) canvasWidth / imageWidth else 1f
        val scaleY = if (imageHeight > 0) canvasHeight / imageHeight else 1f

        // Draw each graphic
        graphics.forEach { graphic ->
            graphic.draw(
                drawScope = this,
                scaleX = scaleX,
                scaleY = scaleY,
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                isImageFlipped = isImageFlipped
            )
        }
    }
}

/**
 * Base class for all drawable graphics
 */
sealed class Graphic {
    abstract fun draw(
        drawScope: DrawScope,
        scaleX: Float,
        scaleY: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        isImageFlipped: Boolean
    )
    
    /**
     * Transforms a point from image coordinates to screen coordinates
     */
    protected fun transformX(
        x: Float,
        scaleX: Float,
        canvasWidth: Float,
        isImageFlipped: Boolean
    ): Float {
        return if (isImageFlipped) {
            canvasWidth - (x * scaleX)
        } else {
            x * scaleX
        }
    }
    
    protected fun transformY(y: Float, scaleY: Float): Float {
        return y * scaleY
    }
}

/**
 * Draws a point/circle
 */
data class PointGraphic(
    val x: Float,
    val y: Float,
    val radius: Float = 10f,
    val color: Color = Color.Red,
    val label: String? = null
) : Graphic() {
    override fun draw(
        drawScope: DrawScope,
        scaleX: Float,
        scaleY: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        isImageFlipped: Boolean
    ) {
        with(drawScope) {
            val screenX = transformX(x, scaleX, canvasWidth, isImageFlipped)
            val screenY = transformY(y, scaleY)
            
            // Draw circle
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(screenX, screenY),
                style = Stroke(width = 3.dp.toPx())
            )
            
            // Draw filled center
            drawCircle(
                color = color,
                radius = radius / 2,
                center = Offset(screenX, screenY)
            )
            
            // Draw label if provided
            label?.let {
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        textSize = 32f
                        this.color = color.hashCode()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText(it, screenX, screenY - radius - 10f, paint)
                }
            }
        }
    }
}

/**
 * Draws a line between two points
 */
data class LineGraphic(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val color: Color = Color.Green,
    val strokeWidth: Float = 5f
) : Graphic() {
    override fun draw(
        drawScope: DrawScope,
        scaleX: Float,
        scaleY: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        isImageFlipped: Boolean
    ) {
        with(drawScope) {
            val screenStartX = transformX(startX, scaleX, canvasWidth, isImageFlipped)
            val screenStartY = transformY(startY, scaleY)
            val screenEndX = transformX(endX, scaleX, canvasWidth, isImageFlipped)
            val screenEndY = transformY(endY, scaleY)
            
            drawLine(
                color = color,
                start = Offset(screenStartX, screenStartY),
                end = Offset(screenEndX, screenEndY),
                strokeWidth = strokeWidth
            )
        }
    }
}

/**
 * Draws a rectangle
 */
data class RectGraphic(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val color: Color = Color.Blue,
    val strokeWidth: Float = 5f,
    val label: String? = null
) : Graphic() {
    override fun draw(
        drawScope: DrawScope,
        scaleX: Float,
        scaleY: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        isImageFlipped: Boolean
    ) {
        with(drawScope) {
            val screenLeft = transformX(left, scaleX, canvasWidth, isImageFlipped)
            val screenTop = transformY(top, scaleY)
            val screenRight = transformX(right, scaleX, canvasWidth, isImageFlipped)
            val screenBottom = transformY(bottom, scaleY)
            
            // Ensure correct ordering (left < right)
            val actualLeft = min(screenLeft, screenRight)
            val actualRight = max(screenLeft, screenRight)
            
            drawRect(
                color = color,
                topLeft = Offset(actualLeft, screenTop),
                size = Size(actualRight - actualLeft, screenBottom - screenTop),
                style = Stroke(width = strokeWidth)
            )
            
            // Draw label if provided
            label?.let {
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        textSize = 40f
                        this.color = color.hashCode()
                        textAlign = android.graphics.Paint.Align.LEFT
                    }
                    drawText(it, actualLeft, screenTop - 10f, paint)
                }
            }
        }
    }
}

/**
 * Draws a polygon (connected points)
 */
data class PolygonGraphic(
    val points: List<Pair<Float, Float>>,
    val color: Color = Color.Cyan,
    val strokeWidth: Float = 5f,
    val isClosed: Boolean = true
) : Graphic() {
    override fun draw(
        drawScope: DrawScope,
        scaleX: Float,
        scaleY: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        isImageFlipped: Boolean
    ) {
        with(drawScope) {
            if (points.size < 2) return
            
            // Transform all points
            val transformedPoints = points.map { (x, y) ->
                Offset(
                    transformX(x, scaleX, canvasWidth, isImageFlipped),
                    transformY(y, scaleY)
                )
            }
            
            // Draw lines connecting points
            for (i in 0 until transformedPoints.size - 1) {
                drawLine(
                    color = color,
                    start = transformedPoints[i],
                    end = transformedPoints[i + 1],
                    strokeWidth = strokeWidth
                )
            }
            
            // Close the polygon if requested
            if (isClosed && transformedPoints.size > 2) {
                drawLine(
                    color = color,
                    start = transformedPoints.last(),
                    end = transformedPoints.first(),
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}

/**
 * Draws text at a specific location
 */
data class TextGraphic(
    val x: Float,
    val y: Float,
    val text: String,
    val color: Color = Color.White,
    val textSize: Float = 48f
) : Graphic() {
    override fun draw(
        drawScope: DrawScope,
        scaleX: Float,
        scaleY: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        isImageFlipped: Boolean
    ) {
        with(drawScope) {
            val screenX = transformX(x, scaleX, canvasWidth, isImageFlipped)
            val screenY = transformY(y, scaleY)
            
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    this.textSize = textSize
                    this.color = color.hashCode()
                    textAlign = android.graphics.Paint.Align.LEFT
                    isAntiAlias = true
                }
                drawText(text, screenX, screenY, paint)
            }
        }
    }
}