package com.magical.location.client

import android.location.Location
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
}