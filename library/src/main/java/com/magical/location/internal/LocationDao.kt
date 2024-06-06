package com.magical.location.internal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.magical.location.model.LocationPoint
import com.magical.location.model.LocationTrace

/**
 * 数据库查询
 * @author RAE
 * @date 2022/10/31
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
@Dao
interface LocationDao {
    /**
     * 插入轨迹点
     */
    @Insert
    fun insert(location: LocationPoint): Long

    /**
     * 新增轨迹
     */
    @Insert
    fun insert(trace: LocationTrace): Long

    /**
     * 更新轨迹信息
     */
    @Update
    fun update(trace: LocationTrace): Int

    /**
     * 删除轨迹信息
     */
    @Delete
    fun delete(trace: LocationTrace): Int

    /**
     * 查询轨迹点
     */
    @Query("SELECT * FROM POINTS WHERE trace_id=:traceId")
    fun queryPoints(traceId: Long): List<LocationPoint>

    /**
     * 查询轨迹
     */
    @Query("SELECT * FROM TRACES WHERE trace_name LIKE :name")
    fun findTrace(name: String): LocationTrace?

    /**
     * 查询轨迹
     */
    @Query("SELECT * FROM TRACES WHERE trace_id=:id")
    fun findTrace(id: Long): LocationTrace?

    /**
     * 查询轨迹
     */
    @Query("SELECT * FROM TRACES WHERE created_at BETWEEN :startDate AND :endDate")
    fun findTraces(startDate: String, endDate: String): List<LocationTrace>

}