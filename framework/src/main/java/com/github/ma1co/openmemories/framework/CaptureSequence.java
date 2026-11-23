package com.github.ma1co.openmemories.framework;

import com.sony.scalar.graphics.OptimizedImage;
import com.sony.scalar.hardware.CameraSequence;

/**
 * Capture pipeline: shutter, raw development, and storing the result.
 * On a camera this is {@code CameraSequence}. On an emulator, JPEG captures from {@link Camera}
 * are delivered to the same listener and stored with {@link JpegSaver}.
 */
public class CaptureSequence {
    /**
     * Sony capture sequence.
     */
    public static class DeviceSequence extends CaptureSequence {
        private final CameraSequence sequence;

        private DeviceSequence(Camera camera) {
            super(camera, false);
            sequence = CameraSequence.open(((Camera.DeviceCamera) camera).nativeCamera());
            sequence.setShutterSequenceCallback(new CameraSequence.ShutterSequenceCallback() {
                @Override public void onShutterSequence(CameraSequence.RawData raw, CameraSequence seq) {
                    if (shutterListener != null)
                        shutterListener.onFrame(new DeviceFrame(raw));
                }
            });
        }

        @Override
        public void setOptions(int exposureCount, int recordCount, String memoryMapFile) {
            CameraSequence.Options options = new CameraSequence.Options();
            options.setOption(CameraSequence.Options.EXPOSURE_COUNT, exposureCount);
            options.setOption(CameraSequence.Options.RECORD_COUNT, recordCount);
            if (memoryMapFile != null)
                options.setOption(CameraSequence.Options.MEMORY_MAP_FILE, memoryMapFile);
            sequence.setShutterSequenceCallback(new CameraSequence.ShutterSequenceCallback() {
                @Override public void onShutterSequence(CameraSequence.RawData raw, CameraSequence seq) {
                    if (shutterListener != null)
                        shutterListener.onFrame(new DeviceFrame(raw));
                }
            }, options);
        }

        @Override
        public StillImage develop(RawFrame frame) {
            if (!(frame instanceof DeviceFrame))
                return null;
            CameraSequence.DefaultDevelopFilter filter = new CameraSequence.DefaultDevelopFilter();
            try {
                if (!filter.isSupported())
                    return null;
                filter.setSource(((DeviceFrame) frame).raw, false);
                if (!filter.execute())
                    return null;
                return StillImage.DeviceImage.adopt(filter.getOutput());
            } finally {
                filter.release();
            }
        }

        @Override
        public void store(StillImage image, boolean releaseAfter) {
            if (image instanceof StillImage.DeviceImage) {
                OptimizedImage hardware = ((StillImage.DeviceImage) image).getHardwareImage();
                if (hardware != null) {
                    sequence.storeImage(hardware, releaseAfter);
                    return;
                }
            }
            super.store(image, releaseAfter);
        }

        @Override
        public void setPreviewProcessor(SignalProcessor processor) {
            sequence.setPreviewPlugin(processor instanceof SignalProcessor.DeviceProcessor
                    ? ((SignalProcessor.DeviceProcessor) processor).nativeProcessor()
                    : null);
        }

        @Override
        public void startPreview(int frameRate, int width, int height) {
            CameraSequence.Options options = new CameraSequence.Options();
            options.setOption(CameraSequence.Options.PREVIEW_FRAME_RATE, frameRate);
            options.setOption(CameraSequence.Options.PREVIEW_FRAME_WIDTH, width);
            options.setOption(CameraSequence.Options.PREVIEW_FRAME_HEIGHT, height);
            options.setOption(CameraSequence.Options.PREVIEW_PLUGIN_OUTPUT_ENABLED, true);
            sequence.startPreviewSequence(options);
        }

        @Override
        public void stopPreview() {
            sequence.stopPreviewSequence();
        }

        @Override
        public void release() {
            sequence.setPreviewPlugin(null);
            sequence.release();
        }
    }

    private static class DeviceFrame implements RawFrame {
        final CameraSequence.RawData raw;
        private final RawInfo info;

