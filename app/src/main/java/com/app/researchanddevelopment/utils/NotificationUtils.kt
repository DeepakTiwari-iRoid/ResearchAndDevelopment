package com.app.researchanddevelopment.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.app.researchanddevelopment.R

class NotificationUtils {
    private var notificationBuilder: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManager? = null

    fun sendNotification(
        context: Context,
        title: String,
        message: String,
        pendingIntent: PendingIntent?,
        id: Int
    ) {
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = context.packageName + "App"
        val bitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_round)

        notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setTicker(title)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setLargeIcon(bitmap)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)

        // Set pending intent if available
        if (pendingIntent != null) {
            notificationBuilder?.setContentIntent(pendingIntent)
        }

        notificationBuilder?.color = Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelComment(channelId, context)
        }

        notificationManager?.notify(id, notificationBuilder?.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun channelComment(channelId: String, context: Context) {
        notificationBuilder?.setChannelId(channelId)
        val description = context.getString(R.string.app_name)
        val importance = NotificationManager.IMPORTANCE_HIGH

        val mChannel = NotificationChannel(channelId, "Match Notifications", importance)
        mChannel.description = description
        mChannel.enableLights(true)
        mChannel.lightColor = Color.parseColor("#3ECAFC")
        mChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(mChannel)
    }
}