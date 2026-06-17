package com.example.batterymonitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var batteryLevelText: TextView
    private lateinit var batteryProgress: ProgressBar
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private var batteryReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        batteryLevelText = findViewById(R.id.batteryLevelText)
        batteryProgress = findViewById(R.id.batteryProgress)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)

        registerBatteryReceiver()
        updateBatteryInfo()

        startButton.setOnClickListener {
            val serviceIntent = Intent(this, BatteryMonitorService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        }

        stopButton.setOnClickListener {
            val serviceIntent = Intent(this, BatteryMonitorService::class.java)
            stopService(serviceIntent)
        }
    }

    private fun registerBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    updateBatteryInfo()
                }
            }
        }
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
    }

    private fun updateBatteryInfo() {
        val batteryIntent = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { intentFilter ->
            registerReceiver(null, intentFilter)
        }
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

        batteryLevelText.text = getString(R.string.battery_level, batteryPct)
        batteryProgress.progress = batteryPct
    }

    override fun onDestroy() {
        super.onDestroy()
        batteryReceiver?.let { unregisterReceiver(it) }
    }
}
