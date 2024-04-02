package com.magical.location.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.PowerManager
import com.magical.location.internal.Log
import kotlin.concurrent.thread

/**
 * 位置服务保活，保活方式：
 * 1、锁屏时
 * @author RAE
 * @date 2022/10/30
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
internal class LocationKeepAlive(private val service: LocationService) :
    MediaPlayer.OnPreparedListener {


    /**
     * 新建这个播放器，主要是用来获取媒体锁，从而使服务不被系统杀掉，一般对于锁而言。锁定了通常须要解锁。
     * 可是这里的唤醒锁与MediaPlayer关联，所以仅仅须要在使用完之后release()释放MediaPlayer就可以，无需显式的为其解锁
     */
    private val _player = MediaPlayer()

    /** 锁屏广播 */
    private val _receiver = object : BroadcastReceiver() {
        var isRegister = false
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                // 系统锁屏
                Intent.ACTION_SCREEN_OFF -> thread { play() }
                // 系统解锁
                Intent.ACTION_USER_PRESENT -> thread { stop() }
                // 闹钟回调
                LocationServiceCompat.ACTION_LOCATION_ALARM -> thread {
                    Log.debug("闹钟服务保活")
                    if (!LocationServiceCompat.isServiceRunning(service)) {
                        // 重启服务
                        Log.warn("重启位置服务")
                        LocationServiceCompat.startService(service.applicationContext)
                    }
                }
            }
        }
    }


    /**
     * 播放音频文件
     */
    fun play() = runCatching {
        if (_player.isPlaying) return
        _player.setOnPreparedListener(this)
        val fileDescriptor = service.assets.openFd("mute.mp3")
        _player.setDataSource(
            fileDescriptor.fileDescriptor,
            fileDescriptor.startOffset,
            fileDescriptor.length
        )
        _player.prepareAsync()
    }.getOrDefault(Unit)

    /**
     * 停止播放
     */
    private fun stop() = runCatching {
        if (_player.isPlaying) {
            _player.stop()
            _player.reset()
            Log.debug("停止位置无声音乐")
        }
    }.getOrDefault(Unit)

    /**
     * 释放资源
     */
    private fun release() = runCatching {
        _player.release()
    }.getOrDefault(Unit)

    fun register() {
        if (_receiver.isRegister) return
        Log.debug("注册位置保活服务")
        // 注册锁屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            service.registerReceiver(_receiver, IntentFilter().apply {
                addAction(LocationServiceCompat.ACTION_LOCATION_ALARM)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            }, Context.RECEIVER_EXPORTED)
        } else {
            service.registerReceiver(_receiver, IntentFilter().apply {
                addAction(LocationServiceCompat.ACTION_LOCATION_ALARM)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            })
        }
        // 注册定时闹钟
        val am = service.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // 定时1分钟检查
        am.setRepeating(AlarmManager.RTC_WAKEUP, 5000, 60000, createAlarmIntent())
        _receiver.isRegister = true
    }

    private fun createAlarmIntent(): PendingIntent {
        return PendingIntent.getBroadcast(
            service, 12306, Intent(LocationServiceCompat.ACTION_LOCATION_ALARM), when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                else -> PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
    }

    fun unregister() {
        Log.debug("取消位置保活服务")
        this.stop()
        this.release()
        if (_receiver.isRegister) {
            // 取消广播注册
            service.unregisterReceiver(_receiver)
            // 取消闹钟
            val am = service.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(createAlarmIntent())
            _receiver.isRegister = false
        }
    }

    override fun onPrepared(mp: MediaPlayer) {
        mp.apply {
            isLooping = true
            setAudioAttributes(
                AudioAttributes.Builder().setLegacyStreamType(AudioManager.STREAM_MUSIC).build()
            )
            // 重要的方法，用于保持CPU运行
            setWakeMode(service, PowerManager.PARTIAL_WAKE_LOCK)
        }
        mp.start()
        Log.debug("播放位置无声音乐")
    }
}