package org.cerion.stockcharts.repository;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public abstract class SQLiteRepositoryBase {
    private static final String TAG = SQLiteRepositoryBase.class.getSimpleName();
    protected SQLiteDatabase db;

    public SQLiteRepositoryBase(SQLiteOpenHelper openHelper) {
        db = openHelper.getWritableDatabase();
    }

    public void insert(String sTable, ContentValues values) {
        long result = db.insert(sTable, null, values);
        if(result < 0)
            Log.e(TAG, "insert: " + values.toString());
    }

    protected void update(String table, ContentValues values, String whereField, Object whereValue) {
        String where;
        if (whereValue instanceof Integer)
            where = String.format("%s=%d", whereField, (int)whereValue);
        else
            throw new RuntimeException("unimplemented type");

        update(table, values, where);
    }

    protected void update(String sTable, ContentValues values, String sWhere) {
        long result = db.update(sTable, values, sWhere, null);
        if(result < 0)
            Log.e(TAG, "update: " + values.toString() + " where: " + sWhere);
        else if(result >= 0)
            Log.d(TAG, "updated " + result + " rows");
    }

    protected void delete(String sTable, String sWhere) {
        long result = db.delete(sTable, sWhere, null);
        if(result < 0)
            Log.e(TAG, "delete: " + sWhere);
        else
            Log.d(TAG, "deleted " + result + " rows");
    }

    protected void deleteAll(String table) {
        db.execSQL("delete from "+ table);
    }

    protected void optimize() {
        db.execSQL("VACUUM");
    }

}
