package com.github.ma1co.openmemories.framework;

import android.view.KeyEvent;
import com.sony.scalar.sysutil.ScalarInput;

/**
 * Physical camera keys. The numeric values match {@code ScalarInput} scan codes.
 * {@link #fromKeyEvent(KeyEvent)} returns the scan code on a camera and a mapped code on a phone or emulator.
 */
public final class CameraKeys {
    public static final int UP = 103;
    public static final int DOWN = 108;
    public static final int LEFT = 105;
    public static final int RIGHT = 106;
    public static final int ENTER = 232;
    public static final int MENU = 514;
    public static final int SOFT_KEY_1 = 229;
    public static final int DELETE = 595;
    public static final int SOFT_KEY_2 = 513;
    public static final int SHUTTER_HALF = 516;
    public static final int SHUTTER_HALF_2 = 517;
    public static final int SHUTTER = 518;
    public static final int MOVIE = 515;
    public static final int FUNCTION = 520;
    public static final int AEL = 532;
    public static final int PLAY = 207;
    public static final int CUSTOM_1 = 622;
    public static final int CUSTOM_2 = 623;
    public static final int CUSTOM_3 = 659;
    public static final int LENS_ATTACH = 530;
    public static final int MODE_DIAL = 572;
    public static final int DIAL_1_STATUS = 524;
    public static final int DIAL_1_CLOCKWISE = 525;
    public static final int DIAL_1_COUNTERCLOCKWISE = 526;
    public static final int DIAL_2_STATUS = 527;
    public static final int DIAL_2_CLOCKWISE = 528;
    public static final int DIAL_2_COUNTERCLOCKWISE = 529;
    public static final int RING_STATUS = 647;
    public static final int RING_CLOCKWISE = 648;
    public static final int RING_COUNTERCLOCKWISE = 649;
    public static final int STATUS_OFF = 0;
    public static final int STATUS_ON = 1;

    /** Reads a dial or key through {@code ScalarInput}. */
    public static class DeviceKeys {
        static int status(int scanCode) {
            com.sony.scalar.sysutil.KeyStatus status = ScalarInput.getKeyStatus(scanCode);
            return status == null ? STATUS_OFF : status.status;
        }
    }

    private CameraKeys() {
    }

    /**
     * Current status of a dial or latching key. On a camera this is {@code KeyStatus.status}.
     * Dial positions used by PlayMemories are this value divided by 22.
     */
    public static int getStatus(int scanCode) {
        if (DeviceInfo.getInstance().isCamera())
            return DeviceKeys.status(scanCode);
        return STATUS_OFF;
    }

    /** Scan code on a camera, otherwise a camera key that matches the Android key code. */
    public static int fromKeyEvent(KeyEvent event) {
        if (event == null)
            return 0;
        if (DeviceInfo.getInstance().isCamera())
            return event.getScanCode();
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_UP:
                return UP;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return DOWN;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return LEFT;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return RIGHT;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                return ENTER;
            case KeyEvent.KEYCODE_MENU:
                return MENU;
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_DEL:
                return DELETE;
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_VOLUME_UP:
                return SHUTTER_HALF;
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                return SHUTTER;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                return MOVIE;
            default:
                return event.getKeyCode();
        }
    }
}
