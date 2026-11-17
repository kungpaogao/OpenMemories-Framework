package com.github.ma1co.openmemories.framework;

import com.sony.scalar.media.HighlightMovieMaker;

import java.util.List;

/**
 * Builds a highlight movie from clips already on the card.
 * Supported on a camera only. Completion arrives through {@link Listener}.
 * {@link #execute()} returns {@link #RESULT_OK} when the camera accepts the job, and -1 elsewhere.
 */
public class HighlightMovie {
    public static final String MUSIC_1 = "music1";
    public static final String MUSIC_2 = "music2";
    public static final String MUSIC_3 = "music3";
    public static final String MUSIC_4 = "music4";
    public static final String MUSIC_5 = "music5";
    public static final String MUSIC_6 = "music6";
    public static final String MUSIC_7 = "music7";
    public static final String MUSIC_8 = "music8";
    public static final String LENGTH_15_SEC = "15sec";
    public static final String LENGTH_30_SEC = "30sec";
    public static final String LENGTH_1_MIN = "1min";
    public static final String LENGTH_2_MIN = "2min";
    public static final int RESULT_OK = 0;

    /** Camera highlight maker. */
    public static class DeviceMovie extends HighlightMovie {
        private final HighlightMovieMaker maker = new HighlightMovieMaker();
        private Listener listener;

        @Override
        public boolean isSupported() {
            return true;
        }

        @Override
        public void setListener(Listener listener) {
            this.listener = listener;
            maker.setListener(new HighlightMovieMaker.Listener() {
                @Override public void onPrepare(int result, int count, HighlightMovieMaker maker) {
                }
                @Override public void onExecutingProgress(int percent, HighlightMovieMaker maker) {
                }
                @Override public void onExecute(int result, HighlightMovieMaker maker) {
                    if (DeviceMovie.this.listener != null)
                        DeviceMovie.this.listener.onCompleted(result);
                }
            });
        }

        @Override
        public void setBackgroundMusic(String music) {
            maker.setMovieBgm(music);
        }

        @Override
        public void setLength(String length) {
            maker.setMovieLength(length);
        }

        @Override
        public void prepare(String mediaId, List contents) {
            maker.prepare(mediaId, contents);
        }

        @Override
        public int execute() {
            maker.execute();
            return RESULT_OK;
        }

        @Override
        public void release() {
            maker.release();
        }
    }

    public interface Listener {
        void onCompleted(int result);
    }

    public static HighlightMovie create() {
        if (DeviceInfo.getInstance().isCamera())
            return new DeviceMovie();
        return new HighlightMovie();
    }

    public boolean isSupported() {
        return false;
    }

    public void setBackgroundMusic(String music) {
    }

    public void setLength(String length) {
    }

    public void setListener(Listener listener) {
    }

    public void prepare(String mediaId, List contents) {
    }

    public int execute() {
        return -1;
    }

    public void release() {
    }
}
