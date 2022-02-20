package com.example.musick

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log

class NotificationChannelClass: Application() {

    companion object{
        val CHANNEL_ID = "Notification channel id"
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, "Player Notification",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.setSound(null, null)
            val notificationManager = getSystemService(NotificationManager::class.java)
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }
}