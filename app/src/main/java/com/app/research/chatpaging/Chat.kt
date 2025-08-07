package com.app.research.chatpaging

import com.google.gson.annotations.SerializedName
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

data class Chat(
    @SerializedName("message")
    val message: String = "",
    @SerializedName("type")
    val type: Int = 0,
    @SerializedName("userId")
    val userId: Int = 0,
    @SerializedName("gameId")
    val gameId: Int = 0,
    @SerializedName("roomId")
    val roomId: String = "",
    @SerializedName("chatId")
    val chatId: Int = 0,
    @SerializedName("created_at")
    val createdAt: Long = 0,
    @SerializedName("username")
    val username: String = "",
    @SerializedName("is_message")
    val isFromMe: Boolean = false,
    @SerializedName("match_id")
    val matchId: Int = 0
) {

    private val timeStamp get() = createdAt

    val localTimeStamp: String
        get() = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(
            Date(timeStamp)
        )

    val time: String
        get() = SimpleDateFormat(
            "hh:mm a",
            Locale.getDefault()
        ).format(Date(timeStamp))

    val author get() = if (isFromMe) "You" else username

}