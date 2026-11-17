package com.github.ma1co.openmemories.framework;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.FaceDetector;
import com.sony.scalar.graphics.ImageAnalyzer;
import com.sony.scalar.meta.FaceInfo;

/**
 * Finds faces in a {@link StillImage}.
 * On a camera this is {@code ImageAnalyzer}. Coordinates there are in the -1000..1000 space used by the camera.
 * Elsewhere this uses {@link FaceDetector} and returns pixel rectangles.
 */
public final class FaceFinder {
    public static class Face {
        public final Rect rect;
        public final int score;

        public Face(Rect rect, int score) {
            this.rect = rect;
            this.score = score;
        }
    }

    /** Camera face detector. */
    public static class DeviceFinder {
        static Face[] find(StillImage image) {
            if (!(image instanceof StillImage.DeviceImage))
                return new Face[0];
            com.sony.scalar.graphics.OptimizedImage hardware = ((StillImage.DeviceImage) image).getHardwareImage();
            if (hardware == null)
                return new Face[0];
            ImageAnalyzer analyzer = new ImageAnalyzer();
            try {
                ImageAnalyzer.AnalyzedFace[] slots = new ImageAnalyzer.AnalyzedFace[8];
                for (int i = 0; i < slots.length; i++)
                    slots[i] = new ImageAnalyzer.AnalyzedFace();
                int count = analyzer.findFaces(hardware, slots);
                Face[] faces = new Face[Math.max(0, count)];
                int written = 0;
                for (int i = 0; i < slots.length && written < faces.length; i++) {
                    FaceInfo info = slots[i] == null ? null : slots[i].face;
                    if (info == null || info.rect == null)
                        continue;
                    faces[written++] = new Face(new Rect(info.rect), info.score);
                }
                if (written == faces.length)
                    return faces;
                Face[] trimmed = new Face[written];
                System.arraycopy(faces, 0, trimmed, 0, written);
                return trimmed;
            } finally {
                analyzer.release();
            }
        }
    }

    private FaceFinder() {
    }

    public static Face[] find(StillImage image) {
        if (DeviceInfo.getInstance().isCamera())
            return DeviceFinder.find(image);
        Bitmap bitmap = image == null ? null : image.getBitmap();
        if (bitmap == null)
            return new Face[0];
        Bitmap rgb = bitmap;
        boolean temporary = false;
        if (bitmap.getConfig() != Bitmap.Config.RGB_565 || (bitmap.getWidth() & 1) != 0) {
            int width = bitmap.getWidth() & ~1;
            if (width <= 0)
                return new Face[0];
            rgb = Bitmap.createBitmap(width, bitmap.getHeight(), Bitmap.Config.RGB_565);
            new android.graphics.Canvas(rgb).drawBitmap(bitmap, 0, 0, null);
            temporary = true;
        }
        FaceDetector detector = new FaceDetector(rgb.getWidth(), rgb.getHeight(), 8);
        FaceDetector.Face[] found = new FaceDetector.Face[8];
        int count = detector.findFaces(rgb, found);
        Face[] faces = new Face[count];
        int written = 0;
        for (int i = 0; i < count; i++) {
            if (found[i] == null)
                continue;
            PointF mid = new PointF();
            found[i].getMidPoint(mid);
            float eyes = found[i].eyesDistance();
            int halfW = Math.round(eyes);
            int halfH = Math.round(eyes * 1.2f);
            Rect rect = new Rect(Math.round(mid.x) - halfW, Math.round(mid.y) - halfH, Math.round(mid.x) + halfW, Math.round(mid.y) + halfH);
            faces[written++] = new Face(rect, Math.round(found[i].confidence() * 100));
        }
        if (temporary)
            rgb.recycle();
        if (written == faces.length)
            return faces;
        Face[] trimmed = new Face[written];
        System.arraycopy(faces, 0, trimmed, 0, written);
        return trimmed;
    }
}
