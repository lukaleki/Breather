package com.breather

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class AppUsageService : Service() {

    private val CHANNEL_ID = "app_usage_tracking_channel" // Define your channel ID here

    override fun onCreate() {
        super.onCreate()
        // Create the notification channel for Android 8.0 (API level 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Usage Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Log the service creation
        Log.d("AppUsageService", "Service started")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Create the notification for the foreground service
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking App Usage")
            .setContentText("Your app is tracking usage in the background.")
            // .setSmallIcon(R.drawable.ic_notification) // Make sure you have a valid icon here
            .build()

        // Start the service in the foreground
        startForeground(1, notification)

        // Your usage tracking logic here
        Log.d("AppUsageService", "Service running")

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AppUsageService", "Service destroyed")
    }
}
