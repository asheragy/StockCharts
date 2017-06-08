package org.cerion.stockcharts.viewmodel;

import android.content.Context;

import org.cerion.stockcharts.database.StockDB;
import org.cerion.stockcharts.database.StockDataStore;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.model.Symbol;

import java.util.ArrayList;
import java.util.List;

public class SymbolListViewModel {

    private Context mContext;
    private StockDataStore mDb;

    public static class SymbolItem {
        public Symbol symbol;
        public boolean position;
    }

    public SymbolListViewModel(Context context) {
        mContext = context;
        mDb = StockDB.getInstance(context);
    }

    public List<SymbolItem> getItems() {
        List<Symbol> symbols = mDb.getSymbols();
        List<Position> positions = mDb.getPositions();
        List<SymbolItem> items = new ArrayList<>();

        for(Symbol s : symbols) {
            SymbolItem item = new SymbolItem();
            item.symbol = s;

            for(Position p : positions) {
                if (p.getSymbol().contentEquals(s.getSymbol()))
                    item.position = true;
            }

            items.add(item);
        }

        return items;
    }
}
