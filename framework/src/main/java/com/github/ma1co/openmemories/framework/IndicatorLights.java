package com.github.ma1co.openmemories.framework;

import com.sony.scalar.hardware.avio.Cec;
import com.sony.scalar.hardware.indicator.Light;
import com.sony.scalar.hardware.indicator.SubLCD;

/**
 * Body LEDs, the top info LCD, and HDMI CEC.
 * Calls are ignored when the device is not a Sony camera.
 */
public final class IndicatorLights {
    public static final String LED_ANSWER = "LID_ANSWER_BACK";
    public static final String LED_MEDIA = "LID_MEDIA_ACCS_MMC0";
    public static final String LED_POWER = "LID_POWER";
    public static final String LED_RECORD = "LID_REC";
    public static final String LED_MOVIE = "LID_MOVIE_REC";
    public static final String LED_WIFI = "LID_WIFI";
    public static final String LED_SELF_TIMER = "LID_SELF_TIMER";
    public static final String PATTERN_ON = "PTN_ON";
    public static final String PATTERN_OFF = "PTN_OFF";
    public static final String PATTERN_FAST = "PTN_FAST";
    public static final String PATTERN_SLOW = "PTN_SLOW";
    public static final String PATTERN_MIDDLE = "PTN_MIDDLE";
    public static final int CEC_ONE_TOUCH_PLAY = 1;

    /** Camera indicators. */
    public static class DeviceIndicators {
        static void initialize() {
            Light.initialize();
        }

        static void set(String led, boolean on) {
            Light.setState(led, on);
        }

        static void set(String led, boolean on, String pattern) {
            Light.setState(led, on, pattern);
        }

        static void release() {
            Light.release();
        }

        static void subLcd(String text) {
            SubLCD.initialize();
            SubLCD.setString(text, true, null, null);
        }

        static void cec(int message) {
            Cec.issueMessage(message);
        }
    }

    private IndicatorLights() {
    }

    public static void initialize() {
        if (DeviceInfo.getInstance().isCamera())
            DeviceIndicators.initialize();
    }

    public static void set(String led, boolean on) {
        if (DeviceInfo.getInstance().isCamera())
            DeviceIndicators.set(led, on);
    }

    public static void set(String led, boolean on, String pattern) {
        if (DeviceInfo.getInstance().isCamera())
            DeviceIndicators.set(led, on, pattern);
    }

    public static void release() {
        if (DeviceInfo.getInstance().isCamera())
            DeviceIndicators.release();
    }

    public static void setSubLcd(String text) {
        if (DeviceInfo.getInstance().isCamera())
            DeviceIndicators.subLcd(text);
    }

    public static void hdmiOneTouchPlay() {
        if (DeviceInfo.getInstance().isCamera())
            DeviceIndicators.cec(CEC_ONE_TOUCH_PLAY);
    }
}
