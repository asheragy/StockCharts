package org.cerion.stockcharts.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import org.cerion.stocklist.Enums;

public class StockDataProvider extends ContentProvider {

    private static final String AUTHORITY = "org.cerion.stockcharts.stockdataprovider";
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteDatabase db;
    private StockDBOpenHelper openHelper;

    private static final String PRICES_PATH = "prices"; // TODO should be 3 different interval tables
    public static final Uri CONTENT_URI_PRICES = Uri.parse("content://" + AUTHORITY + "/" + PRICES_PATH);

    // Codes
    private static final int PRICES = 1;

    private static final String TABLE = StockDBOpenHelper.Prices.getTableName(Enums.Interval.MONTHLY); // TODO normally resolved by URI

    static {
        sUriMatcher.addURI(AUTHORITY, PRICES_PATH + "/*", PRICES);
    }

    @Override
    public boolean onCreate() {
        openHelper = StockDBOpenHelper.getInstance(getContext());
        //db = openHelper.getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        int match = sUriMatcher.match(uri);
        db = openHelper.getWritableDatabase();
        switch(match) {
            case PRICES:
                // TODO, workaround for missing _id column
                String symbol = uri.getLastPathSegment();
                String query = String.format("select rowid _id,* from %s WHERE %s='%s' ORDER BY %s", TABLE, StockDBOpenHelper.Prices._SYMBOL, symbol, StockDBOpenHelper.Prices._DATE);
                return db.rawQuery(query, null);


        }

        return null;
        /*
        // TODO, uri needs to be parsed to know what table and query to use
        return db.query(StockDBOpenHelper.Prices.getTableName(Enums.Interval.MONTHLY), StockDBOpenHelper.Prices.ALL_COLUMNS,
                selection,
                selectionArgs,
                null, //group by
                null, //having
                null); //order by
             */
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    /* To call this
        Uri result = getContentResolver().insert(StockDataProvider.CONTENT_URI_PRICES, new ContentValues());
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //long id = db.insert(TABLE, null ,values);
        //return Uri.parse(PRICES_PATH + "/" + id);
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return db.delete(TABLE, selection, selectionArgs);
        //return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return db.update(TABLE, values, selection, selectionArgs);
        //return 0;
    }
}
