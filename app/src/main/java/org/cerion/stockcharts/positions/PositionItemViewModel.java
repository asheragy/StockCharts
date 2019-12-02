package org.cerion.stockcharts.positions;

import androidx.databinding.ObservableField;

import org.cerion.stockcharts.common.BackgroundTask;
import org.cerion.stockcharts.common.Constants;
import org.cerion.stockcharts.common.Utils;
import org.cerion.stocks.core.PriceList;
import org.cerion.stocks.core.PriceRow;
import org.cerion.stocks.core.model.Dividend;
import org.cerion.stocks.core.model.Interval;
import org.cerion.stocks.core.model.Position;
import org.cerion.stocks.core.model.PositionValue;
import org.cerion.stocks.core.web.CachedDataAPI;

import java.text.DecimalFormat;
import java.util.List;

public class PositionItemViewModel {

    private Position position;
    private DecimalFormat df = Utils.decimalFormat;
    private CachedDataAPI api;
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
    public boolean forceUpdate = false;

    PositionItemViewModel(CachedDataAPI api, Position p, String symbolDescription) {
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

            List<PriceRow> list;
            List<Dividend> dividends;
            String symbol = getPosition().getSymbol();

            @Override
            public void doInBackground() throws Exception {

                //Quote q = quotes.get(symbolDescription);
                // Always do this since quotes not working
                // if(p.IsDividendsReinvested())

                list = api.getPrices(symbol, Interval.DAILY, Constants.START_DATE_DAILY, forceUpdate);
                //p.setPriceHistory(list);
                dividends = api.getDividends(symbol);

            }

            @Override
            public void onError(Exception e) {
                hasError.set(true);
            }

            @Override
            public void onFinish() {
                loading.set(false);
                forceUpdate = false;

                setData(new PriceList(symbol, list), dividends);
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
