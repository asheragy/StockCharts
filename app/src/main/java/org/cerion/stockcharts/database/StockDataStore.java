package org.cerion.stockcharts.database;

import org.cerion.stockcharts.model.HistoricalDates;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.Price;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.model.Symbol;

import java.util.List;

public interface StockDataStore {

    // Symbols
    List<Symbol> getSymbols();
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

    // Historical info
    HistoricalDates getHistoricalDates(String symbol, Interval interval);

    // Debug
    void log();
}
