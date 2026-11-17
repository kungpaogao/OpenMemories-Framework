package com.github.ma1co.openmemories.framework;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.sony.scalar.graphics.OptimizedImage;
import com.sony.scalar.graphics.OptimizedImageFactory;

import java.io.ByteArrayOutputStream;

/**
 * A still image backed by an {@code OptimizedImage} on a Sony camera and by a {@link Bitmap} elsewhere.
 * Always call {@link #release()} when finished. Hardware images cannot be turned back into a {@link Bitmap}.
 */
public class StillImage {
    /**
     * Still image stored in Sony hardware memory.
     */
    public static class DeviceImage extends StillImage {
        private OptimizedImage image;

        private DeviceImage(OptimizedImage image) {
            super(null);
            this.image = image;
        }

        static StillImage decode(String path, int imageType) {
            OptimizedImageFactory.Options options = new OptimizedImageFactory.Options();
            options.imageType = imageType;
            options.bBasicInfo = true;
            options.bCamInfo = true;
            options.bExtCamInfo = true;
            options.bGpsInfo = true;
            return new DeviceImage(OptimizedImageFactory.decodeImage(path, options));
        }

        static StillImage adopt(OptimizedImage image) {
            return new DeviceImage(image);
        }

        OptimizedImage getHardwareImage() {
            return image;
        }

        @Override
        public int getWidth() {
            return image == null ? 0 : image.getWidth();
        }

        @Override
        public int getHeight() {
            return image == null ? 0 : image.getHeight();
        }

        @Override
        public boolean isValid() {
            return image != null && image.isValid();
        }

        @Override
        public Bitmap getBitmap() {
            return null;
        }

        @Override
        public void release() {
            if (image != null) {
                image.release();
                image = null;
            }
        }
    }

    /** Full-size decode, matching {@code OptimizedImageFactory.Options.MAIN}. */
    public static final int TYPE_FULL = 3;
    /** Screen-size decode, matching {@code OptimizedImageFactory.Options.SCN}. */
    public static final int TYPE_SCREEN = 2;

    /**
     * Decodes a file. {@code imageType} is {@link #TYPE_FULL} or {@link #TYPE_SCREEN}.
     */
    public static StillImage decodeFile(String path, int imageType) {
        if (DeviceInfo.getInstance().isCamera())
            return DeviceImage.decode(path, imageType);
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (imageType == TYPE_SCREEN)
            options.inSampleSize = 4;
        return new StillImage(BitmapFactory.decodeFile(path, options));
    }

    public static StillImage fromBitmap(Bitmap bitmap) {
        return new StillImage(bitmap);
    }

    public static StillImage fromJpeg(byte[] jpeg) {
        return new StillImage(BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length));
    }

    private Bitmap bitmap;

    private StillImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getWidth() {
        return bitmap == null ? 0 : bitmap.getWidth();
    }

    public int getHeight() {
        return bitmap == null ? 0 : bitmap.getHeight();
    }

    public boolean isValid() {
        return bitmap != null && !bitmap.isRecycled();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public byte[] toJpeg(int quality) {
        if (bitmap == null)
            return null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        return out.toByteArray();
    }

    public void release() {
        if (bitmap != null && !bitmap.isRecycled())
            bitmap.recycle();
        bitmap = null;
    }

    static Bitmap copy(Bitmap source) {
        if (source == null)
            return null;
        return source.copy(source.getConfig() == null ? Bitmap.Config.ARGB_8888 : source.getConfig(), true);
    }
}
