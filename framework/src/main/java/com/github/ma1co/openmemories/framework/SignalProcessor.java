package com.github.ma1co.openmemories.framework;

import com.sony.scalar.hardware.DSP;
import com.sony.scalar.hardware.DeviceBuffer;
import com.sony.scalar.hardware.DeviceMemory;
import com.sony.scalar.hardware.MemoryMapConfig;

import java.nio.ByteBuffer;

/**
 * Hardware image processor ({@code DSP}).
 * The emulator keeps the program name and arguments so call sequences can be tested, and {@link #execute()} returns false.
 */
public class SignalProcessor {
    /** Sony DSP. */
    public static class DeviceProcessor extends SignalProcessor {
        private final DSP dsp;

        private DeviceProcessor() {
            dsp = DSP.createProcessor("sony-di-dsp");
        }

        DSP nativeProcessor() {
            return dsp;
        }

        @Override
        public void setProgram(String path) {
            super.setProgram(path);
            dsp.setProgram(path);
        }

        @Override
        public void clearProgram() {
            super.clearProgram();
            dsp.clearProgram();
        }

        @Override
        public Buffer createBuffer(int size) {
            return new DeviceHardwareBuffer(dsp.createBuffer(size));
        }

        @Override
        public StillImage createImage(int width, int height) {
            DeviceMemory memory = dsp.createImage(width, height);
            if (memory instanceof com.sony.scalar.graphics.OptimizedImage)
                return StillImage.DeviceImage.adopt((com.sony.scalar.graphics.OptimizedImage) memory);
            return null;
        }

        @Override
        public int getIntProperty(String key) {
            return dsp.getPropertyAsInt(key);
        }

        @Override
        public int getIntProperty(StillImage image, String key) {
            if (image instanceof StillImage.DeviceImage) {
                com.sony.scalar.graphics.OptimizedImage hardware = ((StillImage.DeviceImage) image).getHardwareImage();
                if (hardware != null)
                    return dsp.getPropertyAsInt(hardware, key);
            }
            return 0;
        }

        @Override
        public void setArgument(int index, int value) {
            super.setArgument(index, value);
            dsp.setArg(index, value);
        }

        @Override
        public void setArgument(int index, Buffer buffer) {
            super.setArgument(index, buffer);
            if (buffer instanceof DeviceHardwareBuffer)
                dsp.setArg(index, ((DeviceHardwareBuffer) buffer).buffer);
        }

        @Override
        public void setArgument(int index, StillImage image) {
            super.setArgument(index, image);
            if (image instanceof StillImage.DeviceImage) {
                com.sony.scalar.graphics.OptimizedImage hardware = ((StillImage.DeviceImage) image).getHardwareImage();
                if (hardware != null)
                    dsp.setArg(index, hardware);
            }
        }

        @Override
        public boolean execute() {
            return dsp.execute();
        }

        @Override
        public boolean isHardware() {
            return true;
        }

        @Override
        public void release() {
            dsp.clearProgram();
            dsp.release();
        }
    }

    private static class DeviceHardwareBuffer extends Buffer {
        final DeviceBuffer buffer;

        DeviceHardwareBuffer(DeviceBuffer buffer) {
            super(buffer == null ? 0 : buffer.getSize());
            this.buffer = buffer;
        }

        @Override
        public void write(ByteBuffer data) {
            if (buffer != null)
                buffer.write(data);
        }

        @Override
        public void release() {
            if (buffer != null)
                buffer.release();
        }
    }

    /** Argument block passed to {@link #setArgument(int, Buffer)}. */
    public static class Buffer {
        private final byte[] data;

        Buffer(int size) {
            data = new byte[Math.max(0, size)];
        }

        public int getSize() {
            return data.length;
        }

        public void write(ByteBuffer data) {
            if (data == null)
                return;
            data.mark();
            int length = Math.min(this.data.length, data.remaining());
            data.get(this.data, 0, length);
            data.reset();
        }

        public void release() {
        }
    }

    public static final String PROPERTY_PROGRAM = "program-descriptor";
    public static final String PROPERTY_ADDRESS = "memory-address";
    public static final String PROPERTY_IMAGE_OFFSET = "image-data-offset";
    public static final String PROPERTY_CANVAS_WIDTH = "image-canvas-width";
    public static final String PROPERTY_CANVAS_HEIGHT = "image-canvas-height";
    /** Older bodies. Mask an ARM address with this to get an AXI address. */
    public static final int AXI_MASK_AVIP = 0x7fffffff;
    /** Newer bodies. */
    public static final int AXI_MASK_MUSASHI = 0x3fffffff;
    public static final int ALLOCATION_DEFAULT = 0;
    public static final int ALLOCATION_VERSION_1 = 1;
    public static final int ALLOCATION_VERSION_2 = 2;

    private String program;
    private final int[] intArguments = new int[16];

    /**
     * Creates the camera DSP named {@code sony-di-dsp}, or a software stand-in.
     */
    public static SignalProcessor create() {
        if (DeviceInfo.getInstance().isCamera())
            return new DeviceProcessor();
        return new SignalProcessor();
    }

    /** Selects the raw-memory allocation policy. No effect off-camera. */
    public static void setAllocationPolicy(int policy) {
        if (DeviceInfo.getInstance().isCamera())
            DeviceMemoryPolicy.set(policy);
    }

    /** Loads an extra named memory map. No effect off-camera. */
    public static void loadMemoryMap(String name) {
        if (DeviceInfo.getInstance().isCamera())
            DeviceMemoryPolicy.load(name);
    }

    /**
     * Memory-map calls. Loaded only on a camera.
     */
    public static class DeviceMemoryPolicy {
        static void set(int policy) {
            MemoryMapConfig.setAllocationPolicy(policy);
        }

        static void load(String name) {
            MemoryMapConfig.loadExtraMap(name);
        }
    }

    public void setProgram(String path) {
        program = path;
    }

    public String getProgram() {
        return program;
    }

    public void clearProgram() {
        program = null;
    }

    public Buffer createBuffer(int size) {
        return new Buffer(size);
    }

    public StillImage createImage(int width, int height) {
        return null;
    }

    public int getIntProperty(String key) {
        return 0;
    }

    public int getIntProperty(StillImage image, String key) {
        return 0;
    }

    public void setArgument(int index, int value) {
        if (index >= 0 && index < intArguments.length)
            intArguments[index] = value;
    }

    public void setArgument(int index, Buffer buffer) {
    }

    public void setArgument(int index, StillImage image) {
    }

    public int getArgument(int index) {
        if (index < 0 || index >= intArguments.length)
            return 0;
        return intArguments[index];
    }

    /** Runs the loaded program. Returns false when there is no DSP. */
    public boolean execute() {
        return false;
    }

    public boolean isHardware() {
        return false;
    }

    public void release() {
        program = null;
    }

    /** Converts an ARM address to the AXI address used by DSP parameters. */
    public static int toAxiAddress(int armAddress, boolean musashi) {
        return armAddress & (musashi ? AXI_MASK_MUSASHI : AXI_MASK_AVIP);
    }
}
