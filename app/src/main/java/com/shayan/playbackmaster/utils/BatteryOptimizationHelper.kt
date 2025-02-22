package com.shayan.playbackmaster.utils

import android.content.Context
import android.os.Build
import android.os.PowerManager


object BatteryOptimizationHelper {
    fun isBatteryOptimized(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return powerManager.isIgnoringBatteryOptimizations(context.packageName)
        }
        return false // For versions below Marshmallow, optimization is not applicable
    }
}