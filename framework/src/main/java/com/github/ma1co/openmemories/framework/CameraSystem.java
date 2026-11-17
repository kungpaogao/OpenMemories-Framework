package com.github.ma1co.openmemories.framework;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.NfcAdapter;
import android.os.BatteryManager;
import android.os.Bundle;
import com.sony.scalar.sysutil.didep.Bluetooth;
import com.sony.scalar.sysutil.didep.Gps;
import com.sony.scalar.sysutil.didep.Kikilog;
import com.sony.scalar.sysutil.didep.Media;
import com.sony.scalar.sysutil.didep.Nfc;
import com.sony.scalar.sysutil.didep.Power;
import com.sony.scalar.sysutil.didep.ScalarSystemManager;
import com.sony.scalar.sysutil.didep.Settings;
import com.sony.scalar.sysutil.didep.Temperature;

/**
 * Power, temperature, keys, GPS, NFC, Bluetooth, and the memory card.
 * On a camera this calls the {@code didep} classes. Elsewhere it uses the Android system services.
 */
public class CameraSystem {
    /** Sony system services. */
    public static class DeviceSystem extends CameraSystem {
        private final Power power;
        private final Temperature temperature;
        private final Gps gps;
        private int gpsFixState = GPS_OFF;
        private final Nfc nfc;
        private final Bluetooth bluetooth;
        private final Media media;

        private DeviceSystem(Context context) {
            super(context);
            power = Power.getInstance();
            temperature = new Temperature();
            gps = new Gps();
            nfc = new Nfc();
            bluetooth = new Bluetooth();
            media = new Media();
        }

        @Override
        public int getAutoPowerOffSeconds() {
            return Settings.getAutoPowerOffTime();
        }

        @Override
        public void setAutoPowerOffSeconds(int seconds) {
            Settings.setAutoPowerOffTime(seconds);
        }

        @Override
        public String getTargetMediaId() {
            return Settings.getTargetMediaId();
        }

        @Override
        public void setKeyLock(boolean locked) {
            super.setKeyLock(locked);
            Settings.setKeyLock(locked ? Settings.KeyLockInfo.KEYLOCK_ON : Settings.KeyLockInfo.KEYLOCK_OFF);
        }

        @Override
        public boolean isSystemReady() {
            return ScalarSystemManager.isSystemReady();
        }

        @Override
        public boolean requestPowerOff() {
            return ScalarSystemManager.requestPowerOff(ScalarSystemManager.OFF_FACTOR_KEY, 0, 0);
        }

        @Override
        public boolean resetAll() {
            return ScalarSystemManager.resetAll();
        }

        @Override
        public void setBatteryListener(final BatteryListener listener) {
            power.setBatteryRemainTimeListener(new Power.BatteryRemainTimeListener() {
                @Override public void onChanged(int remain) {
                    if (listener != null)
                        listener.onRemainTime(remain);
                }
            });
        }

        @Override
        public int getTemperatureStatus() {
            Temperature.Status status = temperature.getStatus();
            if (status == null)
                return Temperature.Status.STAT_UNKNOWN;
            return status.get(Temperature.Status.ID_BOX);
        }

        @Override
        public void setTemperatureListener(final TemperatureListener listener) {
            temperature.setStatusCallback(new Temperature.StatusCallback() {
                @Override public void onTempStatusChanged() {
                    if (listener != null)
                        listener.onStatus(getTemperatureStatus());
                }
            });
            temperature.setCountDownInfoListener(new Temperature.CountDownInfoListener() {
                @Override public void onChanged(Temperature.CountDownInfo info) {
                    if (listener != null && info != null)
                        listener.onShutdownCountdown(info.enabled, info.countDownSeconds);
                }
            });
        }

        @Override
        public int getGpsPower() {
            return gps.getGpsPowerState();
        }

        @Override
        public int getGpsState() {
            return gpsFixState;
        }

        @Override
        public void startGps() {
            gps.setGpsInfoListener(new Gps.GpsInfoListener() {
                @Override public void onChanged(Gps.gpsInfo info) {
                    if (info != null)
                        gpsFixState = info.state;
                }
            });
            gps.start();
        }

        @Override
        public void stopGps() {
            gps.stop();
            gpsFixState = GPS_OFF;
        }

        @Override
        public boolean isNfcEnabled() {
            return nfc.isEnabled();
        }

        @Override
        public void startNfc() {
            nfc.start();
        }

        @Override
        public void stopNfc() {
            nfc.stop();
        }

        @Override
        public boolean isBluetoothOn() {
            Bluetooth.BluetoothInfo info = bluetooth.getBluetoothInfo();
            return info != null && info.power == Bluetooth.BluetoothInfo.POWER_ON;
        }

        @Override
        public boolean isBluetoothConnected() {
            Bluetooth.BluetoothInfo info = bluetooth.getBluetoothInfo();
            return info != null && info.connect == Bluetooth.BluetoothInfo.CONNECT_CONNECTED;
        }

        @Override
        public void formatCard() {
            media.executeFormat();
        }

        @Override
        public void recoverCard(int mode) {
            media.execRecovery(mode);
        }

        @Override
        public void log(int type) {
            Kikilog.setUserLog(type, new Kikilog.Options());
        }

