package fr.polytech.larynxapp.model.analysis;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import fr.polytech.larynxapp.model.audio.AudioData;

import static org.junit.Assert.*;

public class FeaturesCalculatorTest
{
    private List<Float> pitches;
    private List<Integer> pitchesPositions;
    private FeaturesCalculator featureCalculator;

    // Sample rate example to process faster - normally 44100
    private int sampleRate = 100;

    @Before
    public void setup()
    {
        AudioData audioData = new AudioData();
        pitches = new ArrayList<>();
        pitchesPositions = new ArrayList<>();

        int duration = 5; // duration of sound
        int sampleRate = 44100; // Hz (maximum frequency is 7902.13Hz (B8))
        int frequency = 100;
        int numSamples = duration * sampleRate;
        double samples[] = new double[numSamples];
        short buffer[] = new short[numSamples];
        for (int i = 0; i < numSamples; ++i)
        {
            samples[i] = Math.sin(2 * Math.PI * i / (sampleRate / frequency)); // Sine wave
            buffer[i] = (short) (samples[i] * Short.MAX_VALUE);  // Higher amplitude increases volume
        }

        for(short j = 1; j < (short) numSamples; j++)
        {
            audioData.addData(buffer[j]);
            pitches.add((float) frequency);
        }

        audioData.processData();



        featureCalculator = new FeaturesCalculator(audioData, pitches);
    }

    /*@Test
    public void getF0()
    {
        assertFalse(pitches.isEmpty());
        assertEquals(100d, featureCalculator.getfundamentalFreq(), 0.01);
    }

    @Test
    public void getShimmer()
    {
        assertEquals(0d, featureCalculator.getShimmer(), 0.01);
    }

    @Test
    public void getJitter()
    {
        assertEquals(0d, featureCalculator.getJitter(), 0.01);
    }*/
}