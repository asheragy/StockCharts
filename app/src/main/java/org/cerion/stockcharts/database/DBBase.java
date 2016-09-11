package org.cerion.stockcharts.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public abstract class DBBase {

    private SQLiteOpenHelper mOpenHelper;
    private static final String TAG = DBBase.class.getSimpleName();

    public DBBase(SQLiteOpenHelper openHelper) {
        mOpenHelper = openHelper;
    }

    protected SQLiteDatabase open()
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        //db.execSQL("PRAGMA foreign_keys = ON;");
        return db;
    }

    protected SQLiteDatabase openReadOnly()
    {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        //db.execSQL("PRAGMA foreign_keys = ON;");
        return db;
    }

    protected void insert(String sTable, ContentValues values)
    {
        SQLiteDatabase db = open();
        insert(db,sTable,values);
        db.close();
    }

    protected void insert(SQLiteDatabase db, String sTable, ContentValues values)
    {
        long result = db.insert(sTable, null, values);
        if(result < 0)
            Log.e(TAG, "insert: " + values.toString());
    }

    protected void update(String sTable, ContentValues values, String sWhere)
    {
        SQLiteDatabase db = open();
        update(db,sTable,values,sWhere);
        db.close();
    }

    protected void update(SQLiteDatabase db, String sTable, ContentValues values, String sWhere)
    {
        long result = db.update(sTable, values, sWhere, null);
        if(result < 0)
            Log.e(TAG, "update: " + values.toString() + " where: " + sWhere);
        else if(result >= 0)
            Log.d(TAG, "updated " + result + " rows");
    }

    protected void delete(String sTable, String sWhere)
    {
        SQLiteDatabase db = open();
        delete(db,sTable,sWhere);
        db.close();
    }

    protected void delete(SQLiteDatabase db, String sTable, String sWhere)
    {
        long result = db.delete(sTable, sWhere, null);
        if(result < 0)
            Log.e(TAG, "delete: " + sWhere);
        else
            Log.d(TAG, "deleted " + result + " rows");
    }
}
