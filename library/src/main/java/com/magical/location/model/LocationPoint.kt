package com.magical.location.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 轨迹点
 * @author RAE
 * @date 2022/10/31
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
@Entity(tableName = "POINTS")
data class LocationPoint(
    /** 所属轨迹点 */
    @ColumnInfo(name = "trace_id")
    var traceId: Long,
    /** 位置提供者：GPS/Network */
    var provider: String,
    /** 经度 */
    var longitude: Double,
    /** 纬度 */
    var latitude: Double,
    /** 海拔高度 */
    var altitude: Double,
    /** 精确度 */
    var accuracy: Float,
    /** 速度 */
    var speed: Float,
    /** 位置时间戳 */
    @ColumnInfo(name = "created_at")
    var createdAt: Long = System.currentTimeMillis()
) {
    /** 主键 */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "location_id")
    var locationId: Long = 0L
}
