package com.magical.location

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.magical.location.client.LocationClient
import com.magical.location.client.LocationListener
import com.magical.location.demo.databinding.ActivityMainBinding
import com.magical.location.model.LocationServiceState
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityMainBinding

    private lateinit var client: LocationClient

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        client = LocationClient(this)
        client.options.minAccuracy = 10
        client.listener = object : LocationListener {
            override fun onGnssStatusChanged(count: Int, signal: Int, label: String) {
                _binding.tvGnss.text = "信号强度：$label"
            }

            override fun onProviderStatusChanged(
                provider: String,
                enable: Boolean,
                isLocationEnabled: Boolean
            ) {
                super.onProviderStatusChanged(provider, enable, isLocationEnabled)
                _binding.tvLastLocation.text = "GPS状态：$isLocationEnabled"
            }

            override fun onLocationChanged(location: Location) {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val second = calendar.get(Calendar.SECOND)
                _binding.tvLocation.text =
                    String.format("[%02d:%02d:%02d]: %s", hour, minute, second, location.format())
            }

            override fun onLocationServiceStateChanged(state: LocationServiceState) {
                _binding.tvLocation.text = "当前服务状态：$state"
                when (state) {
                    LocationServiceState.Connecting -> {
                        _binding.btnStart.isEnabled = false
                        _binding.btnStop.isEnabled = false
                    }

                    LocationServiceState.Connected -> {
                        _binding.btnStart.isEnabled = false
                        _binding.btnStop.isEnabled = true
                    }

                    LocationServiceState.Disconnected -> {
                        _binding.btnStart.isEnabled = true
                        _binding.btnStop.isEnabled = false
                    }
                }
            }

            override fun onLocationError(message: String, throwable: Throwable?) {
                super.onLocationError(message, throwable)
                _binding.tvLocation.text = message
            }
        }

        _binding.btnStart.setOnClickListener {
            if (client.hasPermission()) return@setOnClickListener client.start()
            Toast.makeText(it.context, "请求位置权限", Toast.LENGTH_SHORT).show()
            client.requestPermission()
        }

        _binding.btnStop.setOnClickListener {
            client.destroy()
        }

        val location = MagicalLocationManager.getLastLocation(this)
        _binding.tvLastLocation.text = "最后一次位置：${location.format()}"
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