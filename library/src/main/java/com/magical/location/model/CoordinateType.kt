package com.magical.location.model

/**
 * 位置坐标系
 * @author RAE
 * @date 2022/10/28
 * @copyright Copyright (c) https://github.com/raedev All rights reserved.
 */
enum class CoordinateType {

    /**
     * WGS84坐标系，Android系统的GPS默认位置坐标
     */
    WGS84,

    /**
     * GCJ02坐标系
     */
    GCJ02
}