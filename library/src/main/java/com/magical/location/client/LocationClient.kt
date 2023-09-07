package com.magical.location.client

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.magical.location.LocationOptions
import com.magical.location.LocationPermission
import com.magical.location.MagicalLocationManager
import com.magical.location.R
import com.magical.location.internal.LocationDao
import com.magical.location.internal.LocationDatabase
import com.magical.location.internal.Log
import com.magical.location.model.LocationServiceState
import com.magical.location.service.LocationBinder
import com.magical.location.service.LocationServiceCompat

/**
 * 位置客户端
 * @author RAE
 * @date 2022/10/28
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
class LocationClient(private val context: Context) : LocationListener,
    LifecycleEventObserver {

    /** 配置项 */
    val options: LocationOptions
        get() = MagicalLocationManager.options

    /** 位置监听 */
    var listener: LocationListener? = null

    /** 数据库操作 */
    val database: LocationDao by lazy {
        LocationDatabase.create(context, options).locationDao()
    }

    private var _paused: Boolean = false
    private var _binder: LocationBinder? = null
    private var _connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            listener?.onLocationServiceStateChanged(LocationServiceState.Connected)
            _binder = service as LocationBinder
            _binder?.request?.let {
                it.registerListener(this@LocationClient)
                it.start()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            _binder = null
            listener?.onLocationServiceStateChanged(LocationServiceState.Disconnected)
        }
    }

    private var gpsStateReceiver: GpsStateReceiver? = null

    private inner class GpsStateReceiver : BroadcastReceiver() {
        private var lastState: Boolean? = null
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent?.action != LocationManager.PROVIDERS_CHANGED_ACTION) return
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val enable = LocationManagerCompat.isLocationEnabled(manager)
            // 状态一致
            if (lastState == enable) return
            lastState = enable
            listener?.onProviderStatusChanged("gps", enable, enable)
            // 重新启动服务
            if (enable) start() else stop()
        }
    }

    /**
     * 位置服务是否运行中
     */
    val isRunning: Boolean
        get() {
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return _binder != null && LocationManagerCompat.isLocationEnabled(manager) && hasPermission()
        }


    /**
     * 绑定生命周期
     */
    fun bindLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        owner.lifecycle.addObserver(this)
    }

    private fun notifyLocationChanged(location: Location) {
        if (!_paused) listener?.onLocationChanged(location)
    }

    private fun registerStateReceiver() {
        if (gpsStateReceiver == null) {
            // 注册广播
            gpsStateReceiver = GpsStateReceiver()
            context.registerReceiver(
                gpsStateReceiver,
                IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
            )
        }
    }

    private fun unregisterStateReceiver() {
        if (gpsStateReceiver != null) {
            context.unregisterReceiver(gpsStateReceiver)
            gpsStateReceiver = null
        }
    }

    /**
     * 启动服务
     */
    fun start() {
        registerStateReceiver()
        if (!LocationPermission.isPermissionGranted(context)) {
            listener?.onLocationPermissionDenied()
            return
        }

        // 检查GPS是否开启
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!LocationManagerCompat.isLocationEnabled(manager)) {
            listener?.onLocationError(context.getString(R.string.gm_location_disable))
            // 线程监听GPS打开状态
            return
        }

        _paused = false
        _binder?.let { binder ->
            Log.warn("位置服务已经绑定")
            binder.request.start()
            return
        }
        // 绑定服务
        listener?.onLocationServiceStateChanged(LocationServiceState.Connecting)
        LocationServiceCompat.bindService(context, _connection, options)
    }

    /**
     * 停止服务，只是停止位置回调，后台的位置服务还在继续运行，若想彻底结束请调用[destroy]方法
     */
    fun stop() {
        if (_binder != null) {
            _binder!!.request.removeListener(this)
            context.unbindService(_connection)
            _binder = null
            listener?.onLocationServiceStateChanged(LocationServiceState.Disconnected)
        }
    }


    fun destroy() {
        stop()
        unregisterStateReceiver()
        LocationServiceCompat.stopService(context)
    }

    /**
     * 暂停定位
     */
    fun pause() {
        this._paused = true
    }

    /**
     * 更新配置
     */
    fun updateOptions() {
        // 停止服务
        stop()
        LocationServiceCompat.stopService(context)
        // 重新启动服务
        start()
    }

    fun hasPermission(): Boolean {
        return LocationPermission.isPermissionGranted(context)
    }

    fun requestPermission() {
        LocationPermission.requestPermission(context as Activity)
    }

    override fun onGnssStatusChanged(count: Int, signal: Int, label: String) {
        if (!_paused) {
            listener?.onGnssStatusChanged(count, signal, label)
        }
    }

    override fun onProviderStatusChanged(
        provider: String,
        enable: Boolean,
        isLocationEnabled: Boolean
    ) {
        listener?.onProviderStatusChanged(provider, enable, isLocationEnabled)
    }

    override fun onLocationChanged(location: Location) {
        notifyLocationChanged(location)
    }

    /**
     * 生命周期回调
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> _paused = false
            Lifecycle.Event.ON_PAUSE -> _paused = true
            Lifecycle.Event.ON_STOP -> {
                _paused = true
            }

            Lifecycle.Event.ON_DESTROY -> {
                this.destroy()
                source.lifecycle.removeObserver(this)
            }

            else -> return
        }
    }
}