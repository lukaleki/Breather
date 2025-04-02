package com.breather

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import com.facebook.react.bridge.*

class UsageStatsModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return "UsageStatsModule"
    }

    @ReactMethod
    fun openUsageAccessSettings() {
        val context = reactApplicationContext
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }


    @Suppress("DEPRECATION")
    @ReactMethod
    fun checkUsagePermission(promise: Promise) {
        val context = reactApplicationContext
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        promise.resolve(mode == AppOpsManager.MODE_ALLOWED)
    }


    @ReactMethod
    fun getUsageStats(promise: Promise) {
        val context = reactApplicationContext
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 60 * 60 * 24 // Last 24 hours

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        if (usageStatsList.isNullOrEmpty()) {
            promise.reject("NO_DATA", "No usage data available")
            return
        }

        val statsArray = WritableNativeArray()
        for (usageStats in usageStatsList) {
            val appData = WritableNativeMap()
            appData.putString("packageName", usageStats.packageName)
            appData.putDouble("totalTimeInForeground", usageStats.totalTimeInForeground.toDouble())
            statsArray.pushMap(appData)
        }

        promise.resolve(statsArray)
    }

    @ReactMethod
    fun getForegroundApp(promise: Promise) {
        val context = reactApplicationContext
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 10 // Check last 10 seconds

        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        if (stats.isNotEmpty()) {
            val recentApp = stats.maxByOrNull { it.lastTimeUsed }
            if (recentApp != null) {
                promise.resolve(recentApp.packageName)
                return
            }
        }
        promise.reject("No app detected")
    }

     @ReactMethod
     fun startBackgroundService() {
        val serviceIntent = Intent(reactApplicationContext, AppUsageService::class.java)
        
        // For API level 26 (Oreo) and above, start as a foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            reactApplicationContext.startForegroundService(serviceIntent)
        } else {
            reactApplicationContext.startService(serviceIntent)
        }
    }
}

