package com.magical.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location

/**
 * 位置广播接收器
 * @author RAE
 * @date 2022/10/29
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
abstract class LocationBroadcastReceiver : BroadcastReceiver() {

    /**
     * 位置改变回调
     */
    protected abstract fun onLocationChanged(location: Location)

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != MagicalLocationManager.ACTION_LOCATION_CHANGED) return
        intent.getParcelableExtra<Location?>(MagicalLocationManager.EXTRA_LOCATION)?.let {
            onLocationChanged(it)
        }
    }
}