package com.magical.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.magical.location.LocationPermission.isPermissionGranted
import com.magical.location.internal.Log

/**
 * 位置信息上下文
 * @author RAE
 * @date 2022/10/28
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
@Suppress("MemberVisibilityCanBePrivate")
object MagicalLocationManager {

    /** 位置广播Action，查看[LocationBroadcastReceiver] */
    internal const val ACTION_LOCATION_CHANGED: String =
        "com.magical.location.ACTION_LOCATION_CHANGED"

    /** 位置广播位置信息，查看[LocationBroadcastReceiver] */
    internal const val EXTRA_LOCATION: String = "EXTRA_LOCATION"

    /** 是否开启调试，默认False */
    var debug: Boolean = BuildConfig.DEBUG
        set(value) {
            field = value
            Log.debug = value
        }

    /** 当前应用程序最后一次的位置信息 */
    var location: Location? = null
        internal set

    /** 当前位置配置项 */
    val options: LocationOptions = LocationOptions()

    /**
     * 获取最后一次位置信息
     */
    @SuppressLint("MissingPermission")
    fun getLastLocation(context: Context): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!isPermissionGranted(context)) return null
        // 根据时间来获取最新的位置
        val locations = mutableListOf(this.location)
        locations.add(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER))
        locations.add(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
        locations.add(locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER))
        var result: Location? = null
        locations.forEach {
            val item = it ?: return@forEach
            if (result == null) result = item
            if (item.time > result!!.time) result = item
        }
        return result
    }

}