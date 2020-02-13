package fr.polytech.larynxapp.model.analysis;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import fr.polytech.larynxapp.model.audio.AudioData;

import static org.junit.Assert.*;

public class FeatureCalculatorTest
{
    private AudioData audioData;
    private List<Float> pitches;

    // Sample rate example to process faster - normally 44100
    private int sampleRate = 100;

    @Before
    public void setup()
    {
        audioData = new AudioData();
        pitches = new ArrayList<>();

        for(short i = 1; i < sampleRate; i++)
            audioData.addData(i);
        audioData.processData();

        for(float j = 1; j < 4; j++)
            pitches.add(j);

        //Add a decimal value to check if the floor is called or not
        pitches.add((float) 12.55);
    }

    @Test
    public void testSetPeriods()
    {
        FeatureCalculator featureCalculator = new FeatureCalculator();
        featureCalculator.setData(audioData.getData_processed());
        featureCalculator.setPitches(pitches);
        featureCalculator.setSampleRate(sampleRate);
        featureCalculator.setPeriods();
        featureCalculator.setPitchesPositions();
        assertFalse(featureCalculator.getPeriods().isEmpty());
        int periodsSize = featureCalculator.getPeriods().size();
        assertEquals(pitches.size(), periodsSize);
        for(int i = 0 ; i < periodsSize; i++)
            assertEquals(Math.floor(sampleRate / featureCalculator.getPitches().get(i)), featureCalculator.getPeriods().get(i), 0.001);
        assertEquals(13, featureCalculator.getPeriods().get(periodsSize - 1), 0.001);
    }

    @Test
    public void setPitchesPositions()
    {

    }

    @Test
    public void testGetJitter()
    {

    }

    @Test
    public void testGetShimmer()
    {

    }

    @Test
    public void testGetF0()
    {

    }
}