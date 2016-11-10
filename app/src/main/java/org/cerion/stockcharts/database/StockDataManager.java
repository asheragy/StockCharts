package org.cerion.stockcharts.database;

import android.content.Context;
import android.util.Log;

import org.cerion.stockcharts.charts.ChartViewActivity;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.model.HistoricalDates;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.YahooFinance;

import java.util.Calendar;

public class StockDataManager {
    private static final String TAG = StockDataManager.class.getSimpleName();
    private StockDataStore mDb;

    public StockDataManager(Context context) {
        mDb = StockDB.getInstance(context);
    }

    public void updatePrices(String symbol, Interval interval) {
        Calendar cal = Calendar.getInstance();
        cal.set(1990, Calendar.JANUARY, 1);
        //PriceList list = YahooFinance.getPrices(symbol, interval, cal);
        //TODO limit for now
        PriceList list = YahooFinance.getPrices(symbol, interval, 500);

        mDb.addPriceList(list);
        Log.d(TAG,"Updated prices for " + symbol);
        mDb.log();
    }

    public PriceList getLatestPrices(String symbol, Interval interval) {
        HistoricalDates dates = mDb.getHistoricalDates(symbol, interval);
        if(dates == null) { // TODO, need to update based on last updated date and interval
            updatePrices(symbol, interval);
        }

        return mDb.getPriceList(symbol, interval);
    }

}
