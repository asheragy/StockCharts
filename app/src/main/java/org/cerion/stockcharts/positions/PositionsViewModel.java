package org.cerion.stockcharts.positions;

import android.content.Context;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.util.Log;

import org.cerion.stockcharts.Injection;
import org.cerion.stockcharts.common.Constants;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.repository.PositionRepository;
import org.cerion.stockcharts.repository.SymbolRepository;
import org.cerion.stocklist.Price;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.model.Dividend;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.model.Symbol;
import org.cerion.stocklist.web.CachedDataAPI;

import java.util.ArrayList;
import java.util.List;

public class PositionsViewModel {
    private static final String TAG = PositionsViewModel.class.getSimpleName();

    public ObservableField<Boolean> loading = new ObservableField<>(false);
    public List<String> accounts = new ArrayList<>();
    public final ObservableField<Integer> accountIndex = new ObservableField<>(0);
    public final ObservableField<List<PositionItemViewModel>> positions = new ObservableField<>();

    private PositionRepository repo;
    private SymbolRepository symbolRepo;
    private CachedDataAPI api;

    public PositionsViewModel(Context context) {
        repo = new PositionRepository(context);
        symbolRepo = new SymbolRepository(context);
        api = Injection.getAPI(context);

        positions.set(new ArrayList<PositionItemViewModel>());
        accounts.add("Brokerage");
        accounts.add("Roth");
        accounts.add("IRA");
        accounts.add("401k");
        accounts.add("Account 5");

        accountIndex.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                load();
            }
        });
    }

    public void delete(Position position) {
        Log.d(TAG, "removing " + position.getSymbol());
        repo.delete(position);

        load();
    }

    public void load() {
        loading.set(true);

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {

            private List<Symbol> symbols;

            private Symbol lookupSymbol(String symbol) {
                for(Symbol s : symbols) {
                    if(s.getSymbol().contentEquals(symbol))
                        return s;
                }

                return null;
            }

            @Override
            public void run() {
                positions.get().clear();
                symbols = symbolRepo.getAll();
                List<Position> list = repo.getAll();

                for(Position p : list) {
                    if(p.getAccountId() == accountIndex.get()) {
                        //Log.d(TAG,"adding " + p);
                        PositionItemViewModel vm = new PositionItemViewModel(p);

                        Symbol s = lookupSymbol(p.getSymbol());
                        if(s != null)
                            vm.setDescription(s.getName());

                        positions.get().add(vm);
                    }
                }

                positions.notifyChange();
            }

            @Override
            public void onFinish() {
                loading.set(false);

                update();
            }
        });

        task.execute();
    }

    public void update() {
        loading.set(true);

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {

            /*
private Map<String,Quote> getQuotes() {
    Set<String> symbols = new HashSet<>();
    for(Position p : positions)
        symbols.add(p.getSymbol());

    return api.getQuotes(symbols);
}
*/
            @Override
            public void run() {
                //Map<String,Quote> quotes = getQuotes();
                for(PositionItemViewModel vm : positions.get()) {
                    String symbol = vm.getPosition().getSymbol();

                    //Quote q = quotes.get(symbol);
                    // Always do this since quotes not working
                    // if(p.IsDividendsReinvested())

                    try {
                        List<Price> list = api.getPrices(symbol, Interval.DAILY, Constants.START_DATE_DAILY);
                        //p.setPriceHistory(list);

                        List<Dividend> dividends = api.getDividends(symbol);

                        vm.setData(new PriceList(symbol, list), dividends);
                    } catch (Exception e){

                    }
                }

                positions.notifyChange();
            }

            @Override
            public void onFinish() {
                loading.set(false);
            }
        });

        task.execute();
    }
}
