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
            Timber.d("Zone Json: $json")
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Failed to load area tags")
            emptyList()
        }
    }

    fun save(zone: Zone) {
        val zones = loadAll().toMutableList()
        val index = zones.indexOfFirst { it.zoneId == zone.zoneId }
        if (index >= 0) {
            val existing = zones[index]
            zones[index] = existing.copy(tags = existing.tags + zone.tags)
        } else {
            zones.add(zone)
        }
        writeAll(zones)
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
