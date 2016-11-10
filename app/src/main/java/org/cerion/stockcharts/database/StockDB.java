package org.cerion.stockcharts.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.cerion.stockcharts.model.*;
import org.cerion.stockcharts.model.HistoricalDates;
import org.cerion.stocklist.Price;
import org.cerion.stocklist.PriceList;
import org.cerion.stockcharts.database.StockDBOpenHelper.*;
import org.cerion.stocklist.model.Dividend;
import org.cerion.stocklist.model.Interval;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockDB extends DBBase implements StockDataStore
{
    private static final String TAG = StockDB.class.getSimpleName();

    //Singleton class
    private static StockDB mInstance;
    private StockDB(Context context)
    {
        super(StockDBOpenHelper.getInstance(context));
    }

    public synchronized static StockDB getInstance(Context context)
    {
        if(mInstance == null)
            mInstance = new StockDB(context.getApplicationContext());

        return mInstance;
    }

    @Override
    public List<String> getSymbols() {
        SQLiteDatabase db = openReadOnly();

        Cursor c = db.query(Symbols.TABLE_NAME, null, null, null, null, null, Symbols._NAME); //order by name
        List<String> result = new ArrayList<>();

        if (c != null) {
            while (c.moveToNext()) {
                String name = c.getString(c.getColumnIndexOrThrow(Symbols._SYMBOL));
                result.add(name);
            }
            c.close();
        }

        db.close();
        return result;
    }

    @Override
    public void addSymbol(Symbol symbol) {
        ContentValues values = new ContentValues();
        values.put(Symbols._SYMBOL, symbol.getSymbol());
        values.put(Symbols._NAME, symbol.getName());
        values.put(Symbols._EXCHANGE, symbol.getExchange());

        insert(Symbols.TABLE_NAME, values);
    }

    @Override
    public void deleteAllSymbols() {
        delete(Symbols.TABLE_NAME, String.format("%s!='%s'", Symbols._NAME, ".") );
    }

    @Override
    public void addPosition(Position position) {
        ContentValues values = new ContentValues();
        values.put(Positions._SYMBOL, position.getSymbol());
        values.put(Positions._DATE, position.getDate().getTime());
        values.put(Positions._PRICE, position.getOrigPrice());
        values.put(Positions._COUNT, position.getCount());
        values.put(Positions._DR, position.IsDividendsReinvested());

        insert(Positions.TABLE_NAME, values);
    }

    @Override
    public List<Position> getPositions() {
        SQLiteDatabase db = openReadOnly();

        Cursor c = db.query(Positions.TABLE_NAME, null, null, null, null, null, Positions._DATE); //order by date
        List<Position> result = new ArrayList<>();

        if (c != null) {
            while (c.moveToNext()) {

                String symbol = c.getString(c.getColumnIndexOrThrow(Positions._SYMBOL));
                double count = c.getDouble(c.getColumnIndexOrThrow(Positions._COUNT));
                double price = c.getDouble(c.getColumnIndexOrThrow(Positions._PRICE));
                long date = c.getLong(c.getColumnIndexOrThrow(Positions._DATE));
                boolean dr = c.getInt(c.getColumnIndexOrThrow(Positions._DR)) != 0;
                Position p = new Position(symbol, count, price, new Date(date), dr);

                result.add(p);
            }
            c.close();
        }

        db.close();
        return result;
    }

    @Override
    public void deletePosition(Position position) {
        // TODO refine better or use autoID
        delete(Positions.TABLE_NAME, String.format("%s='%s' AND %s=%s", Positions._SYMBOL, position.getSymbol(), Positions._DATE, position.getDate().getTime()) );
    }

    public List<Price> getPrices(String symbol, Interval interval) {
        SQLiteDatabase db = openReadOnly();

        String where = String.format(Prices._SYMBOL + "='%s'", symbol);
        Cursor c = db.query(Prices.getTableName(interval), null, where, null, null, null, null);
        List<Price> prices = new ArrayList<>();

        if(c != null) {
            while (c.moveToNext()) {
                Date date = new Date(c.getLong(c.getColumnIndexOrThrow(Prices._DATE)));
                float open = c.getFloat(c.getColumnIndexOrThrow(Prices._OPEN));
                float high = c.getFloat(c.getColumnIndexOrThrow(Prices._HIGH));
                float low = c.getFloat(c.getColumnIndexOrThrow(Prices._LOW));
                float close = c.getFloat(c.getColumnIndexOrThrow(Prices._CLOSE));
                long volume = c.getLong(c.getColumnIndexOrThrow(Prices._VOLUME));

                Price p = new Price(date,open,high,low,close,volume);
                prices.add(p);
            }
            c.close();
        }

        db.close();
        return prices;
    }

    public PriceList getPriceList(String symbol, Interval interval)
    {
        List<Price> prices = getPrices(symbol, interval);
        return new PriceList(symbol,prices);
    }

    public void addPriceList(PriceList list) {
        SQLiteDatabase db = open();
        db.beginTransaction();

        String table = Prices.getTableName(list.getInterval());
        //Delete current data before re-adding new
        delete(db, table, String.format("%s='%s'", Prices._SYMBOL, list.mSymbol));

        for(Price p : list) {
            ContentValues values = new ContentValues();
            values.put(Prices._SYMBOL, list.mSymbol);
            values.put(Prices._DATE, p.date.getTime());
            values.put(Prices._OPEN, p.open);
            values.put(Prices._HIGH, p.high);
            values.put(Prices._LOW, p.low);
            values.put(Prices._CLOSE, p.close);
            values.put(Prices._VOLUME, p.volume);
            insert(db, table, values);
        }

        //Add dates
        Date first = list.getDates()[0];
        Date last = list.getDates()[list.size() - 1];
        table = StockDBOpenHelper.HistoricalDates.getTableName(list.getInterval());
        ContentValues values = new ContentValues();
        values.put(StockDBOpenHelper.HistoricalDates._SYMBOL, list.mSymbol);
        values.put(StockDBOpenHelper.HistoricalDates._FIRST, first.getTime());
        values.put(StockDBOpenHelper.HistoricalDates._LAST, last.getTime());
        values.put(StockDBOpenHelper.HistoricalDates._UPDATED, new Date().getTime());
        insert(db, table, values);

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    @Override
    public void deletePrices(String symbol, Interval interval) {
        String table = Prices.getTableName(interval);
        delete(table, String.format("%s='%s'", Prices._SYMBOL, symbol) );
    }

    @Override
    public HistoricalDates getHistoricalDates(String symbol, Interval interval) {
        SQLiteDatabase db = openReadOnly();

        String where = String.format(StockDBOpenHelper.HistoricalDates._SYMBOL + "='%s'", symbol);
        Cursor c = db.query(StockDBOpenHelper.HistoricalDates.getTableName(interval), null, where, null, null, null, null);
        HistoricalDates result = null;
        if(c != null) {
            if (c.moveToFirst()) {
                result = new HistoricalDates();
                result.Symbol = symbol;
                result.FirstDate = new Date(c.getLong(c.getColumnIndexOrThrow(StockDBOpenHelper.HistoricalDates._FIRST)));
                result.LastDate = new Date(c.getLong(c.getColumnIndexOrThrow(StockDBOpenHelper.HistoricalDates._LAST)));
                result.LastUpdated = new Date(c.getLong(c.getColumnIndexOrThrow(StockDBOpenHelper.HistoricalDates._UPDATED)));
            }
            c.close();
        }

        db.close();
        return result;
    }

    public void addDividends(String symbol, List<Dividend> list) {
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

    public List<Dividend> getDividends(String symbol) {
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

    public void log()
    {
        SQLiteDatabase db = openReadOnly();
        Cursor c = db.rawQuery("SELECT count(*) FROM " + Symbols.TABLE_NAME, null);

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
            Log.d(TAG, interval.toString());

            c = db.rawQuery(String.format("SELECT %s,count(*) FROM %s GROUP BY %s", Prices._SYMBOL, Prices.getTableName(interval), Prices._SYMBOL), null);
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
        for(Interval interval : Interval.values()) {
            Log.d(TAG, interval.toString());

            c = db.rawQuery(String.format("SELECT %s,count(*) FROM %s GROUP BY %s", StockDBOpenHelper.HistoricalDates._SYMBOL, StockDBOpenHelper.HistoricalDates.getTableName(interval), StockDBOpenHelper.HistoricalDates._SYMBOL), null);
            if (c != null) {
                while (c.moveToNext()) {
                    String symbol = c.getString(0);
                    int count = c.getInt(1);
                    Log.d(TAG, "  " + symbol + ": " + count);
                }
                c.close();
            }
        }

        Log.d(TAG, Dividends.TABLE_NAME);

        c = db.rawQuery(String.format("SELECT %s,count(*) FROM %s GROUP BY %s", Dividends._SYMBOL, Dividends.TABLE_NAME, Dividends._SYMBOL), null);
        if (c != null) {
            while (c.moveToNext()) {
                String symbol = c.getString(0);
                int count = c.getInt(1);
                Log.d(TAG, "  " + symbol + ": " + count);
            }
            c.close();
        }

        Log.d(TAG, Positions.TABLE_NAME);
        c = db.rawQuery(String.format("SELECT count(*) FROM %s", Positions.TABLE_NAME), null);
        if (c != null) {
            if(c.moveToNext()) {
                Log.d(TAG, "  count=" + c.getInt(0));
            }
            c.close();
        }

        db.close();
    }


}