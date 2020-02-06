package fr.polytech.larynxapp.model.database;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.polytech.larynxapp.model.Record;

import static org.junit.Assert.*;

public class DBManagerTest {

    private DBManager dbManager;

    @Before
    public void setup()
    {
        dbManager = new DBManager(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    @After
    public void closeDB() {
        dbManager.closeDB();
    }

    @Test
    public void queryAllRecords()
    {
        Record recordTest1 = new Record("Record Test 1", "Path Test 1", 0, 0, 0);
        Record recordTest2 = new Record("Record Test 2", "Path Test 2", 0, 0,0);
        dbManager.add(recordTest1);
        dbManager.add(recordTest2);
        assertNotNull(dbManager.query());
    }

    @Test
    public void add()
    {
        Record record = new Record("Record Test", "Path Test", 0, 0, 0);
        assertTrue(dbManager.add(record));
    }

    @Test
    public void getRecord()
    {
        Record record = new Record("Record Test", "Path Test", 0, 0, 0);
        Record recordToTest = dbManager.getRecord("Record Test");
        assertEquals(recordToTest.getName(), record.getName());
        assertEquals(recordToTest.getPath(), record.getPath());
        assertEquals(recordToTest.getJitter(), record.getJitter(), 0.001);
        assertEquals(recordToTest.getShimmer(), record.getShimmer(), 0.001);
        assertEquals(recordToTest.getF0(), record.getF0(), 0.001);
     }

    @Test
    public void updateRecordVoiceFeatures()
    {
        String name = "Record Test";
        double jitter = 14.5;
        double shimmer = 5.6;
        double f0 = 185;
        assertTrue(dbManager.updateRecordVoiceFeatures(name, jitter, shimmer, f0));
    }

    @Test
    public void deleteByName()
    {
        String name = "Record Test";
        assertTrue(dbManager.deleteByName(name));
    }

    @Test
    public void isDatabaseEmpty()
    {
        Record record = new Record("Record Test", "Path Test", 0, 0, 0);
        dbManager.add(record);
        assertFalse(dbManager.isDatabaseEmpty());
    }
}