package fr.polytech.larynxapp.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class RecordTest {

    @Test
    public void testToString()
    {
        Record record = new Record("Name", "Path");
        assertEquals(record.getName(), record.toString());
    }
}