package org.cerion.stockcharts;

import android.content.Context;

import org.cerion.stockcharts.repository.PositionRepository;
import org.cerion.stockcharts.repository.SymbolRepository;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.model.Symbol;

import java.util.ArrayList;
import java.util.List;

public class SymbolListViewModel {

    private Context mContext;
    private SymbolRepository repo;
    private PositionRepository positionRepo;

    public static class SymbolItem {
        public Symbol symbol;
        public boolean position;
    }

    public SymbolListViewModel(Context context) {
        mContext = context;
        repo = new SymbolRepository(context);
        positionRepo = new PositionRepository(context);
    }

    public List<SymbolItem> getItems() {
        List<Symbol> symbols = repo.getAll();
        List<Position> positions = positionRepo.getAll();
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
