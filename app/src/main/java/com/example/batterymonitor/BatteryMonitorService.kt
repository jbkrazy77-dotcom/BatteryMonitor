package com.example.batterymonitor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.media.ToneGenerator
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class BatteryMonitorService : Service() {

    private val CHANNEL_ID = "battery_monitor_channel"
    private val NOTIFICATION_ID = 1
    private var toneGenerator: ToneGenerator? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isLowBattery = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        toneGenerator = ToneGenerator(ToneGenerator.TONE_PROP_BEEP, 80)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Começa a checar a bateria a cada 30 segundos
        handler.post(monitorRunnable)

        return START_STICKY
    }

    private val monitorRunnable = object : Runnable {
        override fun run() {
            checkBatteryLevel()
            handler.postDelayed(this, 30000) // 30 segundos
        }
    }

    private fun checkBatteryLevel() {
        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else 0
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        // Beep quando estiver abaixo de 10% e não estiver carregando
        if (batteryPct <= 10 && !isCharging) {
            if (!isLowBattery) {
                isLowBattery = true
            }
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 600)
        } else {
            isLowBattery = false
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Battery Monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BatteryMonitor")
            .setContentText("Monitorando bateria em segundo plano")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(monitorRunnable)
        toneGenerator?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
