package com.magical.location

import android.content.Context
import android.location.Location
import com.magical.location.internal.LocationDao
import com.magical.location.internal.LocationDatabase
import com.magical.location.model.LocationPoint
import com.magical.location.model.LocationTrace

/**
 * 数据库管理器
 * @author RAE
 * @date 2022/10/31
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
class LocationDatabaseManager(val context: Context) {
    private var _dao: LocationDao? = null
    private val dao: LocationDao
        get() = _dao!!

    init {
        reopen()
    }

    fun reopen() {
        _dao = LocationDatabase.create(context, MagicalLocationManager.options).locationDao()
    }

    /**
     * 插入轨迹点
     * @param trace 轨迹
     * @param location 位置信息
     */
    fun addPoint(trace: LocationTrace, location: Location) {
        val point = LocationPoint(
            traceId = trace.traceId,
            provider = location.provider ?: "unknown",
            longitude = location.longitude,
            latitude = location.latitude,
            altitude = location.latitude,
            accuracy = location.accuracy,
            speed = location.speed
        )
        dao.insert(point)
    }
}