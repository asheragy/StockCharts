package org.cerion.stockcharts.database;

import org.cerion.stockcharts.model.Position;
import org.cerion.stockcharts.model.Symbol;
import org.cerion.stocklist.Enums.Interval;
import org.cerion.stocklist.Price;
import org.cerion.stocklist.PriceList;

import java.util.List;

public interface StockDataStore {

    // Symbols
    List<String> getSymbols();
    void addSymbol(Symbol symbol);
    void deleteAllSymbols();

    // Positions
    void addPosition(Position position);
    List<Position> getPositions();
    void deletePosition(Position position);

    // Prices
    List<Price> getPrices(String symbol, Interval interval);
    PriceList getPriceList(String symbol, Interval interval);
    void addPriceList(PriceList list);
    void deletePrices(String symbol, Interval interval);

    // Debug
    void log();
}
