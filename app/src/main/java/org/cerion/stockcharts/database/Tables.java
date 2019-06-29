package org.cerion.stockcharts.database;

import android.provider.BaseColumns;

public class Tables {

    public static class Dividends {
        public static final String TABLE_NAME = "dividends";
        public static final String _SYMBOL = "symbol";
        public static final String _DATE = "date";
        public static final String _AMOUNT = "amount";

        //public static final String[] ALL_COLUMNS = { _SYMBOL, _DATE, _AMOUNT };
        public static String SQL_CREATE =
                "create table " + TABLE_NAME + "("
                        + _SYMBOL + " TEXT NOT NULL, "
                        + _DATE + " INTEGER NOT NULL, "
                        + _AMOUNT + " REAL NOT NULL, "
                        + "PRIMARY KEY (" + _SYMBOL + "," + _DATE + ")"
                        + ")";
    }

    public static class Positions implements BaseColumns {
        public static final String TABLE_NAME = "positions";
        public static final String _SYMBOL = "symbol";
        public static final String _COUNT = "count";
        public static final String _PRICE = "price";
        public static final String _DATE = "date";
        public static final String _DR = "dividendsReinvested";
        public static final String _ACCOUNTID = "accountId";

        public static final String SQL_CREATE = "create table " + TABLE_NAME + "("
                + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + _ACCOUNTID + " INTEGER NOT NULL DEFAULT 0, "
                + _SYMBOL + " TEXT NOT NULL, "
                + _COUNT + " REAL NOT NULL, "
                + _PRICE + " REAL NOT NULL, "
                + _DATE + " INTEGER NOT NULL, "
                + _DR + " INTEGER NOT NULL DEFAULT 0"
                + ")";
    }
}
