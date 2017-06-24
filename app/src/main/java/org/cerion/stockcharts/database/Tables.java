package org.cerion.stockcharts.database;

public class Tables {

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
}
