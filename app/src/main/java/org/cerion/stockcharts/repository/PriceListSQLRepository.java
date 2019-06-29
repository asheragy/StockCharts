package org.cerion.stockcharts.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.cerion.stockcharts.database.StockDBOpenHelper;
import org.cerion.stocklist.Price;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.model.HistoricalDates;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.repository.PriceListRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PriceListSQLRepository extends SQLiteRepositoryBase implements PriceListRepository {
    private static final String TAG = PriceListSQLRepository.class.getSimpleName();

    public PriceListSQLRepository(Context context) {
        super(StockDBOpenHelper.getInstance(context));
    }

    /*
    public PriceList getLatest(String symbolDescription, Interval interval) throws Exception {
        HistoricalDates dates = getHistoricalDates(symbolDescription, interval);
        boolean update = false;

        if(dates == null) {
            update = true;
        } else {
            Date now = new Date();
            long diff = now.getTime() - dates.LastUpdated.getTime();
            diff /= 1000 * 60 * 60 * 24;
            Log.d(TAG, symbolDescription + " " + interval.name() + " last updated " + dates.LastUpdated + " (" + diff + " days ago)");

            // TODO, smarter updates based on last price obtained and weekends
            if(interval == Interval.DAILY && diff >= 1)
                update = true;
            else if(interval == Interval.WEEKLY && diff > 3)
                update = true;
            else if(interval == Interval.MONTHLY && diff > 7)
                update = true;
        }

        if(update)
            updatePrices(symbolDescription, interval);

        return get(symbolDescription, interval);
    }
    */

    @Override
    public PriceList get(String symbol, Interval interval, int max) {
        throw new RuntimeException("not implemented");
        //List<Price> prices = get(symbolDescription, interval);
        ///return new PriceList(symbolDescription,prices);
    }

    @Override
    public List<Price> get(String symbol, Interval interval) {
        String where = String.format(StockDBOpenHelper.Prices._SYMBOL + "='%s'", symbol);
        Cursor c = db.query(StockDBOpenHelper.Prices.getTableName(interval), null, where, null, null, null, StockDBOpenHelper.Prices._DATE + " ASC");
        List<Price> prices = new ArrayList<>();

        if(c != null) {
            while (c.moveToNext()) {
                Date date = new Date(c.getLong(c.getColumnIndexOrThrow(StockDBOpenHelper.Prices._DATE)));
                float open = c.getFloat(c.getColumnIndexOrThrow(StockDBOpenHelper.Prices._OPEN));
                float high = c.getFloat(c.getColumnIndexOrThrow(StockDBOpenHelper.Prices._HIGH));
                float low = c.getFloat(c.getColumnIndexOrThrow(StockDBOpenHelper.Prices._LOW));
                float close = c.getFloat(c.getColumnIndexOrThrow(StockDBOpenHelper.Prices._CLOSE));
                long volume = c.getLong(c.getColumnIndexOrThrow(StockDBOpenHelper.Prices._VOLUME));

                Price p = new Price(date,open,high,low,close,volume);
                prices.add(p);
            }
            c.close();
        }

        return prices;
    }

    @Override
    public void add(PriceList list) {
        db.beginTransaction();

        String table = StockDBOpenHelper.Prices.getTableName(list.getInterval());
        //Delete current data before re-adding new
        delete(table, String.format("%s='%s'", StockDBOpenHelper.Prices._SYMBOL, list.mSymbol));

        for(Price p : list) {
            ContentValues values = new ContentValues();
            values.put(StockDBOpenHelper.Prices._SYMBOL, list.mSymbol);
            values.put(StockDBOpenHelper.Prices._DATE, p.date.getTime());
            values.put(StockDBOpenHelper.Prices._OPEN, p.open);
            values.put(StockDBOpenHelper.Prices._HIGH, p.high);
            values.put(StockDBOpenHelper.Prices._LOW, p.low);
            values.put(StockDBOpenHelper.Prices._CLOSE, p.getClose());
            values.put(StockDBOpenHelper.Prices._VOLUME, p.volume);
            insert(table, values);
        }

        //Add dates
        // TODO refactor to private function
        Date first = list.getDates()[0];
        Date last = list.getDates()[list.size() - 1];
        table = StockDBOpenHelper.HistoricalDates.getTableName(list.getInterval());
        ContentValues values = new ContentValues();
        values.put(StockDBOpenHelper.HistoricalDates._SYMBOL, list.mSymbol);
        values.put(StockDBOpenHelper.HistoricalDates._FIRST, first.getTime());
        values.put(StockDBOpenHelper.HistoricalDates._LAST, last.getTime());
        values.put(StockDBOpenHelper.HistoricalDates._UPDATED, new Date().getTime());
        db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public HistoricalDates getHistoricalDates(String symbol, Interval interval) {
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

        return result;
    }

    @Override
    public void deleteAll() {
        deleteAll(StockDBOpenHelper.Prices.getTableName(Interval.DAILY));
        deleteAll(StockDBOpenHelper.Prices.getTableName(Interval.WEEKLY));
        deleteAll(StockDBOpenHelper.Prices.getTableName(Interval.MONTHLY));
        deleteAll(StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_DAILY);
        deleteAll(StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_WEEKLY);
        deleteAll(StockDBOpenHelper.HistoricalDates.TABLE_HISTORICAL_DATES_MONTHLY);

        optimize();
    }

    /*
    private void updatePrices(String symbolDescription, Interval interval) throws Exception {
        PriceList list = null;
        try
        {
            Calendar cal = Calendar.getInstance();
            cal.set(1990, Calendar.JANUARY, 1);
            //PriceList list = YahooFinance.getPrices(symbolDescription, interval, cal);
            //TODO limit for now
            list = mYahooFinance.getPrices(symbolDescription, interval, 500);
        }
        catch(Exception e)
        {
            // nothing
        }

        if (list != null && list.size() > 0) {
            add(list);
            Log.d(TAG, "Updated prices for " + symbolDescription);
            //mDb.log();
        }
        else {
            throw new Exception("Failed to get updated prices for " + symbolDescription);
        }
    }
    */
}
