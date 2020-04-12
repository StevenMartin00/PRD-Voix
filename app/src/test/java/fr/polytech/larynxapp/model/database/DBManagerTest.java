package fr.polytech.larynxapp.model.database;

import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import fr.polytech.larynxapp.model.Record;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Testing DBManager class (Database Class mocked with Robolectric)
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.P)
public class DBManagerTest {

    private DBManager dbManager;

    /**
     * TEST : Creation of the database
     */
    @Before
    public void setup()
    {
        dbManager = new DBManager(ApplicationProvider.getApplicationContext());
    }

    /**
     * TEST : Closes the database
     */
    @After
    public void closeDB() {
        dbManager.closeDB();
    }

    /**
     * TEST : Queries the different records added in the database
     */
    @Test
    public void queryAllRecords()
    {
        Record recordTest1 = new Record("Record Test 1", "Path Test 1", 0, 0, 0);
        Record recordTest2 = new Record("Record Test 2", "Path Test 2", 0, 0,0);
        dbManager.add(recordTest1);
        dbManager.add(recordTest2);
        assertNotNull(dbManager.query());
    }

    /**
     * TEST : Adds a test record into the database
     */
    @Test
    public void add()
    {
        Record record = new Record("Record Test", "Path Test", 0, 0, 0);
        assertTrue(dbManager.add(record));
    }

    /**
     * TEST : Gets a specific test record from the database
     */
    @Test
    public void getRecord()
    {
        Record record = new Record("Record Test", "Path Test", 0, 0, 0);
        dbManager.add(record);
        Record recordToTest = dbManager.getRecord("Record Test");
        assertEquals(recordToTest.getName(), record.getName());
        assertEquals(recordToTest.getPath(), record.getPath());
        assertEquals(recordToTest.getJitter(), record.getJitter(), 0.001);
        assertEquals(recordToTest.getShimmer(), record.getShimmer(), 0.001);
        assertEquals(recordToTest.getF0(), record.getF0(), 0.001);
    }

    /**
     * TEST : Updates different values in the database concerning voice features
     */
    @Test
    public void updateRecordVoiceFeatures()
    {
        Record recordToUpdate = new Record("Record Test", "Record Test Path");
        dbManager.add(recordToUpdate);
        double jitter = 14.5;
        double shimmer = 5.6;
        double f0 = 185;
        assertTrue(dbManager.updateRecordVoiceFeatures(recordToUpdate.getName(), jitter, shimmer, f0));
        Record recordUpdated = dbManager.getRecord(recordToUpdate.getName());
        assertEquals(jitter, recordUpdated.getJitter(), 0.001);
        assertEquals(shimmer, recordUpdated.getShimmer(), 0.001);
        assertEquals(f0, recordUpdated.getF0(), 0.001);
    }

    /**
     * TEST : Deletes a record by a specific test name
     */
    @Test
    public void deleteByName()
    {
        Record recordToDelete = new Record("Record Test", "Record Test Path");
        dbManager.add(recordToDelete);
        assertTrue(dbManager.deleteByName(recordToDelete.getName()));
    }

    /**
     * TEST : Checks if the database is empty or not
     */
    @Test
    public void isDatabaseEmpty()
    {
        Record record = new Record("Record Test", "Path Test", 0, 0, 0);
        dbManager.add(record);
        assertFalse(dbManager.isDatabaseEmpty());
        dbManager.deleteByName(record.getName());
        assertTrue(dbManager.isDatabaseEmpty());
    }
}