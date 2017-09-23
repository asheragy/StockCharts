package org.cerion.stockcharts.charts;

import android.app.Application;
import android.databinding.Observable;
import android.databinding.ObservableField;

import org.cerion.stockcharts.Injection;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.web.CachedDataAPI;

import java.util.ArrayList;
import java.util.List;

public class ChartsViewModel {

    private String symbol;
    private CachedDataAPI api;

    public final ObservableField<Interval> interval = new ObservableField<>(Interval.DAILY);
    public final ObservableField<PriceList> priceList = new ObservableField<>();
    public List<ChartViewModel> charts = new ArrayList<>();

    public ChartsViewModel(String symbol, CachedDataAPI api) {
        this.symbol = symbol;
        this.api = api;

        loadData();

        interval.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                loadData();
            }
        });
    }

    public void setInterval(Interval interval) {
        this.interval.set(interval);
    }

    public void loadData() {
        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            PriceList result;
            @Override
            public void run() {
                switch (interval.get()) {
                    case DAILY: result = api.getPrices(symbol, Interval.DAILY, 250 * 5); break;
                    case WEEKLY: result = api.getPrices(symbol, Interval.WEEKLY, 52 * 10); break;
                    case MONTHLY: result = api.getPrices(symbol, Interval.MONTHLY, 12 * 20); break;
                    case QUARTERLY: result = api.getPrices(symbol, Interval.MONTHLY, 12 * 50).toQuarterly(); break;
                }
            }

            @Override
            public void onFinish() {
                priceList.set(result);
            }
        });

        task.execute();
    }

    public String getSymbol() {
        return this.symbol;
    }
}
