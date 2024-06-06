package com.magical.location

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.magical.location.client.LocationClient
import com.magical.location.client.LocationListener
import com.magical.location.client.TrackClient
import com.magical.location.demo.databinding.ActivityMainBinding
import com.magical.location.model.LocationServiceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var client: LocationClient
    private lateinit var trackClient: TrackClient

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        client = LocationClient(this)
        client.options.minAccuracy = 10
        client.listener = object : LocationListener {
            override fun onGnssStatusChanged(count: Int, signal: Int, label: String) {
                binding.tvGnss.text = "信号强度：$label"
            }

            override fun onProviderStatusChanged(
                provider: String,
                enable: Boolean,
                isLocationEnabled: Boolean
            ) {
                super.onProviderStatusChanged(provider, enable, isLocationEnabled)
                binding.tvLastLocation.text = "GPS状态：$isLocationEnabled"
            }

            override fun onLocationChanged(location: Location) {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val second = calendar.get(Calendar.SECOND)
                binding.tvLocation.text =
                    String.format("[%02d:%02d:%02d]: %s", hour, minute, second, location.format())
            }

            override fun onLocationServiceStateChanged(state: LocationServiceState) {
                binding.tvLocation.text = "当前服务状态：$state"
                when (state) {
                    LocationServiceState.Connecting -> {
                        binding.btnStart.isEnabled = false
                        binding.btnStop.isEnabled = false
                    }

                    LocationServiceState.Connected -> {
                        binding.btnStart.isEnabled = false
                        binding.btnStop.isEnabled = true
                    }

                    LocationServiceState.Disconnected -> {
                        binding.btnStart.isEnabled = true
                        binding.btnStop.isEnabled = false
                    }
                }
            }

            override fun onLocationError(message: String, throwable: Throwable?) {
                super.onLocationError(message, throwable)
                binding.tvLocation.text = message
            }
        }

        binding.btnStart.setOnClickListener {
            if (client.hasPermission()) return@setOnClickListener client.start()
            Toast.makeText(it.context, "请求位置权限", Toast.LENGTH_SHORT).show()
            client.requestPermission()
        }

        binding.btnStop.setOnClickListener {
            client.destroy()
        }

        val location = MagicalLocationManager.getLastLocation(this)
        binding.tvLastLocation.text = "最后一次位置：${location.format()}"

        trackClient = TrackClient(client)
        binding.btnTrack.setOnClickListener {
            // 是否允许记录模拟位置，默认true
            trackClient.options.enableMockLocation = true
            if (trackClient.isTrackRunning) {
                // 停止记录
                trackClient.stop()
                binding.btnTrack.text = "开始记录轨迹"
            } else {
                trackClient.start("张三1024年10月24日的轨迹记录")
                binding.btnTrack.text = "停止记录"
            }
        }

        binding.btnTrackInfo.setOnClickListener {
            if (!trackClient.isTrackRunning) {
                Toast.makeText(this, "轨迹未开始记录", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val track = withContext(Dispatchers.IO) { trackClient.currentTrace() }
                if (track == null) {
                    Toast.makeText(it.context, "查询不到当前轨迹信息", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        it.context,
                        "当前记录的轨迹为：${track.traceName}，轨迹点${track.points?.size}个",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }

    private fun Location?.format(): String {
        this ?: return "无位置信息"
        val timeText = SimpleDateFormat.getTimeInstance().format(Date(time))
        return "$timeText，来自${provider?.uppercase()}，经纬度($longitude,$latitude)，海拔（${this.altitude}） 精度（${this.accuracy}）"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!client.hasPermission()) {
            LocationPermission.requestBackgroundPermission(this)
        }
    }
}