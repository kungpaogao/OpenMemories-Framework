package com.github.ma1co.openmemories.framework;

import android.view.SurfaceHolder;
import com.sony.scalar.media.MediaRecorder;

import java.io.IOException;

/**
 * Movie recording.
 * On a camera this is {@code com.sony.scalar.media.MediaRecorder} bound to a {@link Camera}.
 * Elsewhere it is {@code android.media.MediaRecorder} writing an MPEG-4 file.
 */
public class MovieRecorder {
    /** Sony movie recorder. */
    public static class DeviceRecorder extends MovieRecorder {
        private final MediaRecorder recorder = new MediaRecorder();

        @Override
        public void setCamera(Camera camera) {
            if (camera instanceof Camera.DeviceCamera)
                recorder.setCamera(((Camera.DeviceCamera) camera).nativeCamera());
        }

        @Override
        public void setOutputMedia(String mediaId) {
            recorder.setOutputMedia(mediaId);
        }

        @Override
        public void prepare() {
            recorder.prepare();
        }

        @Override
        public void start() {
            recorder.start();
        }

        @Override
        public void stop() {
            recorder.stop();
        }

        @Override
        public void release() {
            recorder.release();
        }
    }

    private android.media.MediaRecorder recorder;
    private String path;
    private SurfaceHolder preview;
    private boolean prepared;

    public static MovieRecorder create() {
        if (DeviceInfo.getInstance().isCamera())
            return new DeviceRecorder();
        return new MovieRecorder();
    }

    public void setCamera(Camera camera) {
    }

    public void setOutputMedia(String mediaId) {
    }

    public void setOutputFile(String path) {
        this.path = path;
    }

    public void setPreviewDisplay(SurfaceHolder holder) {
        preview = holder;
    }

    public void prepare() throws IOException {
        if (path == null)
            throw new IOException("No output file");
        if (preview == null)
            throw new IOException("Call setPreviewDisplay before prepare");
        recorder = new android.media.MediaRecorder();
        recorder.setAudioSource(android.media.MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(android.media.MediaRecorder.VideoSource.CAMERA);
        recorder.setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC);
        recorder.setVideoEncoder(android.media.MediaRecorder.VideoEncoder.H264);
        recorder.setVideoFrameRate(30);
        recorder.setVideoSize(640, 480);
        recorder.setOutputFile(path);
        if (preview != null)
            recorder.setPreviewDisplay(preview.getSurface());
        recorder.prepare();
        prepared = true;
    }

    public void start() {
        if (prepared)
            recorder.start();
    }

    public void stop() {
        if (prepared) {
            try {
                recorder.stop();
            } catch (RuntimeException ignored) {
            }
        }
    }

    public void release() {
        if (recorder != null)
            recorder.release();
        recorder = null;
        prepared = false;
    }
}
