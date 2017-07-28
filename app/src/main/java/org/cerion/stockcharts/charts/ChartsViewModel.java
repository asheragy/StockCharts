package org.cerion.stockcharts.charts;

import android.databinding.ObservableField;

import org.cerion.stocklist.model.Interval;

public class ChartsViewModel {

    private String symbol;
    public final ObservableField<Interval> interval = new ObservableField<>(Interval.DAILY);

    public ChartsViewModel(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return this.symbol;
    }
}
