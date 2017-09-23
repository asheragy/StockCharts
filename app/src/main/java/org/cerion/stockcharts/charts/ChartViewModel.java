package org.cerion.stockcharts.charts;

import android.databinding.Observable;

import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.charts.StockChart;
import org.cerion.stocklist.model.Interval;

public class ChartViewModel {

    private StockChart mChart;
    private ChartsViewModel parent;
    private IChartView view;

    ChartViewModel(ChartsViewModel parent, StockChart chart) {
        this.parent = parent;
        mChart = chart;

        parent.priceList.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
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

    public void setChart(StockChart chart) {
        mChart = chart;
        view.reload();
    }

    public ChartsViewModel getParent() {
        return parent;
    }

    public PriceList getList() {
        return parent.priceList.get();
    }

    public Interval getIntervalOLD() {
        return parent.interval.get();
    }
}
