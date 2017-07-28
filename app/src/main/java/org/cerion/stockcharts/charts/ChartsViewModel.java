package org.cerion.stockcharts.charts;

import android.databinding.ObservableField;

import org.cerion.stocklist.model.Interval;

import java.util.ArrayList;
import java.util.List;

public class ChartsViewModel {

    private String symbol;
    public final ObservableField<Interval> interval = new ObservableField<>(Interval.DAILY);
    public List<ChartViewModel> charts = new ArrayList<>();

    public ChartsViewModel(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return this.symbol;
    }
}
