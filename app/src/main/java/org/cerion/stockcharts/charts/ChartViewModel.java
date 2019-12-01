package org.cerion.stockcharts.charts;

import android.databinding.Observable;

import org.cerion.stocks.core.PriceList;
import org.cerion.stocks.core.charts.StockChart;

public class ChartViewModel {

    private StockChart mChart;
    private ChartsViewModel parent;
    private IChartView view;
    private OnRemoveListener listener;

    public interface OnRemoveListener {
        void onRemove();
    }

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

    public void remove() {
        // TODO this should actually call remove on parent
        if (listener != null)
            listener.onRemove();
    }

    public void setOnRemoveListener(OnRemoveListener listener) {
        this.listener = listener;
    }

    public void setView(IChartView view) {
        this.view = view;
    }

    public StockChart getChart() {
        return mChart;
    }

    public void setChart(StockChart chart) {
        mChart = chart;
        view.reload(); // TODO view should be observing this
    }

    public ChartsViewModel getParent() {
        return parent;
    }

    public PriceList getList() {
        return parent.priceList.get();
    }
}
