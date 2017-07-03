package org.cerion.stockcharts.positions;

import android.content.Context;

import org.cerion.stockcharts.Injection;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.common.Utils;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.model.Quote;
import org.cerion.stocklist.web.CachedDataAPI;

import java.util.Observable;

public class PositionDetailViewModel extends Observable {

    private Position mPosition;
    private CachedDataAPI api;

    public PositionDetailViewModel(Context context) {
        api = Injection.getAPI(context);
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
                mPosition.addDividends( api.getDividends(symbol) );
                if(mPosition.IsDividendsReinvested()) {
                    try {
                        mPosition.setPriceHistory(api.getPrices(symbol, Interval.DAILY, 500));
                    } catch (Exception e) {
                        // Failed to load
                    }
                }

                // Get most recent quote
                if(mPosition.getCurrPrice() == 0) {
                    Quote q = api.getQuote(symbol);
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
