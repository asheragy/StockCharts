package org.cerion.stockcharts.watchlist;

import android.os.Handler;

import androidx.databinding.ObservableField;

import org.cerion.stockcharts.common.Utils;
import org.cerion.stocks.core.PriceList;
import org.cerion.stocks.core.charts.StockChart;
import org.cerion.stocks.core.functions.conditions.ICondition;
import org.cerion.stocks.core.web.DataAPI;

public class WatchItemViewModel {

    private DataAPI api;
    private ICondition condition;
    private String symbol;
    private Handler handler;

    public final ObservableField<String> price = new ObservableField<>("--");
    public final ObservableField<String> change = new ObservableField<>("--");
    public final ObservableField<Integer> weekPosition = new ObservableField<>(0);
    public final ObservableField<Integer> yearPosition = new ObservableField<>(0);
    public final ObservableField<Boolean> isTrue = new ObservableField<>(false);
    public final ObservableField<Boolean> loading = new ObservableField<>(true);

    public WatchItemViewModel(Handler handler, DataAPI api, ICondition condition, String symbol) {
        this.handler = handler;
        this.api = api;
        this.condition = condition;
        this.symbol = symbol;
    }

    public void load() {
        /*
        handler.post(new Runnable() {
            @Override
            public void run() {
                loading.set(true);
                try {
                    PriceList prices = new PriceList(getSymbol(), api.getPrices(getSymbol(), Interval.DAILY, Constants.START_DATE_DAILY));
                    apply(prices);
                } catch (Exception e) {
                    // TODO set error state
                    e.printStackTrace();
                }
                loading.set(false);
            }
        });

         */
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

    private void apply(PriceList list) {

        int size = list.size();
        float price = list.getLast().getClose();
        float change = list.getLast().getPercentDiff(list.get(size - 2));

        // TODO include current quote since its not in pricelist
        float low = list.getLow().get(size - 1);
        for(int j = size - 5; j < size; j++) {
            if (list.getLow().get(j) < low)
                low = list.getLow().get(j);
        }

        float high = list.getHigh().get(size - 1);
        for(int j = size - 5; j < size; j++) {
            if (list.getHigh().get(j) > high)
                high = list.getHigh().get(j);
        }

        float range = high - low;
        float diff = list.getLast().getClose() - low;
        float percent = diff / range;
        weekPosition.set((int)(percent * 100));

        // TODO add function to get highest high in given range to PriceList

        // Year
        int start = 250;
        if (list.size() < start)
            start = 0;

        low = list.getLow().get(size - 1);
        for(int j = size - start; j < size; j++) {
            if (list.getLow().get(j) < low)
                low = list.getLow().get(j);
        }

        high = list.getHigh().get(size - 1);
        for(int j = size - start; j < size; j++) {
            if (list.getHigh().get(j) > high)
                high = list.getHigh().get(j);
        }

        range = high - low;
        diff = list.getLast().getClose() - low;
        percent = diff / range;
        yearPosition.set((int)(percent * 100));

        isTrue.set(condition.eval(list));

        // Set strings
        this.price.set(Utils.decimalFormat.format(price));
        this.change.set(Utils.decimalFormat.format(change) + "%");
    }



}
