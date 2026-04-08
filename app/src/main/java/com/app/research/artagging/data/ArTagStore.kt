package com.app.research.artagging.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class ArTagStore(context: Context) {

    private val gson = Gson()
    private val file = File(context.filesDir, "ar_tags.json")

    fun loadAll(): List<ArTag> {
        if (!file.exists()) return emptyList()
        return try {
            val json = file.readText()
            val type = object : TypeToken<List<ArTag>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(tag: ArTag) {
        val tags = loadAll().toMutableList()
        tags.add(tag)
        writeAll(tags)
    }

    fun update(tag: ArTag) {
        val tags = loadAll().toMutableList()
        val index = tags.indexOfFirst { it.id == tag.id }
        if (index != -1) {
            tags[index] = tag
        } else {
            tags.add(tag)
        }
        writeAll(tags)
    }

    fun delete(tagId: String) {
        val tags = loadAll().filter { it.id != tagId }
        writeAll(tags)
    }

    private fun writeAll(tags: List<ArTag>) {
        file.writeText(gson.toJson(tags))
    }
}
