package com.breather

import android.os.Handler
import android.os.Looper
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.app.usage.UsageStatsManager
import android.app.usage.UsageEvents
import android.content.Context
import java.util.*

class AppUsageService : Service() {

    private val BLOCKED_APPS = listOf("com.google.android.youtube") // Change to the package of apps you want to override
    private var isAppOverridden = false

     override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            while (true) {
                checkForegroundApp()
                Thread.sleep(1000) // Check every second
            }
        }.start()

         Log.d("AppUsageService", "Background service is running")

        return START_STICKY
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "AppUsageServiceChannel",
                "App Usage Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            val notification = Notification.Builder(this, "AppUsageServiceChannel")
                .setContentTitle("Breather is Running")
                .setContentText("Monitoring app usage...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()

            startForeground(1, notification)
        }
    }


    private fun checkForegroundApp() {
    val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val startTime = endTime - 5000 // Last 5 seconds

    val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
    var lastApp: String? = null

    val event = UsageEvents.Event()
    while (usageEvents.hasNextEvent()) {
        usageEvents.getNextEvent(event)
        if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
            lastApp = event.packageName
        }
    }

    if (lastApp == null) {
        Log.d("AppUsageService", "No recent foreground app found")
        return
    }

    Log.d("AppUsageService", "Detected foreground app: $lastApp")

    if (BLOCKED_APPS.contains(lastApp)) {
        Log.d("AppUsageService", "Blocked app detected: $lastApp")
        launchBreatherApp()
    }
}

    private fun launchBreatherApp() {
    Log.d("AppUsageService", "Launching Breather app")
    if (isAppOverridden) return
    isAppOverridden = true

    val intent = Intent(Intent.ACTION_MAIN)
intent.addCategory(Intent.CATEGORY_LAUNCHER)
intent.component = ComponentName("com.breather", "com.breather.MainActivity")
intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
startActivity(intent)

    Thread.sleep(3000) // Wait a bit before allowing another override
    isAppOverridden = false
}
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
