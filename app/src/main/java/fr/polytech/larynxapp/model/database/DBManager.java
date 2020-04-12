package fr.polytech.larynxapp.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import fr.polytech.larynxapp.model.Record;

import static fr.polytech.larynxapp.model.database.DBHelper.TABLE_NAME;

/**
 * Class used to manage the database
 */
public class DBManager {

	/**
	 * The database helper.
	 */
	private DBHelper helper;

	/**
	 * The database.
	 */
	private SQLiteDatabase db;
	
	/**
	 * DBManager sole builder.
	 *
	 * @param context the context for database helper generation
	 */
	public DBManager(Context context ) {
		helper = new DBHelper( context );
		db = helper.getWritableDatabase();
		helper.onCreate( db );
	}
	
	/**
	 * Close the dataBase.
	 */
	public void closeDB() {
		db.close();
	}
	
	/**
	 * Add the record into dataBase.
	 *
	 * @param record the record to add
	 * @return true if the record is added else return false
	 */
	public boolean add(Record record) {

		boolean isAdded;
		db.beginTransaction();
		try
		{
			// Create a new map of values, where column names are the keys
			ContentValues values = new ContentValues();
			values.put("Name", record.getName());
			values.put("Path", record.getPath());
			values.put("Jitter", record.getJitter());
			values.put("Shimmer", record.getShimmer());
			values.put("F0", record.getF0());

			db.insertOrThrow(TABLE_NAME, null, values);
			db.setTransactionSuccessful();
			isAdded = true;
		}
		catch(Exception e)
		{
			isAdded = false;
		}
		finally
		{
			db.endTransaction();
		}
		return isAdded;
	}
	
	/**
	 * Delete the record of the given name from the dataBase.
	 *
	 * @param name the name of the record to delete
	 * @return true is the record is deleted else return false
	 */
	public boolean deleteByName( String name )
	{
		boolean isDeleted;
		db.beginTransaction();
		try
		{
			db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE Name like '" + name + "'");
			db.setTransactionSuccessful();
			isDeleted = true;
		}
		catch(Exception e)
		{
			isDeleted = false;
		}
		finally
		{
			db.endTransaction();
		}
		return isDeleted;
	}


	/**
	 * Get all the records in the dataBase
	 *
	 * @return the list of all the Records
	 */
	public List<Record> query() {
		ArrayList<Record> records = new ArrayList<>();
		Cursor c       = queryTheCursor();
		while ( c.moveToNext() ) {
			Record record = new Record();
			record.setName( c.getString( c.getColumnIndex( "Name" ) ) );
			record.setPath( c.getString( c.getColumnIndex( "Path" ) ) );
			record.setJitter( c.getDouble( c.getColumnIndex("Jitter") ) );
			record.setShimmer( c.getDouble( c.getColumnIndex("Shimmer") ) );
			record.setF0( c.getDouble( c.getColumnIndex("F0") ) );
			records.add( record );
		}
		c.close();
		return records;
	}


	/**
	 * Gets a specific record in db
	 * @param name record name
	 * @return record found
	 */
	public Record getRecord(String name)
	{
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE Name like ?", new String[]{name});
		cursor.moveToNext();
		Record record = new Record();
		record.setName(cursor.getString(cursor.getColumnIndex("Name")));
		record.setPath(cursor.getString(cursor.getColumnIndex("Path")));
		record.setJitter(cursor.getDouble(cursor.getColumnIndex("Jitter")));
		record.setShimmer(cursor.getDouble(cursor.getColumnIndex("Shimmer")));
		record.setF0(cursor.getDouble(cursor.getColumnIndex("F0")));
		cursor.close();
		return record;
	}

	/**
	 * Updates Voice Features of a specific record
	 * @param name record's name
	 * @param jitter jitter value
	 * @param shimmer shimmer value
	 * @param f0 fundamental frequency value
	 * @return true if record is updated with voice features else return false
	 */
	public boolean updateRecordVoiceFeatures(String name, double jitter, double shimmer, double f0)
	{
		db.beginTransaction();
		boolean isUpdated;
		try
		{
			db.execSQL("UPDATE " + TABLE_NAME + " SET Jitter = " + jitter + ", Shimmer = " + shimmer + ", F0 = " + f0 + " WHERE Name like '" + name + "'");
			db.setTransactionSuccessful();
			isUpdated = true;
		}
		catch(Exception e)
		{
			isUpdated = false;
		}
		finally
		{
			db.endTransaction();
		}
		return isUpdated;
	}

	/**
	 * Returns the Cursor of the database.
	 *
	 * @return the Cursor
	 */
	private Cursor queryTheCursor() {
		return db.rawQuery( "SELECT * FROM " + TABLE_NAME, null );
	}

	/**
	 * Checks if the database is empty or not
	 * @return true if database is empty, false otherwise
	 */
	public boolean isDatabaseEmpty()
	{
		Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
		boolean isEmpty;
		isEmpty = !cursor.moveToFirst();
		return isEmpty;
	}

	/**
	 * Deletes all records from the database
	 * Used for testing
	 */
	public void resetDB()
	{
		db.beginTransaction();
		db.execSQL("DELETE FROM " + TABLE_NAME );
		db.setTransactionSuccessful();
		db.endTransaction();
	}
}
