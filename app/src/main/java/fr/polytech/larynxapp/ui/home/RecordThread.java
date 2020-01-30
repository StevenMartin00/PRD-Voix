package fr.polytech.larynxapp.ui.home;

import fr.polytech.larynxapp.model.audio.AudioCapturer;

/**
 * Recording thread's class whose interface is Runnable
 */
public class RecordThread implements Runnable {
    private AudioCapturer audioCapturer;

    /**
     * Run override.
     */
    @Override
    public void run() {
        // start capturing the audio
        audioCapturer = new AudioCapturer();
        audioCapturer.startCapture();
    }

    /**
     * Stops thread.
     */
    public void stop() {
        // stop capturing the audio
        audioCapturer.stopCapture();
    }
}