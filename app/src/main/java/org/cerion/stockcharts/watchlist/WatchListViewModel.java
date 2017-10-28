package org.cerion.stockcharts.watchlist;


import android.databinding.ObservableField;
import android.os.AsyncTask;

import org.cerion.stocklist.functions.conditions.Condition;
import org.cerion.stocklist.functions.conditions.IndicatorCondition;
import org.cerion.stocklist.functions.conditions.PriceCondition;
import org.cerion.stocklist.overlays.SimpleMovingAverage;

import java.util.ArrayList;

public class WatchListViewModel {

    public final ObservableField<ArrayList<WatchItem>> items = new ObservableField<>(new ArrayList<WatchItem>());
    public final ObservableField<Boolean> loading = new ObservableField<>(false);

    public WatchListViewModel() {
        items.get().add(new WatchItem(new PriceCondition(Condition.ABOVE, new SimpleMovingAverage(200)), "^GSPC"));
        items.get().add(new WatchItem(new IndicatorCondition(new SimpleMovingAverage(50), Condition.ABOVE, new SimpleMovingAverage(200)), "^GSPC"));
    }

    public void load() {

        AsyncTask<Object, Void, Void> task = new AsyncTask<Object, Void, Void>() {
            @Override
            protected Void doInBackground(Object[] params) {
                loading.set(true);
                items.get().get(0).price = 99;
                publishProgress();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                items.get().get(1).price = 99;
                publishProgress();
                return null;
            }

            @Override
            protected void onPostExecute(Void o) {
                super.onPostExecute(o);
                loading.set(false);
            }

            @Override
            protected void onProgressUpdate(Void[] values) {
                super.onProgressUpdate(values);
                items.notifyChange();
            }
        };

        task.execute((Object)null);
    }
}
