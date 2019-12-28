package org.cerion.stockcharts.watchlist;


import android.os.Handler;

import androidx.databinding.ObservableField;

import org.cerion.stocks.core.functions.conditions.Condition;
import org.cerion.stocks.core.functions.conditions.IndicatorCondition;
import org.cerion.stocks.core.functions.conditions.PriceCondition;
import org.cerion.stocks.core.overlays.SimpleMovingAverage;
import org.cerion.stocks.core.web.DataAPI;

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
