package com.example.batterymonitor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.ToneGenerator
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class BatteryMonitorService : Service() {

    private val CHANNEL_ID = "battery_monitor_channel"
    private val NOTIFICATION_ID = 1
    private var wakeLock: PowerManager.WakeLock? = null
    private var toneGenerator: ToneGenerator? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isLowBattery = false
    private var receiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        toneGenerator = ToneGenerator(ToneGenerator.TONE_PROP_BEEP, 100)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Acquire wake lock
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BatteryMonitor::WakeLock")
        wakeLock?.acquire(10 * 60 * 1000L) // 10 min, but we'll renew

        registerBatteryReceiver()

        // Periodic check
        handler.post(monitorRunnable)

        return START_STICKY
    }

    private fun registerBatteryReceiver() {
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                updateBatteryStatus(intent)
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(receiver, filter)
    }

    private val monitorRunnable = object : Runnable {
        override fun run() {
            checkBatteryLevel()
            handler.postDelayed(this, 30000) // every 30 seconds
        }
    }

    private fun checkBatteryLevel() {
        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) level * 100 / scale else 0
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        if (batteryPct <= 5 && !isCharging) {
            if (!isLowBattery) {
                isLowBattery = true
            }
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 500)
        } else {
            isLowBattery = false
        }
    }

    private fun updateBatteryStatus(intent: Intent?) {
        // Could update UI if bound, but for now just for receiver
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = "Canal para monitoramento de bateria"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(monitorRunnable)
        receiver?.let { unregisterReceiver(it) }
        wakeLock?.release()
        toneGenerator?.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}