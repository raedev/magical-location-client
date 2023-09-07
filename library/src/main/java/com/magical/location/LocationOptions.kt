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
    /** 位置每次更新时间间隔（毫秒），默认1秒。 */
    var refreshTimeInterval: Long = 1000,
    /** 位置更新最小回调距离（米），默认1米 */
    var minDistance: Int = 1,
    /** 位置服务保活（需要引导用户开启相关权限） */
    var enableKeepAlive: Boolean = true,
    /** 是否开启Gnss卫星信号回调 */
    var enableGnss: Boolean = true,
    /** 最小精度(单位米），如果位置精度大于该值将过滤掉，默认0不配置 */
    var minAccuracy: Int = 0,
    /** 位置坐标类型，默认为WGS84坐标 */
    var coordinateType: CoordinateType = CoordinateType.WGS84,
    /** 位置数据库保存路径 */
    var databasePath: String? = null
) : Parcelable {

    /**
     * 仅使用GPS定位
     */
    fun onlyGps() {
        this.enableGPS = true
        this.enableNetwork = false
    }
}