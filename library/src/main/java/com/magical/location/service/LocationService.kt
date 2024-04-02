package com.magical.location.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.magical.location.LocationOptions
import com.magical.location.internal.Log
import com.magical.location.internal.AndroidLocationRequest

/**
 * 位置服务
 * @author RAE
 * @date 2022/10/28
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
class LocationService : Service() {

    private val _binder: LocationBinder = LocationBinder(this)
    private val _keepAlive = LocationKeepAlive(this)

    override fun onBind(intent: Intent?): IBinder {
        Log.debug("位置服务绑定")
        return this._binder
    }

    override fun onCreate() {
        super.onCreate()
        LocationServiceCompat.compatService(this)
        _binder.request = AndroidLocationRequest(applicationContext)
        _binder.request.start()
        Log.debug("位置服务已启动")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getParcelableExtra<LocationOptions>("options")?.let { options ->
            if (options.enableKeepAlive) _keepAlive.register()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.warn("位置服务已经结束")
        _binder.destroy()
        _keepAlive.unregister()
    }

}