package org.cerion.stockcharts.charts;

import org.cerion.stocklist.indicators.FunctionCall;
import org.cerion.stocklist.model.Interval;

import java.util.ArrayList;
import java.util.List;

class ChartParams {
    String symbol;
    FunctionCall function;
    List<OverlayDataSet> overlays = new ArrayList<>();
    boolean logscale = false;
    Interval interval = Interval.DAILY;

    ChartParams(String symbol) {
        this.symbol = symbol;
    }
}
