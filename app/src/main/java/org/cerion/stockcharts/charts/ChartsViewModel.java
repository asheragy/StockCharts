package org.cerion.stockcharts.charts;

import android.databinding.Observable;
import android.databinding.ObservableField;

import org.cerion.stockcharts.common.Constants;
import org.cerion.stockcharts.common.FloatingActionButtonGroup;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.PriceRow;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.web.CachedDataAPI;

import java.util.ArrayList;
import java.util.List;

public class ChartsViewModel implements FloatingActionButtonGroup.FabStateListener {

    private String symbol;
    private CachedDataAPI api;
    private ChartsView view;

    public final ObservableField<Interval> interval = new ObservableField<>(Interval.DAILY);
    public final ObservableField<PriceList> priceList = new ObservableField<>();
    public List<ChartViewModel> charts = new ArrayList<>();
    public final ObservableField<Boolean> loading = new ObservableField<>();
    public final ObservableField<Boolean> fabOpen = new ObservableField<>(false);

    public final ObservableField<String> fullName = new ObservableField<>("");

    public interface ChartsView {
        void onErrorLoading(String error);
    }

    public ChartsViewModel(String symbol, CachedDataAPI api, ChartsView view) {
        this.symbol = symbol;
        this.api = api;
        this.view = view;

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
        loading.set(true);

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            PriceList result;
            Exception error;
            @Override
            public void run() {
                List<PriceRow> prices = null;

                try {
                    switch (interval.get()) {
                        case DAILY:
                            prices = api.getPrices(symbol, Interval.DAILY, Constants.START_DATE_DAILY);
                            break;
                        case WEEKLY:
                            prices = api.getPrices(symbol, Interval.WEEKLY, Constants.START_DATE_WEEKLY);
                            break;
                        case MONTHLY:
                            prices = api.getPrices(symbol, Interval.MONTHLY, Constants.START_DATE_MONTHLY);
                            break;
                        case QUARTERLY:
                            prices = api.getPrices(symbol, Interval.MONTHLY, Constants.START_DATE_MONTHLY);
                            break;
                    }

                    result = new PriceList(symbol, prices);
                    if (interval.get() == Interval.QUARTERLY)
                        result = result.toQuarterly();
                }
                catch (Exception e) {
                    error = e;
                }
            }

            @Override
            public void onFinish() {
                if (error == null)
                    priceList.set(result);
                else
                    view.onErrorLoading(error.getMessage());

                loading.set(false);
            }
        });

        task.execute();
    }

    public String getSymbol() {
        return this.symbol;
    }

    @Override
    public void onStateChange(boolean open) {
        fabOpen.set(open);
    }
}
