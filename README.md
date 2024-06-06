# MagicalLocation

[![](https://jitpack.io/v/raedev/magical-location-client.svg)](https://jitpack.io/#raedev/magical-location-client)

Android 定位库，提供实时位置信息、轨迹记录功能，定位以后台服务方式进行持续定位并带后台保活方案。应用场景：

- 地图类APP，需要在任何代码位置中获取GPS位置信息，或者GPS定位图层。
- 运动类APP，需要实时更新当前位置信息以及后台轨迹记录的。

默认实现为Android系统定位，若需要百度或者高德地图实现可以自行实现`BaseLocationRequest`。

![arch](architecture.drawio.png)

## 集成

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.raedev:location:1.0.3'
}
```

## 使用

更多调用方式请查看`MainActivity.kt`示例。

```kotlin
// TODO 注意：请自行先获取定位权限后再调用LocationClient，否则后面的服务都不可用。
val context: Activity = this
LocationPermission.requestPermission(context)

// 初始化位置客户端
val client = LocationClient(context)
client.listener = object : LocationListener {
    override fun onLocationChanged(location: Location) {
        // 位置信息回调（业务处理）
    }
}


// 配置相关，更多详见：LocationOptions 定义。
client.options.userId = "test"
client.options.enableNetwork = false

// 开始监听位置信息
client.start()

// 获取最后一次位置信息
val location = MagicalLocationManager.getLastLocation(context)

// 停止位置监听
client.stop()

// 释放后台服务
client.destroy()

// 配置发生改变后需要调用该方法重启服务
client.updateOptions()

// 轨迹记录，注意：请自行先获取定位权限后再调用，否则后面都不可用。
// 可使用LocationClient初始化或context初始化TrackClient(context) 
val trackClient = TrackClient(client)

// 开始轨迹记录
trackClient.start("这里是轨迹名称", "业务类型[可选]")
// 停止记录
trackClient.stop()
// 查询轨迹
trackClient.isTrackRunning // 是否记录轨迹中
trackClient.currentTrace() // 当前记录轨迹
trackClient.loadTraceList("2024-01-01", "2024-01-02") // 根据时间段查询轨迹列表

```