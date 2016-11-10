package org.cerion.stockcharts.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import org.cerion.stocklist.model.Interval;

public class StockDBOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = StockDBOpenHelper.class.getSimpleName();
    private static final int DATABASE_VERSION = 9;
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

    /* possible naming convention
        TABLE_DIVIDENDS
        DIVIDEND_SYMBOL
        DIVIDEND_DATE
        etc
        SQL_CREATE_DIVIDENDS
        ALL_COLUMNS_DIVIDENDS
     */


    public static class Dividends {
        public static final String TABLE_NAME = "dividends";
        public static final String _SYMBOL = "symbol";
        public static final String _DATE = "date";
        public static final String _AMOUNT = "amount";

        public static final String[] ALL_COLUMNS = { _SYMBOL, _DATE, _AMOUNT };
        public static String SQL_CREATE =
            "create table " + TABLE_NAME + "("
                    + _SYMBOL + " TEXT NOT NULL, "
                    + _DATE + " INTEGER NOT NULL, "
                    + _AMOUNT + " REAL NOT NULL, "
                    + "PRIMARY KEY (" + _SYMBOL + "," + _DATE + ")"
                    + ")";
    }

    public static class Symbols {
        public static final String TABLE_NAME = "symbols";
        public static final String _SYMBOL = "symbol";
        public static final String _NAME = "name";
        public static final String _EXCHANGE = "exchange";

        public static final String SQL_CREATE = "create table " + TABLE_NAME + "("
                + _SYMBOL + " TEXT PRIMARY KEY NOT NULL, "
                + _NAME + " TEXT NOT NULL DEFAULT '', "
                + _EXCHANGE + " TEXT NOT NULL DEFAULT ''"
                + ")";
    }

    static class HistoricalDates {
        static final String TABLE_HISTORICAL_DATES_DAILY = "historical_dates_daily";
        static final String TABLE_HISTORICAL_DATES_WEEKLY = "historical_dates_weekly";
        static final String TABLE_HISTORICAL_DATES_MONTHLY = "historical_dates_monthly";

        static final String _SYMBOL = "symbol";
        static final String _UPDATED = "updated";
        static final String _FIRST = "first";
        static final String _LAST = "last";

        static final String[] ALL_COLUMNS = { _SYMBOL, _UPDATED, _FIRST, _LAST };

        static String getTableName(Interval interval) {
            if(interval == Interval.WEEKLY)
                return TABLE_HISTORICAL_DATES_WEEKLY;
            if(interval == Interval.MONTHLY)
                return TABLE_HISTORICAL_DATES_MONTHLY;

            return TABLE_HISTORICAL_DATES_DAILY;
        }

        public static String getCreate(Interval interval) {

            return "create table " + getTableName(interval) + "("
                    + _SYMBOL + " TEXT NOT NULL, "
                    + _UPDATED + " INTEGER NOT NULL, "
                    + _FIRST + " INTEGER NOT NULL, "
                    + _LAST + " INTEGER NOT NULL, "
                    + "PRIMARY KEY (" + _SYMBOL + ")"
                    + ")";
        }
    }

    public static class Prices {
        static final String TABLE_PRICES_DAILY = "prices_daily";
        static final String TABLE_PRICES_WEEKLY = "prices_weekly";
        static final String TABLE_PRICES_MONTHLY = "prices_monthly";

        public static final String _SYMBOL = "symbol";
        public static final String _DATE = "date";
        public static final String _OPEN = "open";
        public static final String _HIGH = "high";
        public static final String _LOW = "low";
        public static final String _CLOSE = "close";
        public static final String _VOLUME = "volume";
        public static final String[] ALL_COLUMNS = { _SYMBOL, _DATE, _OPEN, _HIGH, _LOW, _CLOSE, _VOLUME };

        public static String getTableName(Interval interval) {
            if(interval == Interval.WEEKLY)
                return TABLE_PRICES_WEEKLY;
            if(interval == Interval.MONTHLY)
                return TABLE_PRICES_MONTHLY;

            return TABLE_PRICES_DAILY;
        }

        public static String getCreate(Interval interval) {

            return "create table " + getTableName(interval) + "("
                    + _SYMBOL + " TEXT NOT NULL, "
                    + _DATE + " INTEGER NOT NULL, "
                    + _OPEN + " REAL NOT NULL, "
                    + _HIGH + " REAL NOT NULL, "
                    + _LOW + " REAL NOT NULL, "
                    + _CLOSE + " REAL NOT NULL, "
                    + _VOLUME + " INTEGER NOT NULL, "
                    + "PRIMARY KEY (" + _SYMBOL + "," + _DATE + ")"
                    + ")";
        }
    }

    public static class Positions implements BaseColumns {
        public static final String TABLE_NAME = "positions";
        public static final String _SYMBOL = "symbol";
        public static final String _COUNT = "count";
        public static final String _PRICE = "price";
        public static final String _DATE = "date";
        public static final String _DR = "dividendsReinvested";

        public static final String SQL_CREATE = "create table " + TABLE_NAME + "("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + _SYMBOL + " TEXT NOT NULL, "
                + _COUNT + " REAL NOT NULL, "
                + _PRICE + " REAL NOT NULL, "
                + _DATE + " INTEGER NOT NULL, "
                + _DR + " INTEGER NOT NULL DEFAULT 0"
                + ")";
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //db.execSQL("PRAGMA foreign_keys=ON;");
        db.execSQL(Symbols.SQL_CREATE);
        db.execSQL(Prices.getCreate(Interval.DAILY));
        db.execSQL(Prices.getCreate(Interval.WEEKLY));
        db.execSQL(Prices.getCreate(Interval.MONTHLY));
        db.execSQL(Dividends.SQL_CREATE);
        db.execSQL(Positions.SQL_CREATE);
        db.execSQL(HistoricalDates.getCreate(Interval.DAILY));
        db.execSQL(HistoricalDates.getCreate(Interval.WEEKLY));
        db.execSQL(HistoricalDates.getCreate(Interval.MONTHLY));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.d(TAG, "onUpgrade");
        db.execSQL("DROP TABLE IF EXISTS " + Symbols.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Prices.TABLE_PRICES_DAILY);
        db.execSQL("DROP TABLE IF EXISTS " + Prices.TABLE_PRICES_WEEKLY);
        db.execSQL("DROP TABLE IF EXISTS " + Prices.TABLE_PRICES_MONTHLY);
        db.execSQL("DROP TABLE IF EXISTS " + Dividends.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Positions.TABLE_NAME);

        if(oldVersion < 9) {
            db.execSQL("DROP TABLE IF EXISTS " + HistoricalDates.TABLE_HISTORICAL_DATES_DAILY);
            db.execSQL("DROP TABLE IF EXISTS " + HistoricalDates.TABLE_HISTORICAL_DATES_WEEKLY);
            db.execSQL("DROP TABLE IF EXISTS " + HistoricalDates.TABLE_HISTORICAL_DATES_MONTHLY);
        }

        onCreate(db);
    }

}


