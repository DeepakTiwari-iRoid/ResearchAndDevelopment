package com.app.research.data

import android.location.Location
import com.app.research.areatag.data.Zone
import com.app.research.areatag.sensor.Orientation
import com.app.research.areatag.ui.AreaTagUiState
import com.app.research.areatag.ui.CreateTagDialogState
import com.app.research.areatag.ui.TagScreenPosition
import com.app.research.areatag.ui.ZoneArrowState
import com.app.research.chatpaging.Chat
import com.app.research.singlescreen_r_d.skaifitness.MyProgressBarChart.Item

object TempDataSource {

    val sampleAreaTagUiState: AreaTagUiState = run {
        val tags = listOf(
            Zone.Tag(
                id = 1,
                latitude = 23.0225,
                longitude = 72.5714,
                yaw = 45f,
                pitch = 10f,
                title = "Rooftop Antenna",
                description = "Main broadcast mast"
            ),
            Zone.Tag(
                id = 2,
                latitude = 23.0230,
                longitude = 72.5720,
                yaw = 120f,
                pitch = -5f,
                title = "Water Tank"
            ),
            Zone.Tag(
                id = 3,
                latitude = 23.0220,
                longitude = 72.5710,
                yaw = 280f,
                pitch = 25f,
                title = "Skylight"
            )
        )

        val sampleZones = listOf(
            Zone(
                zoneId = "8928308280fffff",
                title = "Rooftop",
                description = "Rooftop zone",
                tags = tags
            ),
            Zone(
                zoneId = "8928308281fffff",
                title = "Courtyard",
                description = "Courtyard zone",
                tags = tags.take(1)
            ),
            Zone(
                zoneId = "8928308283fffff",
                title = "Parking",
                description = "Parking zone",
                tags = emptyList()
            )
        )

        AreaTagUiState(
            orientation = Orientation(yaw = 50f, pitch = 8f, roll = 0f),
            location = Location("preview").apply {
                latitude = 23.0225
                longitude = 72.5714
                accuracy = 4.2f
            },
            tagPositions = listOf(
                TagScreenPosition(
                    tag = tags[0],
                    deltaYaw = -5f,
                    deltaPitch = 2f,
                    distanceMeters = 12.0,
                    isVisible = true
                ),
                TagScreenPosition(
                    tag = tags[1],
                    deltaYaw = 70f,
                    deltaPitch = -13f,
                    distanceMeters = 28.5,
                    isVisible = false
                ),
                TagScreenPosition(
                    tag = tags[2],
                    deltaYaw = -130f,
                    deltaPitch = 17f,
                    distanceMeters = 18.2,
                    isVisible = false
                )
            ),
            dialog = CreateTagDialogState.Hidden,
            zones = sampleZones,
            selectedZoneId = sampleZones.first().zoneId,
            zoneArrow = ZoneArrowState(
                zoneId = sampleZones.first().zoneId,
                colorArgb = sampleZones.first().color,
                title = sampleZones.first().title,
                deltaYaw = -35f,
                distanceMeters = 182.0
            )
        )
    }


    val tempProgressData = listOf(
        Item(
            score = 0,
            month = "Jan",
            date = "01",
        ),
        Item(
            score = 25,
            month = "Jan",
            date = "02"
        ),
        Item(
            score = 37,
            month = "Jan",
            date = "03"
        ),
        Item(
            score = 50,
            month = "Jan",
            date = "04"
        ),
        Item(
            score = 62,
            month = "Jan",
            date = "05"
        ),
        Item(
            score = 75,
            month = "Jan",
            date = "06"
        ),
        Item(
            score = 87,
            month = "Jan",
            date = "07"
        ),
        Item(
            score = 100,
            month = "Jan",
            date = "08"
        )
    )

    val weekList = listOf(
        listOf(1, 2, 3, 4, 5),
        listOf(1),
        emptyList(),
    )

    val chatList = List(50) { index ->
        val decreasingIndex = 50 - index //descending order
        Chat(
            chatId = decreasingIndex,
            message = "Message ${index + 1}",
            isFromMe = decreasingIndex % 3 == 0,
            createdAt = System.currentTimeMillis() - (decreasingIndex * 3600000L), //Newer MSG First
        )
    }

}