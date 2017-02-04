package org.cerion.stockcharts.charts;

import org.cerion.stocklist.functions.FunctionCall;
import org.cerion.stocklist.model.Interval;

import java.util.ArrayList;
import java.util.List;

class ChartParams {

    String symbol;
    FunctionCall function; // TODO this should be restricted to indicators only somehow
    List<OverlayDataSet> overlays = new ArrayList<>();
    boolean logscale = false;
    Interval interval;

    ChartParams(String symbol, Interval interval) {
        this.symbol = symbol;
        this.interval = interval;
    }
}