        @Override
        public void release() {
            if (power != null)
                power.release();
            if (temperature != null)
                temperature.release();
            if (gps != null) {
                gps.releaseGpsInfoListener();
                gps.release();
            }
            if (nfc != null)
                nfc.release();
            if (bluetooth != null)
                bluetooth.release();
            if (media != null)
                media.release();
        }
    }

    public static final int TEMP_UNKNOWN = 0;
    public static final int TEMP_NORMAL = 1;
    public static final int TEMP_PREHIGH = 2;
    public static final int TEMP_NEARHIGH = 3;
    public static final int TEMP_HIGH = 4;
    public static final int GPS_POWER_OFF = 0;
    public static final int GPS_POWER_ON = 1;
    /** Fix state: the receiver is off. */
    public static final int GPS_OFF = 1;
    /** Fix state: a position is locked. */
    public static final int GPS_FIX = 4;
    public static final int CARD_RECOVERY = 0;
    public static final int CARD_SALVAGE = 1;
    public static final int CARD_REPAIR = 2;

    public interface BatteryListener {
        void onRemainTime(int seconds);
    }

    public interface TemperatureListener {
        void onStatus(int status);
        void onShutdownCountdown(boolean enabled, long seconds);
    }

    private final Context context;
    private int autoPowerOffSeconds = 300;
    private boolean keyLock;
    private int gpsState = GPS_OFF;
    private int gpsPower = GPS_POWER_OFF;
    private LocationManager locationManager;
    private LocationListener locationListener;

    public static CameraSystem open(Context context) {
        if (DeviceInfo.getInstance().isCamera())
            return new DeviceSystem(context);
        return new CameraSystem(context);
    }

    /** Tells the camera OS not to auto-power-off while this app is in front. Ignored off-camera. */
    public static void setAutoPowerOffEnabled(Context context, boolean enabled) {
        if (context == null)
            return;
        Intent intent = new Intent("com.android.server.DAConnectionManagerService.apo");
        intent.putExtra("apo_info", enabled ? "APO/NORMAL" : "APO/NO");
        context.sendBroadcast(intent);
    }

    /** Registers the app with the camera OS. Ignored off-camera, aside from the broadcast. */
    public static void notifyRunning(Context context) {
        if (context == null)
            return;
        Intent intent = new Intent("com.android.server.DAConnectionManagerService.AppInfoReceive");
        intent.putExtra("cycleTime", "APO/NO");
        context.sendBroadcast(intent);
    }

    private CameraSystem(Context context) {
        this.context = context == null ? null : context.getApplicationContext();
    }

    public int getAutoPowerOffSeconds() {
        return autoPowerOffSeconds;
    }

    public void setAutoPowerOffSeconds(int seconds) {
        autoPowerOffSeconds = seconds;
    }

    public String getTargetMediaId() {
        return "external";
    }

    public void setKeyLock(boolean locked) {
        keyLock = locked;
    }

    public boolean isKeyLocked() {
        return keyLock;
    }

    public boolean isSystemReady() {
        return true;
    }

    public boolean requestPowerOff() {
        return false;
    }

    public boolean resetAll() {
        return false;
    }

    public int getBatteryPercent() {
        if (context == null)
            return -1;
        Intent battery = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (battery == null)
            return -1;
        int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level < 0 || scale <= 0)
            return -1;
        return level * 100 / scale;
    }

    public void setBatteryListener(BatteryListener listener) {
        if (listener != null)
            listener.onRemainTime(getBatteryPercent());
    }

    public int getTemperatureStatus() {
        return TEMP_NORMAL;
    }

    public void setTemperatureListener(TemperatureListener listener) {
        if (listener != null)
            listener.onStatus(TEMP_NORMAL);
    }

    /** {@link #GPS_POWER_OFF} or {@link #GPS_POWER_ON}. */
    public int getGpsPower() {
        return gpsPower;
    }

    /** {@link #GPS_OFF}, {@link #GPS_FIX}, or another camera fix state. */
    public int getGpsState() {
        return gpsState;
    }

    public void startGps() {
        if (context == null)
            return;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return;
        locationListener = new LocationListener() {
            @Override public void onLocationChanged(Location location) {
                gpsState = GPS_FIX;
            }
            @Override public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            @Override public void onProviderEnabled(String provider) {
            }
            @Override public void onProviderDisabled(String provider) {
                gpsState = GPS_OFF;
            }
        };
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, locationListener);
            gpsPower = GPS_POWER_ON;
        } catch (RuntimeException ignored) {
            gpsState = GPS_OFF;
            gpsPower = GPS_POWER_OFF;
        }
    }

    public void stopGps() {
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
        locationListener = null;
        gpsState = GPS_OFF;
        gpsPower = GPS_POWER_OFF;
    }

    public boolean isNfcEnabled() {
        if (context == null)
            return false;
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(context);
        return adapter != null && adapter.isEnabled();
    }

    public void startNfc() {
    }

    public void stopNfc() {
    }

    public boolean isBluetoothOn() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    public boolean isBluetoothConnected() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null)
            return false;
        return adapter.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothProfile.STATE_CONNECTED
                || adapter.getProfileConnectionState(BluetoothProfile.A2DP) == BluetoothProfile.STATE_CONNECTED;
    }

    public void formatCard() {
    }

    public void recoverCard(int mode) {
    }

    public void log(int type) {
    }

    public void release() {
        stopGps();
    }
}
