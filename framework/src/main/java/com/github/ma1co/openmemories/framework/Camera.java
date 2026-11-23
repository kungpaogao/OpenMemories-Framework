package com.github.ma1co.openmemories.framework;

import android.os.Handler;
import android.util.Pair;
import android.view.SurfaceHolder;
import com.sony.scalar.hardware.CameraEx;
import com.sony.scalar.meta.FaceInfo;
import com.sony.scalar.provider.AvindexStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Still and movie capture control.
 * On a Sony camera this drives {@code CameraEx}. On any other device it drives {@code android.hardware.Camera}.
 * Open with {@link #open(OpenOptions)} and call {@link #release()} from {@code onPause()}.
 */
public class Camera {
    /**
     * Camera implementation used on Sony cameras.
     */
    public static class DeviceCamera extends Camera {
        private CameraEx cameraEx;
        private final Map<GammaCurve, CameraEx.GammaTable> gammaTables = new IdentityHashMap<GammaCurve, CameraEx.GammaTable>();

        private DeviceCamera(OpenOptions options) {
            super(null);
            if (options == null)
                cameraEx = CameraEx.open(0, null);
            else
                cameraEx = CameraEx.open(0, toNativeOptions(options));
            installListeners();
        }

        com.sony.scalar.hardware.CameraEx nativeCamera() {
            return cameraEx;
        }

        private static CameraEx.OpenOptions toNativeOptions(OpenOptions options) {
            CameraEx.OpenOptions nativeOptions = new CameraEx.OpenOptions();
            if (options == null)
                return nativeOptions;
            nativeOptions.setPreview(options.preview);
            nativeOptions.setInheritSetting(options.inheritSetting);
            nativeOptions.setRecordingMode(options.recordingMode);
            String media = options.targetMedia;
            if (media == null) {
                String[] ids = AvindexStore.getExternalMediaIds();
                if (ids != null && ids.length > 0)
                    media = ids[0];
            }
            if (media != null)
                nativeOptions.setTargetMedia(media);
            return nativeOptions;
        }

        private void installListeners() {
            try {
                installShutterListener();
            } catch (Throwable ignored) {
            }
            try {
                installCaptureListener();
            } catch (Throwable ignored) {
            }
            // Only while a listener is set. The camera's own JPEG store is only verified with no CameraEx JPEG listener installed.
            if (jpegListener != null) {
                try {
                    installJpegListener();
                } catch (Throwable ignored) {
                }
            }
            try {
                installFocusListener();
            } catch (Throwable ignored) {
            }
            try {
                installApertureListener();
            } catch (Throwable ignored) {
            }
            try {
                installShutterSpeedListener();
            } catch (Throwable ignored) {
            }
            try {
                installZoomListener();
            } catch (Throwable ignored) {
            }
            try {
                installFaceListener();
            } catch (Throwable ignored) {
            }
            try {
                installStoreListener();
            } catch (Throwable ignored) {
            }
        }

        private void installShutterListener() {
            cameraEx.setShutterListener(new CameraEx.ShutterListener() {
                @Override public void onShutter(int status, CameraEx camera) {
                    if (shutterListener != null)
                        shutterListener.onShutter(status);
                }
            });
        }

        private void installCaptureListener() {
            cameraEx.setCaptureStatusListener(new CameraEx.OnCaptureStatusListener() {
                @Override public void onStart(int reason, CameraEx camera) {
                    if (captureStatusListener != null)
                        captureStatusListener.onStart(reason);
                }
                @Override public void onEnd(int reason, int extra, CameraEx camera) {
                    if (captureStatusListener != null)
                        captureStatusListener.onEnd(reason, extra);
                }
            });
        }

        private void installJpegListener() {
            cameraEx.setJpegListener(new CameraEx.JpegListener() {
                @Override public void onPictureTaken(byte[] data, CameraEx camera) {
                    deliverJpeg(data);
                }
            });
        }

        private void installFocusListener() {
            cameraEx.setFocusDriveListener(new CameraEx.FocusDriveListener() {
                @Override public void onChanged(CameraEx.FocusPosition position, CameraEx camera) {
                    if (focusListener != null && position != null)
                        focusListener.onFocus(position.currentPosition, position.maxPosition);
                }
            });
        }

        private void installApertureListener() {
            cameraEx.setApertureChangeListener(new CameraEx.ApertureChangeListener() {
                @Override public void onApertureChange(CameraEx.ApertureInfo info, CameraEx camera) {
                    if (apertureListener != null && info != null)
                        apertureListener.onAperture(info.currentAperture);
                }
            });
        }

        private void installShutterSpeedListener() {
            cameraEx.setShutterSpeedChangeListener(new CameraEx.ShutterSpeedChangeListener() {
                @Override public void onShutterSpeedChange(CameraEx.ShutterSpeedInfo info, CameraEx camera) {
                    if (shutterSpeedListener != null && info != null)
                        shutterSpeedListener.onShutterSpeed(info.currentShutterSpeed_n, info.currentShutterSpeed_d);
                }
            });
        }

        private void installZoomListener() {
            cameraEx.setZoomChangeListener(new CameraEx.ZoomChangeListener() {
                @Override public void onChanged(CameraEx.ZoomInfo info, CameraEx camera) {
                    if (zoomListener != null && info != null)
                        zoomListener.onZoom(info.opticalPosition, info.digitalPosition, info.stopped);
                }
            });
        }

        private void installFaceListener() {
            cameraEx.setFaceDetectionListener(new CameraEx.FaceDetectionListener() {
                @Override public void onFaceDetected(FaceInfo[] faces, CameraEx camera) {
                    if (faceListener != null)
                        faceListener.onFaces(toFaceRects(faces));
                }
            });
        }

        private void installStoreListener() {
            cameraEx.setStoreImageCompleteListener(new CameraEx.StoreImageCompleteListener() {
                @Override public void onDone(int status, CameraEx.StoreImageInfo info, CameraEx camera) {
                    if (storedImageListener != null)
                        storedImageListener.onStored(status, info == null ? null : info.DirectoryName, info == null ? null : info.FileName);
                }
            });
        }

        private static List<android.graphics.Rect> toFaceRects(FaceInfo[] faces) {
            List<android.graphics.Rect> rects = new ArrayList<android.graphics.Rect>();
            if (faces == null)
                return rects;
            for (int i = 0; i < faces.length; i++) {
                if (faces[i] != null && faces[i].rect != null)
                    rects.add(faces[i].rect);
            }
            return rects;
        }

        @Override
        public void release() {
            for (CameraEx.GammaTable table : gammaTables.values()) {
                if (table != null)
                    table.release();
            }
            gammaTables.clear();
            if (cameraEx != null) {
                // Without this the preview overlay stays held, and the next open fails with "Overlay Creation Failed".
                android.hardware.Camera normal = cameraEx.getNormalCamera();
                if (normal != null)
                    normal.release();
                cameraEx.release();
                cameraEx = null;
            }
        }

        /**
         * Installs the {@code CameraEx} JPEG listener only while {@code listener} is non-null. Pass null to remove it.
         */
        @Override
        public void setJpegListener(JpegListener listener) {
            super.setJpegListener(listener);
            if (cameraEx == null)
                return;
            if (listener != null)
                installJpegListener();
            else
                cameraEx.setJpegListener(null);
        }

        @Override
        public void reopen(final OpenOptions options, final ReopenCallback callback) {
            cameraEx.reopen(toNativeOptions(options), new CameraEx.OpenCallback() {
                @Override public void onReopened(CameraEx camera) {
                    cameraEx = camera;
                    installListeners();
                    if (callback != null)
                        callback.onReopened();
                }
            });
        }

        @Override
        public void setPreviewDisplay(SurfaceHolder holder) {
            try {
                cameraEx.getNormalCamera().setPreviewDisplay(holder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void startPreview() {
            cameraEx.getNormalCamera().startPreview();
        }

        @Override
        public void stopPreview() {
            cameraEx.getNormalCamera().stopPreview();
        }

        @Override
        public android.hardware.Camera getNormalCamera() {
            return cameraEx.getNormalCamera();
        }

        @Override
        public CameraParameters getParameters() {
            return new CameraParameters.DeviceParameters(cameraEx);
        }

        @Override
        public void burstableTakePicture() {
            cameraEx.burstableTakePicture();
        }

        @Override
        public void takePicture() {
            burstableTakePicture();
        }

        @Override
        public void cancelTakePicture() {
            cameraEx.cancelTakePicture();
        }

        @Override
        public void startDirectShutter() {
            cameraEx.startDirectShutter();
        }

        @Override
        public void stopDirectShutter(final DirectShutterCallback callback) {
            cameraEx.stopDirectShutter(new CameraEx.DirectShutterStoppedCallback() {
                @Override public void onShutterStopped(CameraEx camera) {
                    if (callback != null)
                        callback.onStopped();
                }
            });
        }

        @Override
        public void autoFocus() {
            cameraEx.getNormalCamera().autoFocus(new android.hardware.Camera.AutoFocusCallback() {
                @Override public void onAutoFocus(boolean success, android.hardware.Camera camera) {
                    if (autoFocusListener != null)
                        autoFocusListener.onAutoFocus(success);
                }
            });
        }

        @Override
        public void cancelAutoFocus() {
            cameraEx.getNormalCamera().cancelAutoFocus();
        }

        @Override
        public void startOneShotFocusDrive(int direction, int speed) {
            cameraEx.startOneShotFocusDrive(direction, speed);
        }

        @Override
        public void startFocusHold() {
            cameraEx.startFocusHold();
        }

        @Override
        public void stopFocusHold() {
            cameraEx.stopFocusHold();
        }

        @Override
        public void incrementShutterSpeed() {
            cameraEx.incrementShutterSpeed();
        }

        @Override
        public void decrementShutterSpeed() {
            cameraEx.decrementShutterSpeed();
        }

        @Override
        public void adjustShutterSpeed(int steps) {
            cameraEx.adjustShutterSpeed(steps);
        }

        @Override
        public void incrementAperture() {
            cameraEx.incrementAperture();
        }

        @Override
        public void decrementAperture() {
            cameraEx.decrementAperture();
        }

        @Override
        public void adjustAperture(int steps) {
            cameraEx.adjustAperture(steps);
        }

        @Override
        public boolean shiftFocus(int steps) {
            return cameraEx.shiftFocusPosition(steps);
        }

        @Override
        public void startSelfTimerShutter() {
            cameraEx.startSelfTimerShutter();
        }

        @Override
        public boolean cancelSelfTimerShutter() {
            return cameraEx.cancelSelfTimerShutter();
        }

        @Override
        public void cancelExposure() {
            cameraEx.cancelExposure();
        }

        @Override
        public void setPreviewMagnification(int ratio, int x, int y) {
            cameraEx.setPreviewMagnification(ratio, new Pair<Integer, Integer>(Integer.valueOf(x), Integer.valueOf(y)));
        }

        @Override
        public void stopPreviewMagnification() {
            cameraEx.stopPreviewMagnification();
        }

        @Override
        public Lens getLens() {
            CameraEx.LensInfo info = cameraEx.getLensInfo();
            if (info == null)
                return new Lens(null, null, 0, 0);
            int wide = info.FocalLength == null ? 0 : info.FocalLength.wide;
            int tele = info.FocalLength == null ? 0 : info.FocalLength.tele;
            return new Lens(info.LensName, info.LensType, wide, tele);
        }

        @Override
        public SelectedColor getPreviewColor(int x, int y) {
            CameraEx.SelectedColor color = cameraEx.getPreviewDisplayColor(x, y);
            if (color == null)
                return new SelectedColor();
            SelectedColor copy = new SelectedColor();
            copy.y = color.Y;
            copy.cb = color.Cb;
            copy.cr = color.Cr;
            copy.phase = color.Phase;
            copy.range = color.Range;
            copy.saturation = color.Saturation;
            return copy;
        }

        @Override
        public void setColorSelect(int channel, SelectedColor color) {
            CameraEx.SelectedColor nativeColor = new CameraEx.SelectedColor();
            if (color != null) {
                nativeColor.Y = color.y;
                nativeColor.Cb = color.cb;
                nativeColor.Cr = color.cr;
                nativeColor.Phase = color.phase;
                nativeColor.Range = color.range;
                nativeColor.Saturation = color.saturation;
            }
            cameraEx.setColorSelectToChannel(channel, nativeColor);
        }

        @Override
        public void setExtendedGamma(GammaCurve curve) {
            if (curve == null) {
                cameraEx.setExtendedGammaTable(null);
                return;
            }
            CameraEx.GammaTable table = gammaTables.get(curve);
            if (table == null) {
                table = cameraEx.createGammaTable();
                gammaTables.put(curve, table);
            }
            table.setPictureEffectGammaForceOff(curve.isPictureEffectGammaForceOff());
            if (curve.getSamples() != null) {
                ByteBuffer buffer = ByteBuffer.allocate(curve.getSamples().length * 2);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                for (int i = 0; i < curve.getSamples().length; i++)
                    buffer.putShort(curve.getSamples()[i]);
                table.write(new ByteArrayInputStream(buffer.array()));
            }
            cameraEx.setExtendedGammaTable(table);
        }

        @Override
        public Flash getExternalFlash() {
            CameraEx.ExternalFlashInfo info = cameraEx.getExternalFlashInfo();
            if (info == null)
                return new Flash(false, false, false, false);
            return new Flash(info.afLightAutoStatus, info.hssStatus, info.redEyeReductionStatus, info.wirelessStatus);
        }

        @Override
        public void startZoom(int direction, int speed) {
            cameraEx.startZoom(direction, speed);
        }

        @Override
        public void stopZoom() {
            cameraEx.stopZoom();
        }

        @Override
        public void startFaceDetection() {
            cameraEx.startFaceDetection(0);
        }

        @Override
        public void stopFaceDetection() {
            cameraEx.stopFaceDetection();
        }
    }

    /** Still recording mode passed to {@link OpenOptions#setRecordingMode(int)}. */
    public static final int RECORDING_STILL = 0;
    /** Movie recording mode passed to {@link OpenOptions#setRecordingMode(int)}. */
    public static final int RECORDING_MOVIE = 1;
    /** Manual-focus drive toward the camera. */
    public static final int FOCUS_DIRECTION_NEAR = 0;
    /** Manual-focus drive toward infinity. */
    public static final int FOCUS_DIRECTION_FAR = 1;
    /** Zoom toward telephoto; matches {@code CameraEx.ZOOM_DIRECTION_TELE}. */
    public static final int ZOOM_DIRECTION_TELE = 0;
    /** Zoom toward wide angle; matches {@code CameraEx.ZOOM_DIRECTION_WIDE}. */
    public static final int ZOOM_DIRECTION_WIDE = 1;
    /** Shutter listener status: the shutter fired. */
    public static final int SHUTTER_OK = 0;
    /** Shutter listener status: the shutter was canceled. */
    public static final int SHUTTER_CANCELED = 1;
    /** Shutter listener status: the shutter failed. */
    public static final int SHUTTER_ERROR = 2;
    /** Capture status: the capture completed. */
    public static final int CAPTURE_REASON_NORMAL = 1;
    /** Capture status: the capture was canceled. */
    public static final int CAPTURE_REASON_CANCEL = 2;

    /**
     * Options for {@link #open(OpenOptions)}.
     */
    public static class OpenOptions {
        private boolean preview = true;
        private boolean inheritSetting = true;
        private int recordingMode = RECORDING_STILL;
        private String targetMedia;

        public OpenOptions setPreview(boolean preview) {
            this.preview = preview;
            return this;
        }

        public OpenOptions setInheritSetting(boolean inheritSetting) {
            this.inheritSetting = inheritSetting;
            return this;
        }

        /** {@link #RECORDING_STILL} or {@link #RECORDING_MOVIE}. */
        public OpenOptions setRecordingMode(int recordingMode) {
            this.recordingMode = recordingMode;
            return this;
        }

        /** Media id from {@link MediaManager#getMediaIds()}. Ignored off-camera. */
        public OpenOptions setTargetMedia(String targetMedia) {
            this.targetMedia = targetMedia;
            return this;
        }
    }

    /** Color sampled from the live preview, in YCbCr. */
    public static class SelectedColor {
        public int y;
        public int cb;
        public int cr;
        public int phase;
        public int range;
        public int saturation;
    }

    /** Attached lens. Focal lengths are in millimetres at the wide and tele ends. */
    public static class Lens {
        public final String name;
        public final String type;
        public final int wideFocalLength;
        public final int teleFocalLength;

        public Lens(String name, String type, int wideFocalLength, int teleFocalLength) {
            this.name = name;
            this.type = type;
            this.wideFocalLength = wideFocalLength;
            this.teleFocalLength = teleFocalLength;
        }
    }

    /** External flash feature flags. */
    public static class Flash {
        public final boolean afLightAuto;
        public final boolean highSpeedSync;
        public final boolean redEyeReduction;
        public final boolean wireless;

        public Flash(boolean afLightAuto, boolean highSpeedSync, boolean redEyeReduction, boolean wireless) {
            this.afLightAuto = afLightAuto;
            this.highSpeedSync = highSpeedSync;
            this.redEyeReduction = redEyeReduction;
            this.wireless = wireless;
        }
    }

    /**
     * A 1024-sample tone curve. Pass it to {@link #setExtendedGamma(GammaCurve)}.
     * {@code null} restores the camera gamma.
     */
    public static class GammaCurve {
        private short[] samples;
        private boolean pictureEffectGammaForceOff = true;

        public void setPictureEffectGammaForceOff(boolean forceOff) {
            pictureEffectGammaForceOff = forceOff;
        }

        public boolean isPictureEffectGammaForceOff() {
            return pictureEffectGammaForceOff;
        }

        public short[] getSamples() {
            return samples;
        }

        /** Reads 1024 little-endian 16-bit samples, which is the layout of the grading CSV files. */
        public void write(InputStream in) throws IOException {
            byte[] data = new byte[1024 * 2];
            int offset = 0;
            while (offset < data.length) {
                int read = in.read(data, offset, data.length - offset);
                if (read < 0)
                    break;
                offset += read;
            }
            ByteBuffer buffer = ByteBuffer.wrap(data, 0, offset).order(ByteOrder.LITTLE_ENDIAN);
            int count = offset / 2;
            samples = new short[count];
            for (int i = 0; i < count; i++)
                samples[i] = buffer.getShort();
        }
    }

    public interface ShutterListener {
        void onShutter(int status);
    }

    public interface CaptureStatusListener {
        void onStart(int reason);
        void onEnd(int reason, int extra);
    }

    public interface JpegListener {
        void onJpeg(byte[] data);
    }

    public interface AutoFocusListener {
        void onAutoFocus(boolean success);
    }

    public interface FocusListener {
        void onFocus(int position, int maxPosition);
    }

    public interface ApertureListener {
        /** Aperture encoded as f-number times 100. f/2.8 is 280. */
        void onAperture(int apertureTimes100);
    }

    public interface ShutterSpeedListener {
        /** Shutter speed as the fraction {@code numerator / denominator}. */
        void onShutterSpeed(int numerator, int denominator);
    }

    public interface ZoomListener {
        void onZoom(int opticalPosition, int digitalPosition, boolean stopped);
    }

    public interface FaceListener {
        /** Face rectangles. On a camera these are in the -1000..1000 preview space. */
        void onFaces(List<android.graphics.Rect> faces);
    }

    public interface StoredImageListener {
        void onStored(int status, String directory, String filename);
    }

    public interface ReopenCallback {
        void onReopened();
    }

    public interface DirectShutterCallback {
        void onStopped();
    }

    /**
     * Opens the camera. {@code options} may be null.
     */
    public static Camera open(OpenOptions options) {
        if (DeviceInfo.getInstance().isCamera())
            return new DeviceCamera(options);
        return new Camera(openAndroid());
    }

    private static android.hardware.Camera openAndroid() {
        try {
            return android.hardware.Camera.open(0);
        } catch (RuntimeException e) {
            throw new RuntimeException("No camera is available on this device", e);
        }
    }

    private android.hardware.Camera camera;
    private byte[] latestPreview;
    private int previewWidth;
    private int previewHeight;
    private Handler handler;
    private Runnable selfTimerTask;
    private int selfTimerMillis = 10000;

    protected ShutterListener shutterListener;
    protected CaptureStatusListener captureStatusListener;
    protected JpegListener jpegListener;
    protected AutoFocusListener autoFocusListener;
    protected FocusListener focusListener;
    protected ApertureListener apertureListener;
    protected ShutterSpeedListener shutterSpeedListener;
    protected ZoomListener zoomListener;
    protected FaceListener faceListener;
    protected StoredImageListener storedImageListener;

    private Camera(android.hardware.Camera camera) {
        this.camera = camera;
    }

    protected void deliverJpeg(byte[] data) {
        if (jpegListener != null)
            jpegListener.onJpeg(data);
    }

    public void setShutterListener(ShutterListener listener) {
        shutterListener = listener;
    }

    public void setCaptureStatusListener(CaptureStatusListener listener) {
        captureStatusListener = listener;
    }

    public void setJpegListener(JpegListener listener) {
        jpegListener = listener;
    }

    public void setAutoFocusListener(AutoFocusListener listener) {
        autoFocusListener = listener;
    }

    public void setFocusListener(FocusListener listener) {
        focusListener = listener;
    }

    public void setApertureListener(ApertureListener listener) {
        apertureListener = listener;
    }

    public void setShutterSpeedListener(ShutterSpeedListener listener) {
        shutterSpeedListener = listener;
    }

    public void setZoomListener(ZoomListener listener) {
        zoomListener = listener;
    }

    public void setFaceListener(FaceListener listener) {
        faceListener = listener;
    }

    public void setStoredImageListener(StoredImageListener listener) {
        storedImageListener = listener;
    }

    /** Self-timer delay used by the emulator implementation, in milliseconds. */
    public void setSelfTimerMillis(int millis) {
        selfTimerMillis = millis;
    }

    public void setPreviewDisplay(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void startPreview() {
        camera.setPreviewCallback(new android.hardware.Camera.PreviewCallback() {
            @Override public void onPreviewFrame(byte[] data, android.hardware.Camera cam) {
                latestPreview = data;
                android.hardware.Camera.Size size = cam.getParameters().getPreviewSize();
                if (size != null) {
                    previewWidth = size.width;
                    previewHeight = size.height;
                }
            }
        });
        camera.startPreview();
    }

    public void stopPreview() {
        camera.setPreviewCallback(null);
        camera.stopPreview();
    }

    public android.hardware.Camera getNormalCamera() {
        return camera;
    }

    public CameraParameters getParameters() {
        return new CameraParameters(camera);
    }

    public void setParameters(CameraParameters parameters) {
        parameters.apply();
    }

    public void burstableTakePicture() {
        takePicture();
    }

    public void takePicture() {
        if (shutterListener != null)
            shutterListener.onShutter(SHUTTER_OK);
        if (captureStatusListener != null)
            captureStatusListener.onStart(CAPTURE_REASON_NORMAL);
        camera.takePicture(null, null, new android.hardware.Camera.PictureCallback() {
            @Override public void onPictureTaken(byte[] data, android.hardware.Camera cam) {
                if (camera == cam) {
                    try {
                        cam.startPreview();
                    } catch (RuntimeException ignored) {
                    }
                }
                deliverJpeg(data);
                if (captureStatusListener != null)
                    captureStatusListener.onEnd(CAPTURE_REASON_NORMAL, 0);
            }
        });
    }

    public void cancelTakePicture() {
        if (captureStatusListener != null)
            captureStatusListener.onEnd(CAPTURE_REASON_CANCEL, 0);
        try {
            camera.startPreview();
        } catch (RuntimeException ignored) {
        }
    }

    public void startDirectShutter() {
        takePicture();
    }

    public void stopDirectShutter(DirectShutterCallback callback) {
        if (callback != null)
            callback.onStopped();
    }

    public void autoFocus() {
        camera.autoFocus(new android.hardware.Camera.AutoFocusCallback() {
            @Override public void onAutoFocus(boolean success, android.hardware.Camera cam) {
                if (autoFocusListener != null)
                    autoFocusListener.onAutoFocus(success);
            }
        });
    }

    public void cancelAutoFocus() {
        camera.cancelAutoFocus();
    }

    public void startOneShotFocusDrive(int direction, int speed) {
        autoFocus();
    }

    public void startFocusHold() {
    }

    public void stopFocusHold() {
    }

    public void incrementShutterSpeed() {
    }

    public void decrementShutterSpeed() {
    }

    public void adjustShutterSpeed(int steps) {
    }

    public void incrementAperture() {
    }

    public void decrementAperture() {
    }

    public void adjustAperture(int steps) {
    }

    public boolean shiftFocus(int steps) {
        return false;
    }

    public void startSelfTimerShutter() {
        cancelSelfTimerShutter();
        if (handler == null)
            handler = new Handler();
        selfTimerTask = new Runnable() {
            @Override public void run() {
                selfTimerTask = null;
                burstableTakePicture();
            }
        };
        handler.postDelayed(selfTimerTask, selfTimerMillis);
    }

    public boolean cancelSelfTimerShutter() {
        if (handler != null && selfTimerTask != null) {
            handler.removeCallbacks(selfTimerTask);
            selfTimerTask = null;
            return true;
        }
        return false;
    }

    public void cancelExposure() {
        cancelAutoFocus();
        cancelTakePicture();
    }

    public void setPreviewMagnification(int ratio, int x, int y) {
    }

    public void stopPreviewMagnification() {
    }

    public Lens getLens() {
        return new Lens(null, null, 0, 0);
    }

    public SelectedColor getPreviewColor(int x, int y) {
        SelectedColor color = new SelectedColor();
        if (latestPreview == null || previewWidth <= 0 || previewHeight <= 0)
            return color;
        if (x < 0 || y < 0 || x >= previewWidth || y >= previewHeight)
            return color;
        int yIndex = y * previewWidth + x;
        if (yIndex < 0 || yIndex >= latestPreview.length)
            return color;
        color.y = latestPreview[yIndex] & 0xff;
        int uv = previewWidth * previewHeight + (y / 2) * previewWidth + (x & ~1);
        if (uv + 1 < latestPreview.length) {
            color.cr = latestPreview[uv] & 0xff;
            color.cb = latestPreview[uv + 1] & 0xff;
        }
        return color;
    }

    public void setColorSelect(int channel, SelectedColor color) {
    }

    public GammaCurve createGammaCurve() {
        return new GammaCurve();
    }

    public void setExtendedGamma(GammaCurve curve) {
    }

    public Flash getExternalFlash() {
        return new Flash(false, false, false, false);
    }

    /** {@code direction} is {@link #ZOOM_DIRECTION_TELE} or {@link #ZOOM_DIRECTION_WIDE}. */
    public void startZoom(int direction, int speed) {
        if (!camera.getParameters().isZoomSupported())
            return;
        int zoom = camera.getParameters().getZoom();
        int max = camera.getParameters().getMaxZoom();
        int next = direction == ZOOM_DIRECTION_TELE
                ? Math.min(max, zoom + Math.max(1, speed))
                : Math.max(0, zoom - Math.max(1, speed));
        android.hardware.Camera.Parameters parameters = camera.getParameters();
        parameters.setZoom(next);
        camera.setParameters(parameters);
        if (zoomListener != null)
            zoomListener.onZoom(next, next, true);
    }

    public void stopZoom() {
    }

    public void startFaceDetection() {
        try {
            camera.startFaceDetection();
        } catch (RuntimeException ignored) {
        }
    }

    public void stopFaceDetection() {
        try {
            camera.stopFaceDetection();
        } catch (RuntimeException ignored) {
        }
    }

    public void reopen(OpenOptions options, ReopenCallback callback) {
        release();
        camera = openAndroid();
        if (callback != null)
            callback.onReopened();
    }

    public void release() {
        cancelSelfTimerShutter();
        if (camera != null) {
            camera.release();
            camera = null;
        }
        latestPreview = null;
    }
}
