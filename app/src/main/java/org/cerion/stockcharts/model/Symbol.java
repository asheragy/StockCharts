package org.cerion.stockcharts.model;

public class Symbol {

    private String symbol;
    private String name;
    private String exchange;

    public Symbol(String symbol) {
        this.symbol = symbol;
    }

    public Symbol(String symbol, String name, String exchange) {
        this.symbol = symbol;
        this.name = name;
        this.exchange = exchange;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }
}
