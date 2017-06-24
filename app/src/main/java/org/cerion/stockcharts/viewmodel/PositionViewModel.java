package org.cerion.stockcharts.viewmodel;

import android.content.Context;

import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.common.Utils;
import org.cerion.stockcharts.repository.DividendRepository;
import org.cerion.stockcharts.repository.PriceListRepository;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.model.Quote;
import org.cerion.stocklist.web.IYahooFinance;
import org.cerion.stocklist.web.YahooFinance;

import java.util.Observable;

public class PositionViewModel extends Observable {

    private Position mPosition;
    private DividendRepository dividendRepo;
    private PriceListRepository priceRepo;
    private IYahooFinance mAPI = new YahooFinance();

    public PositionViewModel(Context context) {
        dividendRepo = new DividendRepository(context);
        priceRepo = new PriceListRepository(context);
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
                mPosition.addDividends( dividendRepo.get(symbol) );
                if(mPosition.IsDividendsReinvested()) {
                    try {
                        mPosition.setPriceHistory(priceRepo.getLatest(symbol, Interval.DAILY));
                    } catch (Exception e) {
                        // Failed to load
                    }
                }

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
