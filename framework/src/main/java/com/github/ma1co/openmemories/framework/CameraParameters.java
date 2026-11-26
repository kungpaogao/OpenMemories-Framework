package com.github.ma1co.openmemories.framework;

import android.hardware.Camera;
import com.sony.scalar.hardware.CameraEx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Camera parameters shared by the Sony body and the Android camera.
 * Change values, then call {@link #apply()} or {@link com.github.ma1co.openmemories.framework.Camera#setParameters(CameraParameters)}.
 * Sony-only controls are stored and ignored by the emulator, and they are sent to
 * {@code CameraEx.ParametersModifier} on a camera.
 */
public class CameraParameters {
    /**
     * Scene mode string for program auto (P). On a Sony camera this is the exposure mode;
     * {@link android.hardware.Camera.Parameters} uses the same value on an emulator.
     */
    public static final String SCENE_MODE_PROGRAM = "program-auto";
    /**
     * Scene mode string for aperture priority (A). On a Sony camera this is the exposure mode.
     */
    public static final String SCENE_MODE_APERTURE = "aperture-priority";
    /**
     * Scene mode string for shutter priority (S). On a Sony camera this is the exposure mode.
     */
    public static final String SCENE_MODE_SHUTTER = "shutter-speed";
    /**
     * Scene mode string for manual (M). On a Sony camera this is the exposure mode.
     */
    public static final String SCENE_MODE_MANUAL = "manual-exposure";
    /**
     * Scene mode string for intelligent auto. On a Sony camera this is the exposure mode.
     */
    public static final String SCENE_MODE_AUTO = "auto";

    /**
     * Parameter implementation used on Sony cameras.
     */
    public static class DeviceParameters extends CameraParameters {
        private final CameraEx cameraEx;
        private final CameraEx.ParametersModifier modifier;

        DeviceParameters(CameraEx cameraEx) {
            super(cameraEx.getNormalCamera());
            this.cameraEx = cameraEx;
            modifier = cameraEx.createParametersModifier(parameters);
        }

        @Override
        public void apply() {
            cameraEx.getNormalCamera().setParameters(parameters);
        }

        @Override
        public int getIso() {
            return modifier.getISOSensitivity();
        }

        @Override
        public void setIso(int iso) {
            modifier.setISOSensitivity(iso);
            super.setIso(iso);
        }

        @Override
        public List<Integer> getSupportedIso() {
            return integers(modifier.getSupportedISOSensitivities());
        }

        @Override
        public String getDriveMode() {
            return modifier.getDriveMode();
        }

        @Override
        public void setDriveMode(String mode) {
            modifier.setDriveMode(mode);
            super.setDriveMode(mode);
        }

        @Override
        public List<String> getSupportedDriveModes() {
            return strings(modifier.getSupportedDriveModes());
        }

        @Override
        public String getPictureEffect() {
            return modifier.getPictureEffect();
        }

        @Override
        public void setPictureEffect(String effect) {
            modifier.setPictureEffect(effect);
            super.setPictureEffect(effect);
        }

        @Override
        public List<String> getSupportedPictureEffects() {
            return strings(modifier.getSupportedPictureEffects());
        }

        @Override
        public String getImageSize() {
            return modifier.getImageSize();
        }

        @Override
        public void setImageSize(String size) {
            modifier.setImageSize(size);
            super.setImageSize(size);
        }

        @Override
        public List<String> getSupportedImageSizes() {
            return strings(modifier.getSupportedImageSizes());
        }

        @Override
        public String getAspectRatio() {
            return modifier.getImageAspectRatio();
        }

        @Override
        public void setAspectRatio(String ratio) {
            modifier.setImageAspectRatio(ratio);
            super.setAspectRatio(ratio);
        }

        @Override
        public List<String> getSupportedAspectRatios() {
            return strings(modifier.getSupportedImageAspectRatios());
        }

        @Override
        public String getMeteringMode() {
            return modifier.getMeteringMode();
        }

        @Override
        public void setMeteringMode(String mode) {
            modifier.setMeteringMode(mode);
            super.setMeteringMode(mode);
        }

        @Override
        public List<String> getSupportedMeteringModes() {
            return strings(modifier.getSupportedMeteringModes());
        }

        @Override
        public String getDroMode() {
            return modifier.getDROMode();
        }

        @Override
        public void setDroMode(String mode) {
            modifier.setDROMode(mode);
            super.setDroMode(mode);
        }

        @Override
        public int getContrast() {
            return modifier.getContrast();
        }

        @Override
        public void setContrast(int contrast) {
            modifier.setContrast(contrast);
            super.setContrast(contrast);
        }

        @Override
        public int getSaturation() {
            return modifier.getSaturation();
        }

        @Override
        public void setSaturation(int saturation) {
            modifier.setSaturation(saturation);
            super.setSaturation(saturation);
        }

        @Override
        public int getSharpness() {
            return modifier.getSharpness();
        }

        @Override
        public void setSharpness(int sharpness) {
            modifier.setSharpness(sharpness);
            super.setSharpness(sharpness);
        }

        @Override
        public String getFaceDetectionMode() {
            return modifier.getFaceDetectionMode();
        }

        @Override
        public void setFaceDetectionMode(String mode) {
            modifier.setFaceDetectionMode(mode);
            super.setFaceDetectionMode(mode);
        }

        @Override
        public String getAutoFocusMode() {
            return modifier.getAutoFocusMode();
        }

        @Override
        public void setAutoFocusMode(String mode) {
            modifier.setAutoFocusMode(mode);
            super.setAutoFocusMode(mode);
        }

        @Override
        public String getAutoExposureLock() {
            return modifier.getAutoExposureLock();
        }

        @Override
        public void setAutoExposureLock(String lock) {
            modifier.setAutoExposureLock(lock);
            super.setAutoExposureLock(lock);
        }

        @Override
        public boolean getAutoWhiteBalanceLock() {
            return modifier.getAutoWhiteBalanceLock();
        }

        @Override
        public void setAutoWhiteBalanceLock(boolean lock) {
            modifier.setAutoWhiteBalanceLock(lock);
            super.setAutoWhiteBalanceLock(lock);
        }

        @Override
        public boolean isAutoWhiteBalanceLockSupported() {
            return modifier.isAutoWhiteBalanceLockSupported();
        }

        @Override
        public String getAntiHandBlurMode() {
            return modifier.getAntiHandBlurMode();
        }

        @Override
        public void setAntiHandBlurMode(String mode) {
            modifier.setAntiHandBlurMode(mode);
            super.setAntiHandBlurMode(mode);
        }

        @Override
        public boolean getRemoteControlMode() {
            return modifier.getRemoteControlMode();
        }

        @Override
        public void setRemoteControlMode(boolean enabled) {
            modifier.setRemoteControlMode(enabled);
            super.setRemoteControlMode(enabled);
        }

        @Override
        public boolean isRemoteControlModeSupported() {
            return modifier.isSupportedRemoteControlMode();
        }

        @Override
        public int[] getRgbMatrix() {
            return modifier.getRGBMatrix();
        }

        @Override
        public void setRgbMatrix(int[] matrix) {
            modifier.setRGBMatrix(matrix);
            super.setRgbMatrix(matrix);
        }

        @Override
        public boolean isRgbMatrixSupported() {
            return modifier.isRGBMatrixSupported();
        }

        @Override
        public boolean getLongExposureNoiseReduction() {
            return modifier.getLongExposureNR();
        }

        @Override
        public String getColorSelectMode() {
            return modifier.getColorSelectMode();
        }

        @Override
        public void setColorSelectMode(String mode, int[] channels) {
            modifier.setColorSelectMode(mode, channels);
            super.setColorSelectMode(mode, channels);
        }

        @Override
        public boolean isSharpnessGainModeSupported() {
            return modifier.isSharpnessGainModeSupported();
        }

        @Override
        public boolean getSharpnessGainMode() {
            return modifier.getSharpnessGainMode();
        }

        @Override
        public void setSharpnessGainMode(boolean enabled) {
            modifier.setSharpnessGainMode(enabled);
            super.setSharpnessGainMode(enabled);
        }

        @Override
        public int getSharpnessGain() {
            return modifier.getSharpnessGain();
        }

        @Override
        public void setSharpnessGain(int gain) {
            modifier.setSharpnessGain(gain);
            super.setSharpnessGain(gain);
        }

        @Override
        public boolean isWhiteBalanceShiftModeSupported() {
            return modifier.isWhiteBalanceShiftModeSupported();
        }

        @Override
        public boolean getWhiteBalanceShiftMode() {
            return modifier.getWhiteBalanceShiftMode();
        }

        @Override
        public void setWhiteBalanceShiftMode(boolean enabled) {
            modifier.setWhiteBalanceShiftMode(enabled);
            super.setWhiteBalanceShiftMode(enabled);
        }

        @Override
        public int getWhiteBalanceShiftAmberBlue() {
            return modifier.getWhiteBalanceShiftLB();
        }

        @Override
        public void setWhiteBalanceShiftAmberBlue(int shift) {
            modifier.setWhiteBalanceShiftLB(shift);
            super.setWhiteBalanceShiftAmberBlue(shift);
        }

        @Override
        public int getWhiteBalanceShiftGreenMagenta() {
            return modifier.getWhiteBalanceShiftCC();
        }

        @Override
        public void setWhiteBalanceShiftGreenMagenta(int shift) {
            modifier.setWhiteBalanceShiftCC(shift);
            super.setWhiteBalanceShiftGreenMagenta(shift);
        }

        @Override
        public List<String> getSupportedColorDepthTypes() {
            return strings(modifier.getSupportedColorDepthTypes());
        }

        @Override
        public int getColorDepth(String channel) {
            return modifier.getColorDepth(channel);
        }

        @Override
        public void setColorDepth(String channel, int value) {
            modifier.setColorDepth(channel, value);
            super.setColorDepth(channel, value);
        }

        @Override
        public boolean isExtendedGammaSupported() {
            return modifier.isExtendedGammaTableSupported();
        }

        @Override
        public boolean isExposureShiftSupported() {
            return modifier.isPictureControlExposureShiftSupported();
        }

        @Override
        public int getExposureShift() {
            return modifier.getPictureControlExposureShift();
        }

        @Override
        public void setExposureShift(int shift) {
            modifier.setPictureControlExposureShift(shift);
            super.setExposureShift(shift);
        }

        @Override
        public String getFocusAreaMode() {
            return modifier.getFocusAreaMode();
        }

        @Override
        public int getFocusPointIndex() {
            return modifier.getFocusPointIndex();
        }

        @Override
        public void setLensCorrection(boolean enabled) {
            modifier.setLensCorrection(enabled);
            super.setLensCorrection(enabled);
        }

        @Override
        public void setLensCorrectionLevel(String tag, int level) {
            modifier.setLensCorrectionLevel(tag, level);
            super.setLensCorrectionLevel(tag, level);
        }

        @Override
        public List<String> getSupportedLensCorrections() {
            return strings(modifier.getSupportedLensCorrections());
        }

        @Override
        public int getColorTemperature() {
            return modifier.getColorTemperatureForWhiteBalance();
        }

        @Override
        public void setColorTemperature(int kelvin) {
            modifier.setColorTemperatureForWhiteBalance(kelvin);
            super.setColorTemperature(kelvin);
        }

        @Override
        public boolean isSilentShutter() {
            return modifier.isSilentShutterMode();
        }

        @Override
        public void setSilentShutter(boolean enabled) {
            modifier.setSilentShutterMode(enabled);
            super.setSilentShutter(enabled);
        }

        @Override
        public List<Integer> getSupportedPreviewMagnification() {
            return integers(modifier.getSupportedPreviewMagnification());
        }

        @Override
        public boolean getMfAssist() {
            return modifier.getPreviewMagnificationOnManualFocus();
        }

        @Override
        public int getMfAssistSeconds() {
            return modifier.getPreviewMagnificationTimeOnManualFocus();
        }

        @Override
        public int getAperture() {
            return modifier.getAperture();
        }

        @Override
        public int getShutterNumerator() {
            android.util.Pair speed = modifier.getShutterSpeed();
            if (speed == null || speed.first == null)
                return 0;
            return ((Integer) speed.first).intValue();
        }

        @Override
        public int getShutterDenominator() {
            android.util.Pair speed = modifier.getShutterSpeed();
            if (speed == null || speed.second == null)
                return 0;
            return ((Integer) speed.second).intValue();
        }
    }

    public static final String FOCUS_AUTO = "auto";
    public static final String FOCUS_CONTINUOUS = "continuous-picture";
    public static final String FOCUS_DMF = "dmf";
    public static final String FOCUS_MANUAL = "manual";
    public static final String FLASH_AUTO = "auto";
    public static final String FLASH_OFF = "off";
    public static final String FLASH_ON = "on";
    public static final String WHITE_BALANCE_AUTO = "auto";
    public static final String WHITE_BALANCE_DAYLIGHT = "daylight";
    public static final String WHITE_BALANCE_CLOUDY = "cloudy-daylight";
    public static final String WHITE_BALANCE_COLOR_TEMPERATURE = "color-temp";
    public static final String WHITE_BALANCE_CUSTOM = "custom";
    public static final String DRIVE_SINGLE = "single";
    public static final String DRIVE_BURST = "burst";
    public static final String DRIVE_BRACKET = "bracket";
    public static final String EFFECT_OFF = "off";
    public static final String EFFECT_TOY_CAMERA = "toy-camera";
    public static final String EFFECT_POP_COLOR = "pop-color";
    public static final String EFFECT_POSTERIZATION = "posterization";
    public static final String EFFECT_RETRO = "retro-photo";
    public static final String EFFECT_SOFT_HIGH_KEY = "soft-high-key";
    public static final String EFFECT_PART_COLOR = "part-color";
    public static final String EFFECT_SOFT_FOCUS = "soft-focus";
    public static final String EFFECT_MINIATURE = "miniature";
    public static final String ASPECT_3_2 = "3:2";
    public static final String ASPECT_16_9 = "16:9";
    public static final String ASPECT_4_3 = "4:3";
    public static final String ASPECT_1_1 = "1:1";
    public static final String METERING_MULTI = "multi";
    public static final String METERING_CENTER = "center-weighted";
    public static final String METERING_SPOT = "center-spot";
    public static final String DRO_OFF = "off";
    public static final String DRO_ON = "on";
    public static final String DRO_AUTO = "auto";
    public static final String AE_LOCK_ON = "locked";
    public static final String AE_LOCK_OFF = "unlocked";
    public static final String ANTI_BLUR_OFF = "off";
    public static final String ANTI_BLUR_SHOOT = "onetime";
    public static final String ANTI_BLUR_CONTINUOUS = "keep";
    public static final String COLOR_SELECT_EXTRACT = "extract";
    public static final String COLOR_SELECT_OFF = "off";
    public static final String COLOR_RED = "red";
    public static final String COLOR_GREEN = "green";
    public static final String COLOR_BLUE = "blue";
    public static final String COLOR_CYAN = "cyan";
    public static final String COLOR_MAGENTA = "magenta";
    public static final String COLOR_YELLOW = "yellow";

    protected final Camera camera;
    protected final Camera.Parameters parameters;
    private int iso = 100;
    private String driveMode = DRIVE_SINGLE;
    private String pictureEffect = EFFECT_OFF;
    private String imageSize;
    private String aspectRatio = ASPECT_3_2;
    private String meteringMode = METERING_MULTI;
    private String droMode = DRO_AUTO;
    private int contrast;
    private int saturation;
    private int sharpness;
    private String faceDetection = "off";
    private String autoFocusMode = FOCUS_AUTO;
    private String autoExposureLock = AE_LOCK_OFF;
    private boolean autoWhiteBalanceLock;
    private String antiHandBlur = ANTI_BLUR_CONTINUOUS;
    private boolean remoteControl;
    private int[] rgbMatrix;
    private String colorSelectMode = COLOR_SELECT_OFF;
    private int[] colorSelectChannels = new int[0];
    private boolean sharpnessGainMode;
    private int sharpnessGain;
    private boolean whiteBalanceShiftMode;
    private int whiteBalanceShiftLb;
    private int whiteBalanceShiftCc;
    private int exposureShift;
    private boolean lensCorrection = true;
    private int colorTemperature = 5500;
    private boolean silentShutter;
    private int aperture = 280;
    private int shutterNumerator = 1;
    private int shutterDenominator = 60;
    private final Map<String, Integer> colorDepth = new HashMap<String, Integer>();

    CameraParameters(android.hardware.Camera camera) {
        this.camera = camera;
        parameters = camera.getParameters();
    }

    /** Sends the current values to the camera. */
    public void apply() {
        camera.setParameters(parameters);
    }

    public String getWhiteBalance() {
        return parameters.getWhiteBalance();
    }

    public void setWhiteBalance(String whiteBalance) {
        try {
            parameters.setWhiteBalance(whiteBalance);
        } catch (RuntimeException ignored) {
        }
    }

    public List<String> getSupportedWhiteBalance() {
        return copy(parameters.getSupportedWhiteBalance());
    }

    public String getFlashMode() {
        return parameters.getFlashMode();
    }

    public void setFlashMode(String flashMode) {
        try {
            parameters.setFlashMode(flashMode);
        } catch (RuntimeException ignored) {
        }
    }

    public List<String> getSupportedFlashModes() {
        return copy(parameters.getSupportedFlashModes());
    }

    public String getFocusMode() {
        return parameters.getFocusMode();
    }

    public void setFocusMode(String focusMode) {
        try {
            parameters.setFocusMode(focusMode);
        } catch (RuntimeException ignored) {
        }
    }

    public List<String> getSupportedFocusModes() {
        return copy(parameters.getSupportedFocusModes());
    }

    public String getSceneMode() {
        return parameters.getSceneMode();
    }

    public void setSceneMode(String sceneMode) {
        try {
            parameters.setSceneMode(sceneMode);
        } catch (RuntimeException ignored) {
        }
    }

    public List<String> getSupportedSceneModes() {
        return copy(parameters.getSupportedSceneModes());
    }

    public int getExposureCompensation() {
        return parameters.getExposureCompensation();
    }

    public void setExposureCompensation(int steps) {
        try {
            parameters.setExposureCompensation(steps);
        } catch (RuntimeException ignored) {
        }
    }

    public int getMinExposureCompensation() {
        return parameters.getMinExposureCompensation();
    }

    public int getMaxExposureCompensation() {
        return parameters.getMaxExposureCompensation();
    }

    /**
     * EV step size for {@link #getExposureCompensation()} indices (e.g. 0.333 or 0.5).
     * Reads {@link android.hardware.Camera.Parameters#getExposureCompensationStep()} on both camera and emulator.
     */
    public float getExposureCompensationStep() {
        return parameters.getExposureCompensationStep();
    }

    public int getJpegQuality() {
        return parameters.getJpegQuality();
    }

    public void setJpegQuality(int quality) {
        try {
            parameters.setJpegQuality(quality);
        } catch (RuntimeException ignored) {
        }
    }

    public int getPictureWidth() {
        Camera.Size size = parameters.getPictureSize();
        return size == null ? 0 : size.width;
    }

    public int getPictureHeight() {
        Camera.Size size = parameters.getPictureSize();
        return size == null ? 0 : size.height;
    }

    public void setPictureSize(int width, int height) {
        try {
            parameters.setPictureSize(width, height);
        } catch (RuntimeException ignored) {
        }
    }

    public int getIso() {
        String value = parameters.get("iso");
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
            }
        }
        return iso;
    }

    public void setIso(int iso) {
        this.iso = iso;
        parameters.set("iso", Integer.toString(iso));
    }

    public List<Integer> getSupportedIso() {
        return Arrays.asList(Integer.valueOf(100), Integer.valueOf(200), Integer.valueOf(400), Integer.valueOf(800), Integer.valueOf(1600), Integer.valueOf(3200));
    }

    public String getDriveMode() {
        return driveMode;
    }

    public void setDriveMode(String mode) {
        driveMode = mode;
    }

    public List<String> getSupportedDriveModes() {
        return Arrays.asList(DRIVE_SINGLE, DRIVE_BURST, DRIVE_BRACKET);
    }

    public String getPictureEffect() {
        return pictureEffect;
    }

    public void setPictureEffect(String effect) {
        pictureEffect = effect;
    }

    public List<String> getSupportedPictureEffects() {
        return Arrays.asList(EFFECT_OFF, EFFECT_TOY_CAMERA, EFFECT_POP_COLOR, EFFECT_POSTERIZATION, EFFECT_RETRO, EFFECT_PART_COLOR, EFFECT_MINIATURE, EFFECT_SOFT_FOCUS, EFFECT_SOFT_HIGH_KEY);
    }

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String size) {
        imageSize = size;
    }

    public List<String> getSupportedImageSizes() {
        return new ArrayList<String>();
    }

    public String getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(String ratio) {
        aspectRatio = ratio;
    }

    public List<String> getSupportedAspectRatios() {
        return Arrays.asList(ASPECT_3_2, ASPECT_16_9, ASPECT_4_3, ASPECT_1_1);
    }

    public String getMeteringMode() {
        return meteringMode;
    }

    public void setMeteringMode(String mode) {
        meteringMode = mode;
    }

    public List<String> getSupportedMeteringModes() {
        return Arrays.asList(METERING_MULTI, METERING_CENTER, METERING_SPOT);
    }

    public String getDroMode() {
        return droMode;
    }

    public void setDroMode(String mode) {
        droMode = mode;
    }

    public int getContrast() {
        return contrast;
    }

    public void setContrast(int contrast) {
        this.contrast = contrast;
    }

    public int getSaturation() {
        return saturation;
    }

    public void setSaturation(int saturation) {
        this.saturation = saturation;
    }

    public int getSharpness() {
        return sharpness;
    }

    public void setSharpness(int sharpness) {
        this.sharpness = sharpness;
    }

    public String getFaceDetectionMode() {
        return faceDetection;
    }

    public void setFaceDetectionMode(String mode) {
        faceDetection = mode;
    }

    public String getAutoFocusMode() {
        return autoFocusMode;
    }

    public void setAutoFocusMode(String mode) {
        autoFocusMode = mode;
        setFocusMode(mode);
    }

    public String getAutoExposureLock() {
        return autoExposureLock;
    }

    public void setAutoExposureLock(String lock) {
        autoExposureLock = lock;
    }

    public boolean getAutoWhiteBalanceLock() {
        return autoWhiteBalanceLock;
    }

    public void setAutoWhiteBalanceLock(boolean lock) {
        autoWhiteBalanceLock = lock;
    }

    public boolean isAutoWhiteBalanceLockSupported() {
        return false;
    }

    public String getAntiHandBlurMode() {
        return antiHandBlur;
    }

    public void setAntiHandBlurMode(String mode) {
        antiHandBlur = mode;
    }

    public boolean getRemoteControlMode() {
        return remoteControl;
    }

    public void setRemoteControlMode(boolean enabled) {
        remoteControl = enabled;
    }

    public boolean isRemoteControlModeSupported() {
        return false;
    }

    public int[] getRgbMatrix() {
        return rgbMatrix == null ? null : rgbMatrix.clone();
    }

    public void setRgbMatrix(int[] matrix) {
        rgbMatrix = matrix == null ? null : matrix.clone();
    }

    public boolean isRgbMatrixSupported() {
        return false;
    }

    public boolean getLongExposureNoiseReduction() {
        return false;
    }

    public String getColorSelectMode() {
        return colorSelectMode;
    }

    public void setColorSelectMode(String mode, int[] channels) {
        colorSelectMode = mode;
        colorSelectChannels = channels == null ? new int[0] : channels.clone();
    }

    public int[] getColorSelectChannels() {
        return colorSelectChannels.clone();
    }

    public boolean isSharpnessGainModeSupported() {
        return false;
    }

    public boolean getSharpnessGainMode() {
        return sharpnessGainMode;
    }

    public void setSharpnessGainMode(boolean enabled) {
        sharpnessGainMode = enabled;
    }

    public int getSharpnessGain() {
        return sharpnessGain;
    }

    public void setSharpnessGain(int gain) {
        sharpnessGain = gain;
    }

    public boolean isWhiteBalanceShiftModeSupported() {
        return false;
    }

    public boolean getWhiteBalanceShiftMode() {
        return whiteBalanceShiftMode;
    }

    public void setWhiteBalanceShiftMode(boolean enabled) {
        whiteBalanceShiftMode = enabled;
    }

    public int getWhiteBalanceShiftAmberBlue() {
        return whiteBalanceShiftLb;
    }

    public void setWhiteBalanceShiftAmberBlue(int shift) {
        whiteBalanceShiftLb = shift;
    }

    public int getWhiteBalanceShiftGreenMagenta() {
        return whiteBalanceShiftCc;
    }

    public void setWhiteBalanceShiftGreenMagenta(int shift) {
        whiteBalanceShiftCc = shift;
    }

    public List<String> getSupportedColorDepthTypes() {
        return Arrays.asList(COLOR_RED, COLOR_GREEN, COLOR_BLUE, COLOR_CYAN, COLOR_MAGENTA, COLOR_YELLOW);
    }

    public int getColorDepth(String channel) {
        Integer value = colorDepth.get(channel);
        return value == null ? 0 : value.intValue();
    }

    public void setColorDepth(String channel, int value) {
        if (channel != null)
            colorDepth.put(channel, Integer.valueOf(value));
    }

    public boolean isExtendedGammaSupported() {
        return false;
    }

    public boolean isExposureShiftSupported() {
        return false;
    }

    public int getExposureShift() {
        return exposureShift;
    }

    public void setExposureShift(int shift) {
        exposureShift = shift;
    }

    public String getFocusAreaMode() {
        return "wide";
    }

    public int getFocusPointIndex() {
        return 0;
    }

    public void setLensCorrection(boolean enabled) {
        lensCorrection = enabled;
    }

    public boolean getLensCorrection() {
        return lensCorrection;
    }

    public void setLensCorrectionLevel(String tag, int level) {
    }

    public List<String> getSupportedLensCorrections() {
        return new ArrayList<String>();
    }

    public int getColorTemperature() {
        return colorTemperature;
    }

    public void setColorTemperature(int kelvin) {
        colorTemperature = kelvin;
    }

    public boolean isSilentShutter() {
        return silentShutter;
    }

    public void setSilentShutter(boolean enabled) {
        silentShutter = enabled;
    }

    /** Magnification levels for {@link com.github.ma1co.openmemories.framework.Camera#setPreviewMagnification(int, int, int)}. The emulator returns 2 and 3. */
    public List<Integer> getSupportedPreviewMagnification() {
        return Arrays.asList(Integer.valueOf(2), Integer.valueOf(3));
    }

    /** The camera's "MF Assist" menu setting: whether turning the focus ring magnifies the preview. The emulator returns false. */
    public boolean getMfAssist() {
        return false;
    }

    /** Seconds the camera keeps the MF Assist magnification, 0 for no limit. The emulator returns 0. */
    public int getMfAssistSeconds() {
        return 0;
    }

    public int getAperture() {
        return aperture;
    }

    public int getShutterNumerator() {
        return shutterNumerator;
    }

    public int getShutterDenominator() {
        return shutterDenominator;
    }

    private static List<String> copy(List<String> values) {
        if (values == null)
            return new ArrayList<String>();
        return new ArrayList<String>(values);
    }

    @SuppressWarnings("unchecked")
    private static List<String> strings(List values) {
        List<String> copy = new ArrayList<String>();
        if (values == null)
            return copy;
        for (Object value : values) {
            if (value != null)
                copy.add(String.valueOf(value));
        }
        return copy;
    }

    @SuppressWarnings("unchecked")
    private static List<Integer> integers(List values) {
        List<Integer> copy = new ArrayList<Integer>();
        if (values == null)
            return copy;
        for (Object value : values) {
            if (value instanceof Integer)
                copy.add((Integer) value);
        }
        return copy;
    }
}
