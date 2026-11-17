package com.github.ma1co.openmemories.framework;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import com.sony.scalar.media.AudioRecord;

/**
 * Microphone capture and speaker playback.
 * On a camera this uses {@code com.sony.scalar.media.AudioRecord} and {@code AudioTrack}.
 */
public final class AudioDevices {
    public static final String BEEP_OFF = "off";
    public static final String BEEP_ON = "on";
    public static final String BEEP_SHUTTER = "shutter";
    public static final String BEEP_SILENT = "silent";
    public static final String WIND_NOISE_OFF = "off";
    public static final String WIND_NOISE_ON = "on";
    /** Microphone capture. Loaded on its own so a missing recorder class does not block playback or the mixer. */
    static class SonyRecorder {
        private final AudioRecord recorder = new AudioRecord();

        void startRecording() {
            recorder.startRecording();
        }

        void stopRecording() {
            recorder.stopRecording();
        }

        int read(byte[] buffer) {
            return read(buffer, recorder.getReadMarkerPosition(), recorder.getWriteMarkerPosition());
        }

        int read(byte[] buffer, long startFrame, long endFrame) {
            if (buffer == null || buffer.length == 0 || endFrame <= startFrame)
                return 0;
            int size = recorder.getReadEsBufferSize(startFrame, endFrame);
            if (size <= 0)
                return 0;
            if (size > buffer.length)
                size = buffer.length;
            AudioRecord.AudioData data = recorder.readEsBuffer(startFrame, endFrame, buffer, 0, size);
            int read = data == null ? 0 : data.getDataSize();
            if (data != null)
                data.recycle();
            recorder.setReadMarkerPosition(endFrame);
            return read;
        }

        void releaseRecorder() {
            recorder.release();
        }
    }

    /** Speaker playback. */
    static class SonyPlayer {
        private final com.sony.scalar.media.AudioTrack player = new com.sony.scalar.media.AudioTrack(48000, 1, 1, 1);

        void play() {
            player.play();
        }

        void write(byte[] data) {
            player.write(data, 0, data.length, false);
        }

        void stopPlayback() {
            player.stop();
        }

        void releasePlayer() {
            player.release();
        }
    }

    /** Microphone, beep, and volume settings. */
    static class SonyControls {
        private final com.sony.scalar.media.AudioManager audio = new com.sony.scalar.media.AudioManager();

        void setMicrophoneEnabled(boolean enabled) {
            com.sony.scalar.media.AudioManager.Parameters parameters = audio.getParameters();
            parameters.setMicrophoneEnable(enabled);
            audio.setParameters(parameters);
        }

        void setSoundVolume(int volume) {
            com.sony.scalar.media.AudioManager.Parameters parameters = audio.getParameters();
            parameters.setSoundVolume(volume);
            audio.setParameters(parameters);
        }

        int getSoundVolume() {
            return audio.getParameters().getSoundVolume();
        }

        void setOutputBeep(String beep) {
            com.sony.scalar.media.AudioManager.Parameters parameters = audio.getParameters();
            parameters.setOutputBeep(beep);
            audio.setParameters(parameters);
        }

        void setWindNoiseReduction(boolean enabled) {
            com.sony.scalar.media.AudioManager.Parameters parameters = audio.getParameters();
            parameters.setMicrophoneWindNoiseReduction(enabled ? WIND_NOISE_ON : WIND_NOISE_OFF);
            audio.setParameters(parameters);
        }

        void releaseAudio() {
            audio.release();
        }
    }

    private android.media.AudioRecord recorder;
    private AudioTrack player;
    private boolean cameraDevice;
    private SonyRecorder sonyRecorder;
    private Throwable recorderError;
    private SonyPlayer sonyPlayer;
    private Throwable playerError;
    private SonyControls sonyControls;
    private Throwable controlsError;
    private boolean microphoneEnabled = true;
    private int soundVolume;
    private String outputBeep = BEEP_ON;
    private boolean windNoiseReduction;

    public static AudioDevices create() {
        AudioDevices devices = new AudioDevices();
        if (DeviceInfo.getInstance().isCamera()) {
            devices.cameraDevice = true;
            try {
                devices.openRecorder();
            } catch (Throwable t) {
                devices.recorderError = t;
            }
            try {
                devices.openPlayer();
            } catch (Throwable t) {
                devices.playerError = t;
            }
            try {
                devices.openControls();
            } catch (Throwable t) {
                devices.controlsError = t;
            }
        }
        return devices;
    }

    private void openRecorder() {
        try {
            sonyRecorder = new SonyRecorder();
        } catch (Throwable t) {
            recorderError = t;
        }
    }

