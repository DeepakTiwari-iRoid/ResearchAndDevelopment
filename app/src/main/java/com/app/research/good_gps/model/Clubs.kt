package com.app.research.good_gps.model

import com.google.gson.annotations.SerializedName

data class Clubs(
    @SerializedName("apiRequestsLeft")
    val apiRequestsLeft: String = "",
    @SerializedName("numClubs")
    val numClubs: Int = 0,
    @SerializedName("numAllClubs")
    val numAllClubs: Int = 0,
    @SerializedName("clubs")
    val clubs: List<Club> = listOf()
)

data class Club(
    @SerializedName("clubID")
    val clubID: String = "",
    @SerializedName("clubName")
    val clubName: String = "",
    @SerializedName("city")
    val city: String = "",
    @SerializedName("state")
    val state: String = "",
    @SerializedName("country")
    val country: String = "",
    @SerializedName("address")
    val address: String = "",
    @SerializedName("timestampUpdated")
    val timestampUpdated: String = "",
    @SerializedName("distance")
    val distance: Double = 0.0,
    @SerializedName("measureUnit")
    val measureUnit: String = "",
    @SerializedName("courses")
    val courses: List<Course> = listOf()
)

data class Course(
    @SerializedName("courseID")
    val courseID: String = "",
    @SerializedName("courseName")
    val courseName: String = "",
    @SerializedName("numHoles")
    val numHoles: Int = 0,
    @SerializedName("timestampUpdated")
    val timestampUpdated: String = "",
    @SerializedName("hasGPS")
    val hasGPS: Int = 0
)


class ApiResponse<T>(
    @SerializedName("status")
    val status: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("data")
    val data: T
)