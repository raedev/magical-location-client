package com.magical.location.internal

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import com.magical.location.LocationOptions
import com.magical.location.MagicalLocationManager
import com.magical.location.R
import com.magical.location.service.TraceRecorder

/**
 * 位置请求
 * @author RAE
 * @date 2022/10/28
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseLocationRequest(protected val context: Context) {

    /** 轨迹记录 */
    private val _recorder = TraceRecorder(context)

    /** 当前配置项 */
    internal val options: LocationOptions
        get() = MagicalLocationManager.options

    /** 注册监听 */
    private val _listeners: MutableList<LocationListener> = mutableListOf()

    /**
     * 注册位置回调
     */
    fun registerListener(listener: LocationListener) {
        _listeners.add(listener)
        // 立即回调一次
        MagicalLocationManager.location?.let { listener.onLocationChanged(it) }
    }

    /**
     * 移除位置回调
     */
    fun removeListener(listener: LocationListener) = _listeners.remove(listener)

    /**
     * 开始监听位置
     */
    abstract fun start()

    /**
     * 停止位置监听
     */
    abstract fun stop()

    /**
     * 位置发生改变通知
     */
    protected fun notifyLocationChanged(location: Location) {
        // 最后一次位置信息
        val lastLocation = MagicalLocationManager.location
        // 比较位置信息
        if (lastLocation != null && location.distanceTo(lastLocation) <= 0) {
            return Log.debug("this location distance is less than 0 meters")
        }
        // 更新当前位置
        MagicalLocationManager.location = location
        Log.debug("位置发生改变: $location")
        // 发送位置广播
        val intent = Intent(MagicalLocationManager.ACTION_LOCATION_CHANGED)
        intent.putExtra(MagicalLocationManager.EXTRA_LOCATION, location)
        context.sendBroadcast(intent)
        // 回调通知
        _listeners.forEach { it.onLocationChanged(location) }
        _recorder.onLocationChanged(location)
    }

    /**
     * 无位置权限通知
     */
    protected fun notifyPermissionDenied() {
        _recorder.onLocationPermissionDenied()
        notifyRequestLocationError(context.getString(R.string.gm_location_permission_denied))
    }

    /**
     * 位置发生错误通知
     */
    protected fun notifyRequestLocationError(message: String) {
        _recorder.onLocationError(message)
        Log.error(message)
    }

    internal fun destroy() {
        _listeners.clear()
        stop()
    }
}