package org.cerion.stockcharts.viewmodel;

import android.content.Context;

import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.common.Utils;
import org.cerion.stockcharts.database.StockDB;
import org.cerion.stockcharts.database.StockDataManager;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.model.Quote;
import org.cerion.stocklist.web.IYahooFinance;
import org.cerion.stocklist.web.YahooFinance;

import java.util.Observable;

public class PositionViewModel extends Observable {

    private Position mPosition;
    private StockDB mDb;
    private IYahooFinance mAPI = new YahooFinance();
    private StockDataManager mDataManager;

    public PositionViewModel(Context context) {
        mDb = StockDB.getInstance(context);
        mDataManager = new StockDataManager(context);
    }

    public Position getPosition() {
        return mPosition;
    }

    public void setPosition(Position position) {
        mPosition = position;
        onChange();
    }

    public String getCount() {
        return Utils.getDecimalFormat3(mPosition.getCount());
    }

    public String getPurchasePrice() {
        return Utils.decimalFormat.format(mPosition.getOrigPrice());
    }

    public String getPurchaseDate() {
        return Utils.dateFormatLong.format(mPosition.getDate());
    }

    public String getPurchaseCost() {
        return "$" + Utils.decimalFormat.format(mPosition.getOrigValue());
    }

    public void load() {
        new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            @Override
            public void run() {
                final String symbol = mPosition.getSymbol();

                // Add dividends to position
                mPosition.addDividends(mDb.getDividends(symbol));
                if(mPosition.IsDividendsReinvested())
                    mPosition.setPriceHistory(mDataManager.getLatestPrices(symbol, Interval.DAILY));

                // Get most recent quote
                if(mPosition.getCurrPrice() == 0) {
                    Quote q = mAPI.getQuote(symbol);
                    mPosition.setQuote(q);
                }
            }

            @Override
            public void onFinish() {
                onChange();
            }
        }).execute();
    }

    private void onChange() {
        setChanged();
        notifyObservers();
    }

}
