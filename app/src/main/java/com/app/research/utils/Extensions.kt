package com.tradesnap.mobile.app.utils.ext

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
typealias SheetStateCall = (CoroutineScope, SheetState) -> Unit

fun String.extractNumberString(): String? {
    return "\\d+".toRegex().find(this)?.value
}


fun String.removeNumberString(): String? {
    return "\\D+".toRegex().find(this)?.value
}

inline fun <reified T> T.toJsonString(): String? {
    return Gson().toJson(this)
}

inline fun <reified T> String.fromJsonString(): T? {
    return try {
        val type = object : TypeToken<T>() {}.type
        Gson().fromJson(this, type)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun ResponseBody?.extractError(): String {
    return try {
        if (this != null) {
            val responseText = this.string()
            // Try to parse as JSON first
            try {
                val jsonObject = JSONObject(responseText)
                jsonObject.getString("message")
            } catch (jsonException: Exception) {
                // If not JSON, try to extract error from HTML
                extractErrorFromHtml(responseText) ?: responseText
            }
        } else {
            "something went wrong!"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "something went wrong!"
    }
}

private fun extractErrorFromHtml(html: String): String? {
    return try {
        // Extract text from <pre> tags (common in Express error pages)
        val preTagRegex = "<pre>(.*?)</pre>".toRegex(RegexOption.DOT_MATCHES_ALL)
        preTagRegex.find(html)?.groupValues?.get(1)?.trim()
            ?: // Try to extract from <title>Error</title> and body content
            if (html.contains("<title>Error</title>")) {
                val bodyRegex = "<body>.*?<pre>(.*?)</pre>.*?</body>".toRegex(RegexOption.DOT_MATCHES_ALL)
                bodyRegex.find(html)?.groupValues?.get(1)?.trim()
            } else {
                null
            }
    } catch (e: Exception) {
        null
    }
}


fun Context.requireActivity(): ComponentActivity = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.requireActivity()
    else -> error("No Activity Found")
}


fun String.makeUpperCase(b: Boolean): String {
    return if (b) this.uppercase(Locale.getDefault()) else this
}

inline fun Boolean.ifTrue(block: (Boolean) -> Unit) {
    if (this) {
        block(true)
    }
}

fun String.asTextRequestBody() = this.toRequestBody("text/plain".toMediaTypeOrNull())

fun File.asFileRequestBody(): RequestBody? {
    try {
        Timber.d(this.name)
        if (this.exists().not()) throw FileNotFoundException()
        val filePostfix = this.name.takeLastWhile { it != '.' }
        val filePrefix = when (filePostfix.lowercase()) {
            "pdf" -> "application"
            else -> "image"
        }
        return this.asRequestBody("$filePrefix/$filePostfix".toMediaTypeOrNull())
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null

}
