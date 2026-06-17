package com.example.batterymonitor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

class BatteryService : Service() {

    private val CHANNEL_ID = "BatteryMonitorChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Battery Monitor")
            .setContentText("Monitoring battery status...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // O serviço ficará em execução em foreground para monitorar bateria
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Battery Monitor Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
