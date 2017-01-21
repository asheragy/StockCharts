package org.cerion.stockcharts.database;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.cerion.stockcharts.model.HistoricalDates;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.model.Symbol;
import org.cerion.stocklist.web.IYahooFinance;
import org.cerion.stocklist.web.YahooFinance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class StockDataManager {
    private static final String TAG = StockDataManager.class.getSimpleName();
    private StockDataStore mDb;
    private IYahooFinance mYahooFinance; // TODO yahooFinance class should only be used in this file
    private Context mContext;

    public StockDataManager(Context context) {
        mDb = StockDB.getInstance(context);
        mContext = context;
        mYahooFinance = new YahooFinance();
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
        boolean update = false;

        if(dates == null) {
            update = true;
        } else {
            Date now = new Date();
            long diff = now.getTime() - dates.LastUpdated.getTime();
            diff /= 1000 * 60 * 60 * 24;
            Log.d(TAG, symbol + " " + interval.name() + " last updated " + dates.LastUpdated + " (" + diff + " days ago)");

            // TODO, smarter updates based on last price obtained and weekends
            if(interval == Interval.DAILY && diff >= 1)
                update = true;
            else if(interval == Interval.WEEKLY && diff > 3)
                update = true;
            else if(interval == Interval.MONTHLY && diff > 7)
                update = true;
        }

        if(update)
            updatePrices(symbol, interval);

        return mDb.getPriceList(symbol, interval);
    }

    public boolean insertSymbol(String symbol) {
        // TODO For now just always update, look into optimizing

        List<String> arr = new ArrayList<>();
        arr.add(symbol);
        List<Symbol> s = mYahooFinance.getSymbols(arr);
        if(s.size() > 0 && s.get(0) != null) {
            mDb.addSymbol(s.get(0));
            return true;
        }

        return false;
    }

}
