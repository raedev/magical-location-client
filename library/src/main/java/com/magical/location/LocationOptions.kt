package com.magical.location

import android.os.Parcelable
import com.magical.location.model.CoordinateType
import kotlinx.parcelize.Parcelize

/**
 * 位置信息配置项
 * @author RAE
 * @date 2022/10/28
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
@Parcelize
data class LocationOptions(
    /** 当前登录用户ID */
    var userId: String = "GUEST",
    /** 是否开启GSP定位 */
    var enableGPS: Boolean = true,
    /** 是否开启网络定位 */
    var enableNetwork: Boolean = true,
    /** 位置每次更新时间间隔 */
    var refreshTimeInterval: Long = 1000,
    /** 位置更新最小回调距离（米） */
    var minDistance: Int = 3,
    /** 位置服务保活（需要引导用户开启相关权限） */
    var enableKeepAlive: Boolean = true,
    /** 位置坐标类型 */
    var coordinateType: CoordinateType = CoordinateType.WGS84,
    var databasePath: String? = null
) : Parcelable