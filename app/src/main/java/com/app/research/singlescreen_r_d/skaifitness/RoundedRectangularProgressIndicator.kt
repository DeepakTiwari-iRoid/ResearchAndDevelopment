package com.app.research.singlescreen_r_d.skaifitness

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview(showBackground = true)
@Composable
fun RectangularProgressIndicatorPreview() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        RectangularProgressIndicator(
            modifier = Modifier
                .size(200.dp)
                .padding(4.dp)
        )

    }
}

@Composable
private fun RectangularProgressIndicator(modifier: Modifier = Modifier.size(100.dp)) {
    var progress by remember { mutableFloatStateOf(0f) } // Progress as a fraction (0.0 to 1.0)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        val fullPath by remember { mutableStateOf(Path()) }  // Full rounded rectangle path

        Canvas(modifier) {
            val width = size.width
            val height = size.height

            val cornerRadius = 50f

            val progressPath = Path() // Partial progress path

            fullPath.apply {
                arcTo(
                    rect = Rect(
                        offset = Offset(0f, height - cornerRadius),
                        size = Size(cornerRadius, cornerRadius)
                    ),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                lineTo(x = 0f, y = 0f + cornerRadius)
                arcTo(
                    rect = Rect(
                        offset = Offset(0f, 0f),
                        size = Size(cornerRadius, cornerRadius)
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                lineTo(width - cornerRadius, 0f)
                arcTo(
                    rect = Rect(
                        offset = Offset(width - cornerRadius, 0f),
                        size = Size(cornerRadius, cornerRadius)
                    ),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                lineTo(x = width, y = height - cornerRadius)
                arcTo(
                    rect = Rect(
                        offset = Offset(width - cornerRadius, height - cornerRadius),
                        size = Size(cornerRadius, cornerRadius)
                    ),
                    startAngleDegrees = 360f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                close()
            }

            drawPath(
                path = fullPath,
                color = Color.Red,
                style = Stroke(width = 2.dp.toPx())
            )

            // Use PathMeasure to extract the progress segment
            val pathMeasure = PathMeasure()
            pathMeasure.setPath(fullPath, forceClosed = true)

            val segmentLength = pathMeasure.length * progress

            pathMeasure.getSegment(
                startDistance = 0f,
                stopDistance = segmentLength,
                destination = progressPath
            )

            // Draw the progress path as part of the rounded rectangle
            drawPath(
                color = Color.Black,
                path = progressPath,
                style = Stroke(width = 6.dp.toPx())
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Progress: ${(progress * 100).toInt()}%")
            Slider(
                value = progress,
                onValueChange = {
                    progress = it
                },
                valueRange = 0f..1f,
            )
        }
    }
}