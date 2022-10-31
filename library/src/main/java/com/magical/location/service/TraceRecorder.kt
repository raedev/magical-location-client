package com.magical.location.service

import android.content.Context
import android.location.Location
import com.magical.location.client.LocationListener

/**
 * 轨迹记录
 * @author RAE
 * @date 2022/10/31
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
internal class TraceRecorder(val context: Context) : LocationListener {


    override fun onLocationChanged(location: Location) {
    }

}