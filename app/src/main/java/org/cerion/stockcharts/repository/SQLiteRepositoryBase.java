package org.cerion.stockcharts.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.cerion.stockcharts.database.StockDBOpenHelper;
import org.cerion.stockcharts.database.Tables;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.web.IYahooFinance;
import org.cerion.stocklist.web.YahooFinance;

import java.io.File;
import java.util.Date;

public abstract class SQLiteRepositoryBase {

    private SQLiteOpenHelper mOpenHelper;
    private static final String TAG = SQLiteRepositoryBase.class.getSimpleName();
    protected static IYahooFinance mYahooFinance = new YahooFinance(); // TODO yahooFinance class should only be used in this file

    public SQLiteRepositoryBase(Context context) {
        this(StockDBOpenHelper.getInstance(context));
    }

    public SQLiteRepositoryBase(SQLiteOpenHelper openHelper) {
        mOpenHelper = openHelper;
    }

    protected SQLiteDatabase open() {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        //db.execSQL("PRAGMA foreign_keys = ON;");
        return db;
    }

    protected SQLiteDatabase openReadOnly() {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        //db.execSQL("PRAGMA foreign_keys = ON;");
        return db;
    }

    public void insert(String sTable, ContentValues values) {
        SQLiteDatabase db = open();
        insert(db,sTable,values);
        db.close();
    }

    public void insert(SQLiteDatabase db, String sTable, ContentValues values) {
        long result = db.insert(sTable, null, values);
        if(result < 0)
            Log.e(TAG, "insert: " + values.toString());
    }

    protected void update(String sTable, ContentValues values, String sWhere) {
        SQLiteDatabase db = open();
        update(db,sTable,values,sWhere);
        db.close();
    }

    protected void update(SQLiteDatabase db, String sTable, ContentValues values, String sWhere) {
        long result = db.update(sTable, values, sWhere, null);
        if(result < 0)
            Log.e(TAG, "update: " + values.toString() + " where: " + sWhere);
        else if(result >= 0)
            Log.d(TAG, "updated " + result + " rows");
    }

    protected void delete(String sTable, String sWhere) {
        SQLiteDatabase db = open();
        delete(db,sTable,sWhere);
        db.close();
    }

    protected void delete(SQLiteDatabase db, String sTable, String sWhere) {
        long result = db.delete(sTable, sWhere, null);
        if(result < 0)
            Log.e(TAG, "delete: " + sWhere);
        else
            Log.d(TAG, "deleted " + result + " rows");
    }

    protected void deleteAll(String table) {
        SQLiteDatabase db = open();
        db.execSQL("delete from "+ table);
        db.close();
    }

    protected void optimize() {
        SQLiteDatabase db = open();
        db.execSQL("VACUUM");
        db.close();
    }

    public void log() {
        SQLiteDatabase db = openReadOnly();
        Cursor c = db.rawQuery("SELECT count(*) FROM " + StockDBOpenHelper.Symbols.TABLE_NAME, null);

        if(c != null)
        {
            if (c.moveToFirst())
            {
                int count = c.getInt(0);
                Log.d(TAG,"Symbols = " + count);
            }
            c.close();
        }

        // Prices
        for(Interval interval : Interval.values()) {
            if (interval == Interval.QUARTERLY)
                break;

            Log.d(TAG, interval.toString());

            c = db.rawQuery(String.format("SELECT %s,count(*) FROM %s GROUP BY %s", StockDBOpenHelper.Prices._SYMBOL, StockDBOpenHelper.Prices.getTableName(interval), StockDBOpenHelper.Prices._SYMBOL), null);
            if (c != null) {
                while (c.moveToNext()) {
                    String symbol = c.getString(0);
                    int count = c.getInt(1);
                    Log.d(TAG, "  " + symbol + ": " + count);
                }
                c.close();
            }
        }

        // History Dates
        /*
        for(Interval interval : Interval.values()) {
            if (interval == Interval.QUARTERLY)
                break;

            Log.d(TAG, "HISTORY " + interval.toString());

            c = db.rawQuery(String.format("SELECT %s,%s FROM %s", StockDBOpenHelper.HistoricalDates._SYMBOL, StockDBOpenHelper.HistoricalDates._UPDATED, StockDBOpenHelper.HistoricalDates.getTableName(interval)), null);
            if (c != null) {
                while (c.moveToNext()) {
                    String symbol = c.getString(0);
                    long date = c.getLong(1);
                    Log.d(TAG, "  " + symbol + ":\t" + new Date(date));
                }
                c.close();
            }
        }
        */

        logHistoryTable(db, StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_DAILY);
        logHistoryTable(db, StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_WEEKLY);
        logHistoryTable(db, StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_MONTHLY);
        logHistoryTable(db, StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_DIVIDENDS);

        Log.d(TAG, Tables.Dividends.TABLE_NAME);

        c = db.rawQuery(String.format("SELECT %s,count(*) FROM %s GROUP BY %s", Tables.Dividends._SYMBOL, Tables.Dividends.TABLE_NAME, Tables.Dividends._SYMBOL), null);
        if (c != null) {
            while (c.moveToNext()) {
                String symbol = c.getString(0);
                int count = c.getInt(1);
                Log.d(TAG, "  " + symbol + ": " + count);
            }
            c.close();
        }

        Log.d(TAG, StockDBOpenHelper.Positions.TABLE_NAME);
        c = db.rawQuery(String.format("SELECT count(*) FROM %s", StockDBOpenHelper.Positions.TABLE_NAME), null);
        if (c != null) {
            if(c.moveToNext()) {
                Log.d(TAG, "  count=" + c.getInt(0));
            }
            c.close();
        }

        db.close();
    }

    private void logHistoryTable(SQLiteDatabase db, String tableName) {
        Log.d(TAG, "HISTORY " + tableName);

        Cursor c = db.rawQuery(String.format("SELECT %s,%s FROM %s", StockDBOpenHelper.HistoricalDates._SYMBOL, StockDBOpenHelper.HistoricalDates._UPDATED, tableName), null);
        if (c != null) {
            while (c.moveToNext()) {
                String symbol = c.getString(0);
                long date = c.getLong(1);
                Log.d(TAG, "  " + symbol + ":\t" + new Date(date));
            }
            c.close();
        }
    }

    protected long getDbSize() {
        return new File(getPath()).length();
    }

    private String mPath = null;
    private String getPath() {
        if (mPath == null) {
            SQLiteDatabase db = openReadOnly();
            mPath = db.getPath();
            db.close();
        }

        return mPath;
    }
}
