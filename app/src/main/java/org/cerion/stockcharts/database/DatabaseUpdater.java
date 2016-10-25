package org.cerion.stockcharts.database;

import android.content.Context;

import org.cerion.stocklist.Enums;
import org.cerion.stocklist.Enums.Interval;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.YahooFinance;

import java.util.Calendar;

public class DatabaseUpdater {

    private StockDataStore db;

    public DatabaseUpdater(Context context) {
        db = StockDB.getInstance(context);
    }

    public void updatePrices(String symbol, Interval interval) {
        Calendar cal = Calendar.getInstance();
        cal.set(1990, Calendar.JANUARY, 1);
        //PriceList list = YahooFinance.getPrices(symbol, interval, cal);
        //TODO limit for now
        PriceList list = YahooFinance.getPrices(symbol, interval, 100);

        db.addPriceList(list);
        db.log();
    }

    public void getLatestPrices(String symbol, Interval interval) {
        //Thread.currentThread() == c.getMainLooper().getThread();
        // Check historical dates
        // Updated if needed
        // Return latest value
    }

}
