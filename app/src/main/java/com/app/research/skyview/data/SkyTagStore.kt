package com.app.research.skyview.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class SkyTagStore(context: Context) {

    private val gson = Gson()
    private val file = File(context.filesDir, "sky_tags.json")

    fun loadAll(): List<SkyTag> {
        if (!file.exists()) return emptyList()
        return try {
            val json = file.readText()
            val type = object : TypeToken<List<SkyTag>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(tag: SkyTag) {
        val tags = loadAll().toMutableList()
        tags.add(tag)
        writeAll(tags)
    }

    fun delete(tagId: String) {
        val tags = loadAll().filter { it.id != tagId }
        writeAll(tags)
    }

    private fun writeAll(tags: List<SkyTag>) {
        file.writeText(gson.toJson(tags))
    }
}
