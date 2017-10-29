package org.cerion.stockcharts.watchlist;


import android.databinding.ObservableField;

import org.cerion.stockcharts.common.Constants;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.functions.conditions.Condition;
import org.cerion.stocklist.functions.conditions.IndicatorCondition;
import org.cerion.stocklist.functions.conditions.PriceCondition;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.overlays.SimpleMovingAverage;
import org.cerion.stocklist.web.DataAPI;

import java.util.ArrayList;
import java.util.List;

public class WatchListViewModel {

    public final ObservableField<ArrayList<WatchItemViewModel>> items = new ObservableField<>(new ArrayList<WatchItemViewModel>());
    public final ObservableField<Boolean> loading = new ObservableField<>(false);
    private DataAPI api;

    public WatchListViewModel(DataAPI api) {
        this.api = api;
        items.get().add(new WatchItemViewModel(new PriceCondition(Condition.ABOVE, new SimpleMovingAverage(200)), "^GSPC"));
        items.get().add(new WatchItemViewModel(new IndicatorCondition(new SimpleMovingAverage(50), Condition.BELOW, new SimpleMovingAverage(200)), "^GSPC"));
        items.get().add(new WatchItemViewModel(new IndicatorCondition(new SimpleMovingAverage(50), Condition.ABOVE, new SimpleMovingAverage(200)), "^GSPC"));
    }

    public void load() {
        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            @Override
            public void run() {
                loading.set(true);

                List<WatchItemViewModel> list = items.get();
                for(int i = 0; i < list.size(); i++) {
                    WatchItemViewModel item = list.get(i);
                    PriceList prices = api.getPrices(item.getSymbol(), Interval.DAILY, Constants.MAX_DAILY);
                    item.apply(prices);
                }
            }

            @Override
            public void onFinish() {
                loading.set(false);
            }
        });

        task.execute();
    }
}
