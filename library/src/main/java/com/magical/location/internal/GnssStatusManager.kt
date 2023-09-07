package com.magical.location.internal

import android.annotation.SuppressLint
import android.content.Context
import android.location.GnssStatus
import android.location.GpsStatus
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import androidx.core.location.LocationManagerCompat
import com.magical.location.LocationPermission
import com.magical.location.R
import kotlin.math.min

/**
 * 卫星状态管理器
 * @author RAE
 * @date 2023/09/06
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
class GnssStatusManager(private val context: Context) {

    companion object {
        private val SIGNAL_NAMES = arrayOf("无", "差", "弱", "一般", "好", "强")
    }

    interface Listener {
        fun onGnssStarted() = Unit

        fun onGnssStop(reason: String) = Unit

        fun onPermissionDenied() = Unit

        /**
         * 卫星状态发生改变
         */
        fun onGnssStatusChanged(count: Int, @IntRange(0, 5) signal: Int, label: String)

        /**
         * 根据卫星个数计算信号强度
         */
        fun signal(count: Int): Int = min(5, 5 * count / 18)

        fun signalName(signal: Int): String = SIGNAL_NAMES[signal % SIGNAL_NAMES.size]
    }


    private val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var listener: Listener? = null

    @RequiresApi(Build.VERSION_CODES.N)
    private inner class GnssStatusCallbackImpl24 : GnssStatus.Callback() {
        override fun onStarted() {
            super.onStarted()
            listener?.onGnssStarted()
        }

        override fun onStopped() {
            super.onStopped()
            listener?.onGnssStop("onStopped")
        }

        override fun onSatelliteStatusChanged(status: GnssStatus) {
            super.onSatelliteStatusChanged(status)
            var count = 0
            for (i in 0 until status.satelliteCount) {
                if (status.usedInFix(i)) count++
            }
            onGnssStatusChanged(count)
        }
    }

    private inner class GnssStatusCallbackImpl : GpsStatus.Listener {
        @SuppressLint("MissingPermission")
        override fun onGpsStatusChanged(event: Int) {
            when (event) {
                GpsStatus.GPS_EVENT_STARTED -> {
                    listener?.onGnssStarted()
                }

                GpsStatus.GPS_EVENT_STOPPED -> {
                    listener?.onGnssStop("onStopped")
                }

                GpsStatus.GPS_EVENT_SATELLITE_STATUS -> {
                    val status = manager.getGpsStatus(null) ?: return onGnssStatusChanged(0)
                    var count = 0
                    status.satellites.forEach {
                        if (it.usedInFix()) {
                            count++
                        }
                    }
                    onGnssStatusChanged(count)
                }
            }
        }
    }

    private var callbackImpl: GnssStatusCallbackImpl? = null
    private var callbackImpl24: GnssStatusCallbackImpl24? = null


    /**
     * 注册监听
     */
    @SuppressLint("MissingPermission")
    fun register(listener: Listener) {
        if (!LocationManagerCompat.isLocationEnabled(manager)) {
            listener.onGnssStop(context.getString(R.string.gm_location_disable))
            return
        }
        if (!LocationPermission.isPermissionGranted(context)) {
            listener.onPermissionDenied()
        }
        // 已经注册
        if (this.listener != null) return
        Log.d("GnssStatusManager", "注册卫星状态监听器")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            callbackImpl24 = GnssStatusCallbackImpl24()
            manager.registerGnssStatusCallback(callbackImpl24!!)
        } else {
            callbackImpl = GnssStatusCallbackImpl()
            manager.addGpsStatusListener(callbackImpl!!)
        }
        this.listener = listener
    }

    @SuppressLint("MissingPermission")
    fun unregister() {
        if (callbackImpl24 != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                manager.unregisterGnssStatusCallback(callbackImpl24!!)
            }
            callbackImpl24 = null
        }
        if (callbackImpl != null) {
            manager.removeGpsStatusListener(callbackImpl)
            callbackImpl = null
        }
        this.listener = null
    }

    private fun onGnssStatusChanged(count: Int) {
        val listener = this.listener ?: return
        val signal = listener.signal(count)
        val name = listener.signalName(signal)
//        Log.d("GnssStatusManager", "onGnssStatusChanged:卫星数量：$count，信号：$name")
        listener.onGnssStatusChanged(count, signal, name)
    }

}