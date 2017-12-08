package org.cerion.stockcharts.positions;

import android.databinding.ObservableField;

import org.cerion.stockcharts.common.Constants;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.common.Utils;
import org.cerion.stocklist.Price;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.model.Dividend;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.model.PositionValue;
import org.cerion.stocklist.web.DataAPI;

import java.text.DecimalFormat;
import java.util.List;

public class PositionItemViewModel {

    private Position mPosition;
    private DecimalFormat df = Utils.decimalFormat;
    private DataAPI api;

    public ObservableField<String> symbol = new ObservableField<>("");
    public ObservableField<Float> dayChange = new ObservableField<>(0.0f);
    public ObservableField<Float> totalChange = new ObservableField<>(0.0f);
    public ObservableField<String> dayChangeStr = new ObservableField<>("0.00 (0.00%)");
    public ObservableField<String> totalChangeStr = new ObservableField<>("0.00 (0.00%)");
    public ObservableField<String> currentPrice = new ObservableField<>("...");
    public ObservableField<String> dividendProfit = new ObservableField<>("");
    public ObservableField<Boolean> loading = new ObservableField<>(false);

    PositionItemViewModel(DataAPI api, Position p) {
        this.api = api;
        mPosition = p;
        this.symbol.set(p.getSymbol());
    }

    public void load() {
        loading.set(true);
        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            @Override
            public void run() {
                String symbol = getPosition().getSymbol();

                //Quote q = quotes.get(symbol);
                // Always do this since quotes not working
                // if(p.IsDividendsReinvested())

                try {
                    List<Price> list = api.getPrices(symbol, Interval.DAILY, Constants.START_DATE_DAILY);
                    //p.setPriceHistory(list);

                    List<Dividend> dividends = api.getDividends(symbol);

                    setData(new PriceList(symbol, list), dividends);
                } catch (Exception e){

                }
            }

            @Override
            public void onFinish() {
                loading.set(false);
            }
        });

        task.execute();
    }

    public void setDescription(String description) {
        this.symbol.set(mPosition.getSymbol() + " - " + description);
    }

    private void setData(PriceList list, List<Dividend> dividends) {
        PositionValue value = new PositionValue(mPosition, list);
        value.addDividends(dividends);

        float change = list.getChange();
        float percent = list.getPercentChange() * 100;
        String sign = (change > 0 ? "+" : "");
        String format = "%s%s (%s%%)";
        dayChangeStr.set(String.format(format, sign, df.format(change), df.format(percent)));
        dayChange.set(change);

        // TODO possibly only percent should go here, with dividend reinvested increase is price and/or shares increasing
        // This should actually just be the $ amount gain/loss ignoring dividends
        // Total change
        float profit = (float)value.getProfit();
        sign = (profit > 0 ? "+" : "");
        format = "%s$%s (%s%%)";
        format = String.format(format, sign, df.format(profit), df.format(value.getPercentChanged() * 100) );
        totalChangeStr.set(format);
        totalChange.set(profit);

        // Current Price
        currentPrice.set( df.format(value.getCurrPrice()) );

        // Dividends
        profit = (float)value.getDividendProfit();
        if(profit > 0) {
            dividendProfit.set("$" + df.format(profit));
        } else
            dividendProfit.set("");
    }

    public Position getPosition() {
        return mPosition;
    }

    public String purchaseDate() {
        return Utils.dateFormatShort.format(mPosition.getDate());
    }

    // TODO this needs to be dynamic
    public String purchaseLot() {
        return Utils.getDecimalFormat3(mPosition.getCount()) + " @ " + df.format(mPosition.getOrigPrice());
    }
}
