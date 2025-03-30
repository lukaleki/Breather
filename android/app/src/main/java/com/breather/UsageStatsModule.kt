package com.breather

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise

class UsageStatsModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    override fun getName(): String {
        return "UsageStats"
    }


    @Suppress("DEPRECATION")
    @ReactMethod
    fun checkUsageAccessPermission(promise: Promise) {
        val appOps = reactApplicationContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            reactApplicationContext.packageName
        )
        val isGranted = mode == AppOpsManager.MODE_ALLOWED
        promise.resolve(isGranted)
    }
}
