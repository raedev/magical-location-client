package com.magical.location.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * 轨迹表
 * @author RAE
 * @date 2022/10/31
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
@Entity(tableName = "TRACES")
class LocationTrace {

    /** 轨迹Id */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "trace_id")
    var traceId: Long = 0

    /** 轨迹名称 */
    @ColumnInfo(name = "trace_name")
    lateinit var traceName: String

    /** 轨迹类型 */
    @ColumnInfo(name = "trace_type")
    var traceType: String = "SYSTEM"

    /** 轨迹创建时间 */
    @ColumnInfo(name = "created_at")
    var createdAt: Long = System.currentTimeMillis()

    /** 轨迹更新时间 */
    @ColumnInfo(name = "updated_at")
    var updatedAt: Long = System.currentTimeMillis()

    /** 轨迹最后一次上传时间 */
    @ColumnInfo(name = "uploaded_at")
    var uploadedAt: Long? = null

    /** 最后一次上传的点索引 */
    @ColumnInfo(name = "uploaded_index")
    var uploadedIndex: Long = 0

    /** 当前轨迹点数量 */
    @ColumnInfo(name = "point_count")
    var pointCount: Long = 0

    /** 轨迹点 */
    @Ignore
    var points: List<LocationPoint>? = null
}
