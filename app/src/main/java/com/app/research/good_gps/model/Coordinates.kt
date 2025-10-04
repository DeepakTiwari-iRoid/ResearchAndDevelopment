package com.app.research.good_gps.model

import com.google.gson.annotations.SerializedName

data class Coordinates(
    val courseID: String = "",
    val numCoordinates: Int = 0,
    val coordinates: List<Coordinate> = emptyList()
) {
    data class Coordinate(
        @SerializedName("poi")
        val poiID: Int = 0,
        @SerializedName("location")
        val locationID: Int = 0,
        @SerializedName("sideFW")
        val sideID: Int = 0,
        val hole: Int = 0,
        val latitude: Double = 0.0,
        val longitude: Double = 0.0
    ) {
        val poi: POI_NAME get() = POI_NAME.fromId(poiID)
        val sideFW: SIDE_OF_FAIRWAY get() = SIDE_OF_FAIRWAY.fromId(sideID)
        val location: LOCATION get() = LOCATION.fromId(locationID)
        val latLng get() = com.google.android.gms.maps.model.LatLng(latitude, longitude)
    }
}


enum class POI_NAME(val id: Int, val title: String) {
    GREEN(1, "Green"),
    GREEN_BUNKER(2, "Green Bunker"),
    FAIRWAY_BUNKER(3, "Fairway Bunker"),
    WATER(4, "Water"),
    TREES(5, "Trees"),
    MARKER_100(6, "100 Marker"),
    MARKER_150(7, "150 Marker"),
    MARKER_200(8, "200 Marker"),
    DOGLEG(9, "Dogleg"),
    ROAD(10, "Road"),
    FRONT_TEE(11, "Front Tee"),
    BACK_TEE(12, "Back Tee"), ;

    companion object {
        fun fromId(id: Int): POI_NAME {
            return entries.find { it.id == id } ?: ROAD
        }
    }
}

enum class SIDE_OF_FAIRWAY(val id: Int, val title: String) {
    LEFT(1, "Left"),
    RIGHT(2, "Right"),
    CENTER(3, "Center");

    companion object {
        fun fromId(id: Int): SIDE_OF_FAIRWAY {
            return entries.find { it.id == id } ?: LEFT
        }
    }
}

enum class LOCATION(val id: Int, val title: String) {
    FRONT(1, "Front"),
    MIDDLE(2, "Middle"),
    BACK(3, "Back");

    companion object {
        fun fromId(id: Int): LOCATION {
            return entries.find { it.id == id } ?: MIDDLE
        }
    }
}