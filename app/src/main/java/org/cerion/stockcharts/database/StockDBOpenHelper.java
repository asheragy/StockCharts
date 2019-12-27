package org.cerion.stockcharts.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.cerion.stockcharts.database.Tables.Dividends;
import org.cerion.stockcharts.database.Tables.Positions;
import org.cerion.stocks.core.model.Interval;

public class StockDBOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = StockDBOpenHelper.class.getSimpleName();
    private static final int DATABASE_VERSION = 11;
    private static final String DATABASE_NAME = "stocks.db";

    //Singleton class
    private static StockDBOpenHelper mInstance;
    private StockDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public synchronized static StockDBOpenHelper getInstance(Context context)
    {
        if(mInstance == null)
            mInstance = new StockDBOpenHelper(context.getApplicationContext());

        return mInstance;
    }

    public static class HistoricalDates {
        public static final String TABLE_HISTORICAL_DATES_DAILY = "historical_dates_daily";
        public static final String TABLE_HISTORICAL_DATES_WEEKLY = "historical_dates_weekly";
        public static final String TABLE_HISTORICAL_DATES_MONTHLY = "historical_dates_monthly";
        public static final String TABLE_HISTORICAL_DATES_DIVIDENDS = "historical_dates_dividends";

        public static final String _SYMBOL = "symbol";
        public static final String _UPDATED = "updated";
        public static final String _FIRST = "first";
        public static final String _LAST = "last";

        public static final String[] ALL_COLUMNS = { _SYMBOL, _UPDATED, _FIRST, _LAST };

        public static String getTableName(Interval interval) {
            if(interval == Interval.WEEKLY)
                return TABLE_HISTORICAL_DATES_WEEKLY;
            if(interval == Interval.MONTHLY)
                return TABLE_HISTORICAL_DATES_MONTHLY;

            return TABLE_HISTORICAL_DATES_DAILY;
        }

        public static String getCreate(String tableName) {
            return "create table " + tableName + "("
                    + _SYMBOL + " TEXT NOT NULL, "
                    + _UPDATED + " INTEGER NOT NULL, "
                    + _FIRST + " INTEGER NOT NULL, "
                    + _LAST + " INTEGER NOT NULL, "
                    + "PRIMARY KEY (" + _SYMBOL + ")"
                    + ")";
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //db.execSQL("PRAGMA foreign_keys=ON;");
        db.execSQL(Dividends.SQL_CREATE);
        db.execSQL(Positions.SQL_CREATE);
        db.execSQL(HistoricalDates.getCreate(HistoricalDates.TABLE_HISTORICAL_DATES_DAILY));
        db.execSQL(HistoricalDates.getCreate(HistoricalDates.TABLE_HISTORICAL_DATES_WEEKLY));
        db.execSQL(HistoricalDates.getCreate(HistoricalDates.TABLE_HISTORICAL_DATES_MONTHLY));
        db.execSQL(HistoricalDates.getCreate(HistoricalDates.TABLE_HISTORICAL_DATES_DIVIDENDS));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade old=" + oldVersion + " new=" + newVersion);

        switch(oldVersion) {
            case 8:
                db.execSQL("DROP TABLE IF EXISTS " + Dividends.TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + Positions.TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + HistoricalDates.TABLE_HISTORICAL_DATES_DAILY);
                db.execSQL("DROP TABLE IF EXISTS " + HistoricalDates.TABLE_HISTORICAL_DATES_WEEKLY);
                db.execSQL("DROP TABLE IF EXISTS " + HistoricalDates.TABLE_HISTORICAL_DATES_MONTHLY);
                onCreate(db);
            case 9:
                db.execSQL(HistoricalDates.getCreate(HistoricalDates.TABLE_HISTORICAL_DATES_DIVIDENDS));
            case 10:
                db.execSQL(String.format("ALTER TABLE %s ADD COLUMN %s INTEGER DEFAULT 0", Positions.TABLE_NAME, Positions._ACCOUNTID));
        }
    }

}


