package com.coding.dynasty.systeminfo;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private List<SystemInfo> systemInfoList;
    private SystemInfoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("System Info");

        // Initialize RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize data
        systemInfoList = new ArrayList<>();
        initializeData();

        // Set up adapter
        adapter = new SystemInfoAdapter(systemInfoList);
        recyclerView.setAdapter(adapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to the activity
        initializeData();
        adapter.notifyDataSetChanged(); // Notify adapter of data change
    }

    private void initializeData() {
        systemInfoList.clear(); // Clear existing data
        try {
            systemInfoList.add(new SystemInfo("CPU Info", new StringBuilder(getCpuInfo())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            systemInfoList.add(new SystemInfo("RAM Info", new StringBuilder(getRamInfo().toString())));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            systemInfoList.add(new SystemInfo("Storage Info", new StringBuilder(getStorageInfo().toString())));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            systemInfoList.add(new SystemInfo("Battery Info", new StringBuilder(getBatteryInfo().toString())));
        }

        systemInfoList.add(new SystemInfo("Network Info", new StringBuilder(getNetworkInfo().toString())));
    }

    private String getCpuInfo() throws IOException {
        StringBuilder cpuInfo = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("cat /proc/cpuinfo");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                cpuInfo.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            Log.d(e.getMessage(), Objects.requireNonNull(e.getMessage()));
            throw e;
        }
        return cpuInfo.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private StringBuilder getRamInfo() {
        StringBuilder ramInfo = new StringBuilder();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        long totalMem = memoryInfo.totalMem;
        long availMem = memoryInfo.availMem;
        long threshold = memoryInfo.threshold;
        boolean lowMemory = memoryInfo.lowMemory;

        ramInfo.append("Total RAM: ").append(formatRamSize(totalMem)).append("\n");
        ramInfo.append("Available RAM: ").append(formatRamSize(availMem)).append("\n");
        ramInfo.append("Threshold: ").append(formatRamSize(threshold)).append("\n");
        ramInfo.append("Low Memory: ").append(lowMemory).append("\n");

        return ramInfo;
    }

    private String formatRamSize(long size) {
        String suffix = null;
        float sizeFloat = size;

        if (size >= 1024) {
            suffix = "KB";
            sizeFloat /= 1024;
            if (sizeFloat >= 1024) {
                suffix = "MB";
                sizeFloat /= 1024;
                if (sizeFloat >= 1024) {
                    suffix = "GB";
                    sizeFloat /= 1024;
                }
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Float.toString(sizeFloat));

        int commaOffset = resultBuffer.indexOf(".");
        int length = commaOffset + 1;
        if (length < resultBuffer.length()) {
            resultBuffer.setLength(length + 1);
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private StringBuilder getBatteryInfo() {
        StringBuilder batteryInfo = new StringBuilder();
        getSystemService(Context.BATTERY_SERVICE);
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        assert batteryStatus != null;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int cycleCount = batteryStatus.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1);
        int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        int plugged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean present = batteryStatus.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        String technology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
        int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
        int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

        int batteryPct = (int) (level / (float) scale * 100);

        batteryInfo.append("Battery Level: ").append(batteryPct).append("%\n");
        batteryInfo.append("Cycle Count: ").append(cycleCount).append("\n");
        batteryInfo.append("Health: ").append(getHealthString(health)).append("\n");
        batteryInfo.append("Plugged: ").append(getPluggedString(plugged)).append("\n");
        batteryInfo.append("Present: ").append(present).append("\n");
        batteryInfo.append("Status: ").append(getStatusString(status)).append("\n");
        batteryInfo.append("Technology: ").append(technology).append("\n");
        batteryInfo.append("Temperature: ").append(temperature / 10.0).append("°C\n");
        batteryInfo.append("Voltage: ").append(voltage).append("mV\n");

        return batteryInfo;
    }

    private String getHealthString(int health) {
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_COLD:
                return "Cold";
            case BatteryManager.BATTERY_HEALTH_DEAD:
                return "Dead";
            case BatteryManager.BATTERY_HEALTH_GOOD:
                return "Good";
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                return "Overheat";
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                return "Over Voltage";
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                return "Unspecified Failure";
            default:
                return "Unknown";
        }
    }

    private String getPluggedString(int plugged) {
        switch (plugged) {
            case BatteryManager.BATTERY_PLUGGED_AC:
                return "AC";
            case BatteryManager.BATTERY_PLUGGED_USB:
                return "USB";
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                return "Wireless";
            case BatteryManager.BATTERY_PLUGGED_DOCK:
                return "Dock";
            default:
                return "Battery";
        }
    }

    private String getStatusString(int status) {
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                return "Charging";
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                return "Discharging";
            case BatteryManager.BATTERY_STATUS_FULL:
                return "Full";
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                return "Not Charging";
            default:
                return "Unknown";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private StringBuilder getStorageInfo() {
        StringBuilder storageInfo = new StringBuilder();

        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        long totalBytes = statFs.getTotalBytes();
        long availableBytes = statFs.getAvailableBytes();
        long freeBytes = statFs.getFreeBytes();

        storageInfo.append("Total Storage: ").append(formatStorageSize(totalBytes)).append("\n");
        storageInfo.append("Available Storage: ").append(formatStorageSize(availableBytes)).append("\n");
        storageInfo.append("Free Storage: ").append(formatStorageSize(freeBytes)).append("\n");

        return storageInfo;
    }

    private String formatStorageSize(long size) {
        String suffix = null;
        float sizeFloat = size;

        if (size >= 1024) {
            suffix = "KB";
            sizeFloat /= 1024;
            if (sizeFloat >= 1024) {
                suffix = "MB";
                sizeFloat /= 1024;
                if (sizeFloat >= 1024) {
                    suffix = "GB";
                    sizeFloat /= 1024;
                }
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Float.toString(sizeFloat));

        int commaOffset = resultBuffer.indexOf(".");
        int length = commaOffset + 1;
        if (length < resultBuffer.length()) {
            resultBuffer.setLength(length + 1);
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    private StringBuilder getNetworkInfo() {
        StringBuilder networkInfo = new StringBuilder();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        Network[] networks = cm.getAllNetworks();
        for (Network network : networks) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            if (capabilities != null) {
                networkInfo.append("Network: ").append(getNetworkType(capabilities)).append("\n");
                networkInfo.append("Connected: ").append(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)).append("\n");
                networkInfo.append("IPv4 Address: ").append(getIpAddress(network, "ipv4")).append("\n");
                networkInfo.append("IPv6 Address: ").append(getIpAddress(network, "ipv6")).append("\n");
                networkInfo.append("\n");
            }
        }

        return networkInfo;
    }

    private String getNetworkType(NetworkCapabilities capabilities) {
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return "WiFi";
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return "Cellular";
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return "Ethernet";
        } else {
            return "Unknown";
        }
    }

    private String getIpAddress(Network network, String type) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);

        if (capabilities != null) {
            switch (type) {
                case "ipv4":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // Example of handling potential null with a fallback value
                        return Objects.requireNonNullElse(capabilities.getTransportInfo(), "Unknown").toString();
                    }
                case "ipv6":
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        return Objects.requireNonNullElse(capabilities.getTransportInfo(), "Unknown").toString();
                    }
                default:
                    return "Unknown";
            }
        }
        return "Unknown";
    }


}
