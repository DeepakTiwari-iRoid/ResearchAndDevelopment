package com.app.research.arsample

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import java.io.BufferedInputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Decodes Radiance HDR (.hdr / .pic) files into an Android Bitmap.
 * Supports both uncompressed and new-style RLE RGBE encoding.
 */
object HdrImageDecoder {

    fun decode(inputStream: InputStream): Bitmap {
        val stream = BufferedInputStream(inputStream, 8192)
        var width = 0
        var height = 0

        // ── Parse header ─────────────────────────────────────────────
        while (true) {
            val line = readLine(stream)
            if (line.isEmpty()) continue // skip blank lines after header fields

            // Resolution line, e.g. "-Y 1024 +X 2048"
            val resMatch = Regex("""[+-]Y\s+(\d+)\s+[+-]X\s+(\d+)""").find(line)
            if (resMatch != null) {
                height = resMatch.groupValues[1].toInt()
                width = resMatch.groupValues[2].toInt()
                break
            }
            // Skip other header lines (#?, FORMAT=, etc.)
        }

        require(width > 0 && height > 0) { "Invalid HDR resolution: ${width}x${height}" }

        // ── Read pixel data ──────────────────────────────────────────
        val pixels = IntArray(width * height)
        val scanline = ByteArray(width * 4)

        for (y in 0..height) {
            readScanline(stream, scanline, width)

            for (x in 0..width) {
                val r = scanline[x].toInt() and 0xFF
                val g = scanline[x + width].toInt() and 0xFF
                val b = scanline[x + width * 2].toInt() and 0xFF
                val e = scanline[x + width * 3].toInt() and 0xFF

                val (sr, sg, sb) = rgbeToRgb(r, g, b, e)
                pixels[y * width + x] = (0xFF shl 24) or (sr shl 16) or (sg shl 8) or sb
            }
        }

        val bitmap = createBitmap(width, height)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    /** Convert RGBE to tone-mapped sRGB (simple Reinhard). */
    private fun rgbeToRgb(r: Int, g: Int, b: Int, e: Int): Triple<Int, Int, Int> {
        if (e == 0) return Triple(0, 0, 0)

        val scale = 2.0.pow((e - 128).toDouble()).toFloat() / 256f
        val rf = r * scale
        val gf = g * scale
        val bf = b * scale

        // Simple Reinhard tone mapping
        fun tonemap(v: Float): Int {
            val mapped = v / (1f + v)
            // Gamma correction (sRGB approx)
            val gamma = mapped.toDouble().pow(1.0 / 2.2).toFloat()
            return min(255, max(0, (gamma * 255f).toInt()))
        }

        return Triple(tonemap(rf), tonemap(gf), tonemap(bf))
    }

    private fun readScanline(stream: InputStream, scanline: ByteArray, width: Int) {
        // Check for new-style RLE
        val b0 = stream.read()
        val b1 = stream.read()
        val b2 = stream.read()
        val b3 = stream.read()

        if (b0 == 2 && b1 == 2 && b2 >= 0 && b3 >= 0) {
            val lineWidth = (b2 shl 8) or b3
            require(lineWidth == width) { "Scanline width mismatch: $lineWidth != $width" }
            // New-style RLE: each channel is RLE-encoded separately
            for (ch in 0 until 4) {
                var x = 0
                val offset = ch * width
                while (x < width) {
                    val code = stream.read()
                    if (code > 128) {
                        // Run
                        val count = code - 128
                        val value = stream.read().toByte()
                        for (i in 0 until count) {
                            scanline[offset + x++] = value
                        }
                    } else {
                        // Literal
                        val count = code
                        for (i in 0 until count) {
                            scanline[offset + x++] = stream.read().toByte()
                        }
                    }
                }
            }
        } else {
            // Uncompressed or old-style: first 4 bytes are pixel 0
            scanline[0] = b0.toByte()
            scanline[width] = b1.toByte()
            scanline[width * 2] = b2.toByte()
            scanline[width * 3] = b3.toByte()
            for (x in 1 until width) {
                scanline[x] = stream.read().toByte()
                scanline[x + width] = stream.read().toByte()
                scanline[x + width * 2] = stream.read().toByte()
                scanline[x + width * 3] = stream.read().toByte()
            }
        }
    }

    private fun readLine(stream: InputStream): String {
        val sb = StringBuilder()
        while (true) {
            val c = stream.read()
            if (c == -1 || c == '\n'.code) break
            if (c != '\r'.code) sb.append(c.toChar())
        }
        return sb.toString()
    }
}