package com.github.ma1co.openmemories.framework;

import android.content.Context;
import android.graphics.Matrix;
import android.view.View;
import android.widget.ImageView;
import com.sony.scalar.widget.OptimizedImageView;

/**
 * Shows a {@link StillImage}.
 * Pass an {@code OptimizedImageView} on a camera and an {@link ImageView} elsewhere.
 */
public class ImageDisplay {
    /** Binds an {@code OptimizedImageView}. */
    public static class DeviceDisplay extends ImageDisplay {
        private final OptimizedImageView view;

        private DeviceDisplay(OptimizedImageView view) {
            super(null);
            this.view = view;
        }

        @Override
        public void setImage(StillImage image) {
            com.sony.scalar.graphics.OptimizedImage hardware = null;
            if (image instanceof StillImage.DeviceImage)
                hardware = ((StillImage.DeviceImage) image).getHardwareImage();
            view.setOptimizedImage(hardware);
        }

        @Override
        public void setFit() {
            view.setDisplayType(OptimizedImageView.DisplayType.DISPLAY_TYPE_CENTER_INNER);
        }

        @Override
        public void setScale(float scale) {
            view.setScale(scale, OptimizedImageView.BoundType.BOUND_TYPE_LONG_EDGE);
        }

        @Override
        public void pan(int x, int y) {
            view.translate(new android.graphics.Point(x, y), OptimizedImageView.TranslationType.TRANS_TYPE_INNER_CENTER);
        }

        @Override
        public boolean canPanRight() {
            OptimizedImageView.Translatability limits = view.getTranslatability(OptimizedImageView.TranslationType.TRANS_TYPE_INNER_CENTER);
            return limits != null && limits.right;
        }

        @Override
        public void redraw() {
            view.redraw();
        }

        @Override
        public void release() {
            view.setOptimizedImage(null);
            view.release();
        }
    }

    private final ImageView view;
    private float scale = 1f;

    public static ImageDisplay attach(Object view) {
        if (DeviceInfo.getInstance().isCamera())
            return DeviceAttach.attach(view);
        if (view instanceof ImageView)
            return new ImageDisplay((ImageView) view);
        throw new IllegalArgumentException("Expected an ImageView");
    }

    /** Creates the view {@link #attach(Object)} expects: an {@code OptimizedImageView} on a camera and an {@link ImageView} elsewhere. */
    public static View createView(Context context) {
        if (DeviceInfo.getInstance().isCamera())
            return DeviceAttach.createView(context);
        return new ImageView(context);
    }

    /** Camera attach. Loaded only on a camera. */
    public static class DeviceAttach {
        static View createView(Context context) {
            return new OptimizedImageView(context);
        }

        static ImageDisplay attach(Object view) {
            if (view instanceof OptimizedImageView)
                return new DeviceDisplay((OptimizedImageView) view);
            throw new IllegalArgumentException("Expected an OptimizedImageView");
        }
    }

    private ImageDisplay(ImageView view) {
        this.view = view;
        if (view != null)
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    public void setImage(StillImage image) {
        if (view == null)
            return;
        view.setImageBitmap(image == null ? null : image.getBitmap());
    }

    public void setFit() {
        scale = 1f;
        if (view != null) {
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            view.setImageMatrix(new Matrix());
        }
    }

    public void setScale(float scale) {
        this.scale = scale;
        applyMatrix(0, 0);
    }

    public void pan(int x, int y) {
        applyMatrix(x, y);
    }

    public boolean canPanRight() {
        return scale > 1f;
    }

    public void redraw() {
        if (view != null)
            view.invalidate();
    }

    public void release() {
        if (view != null)
            view.setImageBitmap(null);
    }

    private void applyMatrix(int x, int y) {
        if (view == null)
            return;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        matrix.postTranslate(x, y);
        view.setImageMatrix(matrix);
    }
}