        DeviceFrame(CameraSequence.RawData raw) {
            this.raw = raw;
            CameraSequence.RawDataInfo nativeInfo = raw == null ? null : raw.getRawDataInfo();
            info = new RawInfo();
            if (nativeInfo == null)
                return;
            info.canvasWidth = nativeInfo.canvasSizeX;
            info.canvasHeight = nativeInfo.canvasSizeY;
            info.marginOffsetX = nativeInfo.marginOffsetX;
            info.marginOffsetY = nativeInfo.marginOffsetY;
            info.marginWidth = nativeInfo.marginSizeX;
            info.marginHeight = nativeInfo.marginSizeY;
            info.validOffsetX = nativeInfo.validOffsetX;
            info.validOffsetY = nativeInfo.validOffsetY;
            info.validWidth = nativeInfo.validSizeX;
            info.validHeight = nativeInfo.validSizeY;
            info.firstColor = nativeInfo.firstColor;
            info.clipRed = nativeInfo.clpR;
            info.clipGreenRed = nativeInfo.clpGr;
            info.clipGreenBlue = nativeInfo.clpGb;
            info.clipBlue = nativeInfo.clpB;
            info.clipOffset = nativeInfo.clpOfst;
            info.whiteBalanceRed = nativeInfo.wbR;
            info.whiteBalanceBlue = nativeInfo.wbB;
            info.exposureBits = nativeInfo.expBit;
            info.decompressionMode = nativeInfo.decompMode;
            info.compressionMode = nativeInfo.compMode;
            info.roundingBits = nativeInfo.rndBit;
        }

        @Override public RawInfo getInfo() {
            return info;
        }

        @Override public void release() {
            if (raw != null)
                raw.release();
        }
    }

    /**
     * Geometry and white balance of one raw frame. Fields match {@code CameraSequence.RawDataInfo}.
     */
    public static class RawInfo {
        public int canvasWidth;
        public int canvasHeight;
        public int marginOffsetX;
        public int marginOffsetY;
        public int marginWidth;
        public int marginHeight;
        public int validOffsetX;
        public int validOffsetY;
        public int validWidth;
        public int validHeight;
        public int firstColor;
        public int clipRed;
        public int clipGreenRed;
        public int clipGreenBlue;
        public int clipBlue;
        public int clipOffset;
        public int whiteBalanceRed;
        public int whiteBalanceBlue;
        public int exposureBits;
        public int decompressionMode;
        public int compressionMode;
        public int roundingBits;
    }

    public interface RawFrame {
        RawInfo getInfo();
        void release();
    }

    public interface ShutterListener {
        void onFrame(RawFrame frame);
    }

    private static class BitmapFrame implements RawFrame {
        private final StillImage image;

        BitmapFrame(StillImage image) {
            this.image = image;
            info.validWidth = image == null ? 0 : image.getWidth();
            info.validHeight = image == null ? 0 : image.getHeight();
            info.canvasWidth = info.validWidth;
            info.canvasHeight = info.validHeight;
        }

        final RawInfo info = new RawInfo();

        @Override public RawInfo getInfo() {
            return info;
        }

        @Override public void release() {
            if (image != null)
                image.release();
        }

        StillImage image() {
            return image;
        }
    }

    /**
     * Opens a sequence bound to {@code camera}.
     */
    public static CaptureSequence open(Camera camera) {
        if (DeviceInfo.getInstance().isCamera())
            return new DeviceSequence(camera);
        return new CaptureSequence(camera);
    }

    private final Camera camera;
    protected ShutterListener shutterListener;

    private CaptureSequence(Camera camera) {
        this(camera, true);
    }

    private CaptureSequence(Camera camera, boolean watchJpeg) {
        this.camera = camera;
        if (watchJpeg && camera != null) {
            camera.setJpegListener(new Camera.JpegListener() {
                @Override public void onJpeg(byte[] data) {
                    if (shutterListener != null)
                        shutterListener.onFrame(new BitmapFrame(StillImage.fromJpeg(data)));
                }
            });
        }
    }

    public void setShutterListener(ShutterListener listener) {
        shutterListener = listener;
    }

    /** Remembers exposure and record counts. The emulator captures a single JPEG either way. */
    public void setOptions(int exposureCount, int recordCount, String memoryMapFile) {
    }

    /** The caller owns the returned image and must release it (or pass it to {@link #store} with {@code releaseAfter}). */
    public StillImage develop(RawFrame frame) {
        if (!(frame instanceof BitmapFrame))
            return null;
        StillImage image = ((BitmapFrame) frame).image();
        if (image == null || !image.isValid())
            return null;
        android.graphics.Bitmap bitmap = image.getBitmap();
        if (bitmap == null)
            return null;
        return StillImage.fromBitmap(StillImage.copy(bitmap));
    }

    public void store(StillImage image, boolean releaseAfter) {
        JpegSaver.save(null, image, null, JpegSaver.QUALITY_FINE);
        if (releaseAfter && image != null)
            image.release();
    }

    public void setPreviewProcessor(SignalProcessor processor) {
    }

    public void startPreview(int frameRate, int width, int height) {
        if (camera != null)
            camera.startPreview();
    }

    public void stopPreview() {
        if (camera != null)
            camera.stopPreview();
    }

    public void release() {
    }
}
