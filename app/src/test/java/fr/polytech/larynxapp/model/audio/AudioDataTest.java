package fr.polytech.larynxapp.model.audio;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class AudioDataTest {

    private AudioData audioData;

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

    @Test
    public void testProcessData()
    {
        audioData = new AudioData();

        assertNull(audioData.getData_processed());

        for(short i = 0; i <= 10; i++)
            audioData.addData(i);
        audioData.processData();

        assertFalse(audioData.getData_processed().isEmpty());
        assertEquals(audioData.getData().size(), audioData.getData_processed().size());
        assertEquals(10, audioData.getMaxAmplitude());
        assertEquals(0, audioData.getMinAmplitude());
    }
}