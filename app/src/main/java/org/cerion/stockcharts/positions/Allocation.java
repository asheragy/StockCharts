package org.cerion.stockcharts.positions;

public class Allocation {
    public String symbol;
    public float percent; // TODO this should just be value and the chart can calculate the percent

    public Allocation(String symbol, float percent) {
        this.symbol = symbol;
        this.percent = percent;
    }
}
