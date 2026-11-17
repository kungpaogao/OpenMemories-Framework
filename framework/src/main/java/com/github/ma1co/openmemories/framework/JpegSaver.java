package com.github.ma1co.openmemories.framework;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import com.sony.scalar.graphics.JpegExporter;
import com.sony.scalar.provider.AvindexStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Saves a {@link StillImage} as a JPEG.
 * On a camera this uses {@code JpegExporter} and waits for the media database.
 * Elsewhere it writes a file under {@code DCIM/OpenMemories} and inserts it into {@link MediaStore}.
 */
public final class JpegSaver {
    public static final int QUALITY_STANDARD = 1;
    public static final int QUALITY_FINE = 2;
    public static final int QUALITY_EXTRA_FINE = 3;

    private JpegSaver() {
    }

    /**
     * Camera saver. Loaded only on a camera.
     */
    public static class DeviceSaver {
        static String save(Context context, StillImage image, String mediaId, int quality) {
            if (!(image instanceof StillImage.DeviceImage))
                return null;
            com.sony.scalar.graphics.OptimizedImage hardware = ((StillImage.DeviceImage) image).getHardwareImage();
            if (hardware == null)
                return null;
            JpegExporter exporter = new JpegExporter();
            try {
                JpegExporter.Options options = new JpegExporter.Options();
                options.quality = quality;
                exporter.encode(hardware, mediaId, options);
            } finally {
                exporter.release();
            }
            if (context != null)
                AvindexStore.Images.waitAndUpdateDatabase(context.getContentResolver(), mediaId);
            return mediaId;
        }
    }

    /**
     * Saves {@code image}. {@code mediaId} is the camera media id and is ignored off-camera.
     * Returns the destination path, or the media id on a camera.
     */
    public static String save(Context context, StillImage image, String mediaId, int quality) {
        if (DeviceInfo.getInstance().isCamera()) {
            String saved = DeviceSaver.save(context, image, mediaId, quality);
            if (saved != null)
                return saved;
        }
        Bitmap bitmap = image == null ? null : image.getBitmap();
        if (bitmap == null)
            return null;
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "OpenMemories");
        if (!dir.exists() && !dir.mkdirs())
            return null;
        File file = new File(dir, "OM_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, jpegQuality(quality), out);
        } catch (IOException e) {
            return null;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }
        if (context != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
            context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
        return file.getAbsolutePath();
    }

    private static int jpegQuality(int quality) {
        if (quality <= QUALITY_STANDARD)
            return 70;
        if (quality == QUALITY_FINE)
            return 90;
        return 100;
    }
}
