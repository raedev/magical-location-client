package com.magical.location

import android.content.Context
import android.location.Location
import com.magical.location.internal.LocationDao
import com.magical.location.internal.LocationDatabase
import com.magical.location.model.LocationPoint

/**
 * 数据库管理器
 * @author RAE
 * @date 2022/10/31
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
class LocationDatabaseManager(val context: Context) {

    val database: LocationDatabase =
        LocationDatabase.create(context, MagicalLocationManager.options)

    val locationDao: LocationDao = database.locationDao()

    /**
     * 插入轨迹点
     * @param traceId 轨迹Id
     * @param location 位置信息
     */
    fun insertTrackPoint(traceId: Long, location: Location): Long {
        val point = LocationPoint(
            traceId = traceId,
            provider = location.provider ?: "unknown",
            longitude = location.longitude,
            latitude = location.latitude,
            altitude = location.latitude,
            accuracy = location.accuracy,
            speed = location.speed,
        )
        return locationDao.insert(point)
    }
}