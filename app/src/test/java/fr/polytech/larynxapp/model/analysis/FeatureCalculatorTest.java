package fr.polytech.larynxapp.model.analysis;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import fr.polytech.larynxapp.model.audio.AudioData;

import static org.junit.Assert.*;

public class FeatureCalculatorTest
{
    private List<Float> pitches;
    private List<Integer> pitchesPositions;
    private FeatureCalculator featureCalculator;

    // Sample rate example to process faster - normally 44100
    private int sampleRate = 100;

    @Before
    public void setup()
    {
        AudioData audioData = new AudioData();
        pitches = new ArrayList<>();
        pitchesPositions = new ArrayList<>();

        for(short i = 1; i < sampleRate; i++)
            audioData.addData(i);
        audioData.processData();

        pitches.add((float) 10);

        //Add a decimal value to check if the floor is called or not
        //(100 / 1.5 = 66.666 with floor should be 66)
        pitches.add((float) 1.5);

        featureCalculator = new FeatureCalculator();
        featureCalculator.setData(audioData.getData_processed());
        featureCalculator.setPitches(pitches);
        featureCalculator.setSampleRate(sampleRate);
    }

    @Test
    public void testSetPeriods()
    {
        featureCalculator.setPeriods();
        assertFalse(featureCalculator.getPeriods().isEmpty());
        int periodsSize = featureCalculator.getPeriods().size();
        assertEquals(pitches.size(), featureCalculator.getPeriods().size());
        for(int i = 0 ; i < periodsSize; i++)
            assertEquals(Math.floor(sampleRate / featureCalculator.getPitches().get(i)), featureCalculator.getPeriods().get(i), 0.001);
        assertEquals(66, featureCalculator.getPeriods().get(periodsSize - 1), 0.001);

    }

    @Test
    public void testSetPitchesPositions()
    {
        featureCalculator.setPeriods();
        featureCalculator.setPitchesPositions();
        pitchesPositions = featureCalculator.getPitchesPositions();
        assertFalse(pitchesPositions.isEmpty());
        assertEquals(Math.floor(sampleRate / pitches.get(0)) - 1, pitchesPositions.get(0), 0.001);
        assertEquals(Math.floor((sampleRate / pitches.get(1)) + (sampleRate / pitches.get(0))) - 1, pitchesPositions.get(1), 0.001);
    }

    @Test
    public void testGetJitter()
    {
        //TODO
    }

    @Test
    public void testGetShimmer()
    {
        //TODO
    }

    @Test
    public void testGetF0()
    {
        assertFalse(pitches.isEmpty());
        assertEquals((pitches.get(0) + pitches.get(1)) / pitches.size(), featureCalculator.getF0(), 0.001);
    }
}