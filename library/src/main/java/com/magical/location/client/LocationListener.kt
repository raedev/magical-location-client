package com.magical.location.client

import android.location.Location
import androidx.annotation.IntRange
import com.magical.location.model.LocationServiceState

/**
 * 位置监听器
 * @author RAE
 * @date 2022/10/30
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
interface LocationListener {
    /**
     * 无位置权限通知
     */
    fun onLocationPermissionDenied() = Unit

    /**
     * 位置发生改变通知
     */
    fun onLocationChanged(location: Location)

    /** 位置服务状态发生改变通知 */
    fun onLocationServiceStateChanged(state: LocationServiceState) = Unit

    /**
     * 位置发生错误通知
     */
    fun onLocationError(message: String, throwable: Throwable? = null) = Unit

    /**
     * 卫星状态发生改变
     */
    fun onGnssStatusChanged(count: Int, @IntRange(0, 5) signal: Int, label: String) = Unit

    /**
     * 位置提供者状态发生变化回调
     * @param provider 提供程序
     * @param enable 是否启用
     * @param isLocationEnabled  GPS是否打开
     */
    fun onProviderStatusChanged(provider: String, enable: Boolean, isLocationEnabled: Boolean) =
        Unit
}