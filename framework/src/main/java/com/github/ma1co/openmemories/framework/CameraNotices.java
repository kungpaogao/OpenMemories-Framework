package com.github.ma1co.openmemories.framework;

import com.sony.scalar.sysutil.didep.Caution;

/**
 * Camera warning lights and on-screen cautions.
 * Factor and trigger ids are the values the camera OS already uses.
 * Calls are ignored off a camera.
 */
public final class CameraNotices {
    private CameraNotices() {
    }

    public static void setEnabled(int cautionId, boolean enabled) {
        if (DeviceInfo.getInstance().isCamera())
            DeviceNotices.setEnabled(cautionId, enabled);
    }

    public static void setFactor(int factorId, boolean active) {
        if (DeviceInfo.getInstance().isCamera())
            DeviceNotices.setFactor(factorId, active);
    }

    public static void setTrigger(int factorId, int triggerId, boolean active) {
        if (DeviceInfo.getInstance().isCamera())
            DeviceNotices.setTrigger(factorId, triggerId, active);
    }

    /** Sony caution calls. Loaded only on a camera. */
    public static class DeviceNotices {
        static void setEnabled(int cautionId, boolean enabled) {
            Caution.EnableCaution(cautionId, enabled);
        }

        static void setFactor(int factorId, boolean active) {
            Caution.SetFactor(factorId, active);
        }

        static void setTrigger(int factorId, int triggerId, boolean active) {
            Caution.SetTrigger(factorId, triggerId, active);
        }
    }
}
