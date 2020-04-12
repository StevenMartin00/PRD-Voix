package fr.polytech.larynxapp.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Class extending SQLiteOpenHelper for easier database generation and recovery.
 */
public class DBHelper extends SQLiteOpenHelper {
	
	/**
	 * Constant representing the database's version.
	 */
    private static int DB_VERSION = 1;
	
	/**
	 * Constant representing the database's name.
	 */
	private static final String DB_NAME = "voiceDatabase.db";
	
	/**
	 * Constant representing the table's name containing the records.
	 */
    static final String  TABLE_NAME = "Records";

    /**
     * DBHelper sole builder.
	 *
     * @param context the context to use for locating paths to the database
     */
    public DBHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }
    
    /**
     * Override the method for initializing the dataBase
	 *
	 * i.e. : creates the table containing the records if it doesn't already exist.
	 *
     * @param db the database where the table needs to be created
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + TABLE_NAME +
                " (Id integer primary key, Name text, Path text, Jitter real, Shimmer real, F0 real)";
        db.execSQL(sql);

    }

    /**
     * Called when the database needs to be upgraded. The implementation should use this method to drop tables, add tables, or do anything else it needs to upgrade to the new schema version.
	 *
     * @param db the database
     * @param oldVersion the old database version
     * @param newVersion the new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        DB_VERSION = newVersion;
        db.execSQL(sql);
        onCreate(db);
    }
}
