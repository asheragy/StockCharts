package org.cerion.stockcharts.charts;

import org.cerion.stocklist.indicators.FunctionCall;

import java.util.ArrayList;
import java.util.List;

class ChartParams {
    FunctionCall function;
    List<Overlay> overlays = new ArrayList<>();
}
