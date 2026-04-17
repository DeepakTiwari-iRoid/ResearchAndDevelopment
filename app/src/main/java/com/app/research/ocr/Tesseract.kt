package com.app.research.ocr

import android.content.Context
import com.app.research.R
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.InputStream


/**
 * class TesseractUsage(val context: Context) {
 *     val tesseract = Tesseract
 *         .Builder(context = context)
 *         .setLanguage("eng")
 *         .build()
 *
 *     init {
 *         val text = tesseract.recognize(File("/path/to/image.png"))
 *     }
 *
 * }*/

class Tesseract(
    private val tess: TessBaseAPI? = null
) {

    class Builder(private val context: Context) {

        private val initTess: TessBaseAPI = TessBaseAPI()
        private var lang: String = "eng"


        fun setLanguage(lang: String) = apply { this.lang = lang }

        fun build(): Tesseract {
            saveFile(lang)
            val dataPath: String = context.filesDir.absolutePath

            check(initTess.init(dataPath, lang)) {
                initTess.recycle()
                "Failed to initialize Tesseract at $dataPath for $lang"
            }
            return Tesseract(tess = initTess)
        }

        // Given path must contain subdirectory `tessdata` where are `*.traineddata` language files
        // The path must be directly readable by the app
        private fun saveFile(fileName: String) {

            try {

                val tessDir = File(context.filesDir.absolutePath, "tessdata")

                if (!tessDir.exists() && !tessDir.mkdir()) {
                    throw RuntimeException("Can't create directory $tessDir");
                }

                val langFile = File(tessDir, "$fileName.traineddata")

                if (!langFile.exists()) {
                    context.resources.openRawResource(R.raw.eng).use { input ->
                        langFile.outputStream().use { output -> input.copyTo(output) }
                    }
                }


            } catch (e: Exception) {
                e.printStackTrace()
                println(e.message)
            }
        }
    }

    fun recognize(image: File): String? {
        tess?.setImage(image)
        return tess?.utF8Text
    }

    fun addLangFile(context: Context, name: String, fileResource: Int) {
        val file = File(context.filesDir, name)
        if (!file.exists()) {
            val inputStream: InputStream = context.resources.openRawResource(fileResource)
            file.appendBytes(inputStream.readBytes())
            file.createNewFile()
        }
    }

    // Release the native Tesseract instance when you don't want to use it anymore
    // After this call, no method can be called on this TessBaseAPI instance
    fun release() = tess?.recycle()
}