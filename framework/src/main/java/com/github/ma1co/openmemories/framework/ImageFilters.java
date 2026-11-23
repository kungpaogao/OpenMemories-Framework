package com.github.ma1co.openmemories.framework;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import com.sony.scalar.graphics.OptimizedImage;
import com.sony.scalar.graphics.imagefilter.ContrastPlusImageFilter;
import com.sony.scalar.graphics.imagefilter.CropImageFilter;
import com.sony.scalar.graphics.imagefilter.FaceNRImageFilter;
import com.sony.scalar.graphics.imagefilter.ImageFilter;
import com.sony.scalar.graphics.imagefilter.MiniatureImageFilter;
import com.sony.scalar.graphics.imagefilter.RedEyeImageFilter;
import com.sony.scalar.graphics.imagefilter.RotateImageFilter;
import com.sony.scalar.graphics.imagefilter.ScaleImageFilter;
import com.sony.scalar.graphics.imagefilter.SoftFocusImageFilter;
import com.sony.scalar.graphics.imagefilter.SuperResolutionImageFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Image operations used by the PlayMemories editing apps.
 * On a camera each method runs the matching {@code com.sony.scalar.graphics.imagefilter} class.
 * Elsewhere the same call runs on a {@link Bitmap}.
 */
public final class ImageFilters {
    /** Horizontal center strip stays sharp. */
    public static final int MINIATURE_HORIZONTAL_CENTER = 1;
    /** Vertical center strip stays sharp. */
    public static final int MINIATURE_VERTICAL_CENTER = 2;
    public static final int MINIATURE_LEFT = 3;
    public static final int MINIATURE_RIGHT = 4;
    public static final int MINIATURE_UPPER = 5;
    public static final int MINIATURE_LOWER = 6;
    /** Face noise-reduction strength. Matches the camera constants. */
    public static final int NR_OFF = 1;
    public static final int NR_LOW = 2;
    public static final int NR_MID = 3;
    public static final int NR_HIGH = 4;
    public static final int NR_AUTO = 5;
    /** Rotate and crop so the result fits inside the rotated bounds. */
    public static final int TRIM_INSCRIBED = 3;

    private ImageFilters() {
    }

    /**
     * Hardware filters. Loaded only on a camera.
     */
    public static class DeviceFilters {
        static StillImage scale(StillImage source, int width, int height) {
            OptimizedImage image = hardware(source);
            if (image == null)
                return null;
            ScaleImageFilter filter = new ScaleImageFilter();
            try {
                filter.setSource(image, false);
                filter.setDestSize(width & ~3, height & ~1);
                return output(filter);
            } finally {
                filter.clearSources();
                filter.release();
            }
        }

        static StillImage crop(StillImage source, Rect rect) {
            OptimizedImage image = hardware(source);
            if (image == null)
                return null;
            CropImageFilter filter = new CropImageFilter();
            try {
                filter.setSource(image, false);
                filter.setSrcRect(rect);
                return output(filter);
            } finally {
                filter.clearSources();
                filter.release();
            }
        }

        static StillImage rotate(StillImage source, double degrees) {
            OptimizedImage image = hardware(source);
            if (image == null)
                return null;
            RotateImageFilter filter = new RotateImageFilter();
            try {
                filter.setSource(image, false);
                filter.setTrimMode(TRIM_INSCRIBED);
                filter.setRotation(degrees);
                return output(filter);
            } finally {
                filter.clearSources();
                filter.release();
            }
        }

        static StillImage miniature(StillImage source, int area) {
            OptimizedImage image = hardware(source);
            if (image == null)
                return null;
            MiniatureImageFilter filter = new MiniatureImageFilter();
            try {
                filter.setSource(image, false);
                filter.setMiniatureArea(area);
                return output(filter);
            } finally {
                filter.clearSources();
                filter.release();
            }
        }

        static StillImage faceNoiseReduction(StillImage source, List<Rect> faces, int iso, int level) {
            OptimizedImage image = hardware(source);
            if (image == null)
                return null;
            FaceNRImageFilter filter = new FaceNRImageFilter();
            try {
                filter.setSource(image, false);
                filter.setFaceList(faces == null ? new ArrayList<Rect>() : faces);
                filter.setISOValue(iso);
                filter.setNRLevel(level);
                return output(filter);
            } finally {
                filter.clearSources();
                filter.release();
            }
        }

        static StillImage contrast(StillImage source, int mode) {
            OptimizedImage image = hardware(source);
            if (image == null)
                return null;
            ContrastPlusImageFilter filter = new ContrastPlusImageFilter();
            try {
                filter.setSource(image, false);
                filter.setCompensationMode(mode);
                return output(filter);
            } finally {
                filter.clearSources();
                filter.release();
            }
        }

