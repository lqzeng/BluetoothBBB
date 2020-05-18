package au.edu.rmit.bluetoothcommunication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "buildings.db";
    public static final String TABLE_NAME = "buildings_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "NAME";
    public static final String COL_3 = "TRAFFIC_COUNT";
    public static final String COL_4 = "VENTILATION";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, TRAFFIC_COUNT INTEGER, VENTILATION INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }



    /*
    public void deleteTable(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
    }*/

    public boolean insertData(String name,String traffic_count,String ventilation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,name);
        contentValues.put(COL_3,traffic_count);
        contentValues.put(COL_4,ventilation);
        long result = db.insert(TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        String TAG = "getAllData";
        Log.d(TAG, "inside getAllData()");
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }

    // getter for graph function
    public Cursor getBuildingData(String buildingNumber){
        String TAG = "getBuildingData";
        Log.d(TAG, "inside getBuildingData(), looking for building " + buildingNumber);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from buildings_table where name = ?" , new String[]{buildingNumber} );
        Log.d(TAG, "returning to graph function");
        return res;
    }

    public boolean updateData(String id,String name,String traffic_count,String ventilation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,id);
        contentValues.put(COL_2,name);
        contentValues.put(COL_3,traffic_count);
        contentValues.put(COL_4,ventilation);
        db.update(TABLE_NAME, contentValues, "ID = ?",new String[] { id });
        return true;
    }

    public Integer deleteData (String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "ID = ?",new String[] {id});
    }

    public void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME);
    }

}