    private void openPlayer() {
        try {
            sonyPlayer = new SonyPlayer();
        } catch (Throwable t) {
            playerError = t;
        }
    }

    private void openControls() {
        try {
            sonyControls = new SonyControls();
        } catch (Throwable t) {
            controlsError = t;
        }
    }

    private static void requireOpen(Object opened, Throwable error, String what) {
        if (opened != null)
            return;
        if (error instanceof RuntimeException)
            throw (RuntimeException) error;
        if (error instanceof Error)
            throw (Error) error;
        if (error != null)
            throw new RuntimeException(error);
        throw new IllegalStateException(what + " not open");
    }

    public void startRecording() {
        if (cameraDevice) {
            requireOpen(sonyRecorder, recorderError, "audio recorder");
            sonyRecorder.startRecording();
            return;
        }
        int rate = 44100;
        int min = android.media.AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        recorder = new android.media.AudioRecord(android.media.MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, Math.max(min, 4096));
        recorder.startRecording();
    }

    public void stopRecording() {
        if (cameraDevice) {
            requireOpen(sonyRecorder, recorderError, "audio recorder");
            sonyRecorder.stopRecording();
            return;
        }
        if (recorder != null)
            recorder.stop();
    }

    public int read(byte[] buffer) {
        if (cameraDevice) {
            requireOpen(sonyRecorder, recorderError, "audio recorder");
            return sonyRecorder.read(buffer);
        }
        if (recorder == null || buffer == null)
            return 0;
        return recorder.read(buffer, 0, buffer.length);
    }

    /**
     * Reads one recorded span. On a camera {@code startFrame} and {@code endFrame} are the
     * positions passed to {@code AudioRecord.readEsBuffer}. Off a camera they are ignored.
     */
    public int read(byte[] buffer, long startFrame, long endFrame) {
        if (cameraDevice) {
            requireOpen(sonyRecorder, recorderError, "audio recorder");
            return sonyRecorder.read(buffer, startFrame, endFrame);
        }
        return read(buffer);
    }

    public void play() {
        if (cameraDevice) {
            requireOpen(sonyPlayer, playerError, "audio player");
            sonyPlayer.play();
            return;
        }
        int rate = 44100;
        int min = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        player = new AudioTrack(AudioManager.STREAM_MUSIC, rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, Math.max(min, 4096), AudioTrack.MODE_STREAM);
        player.play();
    }

    public void write(byte[] data) {
        if (data == null)
            return;
        if (cameraDevice) {
            requireOpen(sonyPlayer, playerError, "audio player");
            sonyPlayer.write(data);
            return;
        }
        if (player != null)
            player.write(data, 0, data.length);
    }

    public void stopPlayback() {
        if (cameraDevice) {
            requireOpen(sonyPlayer, playerError, "audio player");
            sonyPlayer.stopPlayback();
            return;
        }
        if (player != null)
            player.stop();
    }

    public void release() {
        if (cameraDevice) {
            if (sonyRecorder != null)
                sonyRecorder.releaseRecorder();
            if (sonyPlayer != null)
                sonyPlayer.releasePlayer();
            if (sonyControls != null)
                sonyControls.releaseAudio();
            sonyRecorder = null;
            sonyPlayer = null;
            sonyControls = null;
            return;
        }
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

    public void setMicrophoneEnabled(boolean enabled) {
        microphoneEnabled = enabled;
        if (cameraDevice) {
            requireOpen(sonyControls, controlsError, "audio controls");
            sonyControls.setMicrophoneEnabled(enabled);
        }
    }

    public boolean isMicrophoneEnabled() {
        return microphoneEnabled;
    }

    public void setSoundVolume(int volume) {
        soundVolume = volume;
        if (cameraDevice) {
            requireOpen(sonyControls, controlsError, "audio controls");
            sonyControls.setSoundVolume(volume);
        }
    }

    public int getSoundVolume() {
        if (cameraDevice) {
            requireOpen(sonyControls, controlsError, "audio controls");
            return sonyControls.getSoundVolume();
        }
        return soundVolume;
    }

    public void setOutputBeep(String beep) {
        outputBeep = beep;
        if (cameraDevice) {
            requireOpen(sonyControls, controlsError, "audio controls");
            sonyControls.setOutputBeep(beep);
        }
    }

    public String getOutputBeep() {
        return outputBeep;
    }

    public void setWindNoiseReduction(boolean enabled) {
        windNoiseReduction = enabled;
        if (cameraDevice) {
            requireOpen(sonyControls, controlsError, "audio controls");
            sonyControls.setWindNoiseReduction(enabled);
        }
    }

    public boolean isWindNoiseReduction() {
        return windNoiseReduction;
    }
}
