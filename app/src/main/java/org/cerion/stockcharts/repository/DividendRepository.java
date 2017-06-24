package org.cerion.stockcharts.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.cerion.stockcharts.database.StockDBOpenHelper;
import org.cerion.stocklist.model.Dividend;
import org.cerion.stockcharts.database.Tables.Dividends;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DividendRepository extends SQLiteRepositoryBase {

    public DividendRepository(Context context) {
        super(StockDBOpenHelper.getInstance(context));
    }

    public void add(String symbol, List<Dividend> list) {
        SQLiteDatabase db = open();
        db.beginTransaction();

        String table = Dividends.TABLE_NAME;
        //Delete current data before re-adding new
        delete(db, table, String.format("%s='%s'", Dividends._SYMBOL, symbol));

        for(Dividend d : list) {
            ContentValues values = new ContentValues();
            values.put(Dividends._SYMBOL, symbol);
            values.put(Dividends._DATE, d.mDate.getTime());
            values.put(Dividends._AMOUNT, d.mDividend);
            insert(db, table, values);
        }

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    // TODO insert to database here
    // TODO Also add something similar to what is done with prices
    public List<Dividend> getLatest(String symbol) {
        return mYahooFinance.getDividends(symbol);
    }

    public List<Dividend> get(String symbol) {
        SQLiteDatabase db = openReadOnly();

        String where = String.format(Dividends._SYMBOL + "='%s'", symbol);
        Cursor c = db.query(Dividends.TABLE_NAME, null, where, null, null, null, null);
        List<Dividend> result = new ArrayList<>();

        if(c != null) {
            while (c.moveToNext()) {
                Date date = new Date(c.getLong(c.getColumnIndexOrThrow(Dividends._DATE)));
                float amount = c.getFloat(c.getColumnIndexOrThrow(Dividends._AMOUNT));

                Dividend d = new Dividend(date, amount);
                result.add(d);
            }
            c.close();
        }

        db.close();
        return result;
    }
}
