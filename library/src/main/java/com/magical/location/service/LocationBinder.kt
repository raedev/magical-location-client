package com.magical.location.service

import android.content.Context
import android.os.Binder
import com.magical.location.internal.MagicalLocationRequest

/**
 * 位置服务绑定
 * @author RAE
 * @date 2022/10/30
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
class LocationBinder(context: Context) : Binder() {
    lateinit var request: MagicalLocationRequest

    internal fun destroy() {
        request.destroy()
    }

}