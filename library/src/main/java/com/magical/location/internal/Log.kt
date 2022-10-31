package com.magical.location.internal

import android.util.Log
import com.magical.location.BuildConfig

/**
 * 日志记录
 * @author RAE
 * @date 2022/10/30
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
internal object Log {

    private const val TAG: String = "Magical.Location"
    internal var debug: Boolean = BuildConfig.DEBUG

    internal fun error(
        message: String,
        throwable: Throwable? = null
    ) {
        if (throwable == null) Log.e(TAG, message)
        else Log.e(TAG, message, throwable)
    }

    fun debug(message: String) {
        if (debug) Log.d(TAG, message)
    }

    fun info(message: String) {
        if (debug) Log.i(TAG, message)
    }

    fun warn(message: String) = Log.w(TAG, message)
}