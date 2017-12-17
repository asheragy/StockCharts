package org.cerion.stockcharts.positions;

import android.databinding.ObservableField;

import org.cerion.stockcharts.common.BackgroundTask;
import org.cerion.stockcharts.common.Constants;
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

    private Position position;
    private DecimalFormat df = Utils.decimalFormat;
    private DataAPI api;
    private String symbolDescription;
    private float cash;

    public ObservableField<Float> dayChange = new ObservableField<>(0.0f);
    public ObservableField<Float> totalChange = new ObservableField<>(0.0f);
    public ObservableField<String> dayChangeStr = new ObservableField<>("0.00 (0.00%)");
    public ObservableField<String> totalChangeStr = new ObservableField<>("0.00 (0.00%)");
    public ObservableField<String> currentPrice = new ObservableField<>("...");
    public ObservableField<String> dividendProfit = new ObservableField<>("");
    public ObservableField<Boolean> loading = new ObservableField<>(false);
    public ObservableField<Boolean> hasError = new ObservableField<>(false);
    public ObservableField<Float> totalValue = new ObservableField<>(0.0f);

    PositionItemViewModel(DataAPI api, Position p, String symbolDescription) {
        this.api = api;
        position = p;
        this.symbolDescription = symbolDescription;
        this.totalValue.set((float)p.getOrigValue());
    }

    public String getSymbolDescription() {
        return symbolDescription;
    }

    public void load() {
        loading.set(true);
        hasError.set(false);

        BackgroundTask.run(new BackgroundTask() {
            @Override
            public void doInBackground() throws Exception {
                String symbol = getPosition().getSymbol();
                //Quote q = quotes.get(symbolDescription);
                // Always do this since quotes not working
                // if(p.IsDividendsReinvested())

                List<Price> list = api.getPrices(symbol, Interval.DAILY, Constants.START_DATE_DAILY);
                //p.setPriceHistory(list);
                List<Dividend> dividends = api.getDividends(symbol);
                setData(new PriceList(symbol, list), dividends);
            }

            @Override
            public void onError(Exception e) {
                hasError.set(true);
            }

            @Override
            public void onFinish() {
                loading.set(false);
            }
        });
    }

    private void setData(PriceList list, List<Dividend> dividends) {
        PositionValue value = new PositionValue(position, list);
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

        cash = profit;

        totalValue.set((float)value.getCurrValue());
    }

    public float getCash() {
        return cash;
    }

    public Position getPosition() {
        return position;
    }

    public String purchaseDate() {
        return Utils.dateFormatShort.format(position.getDate());
    }

    // TODO this needs to be dynamic
    public String purchaseLot() {
        return Utils.getDecimalFormat3(position.getCount()) + " @ " + df.format(position.getOrigPrice());
    }
}
