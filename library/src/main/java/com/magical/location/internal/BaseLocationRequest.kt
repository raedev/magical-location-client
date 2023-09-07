package com.magical.location.internal

import android.content.Context
import android.content.Intent
import android.location.Location
import com.magical.location.LocationOptions
import com.magical.location.MagicalLocationManager
import com.magical.location.R
import com.magical.location.client.LocationListener
import com.magical.location.service.TraceRecorder
import java.util.*

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
    protected val listeners: MutableList<LocationListener> = mutableListOf()

    /** 队列 */
    private val _queue: Queue<Location> = LinkedList()

    /**
     * 注册位置回调
     */
    fun registerListener(listener: LocationListener) {
        listeners.add(listener)
        // 立即回调一次
        MagicalLocationManager.location?.let { listener.onLocationChanged(it) }
    }

    /**
     * 移除位置回调
     */
    fun removeListener(listener: LocationListener) = listeners.remove(listener)

    /**
     * 开始监听位置
     */
    abstract fun start()

    /**
     * 停止位置监听
     */
    abstract fun stop()

    /**
     * 重启位置监听
     */
    fun restart() {
        this.stop()
        this.start()
    }

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
        // 若当前位置和上一次位置时间间隔小于1秒，并且精度较大则舍弃
        if (lastLocation != null && location.time - lastLocation.time < 1000 && location.accuracy < lastLocation.accuracy) {
            Log.warn("this location time is less than 1000ms at last location ")
            return
        }
        // 精度过滤
        if (options.minAccuracy > 0 && location.accuracy > options.minAccuracy) {
            Log.warn("this location  ${location.accuracy} accuracy is greater than ${options.minAccuracy}, filter it.")
            return
        }

        // 更新当前位置
        MagicalLocationManager.location = location
        Log.debug("location has changed: $location")
        // 发送位置广播
        val intent = Intent(MagicalLocationManager.ACTION_LOCATION_CHANGED)
        intent.putExtra(MagicalLocationManager.EXTRA_LOCATION, location)
        context.sendBroadcast(intent)
        // 回调通知
        listeners.forEach { it.onLocationChanged(location) }
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

    protected fun notifyStatusChanged(count: Int, signal: Int, name: String) {
        listeners.forEach {
            it.onGnssStatusChanged(count, signal, name)
        }
    }

    protected inline fun eachListener(crossinline block: (LocationListener) -> Unit) {
        listeners.forEach {
            block(it)
        }
    }

    internal fun destroy() {
        listeners.clear()
        stop()
    }


}