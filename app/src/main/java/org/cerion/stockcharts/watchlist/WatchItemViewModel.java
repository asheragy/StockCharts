package org.cerion.stockcharts.watchlist;

import android.databinding.ObservableField;

import org.cerion.stockcharts.common.Utils;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.charts.StockChart;
import org.cerion.stocklist.functions.conditions.ICondition;

public class WatchItemViewModel {

    private ICondition condition;
    private String symbol;

    public final ObservableField<String> price = new ObservableField<>("--");
    public final ObservableField<String> change = new ObservableField<>("--");
    public final ObservableField<Integer> weekPosition = new ObservableField<>(0);
    public final ObservableField<Integer> yearPosition = new ObservableField<>(0);
    public final ObservableField<Boolean> isTrue = new ObservableField<>(false);

    public WatchItemViewModel(ICondition condition, String symbol) {
        this.condition = condition;
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCondition() {
        return condition.toString();
    }

    public StockChart getChart() {
        return condition.getChart();
    }

    public void apply(PriceList list) {

        int size = list.size();
        float price = list.getLast().close;
        float change = list.getLast().getPercentDiff(list.get(size - 2));

        // TODO include current quote since its not in pricelist
        float low = list.low(size - 1);
        for(int j = size - 5; j < size; j++) {
            if (list.low(j) < low)
                low = list.low(j);
        }

        float high = list.high(size - 1);
        for(int j = size - 5; j < size; j++) {
            if (list.high(j) > high)
                high = list.high(j);
        }

        float range = high - low;
        float diff = list.getLast().close - low;
        float percent = diff / range;
        weekPosition.set((int)(percent * 100));

        // Year
        low = list.low(size - 1);
        for(int j = size - 250; j < size; j++) {
            if (list.low(j) < low)
                low = list.low(j);
        }

        high = list.high(size - 1);
        for(int j = size - 250; j < size; j++) {
            if (list.high(j) > high)
                high = list.high(j);
        }

        range = high - low;
        diff = list.getLast().close - low;
        percent = diff / range;
        yearPosition.set((int)(percent * 100));

        isTrue.set(condition.eval(list));

        // Set strings
        this.price.set(Utils.decimalFormat.format(price));
        this.change.set(Utils.decimalFormat.format(change) + "%");
    }



}
