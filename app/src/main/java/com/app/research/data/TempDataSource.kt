package com.app.research.data

import com.app.research.chatpaging.Chat
import com.app.research.singlescreen_r_d.skaifitness.MyProgressBarChart.Item

object TempDataSource {


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