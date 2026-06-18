package com.example.batterymonitor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

public class BatteryMonitorService extends Service {

    private static final String CHANNEL_ID = "battery_monitor_channel";
    private static final int NOTIFICATION_ID = 1;
    private PowerManager.WakeLock wakeLock;
    private ToneGenerator toneGenerator;
    private Handler handler;
    private boolean isLowBattery = false;
    private BroadcastReceiver receiver;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        handler = new Handler(Looper.getMainLooper());
        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BatteryMonitor::WakeLock");
        wakeLock.acquire(10 * 60 * 1000L);

        registerBatteryReceiver();
        handler.post(monitorRunnable);

        return START_STICKY;
    }

    private void registerBatteryReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    updateBatteryStatus(intent);
                }
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    private final Runnable monitorRunnable = new Runnable() {
        @Override
        public void run() {
            checkBatteryLevel();
            handler.postDelayed(this, 30000);
        }
    };

    private void checkBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryIntent = registerReceiver(null, ifilter);
        
        if (batteryIntent != null) {
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = (level >= 0 && scale > 0) ? (level * 100 / scale) : 0;
            int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || 
                                status == BatteryManager.BATTERY_STATUS_FULL);

            if (batteryPct <= 5 && !isCharging) {
                if (!isLowBattery) {
                    isLowBattery = true;
                    try {
                        if (toneGenerator != null) {
                            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 500);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                isLowBattery = false;
            }
        }
    }

    private void updateBatteryStatus(Intent intent) {
        // Atualizar status de bateria
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = getString(R.string.notification_channel_name);
            String descriptionText = "Monitoramento de bateria";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(descriptionText);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(monitorRunnable);
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        if (wakeLock != null) {
            wakeLock.release();
        }
        if (toneGenerator != null) {
            toneGenerator.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
