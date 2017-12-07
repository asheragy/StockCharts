package org.cerion.stockcharts.watchlist;


import android.databinding.ObservableField;
import android.os.Handler;

import org.cerion.stocklist.functions.conditions.Condition;
import org.cerion.stocklist.functions.conditions.IndicatorCondition;
import org.cerion.stocklist.functions.conditions.PriceCondition;
import org.cerion.stocklist.overlays.SimpleMovingAverage;
import org.cerion.stocklist.web.DataAPI;

import java.util.ArrayList;

public class WatchListViewModel {

    public final ObservableField<ArrayList<WatchItemViewModel>> items = new ObservableField<>(new ArrayList<WatchItemViewModel>());
    public final ObservableField<Boolean> loading = new ObservableField<>(false);

    public WatchListViewModel(Handler handler, DataAPI api) {
        // TODO save in database
        items.get().add(new WatchItemViewModel(handler, api, new PriceCondition(Condition.ABOVE, new SimpleMovingAverage(225)), "^GSPC"));
        items.get().add(new WatchItemViewModel(handler, api, new IndicatorCondition(new SimpleMovingAverage(35), Condition.BELOW, new SimpleMovingAverage(225)), "^GSPC"));
        items.get().add(new WatchItemViewModel(handler, api, new IndicatorCondition(new SimpleMovingAverage(35), Condition.ABOVE, new SimpleMovingAverage(225)), "^GSPC"));
    }

    public void load() {
        // TODO load items from database
    }
}
