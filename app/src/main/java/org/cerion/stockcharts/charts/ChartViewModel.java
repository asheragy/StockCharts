package org.cerion.stockcharts.charts;

import android.databinding.Observable;

import org.cerion.stocklist.charts.StockChart;
import org.cerion.stocklist.model.Interval;

public class ChartViewModel {

    private StockChart mChart;
    private ChartsViewModel parent;
    private IChartView view;

    ChartViewModel(ChartsViewModel parent, StockChart chart) {
        this.parent = parent;
        mChart = chart;

        parent.interval.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (view != null)
                    view.reload();
            }
        });
    }

    public void setView(IChartView view) {
        this.view = view;
    }

    public StockChart getChart() {
        return mChart;
    }

    public ChartsViewModel getParent() {
        return parent;
    }

    public Interval getInterval() {
        return parent.interval.get();
    }
}
