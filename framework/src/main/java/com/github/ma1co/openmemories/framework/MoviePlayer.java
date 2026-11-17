package com.github.ma1co.openmemories.framework;

import android.view.SurfaceHolder;
import com.sony.scalar.media.MediaPlayer;

import java.io.IOException;

/**
 * Movie playback with frame step.
 * On a camera this is {@code com.sony.scalar.media.MediaPlayer}. Elsewhere it is {@code android.media.MediaPlayer},
 * and frame step seeks by one tenth of a second.
 */
public class MoviePlayer {
    public static final int SPEED_NORMAL = 0;
    public static final int SPEED_FAST_2 = 2;
    public static final int DISPLAY_FULL = 0;
    public static final int DISPLAY_HISTOGRAM = 1;

    /** Sony movie player. */
    public static class DevicePlayer extends MoviePlayer {
        private final MediaPlayer player = new MediaPlayer();

        @Override
        public void setDataSource(String path) throws IOException {
            player.setDataSource(path);
        }

        @Override
        public void setDisplay(SurfaceHolder holder) {
            player.setDisplay(holder);
        }

        @Override
        public void prepare() throws IOException {
            player.prepare();
        }

        @Override
        public void start() {
            player.start();
        }

        @Override
        public void pause() {
            player.pause();
        }

        @Override
        public void stop() {
            player.stop();
        }

        @Override
        public void seekTo(int milliseconds) {
            player.seekTo(milliseconds);
        }

        @Override
        public void stepForward() {
            player.stepForward();
        }

        @Override
        public void stepRewind() {
            player.stepRewind();
        }

        @Override
        public void setPlaybackSpeed(int speed) {
            player.setPlaybackSpeed(speed);
        }

        @Override
        public void setDisplayMode(int mode) {
            player.setDisplayMode(mode);
        }

        @Override
        public int getCurrentPosition() {
            return player.getCurrentPosition();
        }

        @Override
        public int getDuration() {
            return player.getDuration();
        }

        @Override
        public boolean isPlaying() {
            return player.isPlaying();
        }

        @Override
        public void release() {
            player.release();
        }
    }

    private android.media.MediaPlayer player;

    public static MoviePlayer create() {
        if (DeviceInfo.getInstance().isCamera())
            return new DevicePlayer();
        return new MoviePlayer();
    }

    public void setDataSource(String path) throws IOException {
        player = new android.media.MediaPlayer();
        player.setDataSource(path);
    }

    public void setDisplay(SurfaceHolder holder) {
        if (player != null)
            player.setDisplay(holder);
    }

    public void prepare() throws IOException {
        if (player != null)
            player.prepare();
    }

    public void start() {
        if (player != null)
            player.start();
    }

    public void pause() {
        if (player != null)
            player.pause();
    }

    public void stop() {
        if (player != null)
            player.stop();
    }

    public void seekTo(int milliseconds) {
        if (player != null)
            player.seekTo(milliseconds);
    }

    public void stepForward() {
        seekTo(getCurrentPosition() + 100);
    }

    public void stepRewind() {
        seekTo(Math.max(0, getCurrentPosition() - 100));
    }

    public void setPlaybackSpeed(int speed) {
    }

    public void setDisplayMode(int mode) {
    }

    public int getCurrentPosition() {
        return player == null ? 0 : player.getCurrentPosition();
    }

    public int getDuration() {
        return player == null ? 0 : player.getDuration();
    }

    public boolean isPlaying() {
        return player != null && player.isPlaying();
    }

    public void release() {
        if (player != null)
            player.release();
        player = null;
    }
}
