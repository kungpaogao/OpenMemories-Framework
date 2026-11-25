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
    public static final int AF_MF = 533;
    public static final int PLAY = 207;
    public static final int CUSTOM_1 = 622;
    public static final int CUSTOM_2 = 623;
    public static final int CUSTOM_3 = 659;
    public static final int LENS_ATTACH = 530;
    public static final int MODE_DIAL = 572;
    public static final int MODE_DIAL_AUTO = 535;
    public static final int MODE_DIAL_PROGRAM = 537;
    public static final int MODE_DIAL_SHUTTER = 538;
    public static final int MODE_DIAL_APERTURE = 539;
    public static final int MODE_DIAL_MANUAL = 540;
    public static final int WHEEL_STATUS = 521;
    public static final int WHEEL_CLOCKWISE = 522;
    public static final int WHEEL_COUNTERCLOCKWISE = 523;
    public static final int DIAL_1_STATUS = 524;
    public static final int DIAL_1_CLOCKWISE = 525;
    public static final int DIAL_1_COUNTERCLOCKWISE = 526;
    public static final int DIAL_2_STATUS = 527;
    public static final int DIAL_2_CLOCKWISE = 528;
    public static final int DIAL_2_COUNTERCLOCKWISE = 529;
    public static final int DIAL_3_STATUS = 633;
    public static final int DIAL_3_COUNTERCLOCKWISE = 634;
    public static final int DIAL_3_CLOCKWISE = 635;
    public static final int EV_DIAL = 620;
    public static final int IRIS_DIAL = 621;
    public static final int SLIDE_AEL_FOCUS = 589;
    public static final int AF_MF_AEL = 638;
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

        static int evDialPosition() {
            com.sony.scalar.sysutil.KeyStatus key = ScalarInput.getKeyStatus(EV_DIAL);
            if (key == null || key.valid != 1)
                return 0;
            int status = key.status;
            return (status & 512) == 512 ? -(status - 512) : status;
        }
    }

    private CameraKeys() {
    }

    /**
     * Current status of a dial or latching key. On a camera this is {@code KeyStatus.status}.
     * For {@link #DIAL_1_STATUS} and {@link #DIAL_2_STATUS}, PlayMemories dial positions are this value
     * divided by 22; the mode dial ({@link #MODE_DIAL}) and {@link #EV_DIAL} use the raw status.
     * On an emulator this always returns {@link #STATUS_OFF}.
     */
    public static int getStatus(int scanCode) {
        if (DeviceInfo.getInstance().isCamera())
            return DeviceKeys.status(scanCode);
        return STATUS_OFF;
    }

    /**
     * Position of the physical exposure-compensation dial in detent units (sign from bit 512).
     * On a camera this reads {@link #EV_DIAL} through {@code ScalarInput}; on an emulator this returns 0.
     */
    public static int getEvDialPosition() {
        if (DeviceInfo.getInstance().isCamera())
            return DeviceKeys.evDialPosition();
        return 0;
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
