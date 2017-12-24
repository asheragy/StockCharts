package org.cerion.stockcharts.ui.symbols;

import android.content.Context;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.AsyncTask;
import android.widget.Toast;

import org.cerion.stockcharts.Injection;
import org.cerion.stockcharts.repository.PositionRepository;
import org.cerion.stockcharts.repository.SymbolRepository;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.model.Symbol;
import org.cerion.stocklist.web.DataAPI;

import java.util.List;

public class SymbolsViewModel {

    private Context context;
    private SymbolRepository repo;
    private PositionRepository positionRepo;
    public final ObservableList<SymbolItemViewModel> items = new  ObservableArrayList<>();

    public SymbolsViewModel(Context context) {
        this.context = context.getApplicationContext();
        repo = new SymbolRepository(this.context);
        positionRepo = new PositionRepository(this.context);
    }

    public void load() {
        List<Symbol> symbols = repo.getAll();
        List<Position> positions = positionRepo.getAll();
        items.clear();

        for(Symbol s : symbols) {
            boolean position = false;
            for(Position p : positions) {
                if (p.getSymbol().contentEquals(s.getSymbol()))
                    position = true;
            }

            SymbolItemViewModel item = new SymbolItemViewModel(s, position);
            items.add(item);
        }
    }

    public void add(final String symbol) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                DataAPI api = Injection.getAPI(context);
                Symbol s = api.getSymbol(symbol);
                if (s != null) {
                    repo.add(s);
                    return true;
                }

                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result)
                    load();
                else
                    Toast.makeText(context, "Could not find '" + symbol + "'", Toast.LENGTH_SHORT).show();
            }
        };

        task.execute();
    }

    public void delete(final String symbol) {
        for(SymbolItemViewModel item : items) {
            if (item.getSymbol().contentEquals(symbol)) {
                if (item.getUses().length() > 0) {
                    Toast.makeText(context, "Cannot delete symbol used elsewhere", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        repo.delete(symbol);
        load();
    }
}
