package org.cerion.stockcharts.watchlist;

import org.cerion.stocklist.functions.conditions.ICondition;

public class WatchItem {

    private ICondition condition;
    private String symbol;

    public float price;

    public WatchItem(ICondition condition, String symbol) {
        this.condition = condition;
        this.symbol = symbol;
        this.price = 0;
    }

    public String getSymbol() {
        return symbol;
    }

    public ICondition getCondition() {
        return condition;
    }

}
