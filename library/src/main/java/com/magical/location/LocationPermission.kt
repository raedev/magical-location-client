package com.magical.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 位置权限处理器
 * @author RAE
 * @date 2022/10/30
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
object LocationPermission {

    /**
     * 请求位置权限
     */
    fun requestPermission(context: Activity) {
        if (isPermissionGranted(context)) return
        val groups = mutableListOf<String?>(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        ActivityCompat.requestPermissions(context, groups.toTypedArray(), 0x3721)
    }


    /**
     * 请求后台位置权限
     */
    fun requestBackgroundPermission(context: Activity) {
        if (isPermissionGranted(context) || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val groups = mutableListOf<String?>(
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        )
        ActivityCompat.requestPermissions(context, groups.toTypedArray(), 0x3721)
    }

    /**
     * 是否有后台访问位置权限
     */
    fun isBackgroundPermissionGranted(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Manifest.permission.ACCESS_BACKGROUND_LOCATION.isGranted(
            context
        )
    }

    /**
     * 是否有权限访问定位 (完全访问位置权限）
     */
    fun isPermissionGranted(context: Context): Boolean {
        // 网络定位权限
        val coarse = Manifest.permission.ACCESS_COARSE_LOCATION.isGranted(context)
        // GPS定位权限
        val fine = Manifest.permission.ACCESS_FINE_LOCATION.isGranted(context)
        // 低版本的定位权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return coarse && fine
        // 后台定位权限
        val background = Manifest.permission.ACCESS_BACKGROUND_LOCATION.isGranted(context)
        return coarse && fine && background
    }

    /**
     * 是否已授权
     */
    private fun String.isGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, this) == PackageManager.PERMISSION_GRANTED
    }
}