        static StillImage redEye(StillImage source) {
            OptimizedImage image = hardware(source);
            if (image == null)
                return null;
            RedEyeImageFilter filter = new RedEyeImageFilter();
            try {
                filter.setSource(image, false);
                return output(filter);
            } finally {
                filter.clearSources();
                filter.release();
            }
        }

        static StillImage softFocus(StillImage source, int level) {
            OptimizedImage image = hardware(source);
            if (image == null)
                return null;
            SoftFocusImageFilter filter = new SoftFocusImageFilter();
            try {
                filter.setSource(image, false);
                filter.setEffectLevel(level);
                return output(filter);
            } finally {
                filter.clearSources();
                filter.release();
            }
        }

        static StillImage superResolution(StillImage source) {
            OptimizedImage image = hardware(source);
            if (image == null)
                return null;
            SuperResolutionImageFilter filter = new SuperResolutionImageFilter();
            try {
                filter.setSource(image, false);
                return output(filter);
            } finally {
                filter.clearSources();
                filter.release();
            }
        }

        private static OptimizedImage hardware(StillImage source) {
            if (source instanceof StillImage.DeviceImage)
                return ((StillImage.DeviceImage) source).getHardwareImage();
            return null;
        }

        private static StillImage output(ImageFilter filter) {
            if (!filter.execute())
                return null;
            return StillImage.DeviceImage.adopt(filter.getOutput());
        }
    }

    public static StillImage scale(StillImage source, int width, int height) {
        if (DeviceInfo.getInstance().isCamera()) {
            StillImage scaled = DeviceFilters.scale(source, width, height);
            if (scaled != null)
                return scaled;
        }
        Bitmap bitmap = bitmap(source);
        if (bitmap == null)
            return null;
        int w = Math.max(1, width & ~3);
        int h = Math.max(1, height & ~1);
        return StillImage.fromBitmap(Bitmap.createScaledBitmap(bitmap, w, h, true));
    }

