package com.app.research.areatag.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import java.io.File

class AreaTagStore(context: Context) {

    private val gson = Gson()
    private val file = File(context.filesDir, "area_tags.json")

    fun loadAll(): List<Zone> {
        if (!file.exists()) return emptyList()
        return try {
            val json = file.readText()
            val type = object : TypeToken<List<Zone>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Failed to load area tags")
            emptyList()
        }
    }

    fun save(zone: Zone) {
        val tags = loadAll().toMutableList()
        val existingZone = tags.find { it.zoneId == zone.zoneId }
        if (existingZone != null) {
            val addTagToZone =
                existingZone.copy(tags = (existingZone.tags + zone.tags))
            tags.add(addTagToZone)
        } else {
            tags.add(zone)
        }
        writeAll(tags)
    }

    fun delete(uuid: String) {
        val zones = loadAll()
        val updatedZone = zones.map { zone ->
            zone.copy(tags = zone.tags.filterNot { it.uuid == uuid })
        }
        writeAll(updatedZone)
    }

    private fun writeAll(tags: List<Zone>) {
        file.writeText(gson.toJson(tags))
    }
}
