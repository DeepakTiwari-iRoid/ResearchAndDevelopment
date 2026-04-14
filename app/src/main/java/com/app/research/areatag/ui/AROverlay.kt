package com.app.research.areatag.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.app.research.areatag.ui.AreaTagViewModel.Companion.VISIBILITY_PITCH_THRESHOLD
import com.app.research.areatag.ui.AreaTagViewModel.Companion.VISIBILITY_YAW_THRESHOLD
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun AROverlay(
    tagPositions: List<TagScreenPosition>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawCrosshair()
        tagPositions.forEach { tagPos ->
            if (tagPos.isVisible) {
                drawTagMarker(tagPos)
            } else {
                drawDirectionalArrow(tagPos)
            }
        }
    }
}

private fun DrawScope.drawCrosshair() {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val armLen = 20.dp.toPx()
    val gap = 6.dp.toPx()
    val strokeWidth = 2.dp.toPx()
    val color = Color.White

    // Horizontal arms
    drawLine(color, Offset(cx - armLen, cy), Offset(cx - gap, cy), strokeWidth)
    drawLine(color, Offset(cx + gap, cy), Offset(cx + armLen, cy), strokeWidth)
    // Vertical arms
    drawLine(color, Offset(cx, cy - armLen), Offset(cx, cy - gap), strokeWidth)
    drawLine(color, Offset(cx, cy + gap), Offset(cx, cy + armLen), strokeWidth)

    // Small center dot
    drawCircle(Color.White.copy(alpha = 0.6f), radius = 2.dp.toPx(), center = Offset(cx, cy))
}

private fun DrawScope.drawTagMarker(tagPos: TagScreenPosition) {
    val cx = size.width / 2f
    val cy = size.height / 2f

    // Map deltaYaw/deltaPitch to screen offset
    val pixelsPerDegreeX = size.width / (VISIBILITY_YAW_THRESHOLD * 2f)
    val pixelsPerDegreeY = size.height / (VISIBILITY_PITCH_THRESHOLD * 2f)

    val screenX = cx + tagPos.deltaYaw * pixelsPerDegreeX
    // Invert pitch: positive pitch = look up = marker moves up on screen
    val screenY = cy - tagPos.deltaPitch * pixelsPerDegreeY

    val markerRadius = 14.dp.toPx()

    // Outer glow
    drawCircle(
        color = Color(0x4400E5FF),
        radius = markerRadius + 4.dp.toPx(),
        center = Offset(screenX, screenY)
    )

    // Main marker circle
    drawCircle(
        color = Color(0xFF00E5FF),
        radius = markerRadius,
        center = Offset(screenX, screenY),
        style = Stroke(width = 2.5.dp.toPx())
    )

    // Inner dot
    drawCircle(
        color = Color(0xFF00E5FF),
        radius = 4.dp.toPx(),
        center = Offset(screenX, screenY)
    )

    // Label
    val paint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.WHITE
        textSize = 12.dp.toPx()
        isAntiAlias = true
        setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
    }

    val label = tagPos.tag.title
    val distLabel = "${tagPos.distanceMeters.roundToInt()}m"
    val fullLabel = "$label  $distLabel"

    val textWidth = paint.measureText(fullLabel)
    drawContext.canvas.nativeCanvas.drawText(
        fullLabel,
        screenX - textWidth / 2f,
        screenY - markerRadius - 8.dp.toPx(),
        paint
    )
}

private fun DrawScope.drawDirectionalArrow(tagPos: TagScreenPosition) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val margin = 60.dp.toPx()

    // Calculate angle from center toward the tag direction
    val angleDeg = Math.toDegrees(
        atan2(
            (-tagPos.deltaPitch).toDouble(),
            tagPos.deltaYaw.toDouble()
        )
    ).toFloat()

    // Position arrow at screen edge
    val edgeX = when {
        tagPos.deltaYaw > VISIBILITY_YAW_THRESHOLD -> size.width - margin
        tagPos.deltaYaw < -VISIBILITY_YAW_THRESHOLD -> margin
        else -> cx + (tagPos.deltaYaw / VISIBILITY_YAW_THRESHOLD) * (cx - margin)
    }
    val edgeY = when {
        tagPos.deltaPitch > VISIBILITY_PITCH_THRESHOLD -> margin // looking up
        tagPos.deltaPitch < -VISIBILITY_PITCH_THRESHOLD -> size.height - margin // looking down
        else -> cy - (tagPos.deltaPitch / VISIBILITY_PITCH_THRESHOLD) * (cy - margin)
    }

    val arrowSize = 12.dp.toPx()

    rotate(degrees = -angleDeg + 90f, pivot = Offset(edgeX, edgeY)) {
        val path = Path().apply {
            moveTo(edgeX, edgeY - arrowSize)
            lineTo(edgeX - arrowSize * 0.6f, edgeY + arrowSize * 0.5f)
            lineTo(edgeX + arrowSize * 0.6f, edgeY + arrowSize * 0.5f)
            close()
        }
        drawPath(path, color = Color(0xCCFFAB00))
    }

    // Small label near arrow
    val paint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.WHITE
        textSize = 10.dp.toPx()
        isAntiAlias = true
        setShadowLayer(3f, 0f, 0f, android.graphics.Color.BLACK)
    }

    val label = tagPos.tag.title
    val textW = paint.measureText(label)
    drawContext.canvas.nativeCanvas.drawText(
        label,
        edgeX - textW / 2f,
        edgeY + arrowSize + 16.dp.toPx(),
        paint
    )
}
