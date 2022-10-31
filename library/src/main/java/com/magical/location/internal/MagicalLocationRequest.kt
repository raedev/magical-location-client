package com.magical.location.internal

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
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
class MagicalLocationRequest internal constructor(context: Context) : BaseLocationRequest(context),
    LocationListener {

    /** 系统定位管理器 */
    private val locationManager: android.location.LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager

    /** 是否已经注册监听 */
    private var isRegister: Boolean = false

    override fun start() {
        if (!LocationPermission.isPermissionGranted(context)) return notifyPermissionDenied()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && !locationManager.isLocationEnabled) {
            return notifyRequestLocationError(context.getString(R.string.gm_location_disable))
        }
        this.unregister()
        val providers = mutableListOf<String>()
        if (options.enableGPS && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 启用GPS
            providers.add(LocationManager.GPS_PROVIDER)
            providers.add(LocationManager.PASSIVE_PROVIDER)
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
                this
            )
        }
        // 回调位置信息
        MagicalLocationManager.getLastLocation(context)?.let {
            MagicalLocationManager.location = null
            notifyLocationChanged(it)
        }
    }

    override fun stop() = unregister()

    override fun onLocationChanged(location: Location) {
        notifyLocationChanged(location)
    }

    override fun onProviderEnabled(provider: String) {
        super.onProviderEnabled(provider)
        Log.info("the $provider provider is enable")
    }

    override fun onProviderDisabled(provider: String) {
        super.onProviderDisabled(provider)
        Log.warn("the $provider provider is disable")
    }

    private fun unregister() {
        if (isRegister) locationManager.removeUpdates(this)
    }

}