    public static StillImage crop(StillImage source, Rect rect) {
        if (DeviceInfo.getInstance().isCamera()) {
            StillImage cropped = DeviceFilters.crop(source, rect);
            if (cropped != null)
                return cropped;
        }
        Bitmap bitmap = bitmap(source);
        if (bitmap == null || rect == null)
            return null;
        int left = clamp(rect.left, 0, bitmap.getWidth() - 1);
        int top = clamp(rect.top, 0, bitmap.getHeight() - 1);
        int right = clamp(rect.right, left + 1, bitmap.getWidth());
        int bottom = clamp(rect.bottom, top + 1, bitmap.getHeight());
        return StillImage.fromBitmap(Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top));
    }

    public static StillImage rotate(StillImage source, double degrees) {
        if (DeviceInfo.getInstance().isCamera()) {
            StillImage rotated = DeviceFilters.rotate(source, degrees);
            if (rotated != null)
                return rotated;
        }
        Bitmap bitmap = bitmap(source);
        if (bitmap == null)
            return null;
        Matrix matrix = new Matrix();
        matrix.postRotate((float) degrees);
        return StillImage.fromBitmap(Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true));
    }

    public static StillImage miniature(StillImage source, int area) {
        if (DeviceInfo.getInstance().isCamera()) {
            StillImage filtered = DeviceFilters.miniature(source, area);
            if (filtered != null)
                return filtered;
        }
        Bitmap bitmap = bitmap(source);
        if (bitmap == null)
            return null;
        Bitmap blurred = blur(bitmap);
        Canvas canvas = new Canvas(blurred);
        Rect sharp = sharpBand(bitmap.getWidth(), bitmap.getHeight(), area);
        canvas.drawBitmap(bitmap, sharp, sharp, null);
        return StillImage.fromBitmap(blurred);
    }

    public static StillImage faceNoiseReduction(StillImage source, List<Rect> faces, int iso, int level) {
        if (DeviceInfo.getInstance().isCamera()) {
            StillImage filtered = DeviceFilters.faceNoiseReduction(source, faces, iso, level);
            if (filtered != null)
                return filtered;
        }
        if (level == NR_OFF)
            return StillImage.fromBitmap(StillImage.copy(bitmap(source)));
        Bitmap bitmap = bitmap(source);
        if (bitmap == null)
            return null;
        if (faces == null || faces.isEmpty())
            return StillImage.fromBitmap(blur(bitmap));
        Bitmap result = StillImage.copy(bitmap);
        Canvas canvas = new Canvas(result);
        Bitmap blurred = blur(bitmap);
        for (int i = 0; i < faces.size(); i++) {
            Rect face = faces.get(i);
            if (face != null)
                canvas.drawBitmap(blurred, face, face, null);
        }
        blurred.recycle();
        return StillImage.fromBitmap(result);
    }

    public static StillImage contrast(StillImage source, int mode) {
        if (DeviceInfo.getInstance().isCamera()) {
            StillImage filtered = DeviceFilters.contrast(source, mode);
            if (filtered != null)
                return filtered;
        }
        float scale = mode <= 1 ? 1.15f : mode == 2 ? 1.3f : 1.5f;
        return colorMatrix(source, contrastMatrix(scale));
    }

    public static StillImage redEye(StillImage source) {
        if (DeviceInfo.getInstance().isCamera()) {
            StillImage filtered = DeviceFilters.redEye(source);
            if (filtered != null)
                return filtered;
        }
        Bitmap bitmap = bitmap(source);
        if (bitmap == null)
            return null;
        Bitmap result = StillImage.copy(bitmap);
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        result.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            int r = (color >> 16) & 0xff;
            int g = (color >> 8) & 0xff;
            int b = color & 0xff;
            if (r > 150 && r > g * 2 && r > b * 2)
                pixels[i] = (color & 0xff000000) | ((r / 3) << 16) | (g << 8) | b;
        }
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return StillImage.fromBitmap(result);
    }

    public static StillImage softFocus(StillImage source, int level) {
        if (DeviceInfo.getInstance().isCamera()) {
            StillImage filtered = DeviceFilters.softFocus(source, level);
            if (filtered != null)
                return filtered;
        }
        Bitmap bitmap = bitmap(source);
        if (bitmap == null)
            return null;
        Bitmap blurred = blur(bitmap);
        Bitmap result = StillImage.copy(bitmap);
        Paint paint = new Paint();
        paint.setAlpha(level >= 3 ? 180 : level == 2 ? 120 : 70);
        new Canvas(result).drawBitmap(blurred, 0, 0, paint);
        blurred.recycle();
        return StillImage.fromBitmap(result);
    }

    public static StillImage superResolution(StillImage source) {
        if (DeviceInfo.getInstance().isCamera()) {
            StillImage filtered = DeviceFilters.superResolution(source);
            if (filtered != null)
                return filtered;
        }
        return scale(source, Math.max(1, source.getWidth() * 2), Math.max(1, source.getHeight() * 2));
    }

    private static StillImage colorMatrix(StillImage source, ColorMatrix matrix) {
        Bitmap bitmap = bitmap(source);
        if (bitmap == null)
            return null;
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        new Canvas(result).drawBitmap(bitmap, 0, 0, paint);
        return StillImage.fromBitmap(result);
    }

    private static ColorMatrix contrastMatrix(float scale) {
        float translate = 128f * (1f - scale);
        ColorMatrix matrix = new ColorMatrix();
        matrix.set(new float[] {
                scale, 0, 0, 0, translate,
                0, scale, 0, 0, translate,
                0, 0, scale, 0, translate,
                0, 0, 0, 1, 0
        });
        return matrix;
    }

    private static Bitmap blur(Bitmap source) {
        int w = Math.max(1, source.getWidth() / 8);
        int h = Math.max(1, source.getHeight() / 8);
        Bitmap small = Bitmap.createScaledBitmap(source, w, h, true);
        Bitmap blurred = Bitmap.createScaledBitmap(small, source.getWidth(), source.getHeight(), true);
        if (small != blurred)
            small.recycle();
        return blurred;
    }

    private static Rect sharpBand(int width, int height, int area) {
        int bandW = Math.max(1, width / 3);
        int bandH = Math.max(1, height / 3);
        if (area == MINIATURE_VERTICAL_CENTER)
            return new Rect((width - bandW) / 2, 0, (width + bandW) / 2, height);
        if (area == MINIATURE_LEFT)
            return new Rect(0, 0, bandW, height);
        if (area == MINIATURE_RIGHT)
            return new Rect(width - bandW, 0, width, height);
        if (area == MINIATURE_UPPER)
            return new Rect(0, 0, width, bandH);
        if (area == MINIATURE_LOWER)
            return new Rect(0, height - bandH, width, height);
        return new Rect(0, (height - bandH) / 2, width, (height + bandH) / 2);
    }

    private static Bitmap bitmap(StillImage source) {
        return source == null ? null : source.getBitmap();
    }

    private static int clamp(int value, int min, int max) {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }
}
