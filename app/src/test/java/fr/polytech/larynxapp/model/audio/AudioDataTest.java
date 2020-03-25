package fr.polytech.larynxapp.model.audio;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Testing AudioData class
 */
public class AudioDataTest {

    private AudioData audioData;


    /**
     * Tests the addition of data
     */
    @Test
    public void testAddData()
    {
        audioData = new AudioData();
        assertTrue(audioData.getData().isEmpty());
        audioData.addData((short) 0);
        assertFalse(audioData.getData().isEmpty());

        List<Short> data = audioData.getData();
        assertEquals(0, (short) data.get(0));
    }

    /**
     * Tests the processing of data
     */
    @Test
    public void testProcessData()
    {
        audioData = new AudioData();

        assertNull(audioData.getData_processed());

        for(short i = 0; i < 10; i++)
            audioData.addData(i);
        audioData.processData();

        //Process Data removes the first 30% and the last 20%
        assertFalse(audioData.getData_processed().isEmpty());
        assertEquals(0.8*audioData.getDataSize(), audioData.getMaxAmplitude(), 0.01);
        assertEquals(0.3*audioData.getDataSize(), audioData.getMinAmplitude(), 0.01);
    }
}