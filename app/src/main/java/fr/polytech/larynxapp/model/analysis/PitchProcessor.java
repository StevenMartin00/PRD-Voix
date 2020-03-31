package fr.polytech.larynxapp.model.analysis;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchDetector;

/**
 * Overriding class from TarsosDSP to change constructor
 */
public class PitchProcessor implements AudioProcessor {

    /**
     * The underlying pitch detector;
     */
    private final PitchDetector detector;

    private final PitchDetectionHandler handler;

    /**
     * Initialize a new pitch processor.
     *
     * @param algorithm
     *            An enum defining the algorithm.
     * @param sampleRate
     *            The sample rate of the buffer (Hz).
     * @param bufferSize
     *            The size of the buffer in samples.
     * @param handler
     *            The handler handles detected pitch.
     */
    public PitchProcessor(PitchDetector algorithm, float sampleRate,
                          int bufferSize,
                          PitchDetectionHandler handler) {
        detector = algorithm;
        this.handler = handler;
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] audioFloatBuffer = audioEvent.getFloatBuffer();

        PitchDetectionResult result = detector.getPitch(audioFloatBuffer);


        handler.handlePitch(result,audioEvent);
        return true;
    }

    @Override
    public void processingFinished() {
    }


}
