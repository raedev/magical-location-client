package com.magical.location.service

import android.content.Context
import android.location.Location
import androidx.core.location.LocationCompat
import com.magical.location.LocationDatabaseManager
import com.magical.location.MagicalLocationManager
import com.magical.location.client.LocationListener
import com.magical.location.internal.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * 轨迹记录
 * @author RAE
 * @date 2022/10/31
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
internal class TraceRecorder(val context: Context) : LocationListener {

    private val manager = LocationDatabaseManager(context)

    override fun onLocationChanged(location: Location) {
        // 获取到当前的配置的轨迹
        val traceId = MagicalLocationManager.options.traceId ?: return
        if (LocationCompat.isMock(location) && !MagicalLocationManager.options.enableMockLocation) {
            Log.warn("this location is mock location, ignore it: $location")
            return
        }
        MainScope().launch(Dispatchers.IO) {
            val result = manager.insertTrackPoint(traceId, location)
            Log.debug(
                "轨迹记录[${result > 0}]，Id=$traceId,Point(${location.longitude},${location.latitude})"
            )
        }
    }

}