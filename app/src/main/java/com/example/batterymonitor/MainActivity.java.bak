package com.example.batterymonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private TextView batteryLevelText;
    private ProgressBar batteryProgress;
    private Button startButton;
    private Button stopButton;
    private BroadcastReceiver batteryReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        batteryLevelText = findViewById(R.id.batteryLevelText);
        batteryProgress = findViewById(R.id.batteryProgress);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        registerBatteryReceiver();
        updateBatteryInfo();

        startButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, BatteryMonitorService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
        });

        stopButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, BatteryMonitorService.class);
            stopService(serviceIntent);
        });
    }

    private void registerBatteryReceiver() {
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                    updateBatteryInfo();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
    }

    private void updateBatteryInfo() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryIntent = registerReceiver(null, ifilter);
        
        if (batteryIntent != null) {
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = (level >= 0 && scale > 0) ? (level * 100 / scale) : 0;

            batteryLevelText.setText(getString(R.string.battery_level, batteryPct));
            batteryProgress.setProgress(batteryPct);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (batteryReceiver != null) {
            unregisterReceiver(batteryReceiver);
        }
    }
}
