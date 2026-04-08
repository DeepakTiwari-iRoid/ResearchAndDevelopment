package com.app.research.arsample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.concurrent.thread

/**
 * Full-screen 360-degree image viewer.
 * Move your phone around to look in any direction.
 *
 * Supports .hdr (Radiance HDR) and standard image formats (jpg, png).
 * Tap "Load 360 Image" to pick a file from your device.
 */
@Composable
fun ARSampleScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sensorOrientation = remember { SensorOrientationManager(context) }
    val renderer = remember { Sphere360Renderer(sensorOrientation) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    DisposableEffect(Unit) {
        sensorOrientation.start()
        // Load default generated sample
        renderer.pendingBitmap = generateSample360Bitmap()
        onDispose { sensorOrientation.stop() }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        loading = true
        error = null

        thread {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Cannot open file")

                val fileName = uri.lastPathSegment?.lowercase() ?: ""
                val bitmap: Bitmap = if (fileName.endsWith(".hdr") || fileName.endsWith(".pic")) {
                    HdrImageDecoder.decode(inputStream)
                } else {
                    // Try HDR first by peeking, fallback to standard decode
                    val buffered = inputStream.buffered()
                    buffered.mark(10)
                    val header = ByteArray(10)
                    buffered.read(header)
                    buffered.reset()

                    val headerStr = String(header)
                    if (headerStr.startsWith("#?")) {
                        // It's an HDR file regardless of extension
                        HdrImageDecoder.decode(buffered)
                    } else {
                        BitmapFactory.decodeStream(buffered)
                            ?: throw Exception("Cannot decode image")
                    }
                }

                renderer.pendingBitmap = bitmap
                loading = false
            } catch (e: Exception) {
                error = e.message ?: "Failed to load image"
                loading = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx -> Sphere360GLSurfaceView(ctx, renderer) },
            modifier = Modifier.fillMaxSize()
        )

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        error?.let { msg ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Error: $msg",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .background(Color(0xAA000000), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                )
            }
        }

        // Load button at top
        Button(
            onClick = { filePicker.launch("*/*") },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xCC000000)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Load 360 Image (.hdr / .jpg / .png)", fontSize = 13.sp)
        }

        // Hint label at bottom
        Text(
            text = "Move your device to look around 360\u00B0",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .background(Color(0x88000000), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Generates a colorful equirectangular bitmap with direction labels
 * as a default placeholder until a real 360 image is loaded.
 */
private fun generateSample360Bitmap(): Bitmap {
    val w = 2048
    val h = 1024
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)

    val gridPaint = Paint().apply { style = Paint.Style.FILL }
    val linePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 80f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
    }
    val shadowPaint = Paint(textPaint).apply {
        color = android.graphics.Color.BLACK
        textSize = 80f
    }

    val colors = intArrayOf(
        android.graphics.Color.rgb(220, 50, 50),
        android.graphics.Color.rgb(220, 130, 30),
        android.graphics.Color.rgb(220, 200, 30),
        android.graphics.Color.rgb(30, 180, 60),
        android.graphics.Color.rgb(30, 160, 180),
        android.graphics.Color.rgb(40, 80, 200),
        android.graphics.Color.rgb(130, 40, 200),
        android.graphics.Color.rgb(200, 40, 140),
    )

    val labels = arrayOf("FRONT", "FR", "RIGHT", "BR", "BACK", "BL", "LEFT", "FL")

    val segW = w / 8
    val segH = h / 4

    for (col in 0 until 8) {
        for (row in 0 until 4) {
            val darken = if (row == 0 || row == 3) 0.5f else 1.0f
            val baseColor = colors[col]
            val r = (android.graphics.Color.red(baseColor) * darken).toInt()
            val g = (android.graphics.Color.green(baseColor) * darken).toInt()
            val b = (android.graphics.Color.blue(baseColor) * darken).toInt()
            gridPaint.color = android.graphics.Color.rgb(r, g, b)

            val left = col * segW.toFloat()
            val top = row * segH.toFloat()
            canvas.drawRect(left, top, left + segW, top + segH, gridPaint)
        }
    }

    for (i in 0..8) canvas.drawLine(i * segW.toFloat(), 0f, i * segW.toFloat(), h.toFloat(), linePaint)
    for (i in 0..4) canvas.drawLine(0f, i * segH.toFloat(), w.toFloat(), i * segH.toFloat(), linePaint)

    val equatorPaint = Paint(linePaint).apply { strokeWidth = 6f }
    canvas.drawLine(0f, h / 2f, w.toFloat(), h / 2f, equatorPaint)

    for (i in 0 until 8) {
        val cx = (i * segW + segW / 2).toFloat()
        val cy = h / 2f + 25f
        canvas.drawText(labels[i], cx + 3, cy + 3, shadowPaint)
        canvas.drawText(labels[i], cx, cy, textPaint)
    }

    val polePaint = Paint(textPaint).apply { textSize = 60f }
    val poleShadow = Paint(shadowPaint).apply { textSize = 60f }
    canvas.drawText("TOP (Sky)", w / 2f + 2, segH / 2f + 22f, poleShadow)
    canvas.drawText("TOP (Sky)", w / 2f, segH / 2f + 20f, polePaint)
    canvas.drawText("BOTTOM (Ground)", w / 2f + 2, h - segH / 2f + 22f, poleShadow)
    canvas.drawText("BOTTOM (Ground)", w / 2f, h - segH / 2f + 20f, polePaint)

    return bmp
}