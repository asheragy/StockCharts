package org.cerion.stockcharts.ui.symbols;

import org.cerion.stocklist.model.Symbol;

public class SymbolItemViewModel {

    private Symbol symbol;
    private boolean position;

    public SymbolItemViewModel(Symbol symbol, boolean position) {
        this.symbol = symbol;
        this.position = position;
    }

    public String getSymbol() {
        return symbol.getSymbol();
    }

    public String getExchange() {
        return symbol.getExchange();
    }

    public String getName() {
        return symbol.getName();
    }

    public String getUses() {
        return position ? "P" : "";
    }
}
