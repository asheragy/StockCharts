package org.cerion.stockcharts.charts;

import org.cerion.stocklist.functions.FunctionCall;
import org.cerion.stocklist.functions.IPriceOverlay;
import org.cerion.stocklist.functions.Indicator;
import org.cerion.stocklist.model.Interval;

import java.util.ArrayList;
import java.util.List;

class ChartParams {

    String symbol;
    Interval interval;

    List<OverlayDataSet> overlays = new ArrayList<>();
    List<IPriceOverlay> overlaysNEW = new ArrayList<>();

    boolean logscale = false;

    ChartParams(String symbol, Interval interval) {
        this.symbol = symbol;
        this.interval = interval;
    }

    public Indicator toIndicator() {
        return new Indicator(symbol, interval);
    }

    public Price toPrice() {
        return new Price(symbol, interval);
    }

    public static class Price extends ChartParams {
        boolean lineChart = false;
        Price(String symbol, Interval interval) {
            super(symbol, interval);
        }
    }

    public class Indicator extends ChartParams {
        FunctionCall function;
        Indicator(String symbol, Interval interval) {
            super(symbol, interval);
        }
    }
}
