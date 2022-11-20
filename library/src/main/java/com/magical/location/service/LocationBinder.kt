package com.magical.location.service

import android.content.Context
import android.os.Binder
import com.magical.location.internal.AndroidLocationRequest

/**
 * 位置服务绑定
 * @author RAE
 * @date 2022/10/30
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
class LocationBinder(context: Context) : Binder() {
    lateinit var request: AndroidLocationRequest

    internal fun destroy() {
        request.destroy()
    }

}