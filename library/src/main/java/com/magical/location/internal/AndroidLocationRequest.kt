@file:Suppress("DeprecatedCallableAddReplaceWith")

package com.magical.location.internal

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.location.LocationManagerCompat
import com.magical.location.LocationPermission
import com.magical.location.MagicalLocationManager
import com.magical.location.R

/**
 * Android系统位置请求
 * @author RAE
 * @date 2022/10/28
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
@SuppressLint("MissingPermission")
class AndroidLocationRequest internal constructor(context: Context) : BaseLocationRequest(context),
    LocationListener, GnssStatusManager.Listener {

    /** 系统定位管理器 */
    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    /** 是否已经注册监听 */
    private var isRegister: Boolean = false

    private val gnssStatusManager = GnssStatusManager(context)

    override fun start() {
        if (!LocationPermission.isPermissionGranted(context)) return notifyPermissionDenied()
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            return notifyRequestLocationError(context.getString(R.string.gm_location_disable))
        }
        this.unregister()
        val providers = mutableListOf<String>()
        if (options.enableGPS && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 启用GPS
            providers.add(LocationManager.GPS_PROVIDER)
        }
        if (options.enableNetwork && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // 启用网络位置
            providers.add(LocationManager.NETWORK_PROVIDER)
        }
        if (providers.isEmpty()) return notifyRequestLocationError(context.getString(R.string.gm_location_provider_not_found))
        providers.forEach { provider ->
            locationManager.requestLocationUpdates(
                provider,
                options.refreshTimeInterval,
                options.minDistance.toFloat(),
                this,
                Looper.getMainLooper()
            )
        }
        // 回调位置信息
        MagicalLocationManager.getLastLocation(context)?.let {
            MagicalLocationManager.location = null
            notifyLocationChanged(it)
        }
        // 注册GPS信号监听
        if (options.enableGnss) {
            gnssStatusManager.register(this)
        }
    }

    override fun stop() = unregister()

    override fun onLocationChanged(location: Location) {
        notifyLocationChanged(location)
    }

    override fun onProviderEnabled(provider: String) {
        Log.info("the $provider provider is enable")
        eachListener {
            it.onProviderStatusChanged(provider, enable = true, true)
        }
        restart()
    }

    override fun onProviderDisabled(provider: String) {
        Log.warn("the $provider provider is disable")
        eachListener {
            it.onProviderStatusChanged(provider, enable = false, true)
        }
    }

    override fun onLocationChanged(locations: MutableList<Location>) {
        super.onLocationChanged(locations)
    }

    override fun onFlushComplete(requestCode: Int) {
        Log.debug("location onFlushComplete $requestCode")
    }

    /**
     * 必须重写这个方法，在部分手机上会报找不到实现类
     */
    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.debug("location onStatusChanged $provider")
    }

    private fun unregister() {
        if (isRegister) locationManager.removeUpdates(this)
        gnssStatusManager.unregister()
    }

    override fun onGnssStatusChanged(count: Int, signal: Int, label: String) {
        notifyStatusChanged(count, signal, label)
    }

}