package com.magical.location.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.magical.location.LocationOptions
import com.magical.location.LocationPermission
import com.magical.location.R
import com.magical.location.internal.Log

/**
 * 位置服务兼容器
 * @author RAE
 * @date 2022/10/30
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
object LocationServiceCompat {

    private const val NOTIFY_ID: Int = 12580
    private const val CHANNEL: String = "LocationServiceChannel"
    internal const val ACTION_LOCATION_ALARM = "com.magical.location.action.ALARM"

    /**
     * 启动位置服务
     */
    fun startService(context: Context, options: LocationOptions? = null) {
        if (!LocationPermission.isPermissionGranted(context)) return Log.error("无位置权限无法启动服务")
        val intent = Intent(context, LocationService::class.java)
        options?.let { intent.putExtra("options", it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    /**
     * 绑定位置服务
     */
    fun bindService(
        context: Context,
        connection: ServiceConnection,
        options: LocationOptions? = null
    ) {
        val intent = Intent(context, LocationService::class.java)
        options?.let { intent.putExtra("options", it) }
        startService(context, options)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun stopService(context: Context) {
        context.stopService(Intent(context, LocationService::class.java))
    }

    /**
     * 适配服务
     */
    internal fun compatService(service: Service) {
        val iconId = service.applicationInfo.icon
        val appLabel = service.getString(service.applicationInfo.labelRes)
        val manager = service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 适配通知渠道
            val name = service.getString(R.string.gm_location_channel_name)
            val channel = NotificationChannel(CHANNEL, name, NotificationManager.IMPORTANCE_HIGH)
            channel.setShowBadge(false)
            channel.description = service.getString(R.string.gm_location_channel_description)
            manager.createNotificationChannel(channel)
            manager.getNotificationChannel(CHANNEL)
        }
        val notification = NotificationCompat.Builder(service, CHANNEL).apply {
            val appIntent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.parse("package:${service.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val flags = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                else -> PendingIntent.FLAG_CANCEL_CURRENT
            }
            val intent = PendingIntent.getActivity(service, 1024, appIntent, flags)
            setContentTitle(service.getString(R.string.gm_location_notification_title))
            setContentText(appLabel + service.getString(R.string.gm_location_notification_desc))
            setContentIntent(intent)
            setWhen(System.currentTimeMillis())
            setSmallIcon(iconId)
            setLargeIcon(BitmapFactory.decodeResource(service.resources, iconId))
        }.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            service.startForeground(NOTIFY_ID, notification)
        } else {
            manager.notify(NOTIFY_ID, notification)
        }
    }

    internal fun isServiceRunning(context: Context): Boolean = runCatching {
        val className: String = LocationService::class.java.name
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        am.getRunningServices(Int.MAX_VALUE)?.find { it.service.className == className } != null
    }.getOrDefault(false)

}