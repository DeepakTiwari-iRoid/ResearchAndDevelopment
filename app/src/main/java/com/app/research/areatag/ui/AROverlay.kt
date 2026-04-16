package com.app.research.areatag.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.app.research.areatag.ui.AreaTagViewModel.Companion.VISIBILITY_PITCH_THRESHOLD
import com.app.research.areatag.ui.AreaTagViewModel.Companion.VISIBILITY_YAW_THRESHOLD
import com.app.research.ui.theme.vividCyan
import com.app.research.ui.theme.vividCyanA27
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun AROverlay(
    tagPositions: List<TagScreenPosition>,
    modifier: Modifier = Modifier,
    zoneArrow: ZoneArrowState? = null,
    onTagClick: ((TagScreenPosition) -> Unit)? = null
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current

        val widthPx = constraints.maxWidth
        val heightPx = constraints.maxHeight

        // Canvas for crosshair, directional arrows, zone nav
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCrosshair()
            tagPositions.forEach { tagPos ->
                if (tagPos.isVisible) {
                    drawTagMarker(tagPos)
                } else {
                    drawDirectionalArrow(tagPos)
                }
            }
            zoneArrow?.let { drawZoneNavArrow(it) }
        }

        // Clickable hit targets over visible tags
        if (onTagClick != null) {
            val hitSize = 56.dp
            val hitSizePx = with(density) { hitSize.toPx() }
            val cx = widthPx / 2f
            val cy = heightPx / 2f
            val pxPerDegX = widthPx / (VISIBILITY_YAW_THRESHOLD * 2f)
            val pxPerDegY = heightPx / (VISIBILITY_PITCH_THRESHOLD * 2f)

            tagPositions.filter { it.isVisible }.forEach { tagPos ->
                val screenX = cx + tagPos.deltaYaw * pxPerDegX
                val screenY = cy - tagPos.deltaPitch * pxPerDegY

                Box(
                    modifier = Modifier
                        .size(hitSize)
                        .offset {
                            IntOffset(
                                (screenX - hitSizePx / 2f).roundToInt(),
                                (screenY - hitSizePx / 2f).roundToInt()
                            )
                        }
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onTagClick(tagPos) }
                )
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
        color = vividCyanA27,
        radius = markerRadius + 4.dp.toPx(),
        center = Offset(screenX, screenY)
    )

    // Main marker circle
    drawCircle(
        color = vividCyan,
        radius = markerRadius,
        center = Offset(screenX, screenY),
        style = Stroke(width = 2.5.dp.toPx())
    )

    // Inner dot
    drawCircle(
        color = vividCyan,
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
//    val distLabel = "${tagPos.distanceMeters.roundToInt()}m"
//    val fullLabel = "$label  $distLabel" TODO: Optimize this
    val fullLabel = label

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

/**
 * Compass-style zone pointer. Shown at bottom-center; rotates around its pivot so the arrow tip
 * points toward the selected zone's center (bearing from current device heading).
 * deltaYaw = 0 → arrow points straight up (zone is ahead).
 */
private fun DrawScope.drawZoneNavArrow(zoneArrow: ZoneArrowState) {
    val cx = size.width / 2f
    val pivotY = size.height - 140.dp.toPx()
    val pivot = Offset(cx, pivotY)

    val ringRadius = 48.dp.toPx()
    val arrowLen = 40.dp.toPx()
    val arrowWidth = 22.dp.toPx()

    val zoneColor = Color(zoneArrow.colorArgb)

    // Backing ring
    drawCircle(
        color = Color(0x66000000),
        radius = ringRadius,
        center = pivot
    )
    drawCircle(
        color = zoneColor.copy(alpha = 0.9f),
        radius = ringRadius,
        center = pivot,
        style = Stroke(width = 2.dp.toPx())
    )

    rotate(degrees = zoneArrow.deltaYaw, pivot = pivot) {
        val tipY = pivotY - arrowLen
        val baseY = pivotY + arrowLen * 0.25f
        val path = Path().apply {
            moveTo(cx, tipY)
            lineTo(cx - arrowWidth / 2f, baseY)
            lineTo(cx, baseY - arrowWidth * 0.25f)
            lineTo(cx + arrowWidth / 2f, baseY)
            close()
        }
        drawPath(path, color = zoneColor)
        drawPath(
            path,
            color = Color.White.copy(alpha = 0.85f),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }

    // Distance + title label under the compass
    val paint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.WHITE
        textSize = 11.dp.toPx()
        isAntiAlias = true
        setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
    }
    val distanceText = formatDistance(zoneArrow.distanceMeters)
    val label = "${zoneArrow.title} • $distanceText"
    val labelWidth = paint.measureText(label)
    drawContext.canvas.nativeCanvas.drawText(
        label,
        cx - labelWidth / 2f,
        pivotY + ringRadius + 18.dp.toPx(),
        paint
    )
}

private fun formatDistance(meters: Double): String = when {
    meters < 1000.0 -> "${meters.roundToInt()} m"
    else -> "${"%.1f".format(meters / 1000.0)} km"
}