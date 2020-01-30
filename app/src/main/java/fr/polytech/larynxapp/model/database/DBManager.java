package fr.polytech.larynxapp.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import fr.polytech.larynxapp.model.Record;
import fr.polytech.larynxapp.model.database.DBHelper;

import static fr.polytech.larynxapp.model.database.DBHelper.TABLE_NAME;

/*
  Created by XU Jiaoqiang on 2018/3/20.
 */

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
		
		//checkDataBase();
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
	 */
	public void add(Record record) {
		
		db.beginTransaction();
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put( "Name", record.getName() );
		values.put( "Path", record.getPath() );
		values.put( "Jitter", record.getJitter() );
		values.put( "Shimmer", record.getShimmer() );
		values.put( "F0", record.getF0() );
		
		db.insertOrThrow( TABLE_NAME, null, values );
		db.setTransactionSuccessful();
		db.endTransaction();
	}
	
	/**
	 * Delete the record of the given name from the dataBase.
	 *
	 * @param name the name of the record to delete
	 */
	public void deleteByName( String name ) {
		String whereClauses = "Name=?";
		String[] whereArgs    = { name };
		db.delete( TABLE_NAME, whereClauses, whereArgs );
	}


	/**
	 * Get all the voices in the dataBase
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
		Cursor cursor = db.rawQuery("SELECT * FROM Voices WHERE Name like ?", new String[]{name});
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
	 * @param jitter
	 * @param shimmer
	 * @param f0
	 */
	public void updateRecordVoiceFeatures(String name, double jitter, double shimmer, double f0)
	{
		String sql = "UPDATE Voices SET Jitter = " + jitter + ", Shimmer = " + shimmer + ", F0 = " + f0 + " WHERE Name like '" + name + "'";
		db.execSQL("UPDATE Voices SET Jitter = " + jitter + ", Shimmer = " + shimmer + ", F0 = " + f0 + " WHERE Name like '" + name + "'");
	}

	/**
	 * Returns the Cursor of the database.
	 *
	 * @return the Cursor
	 */
	private Cursor queryTheCursor() {
		return db.rawQuery( "SELECT * FROM Voices", null );
	}
}
