package com.magical.location.client

import android.content.Context
import com.magical.location.LocationOptions
import com.magical.location.model.LocationTrace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 轨迹客户端
 * @author RAE
 * @date 2024/06/06
 */
class TrackClient(private val locationClient: LocationClient) {

    val options: LocationOptions get() = locationClient.options
    private val locationDao get() = locationClient.database

    /**
     * 轨迹是否记录中
     */
    val isTrackRunning
        get() = locationClient.isRunning && locationClient.options.traceId != null


    constructor(context: Context) : this(LocationClient(context))

    /**
     * 设置位置回调监听
     */
    fun setLocationListener(listener: LocationListener) {
        locationClient.listener = listener
    }

    /**
     * 查询轨迹信息
     * @param name 轨迹名称
     */
    fun loadTrack(name: String): LocationTrace? {
        val trace = locationDao.findTrace(name) ?: return null
        // 查询轨迹点
        trace.points = locationDao.queryPoints(trace.traceId)
        return trace
    }

    /**
     * 查询轨迹列表
     * @param start 开始时间
     * @param end 结束时间
     */
    fun loadTraceList(start: String, end: String): List<LocationTrace> {
        return locationDao.findTraces(start, end)
    }

    /**
     * 查询正在记录的轨迹
     */
    fun currentTrace(): LocationTrace? {
        val id = options.traceId ?: return null
        return locationDao.findTrace(id)?.also {
            it.points = locationDao.queryPoints(it.traceId)
        }
    }

    /**
     * 开始记录轨迹
     * @param name 轨迹名称[LocationTrace.traceName]
     * @param type 轨迹类型[LocationTrace.traceType]
     */
    fun start(name: String, type: String = "System") {
        MainScope().launch(Dispatchers.IO) {
            // 插入或新增一条轨迹
            var model = locationDao.findTrace(name)
            if (model == null) {
                model = LocationTrace().apply {
                    traceName = name
                    traceType = type
                }
                val result = locationDao.insert(model)
                if (result <= 0) error("轨迹新增失败：$name")
            }
            withContext(Dispatchers.Main) {
                // 关联到当前记录的轨迹中
                locationClient.options.traceId = model.traceId
                locationClient.start()
            }
        }
    }

    /**
     * 停止记录轨迹
     * @param destroyService 是否停止位置服务，位置服务将彻底释放，默认为false
     */
    fun stop(destroyService: Boolean = false) {
        locationClient.options.traceId = null
        locationClient.stop()
        if (destroyService) {
            MainScope().launch(Dispatchers.IO) {
                // 3秒后停止服务
                delay(3000)
                locationClient.destroy()
            }
        }
    }

}