package pl.smyt.smsgateway.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper {

    private static final String TAG = "DatabaseHelper";

    // database configuration
    // if you want the onUpgrade to run then change the database_version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "smyt.db";

    //online system connection configuration
    private static final String TABLE_MY_SYSTEM = "my_system";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_SYSTEM_NAME = "name";
    public static final String KEY_OUTBOX_URL = "outbox_url";
    public static final String KEY_STATUS = "system_status";

    //log
    private static final String TABLE_LOG = "log";
    public static final String KEY_SYSTEM = "system";
    public static final String KEY_TIME = "time";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_FLAG = "flag";

    //log
    private static final String TABLE_POLL = "poll";
    public static final String KEY_POLL_TIME = "poll_time"; //in ms

    private DatabaseOpenHelper openHelper;
    private SQLiteDatabase database;

    // this is a wrapper class. that means, from outside world, anyone will communicate with PersonDatabaseHelper,
    // but under the hood actually DatabaseOpenHelper class will perform database CRUD operations
    public DatabaseHelper(Context aContext) {
        openHelper = new DatabaseOpenHelper(aContext);
        database = openHelper.getWritableDatabase();
    }

    public boolean getDuplicateSystemName (String strSystemName) {
        String buildSQL = "SELECT * FROM " + TABLE_MY_SYSTEM +" WHERE " + KEY_SYSTEM_NAME + " = '"+strSystemName+"'";
        Cursor inboxurl = database.rawQuery(buildSQL, null);
        inboxurl.moveToFirst();
        if(inboxurl.isFirst()){
            //yes we have a duplicate
            return true;
        }else{
            return false;
        }
    }

    public boolean getDuplicateSystemName (String strSystemName, String rowId) {
        String buildSQL = "SELECT * FROM " + TABLE_MY_SYSTEM +" WHERE " + KEY_SYSTEM_NAME + " = '"+strSystemName+"' AND "+KEY_ROWID+" != '"+rowId+"'";
        Cursor inboxurl = database.rawQuery(buildSQL, null);
        inboxurl.moveToFirst();
        if(inboxurl.isFirst()){
            //yes we have a duplicate
            return true;
        }else{
            return false;
        }
    }

    public boolean getDuplicateOutBoxURL (String strOutBoxUrl) {
        String buildSQL = "SELECT * FROM " + TABLE_MY_SYSTEM +" WHERE " + KEY_OUTBOX_URL + " = '"+strOutBoxUrl+"'";
        Cursor inboxurl = database.rawQuery(buildSQL, null);
        inboxurl.moveToFirst();
        if(inboxurl.isFirst()){
            //yes we have a duplicate
            return true;
        }else{
            return false;
        }
    }

    public boolean getDuplicateOutBoxURL (String strOutBoxUrl, String rowId) {
        String buildSQL = "SELECT * FROM " + TABLE_MY_SYSTEM +" WHERE " + KEY_OUTBOX_URL + " = '"+strOutBoxUrl+"' AND "+KEY_ROWID+" != '"+rowId+"'";
        Cursor inboxurl = database.rawQuery(buildSQL, null);
        inboxurl.moveToFirst();
        if(inboxurl.isFirst()){
            //yes we have a duplicate
            return true;
        }else{
            return false;
        }
    }


    public void insertSystem (String strSystemName, String strOutboxURL, String strStatus) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SYSTEM_NAME, strSystemName);
        contentValues.put(KEY_OUTBOX_URL, strOutboxURL);
        contentValues.put(KEY_STATUS, strStatus);
        database.insert(TABLE_MY_SYSTEM, null, contentValues);
    }

    public void insertLog (String strSystemName, String strMessage, String strFlag, String strTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SYSTEM, strSystemName);
        contentValues.put(KEY_MESSAGE, strMessage);
        contentValues.put(KEY_FLAG, strFlag);
        contentValues.put(KEY_TIME, strTime);

        if(countAllLogs()>=1000){
            deleteOldestLog();
        }
        database.insert(TABLE_LOG, null, contentValues);

        Log.e(TAG, "Log: " + TABLE_LOG + ": " + strTime + ":" + strMessage + ", " + strFlag);
    }

    public void updateSystem(String rowId, String strSystemName,  String strOutboxURL, String strStatus) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SYSTEM_NAME, strSystemName);
        contentValues.put(KEY_OUTBOX_URL, strOutboxURL);
        contentValues.put(KEY_STATUS, strStatus);
        database.update(TABLE_MY_SYSTEM, contentValues, KEY_ROWID + " = " + rowId, null);
    }

    public void updatePollTime(String rowId, String poll_time) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_POLL_TIME, poll_time);
        database.update(TABLE_POLL, contentValues, KEY_ROWID + " = " + rowId, null);
    }

    public Cursor getAllSystems(){
        String buildSQL = "SELECT * FROM " + TABLE_MY_SYSTEM + " ORDER BY "+KEY_ROWID + " ASC";
        Cursor systems =  database.rawQuery(buildSQL, null);
        systems.moveToFirst();
        return systems;
    }

    public Cursor getAllLogs(){
        String buildSQL = "SELECT * FROM " + TABLE_LOG + " ORDER BY "+KEY_ROWID + " ASC";
        Cursor systems =  database.rawQuery(buildSQL, null);
        systems.moveToFirst();
        return systems;
    }

    public String getTimeToPoll(){
        String buildSQL = "SELECT * FROM " + TABLE_POLL + " ORDER BY "+KEY_ROWID + " ASC";
        Cursor systems =  database.rawQuery(buildSQL, null);
        systems.moveToFirst();
        return systems.getString(systems.getColumnIndex(systems.getColumnName(1)));
    }

    public int countAllLogs(){
        String buildSQL = "SELECT * FROM " + TABLE_LOG + " ORDER BY "+KEY_ROWID + " ASC";
        Cursor systems =  database.rawQuery(buildSQL, null);
        systems.moveToFirst();
        Log.e(TAG, "number of rows in: " + TABLE_LOG + ": " + systems.getCount());
        return systems.getCount();
    }

    public void deleteOldestLog(){
        String buildSQL = "SELECT * FROM " + TABLE_LOG + " ORDER BY "+KEY_ROWID + " ASC";
        Cursor systems =  database.rawQuery(buildSQL, null);
        systems.moveToFirst();
        String rowId = systems.getString(systems.getColumnIndex(systems.getColumnName(0)));
        deleteLog(rowId);
        Log.e(TAG, "Log to delete: "+rowId);
    }

    public void clearLog() {
        String buildSQL = "SELECT * FROM " + TABLE_LOG + " ORDER BY " + KEY_ROWID + " ASC";
        Cursor systems = database.rawQuery(buildSQL, null);
        if (systems.moveToFirst()){
            String rowId = systems.getString(systems.getColumnIndex(systems.getColumnName(0)));
            deleteLog(rowId);
            Log.e(TAG, "Log to delete: " + rowId);
            while (systems.moveToNext()) {
                rowId = systems.getString(systems.getColumnIndex(systems.getColumnName(0)));
                deleteLog(rowId);
                Log.e(TAG, "Log to delete: " + rowId);
            }
        }
    }

    public void deleteSystem(String rowId) {
        database.delete(TABLE_MY_SYSTEM, KEY_ROWID + " = " + rowId, null);
    }

    public void deleteLog(String rowId) {
        database.delete(TABLE_LOG, KEY_ROWID + " = " + rowId, null);
        Log.e(TAG, "Deleting log id: " + rowId);
    }


    public void changeSystemStatus(String rowId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ROWID, rowId);
        if(getSystemStatus(rowId).equalsIgnoreCase("Active")) {
            contentValues.put(KEY_STATUS, "Inactive");
        }else{
            contentValues.put(KEY_STATUS, "Active");
        }
        database.update(TABLE_MY_SYSTEM, contentValues, KEY_ROWID + " = " + rowId, null);
    }

    public String getSystemStatus(String rowId){
        String buildSQL = "SELECT * FROM " + TABLE_MY_SYSTEM + " WHERE "+KEY_ROWID + "='"+rowId+"'";
        Cursor systems =  database.rawQuery(buildSQL, null);
        systems.moveToFirst();
        return systems.getString(systems.getColumnIndex(systems.getColumnName(3)));
    }

    public Cursor getSystemDetails (String rowId) {
        String buildSQL = "SELECT * FROM " + TABLE_MY_SYSTEM +" WHERE " + KEY_ROWID + " = '"+rowId+"'";
        return database.rawQuery(buildSQL, null);
    }

    // this DatabaseOpenHelper class will actually be used to perform database related operation
    private class DatabaseOpenHelper extends SQLiteOpenHelper {
 
        public DatabaseOpenHelper(Context aContext) {
            super(aContext, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            // Create your tables here
        	String buildSQL ="create table " + TABLE_MY_SYSTEM + " ("
                    		+ KEY_ROWID + " integer primary key autoincrement, " //0
                            + KEY_SYSTEM_NAME + " text, " 					   //1
                            + KEY_OUTBOX_URL + " text, "				//2
                            + KEY_STATUS + " text);"; 				//03
            Log.d(TAG, "onCreate SQL: " + buildSQL);
            sqLiteDatabase.execSQL(buildSQL);

            // Create your tables here
            String buildLog ="create table " + TABLE_LOG + " ("
                    + KEY_ROWID + " integer primary key autoincrement, " //0
                    + KEY_SYSTEM + " text, "
                    + KEY_FLAG + " text, "
                    + KEY_TIME + " text, "//1
                    + KEY_MESSAGE + " text);"; 				//03
            Log.d(TAG, "onCreate SQL: " + buildSQL);
            sqLiteDatabase.execSQL(buildLog);

            // Create your tables here
            String buildPollTable ="create table " + TABLE_POLL + " ("
                    + KEY_ROWID + " integer primary key autoincrement, " //0
                    + KEY_POLL_TIME+ " text);"; 				//03
            Log.d(TAG, "onCreate SQL: " + buildPollTable);
            sqLiteDatabase.execSQL(buildPollTable);

            String insertDefaultPoll = "INSERT INTO "+TABLE_POLL+" ("+KEY_POLL_TIME+")VALUES(\"10000\")";
            sqLiteDatabase.execSQL(insertDefaultPoll);
        }
 
        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            // Database schema upgrade code goes here
            String buildSQL = "DROP TABLE IF EXISTS " + TABLE_MY_SYSTEM;
            String buildLog = "DROP TABLE IF EXISTS " + TABLE_LOG;
            String buildPollTable = "DROP TABLE IF EXISTS " + TABLE_POLL;
            Log.d(TAG, "onUpgrade SQL: " + buildSQL);

            sqLiteDatabase.execSQL(buildSQL);       // drop previous table
            sqLiteDatabase.execSQL(buildLog);
            sqLiteDatabase.execSQL(buildPollTable);

            onCreate(sqLiteDatabase);               // create the table from the beginning
        }
    }
    
    public DatabaseHelper open() throws SQLException {
        database = openHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
    	openHelper.close();
    }

}