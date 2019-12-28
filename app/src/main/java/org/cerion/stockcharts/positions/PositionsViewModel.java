package org.cerion.stockcharts.positions;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import org.cerion.stockcharts.Injection;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.repository.PositionWithDividendsRepository;
import org.cerion.stockcharts.repository.SymbolRepository;
import org.cerion.stocks.core.model.PositionWithDividends;
import org.cerion.stocks.core.model.Symbol;
import org.cerion.stocks.core.repository.CachedPriceListRepository;
import org.cerion.stocks.core.web.CachedDataAPI;

import java.util.ArrayList;
import java.util.List;

public class PositionsViewModel {
    private static final String TAG = PositionsViewModel.class.getSimpleName();

    public ObservableField<Boolean> loading = new ObservableField<>(false);
    public List<String> accounts = new ArrayList<>();
    public final ObservableField<Integer> accountIndex = new ObservableField<>(0);
    public final ObservableField<List<PositionItemViewModel>> positions = new ObservableField<>();
    public final ObservableField<List<Pair<String,Float>>> allocations = new ObservableField<>();

    private PositionWithDividendsRepository repo;
    private SymbolRepository symbolRepo;
    private CachedDataAPI api;
    private CachedPriceListRepository priceRepo;

    public PositionsViewModel(Context context) {
        repo = new PositionWithDividendsRepository(context);
        symbolRepo = new SymbolRepository(context);
        api = Injection.getAPI(context);
        priceRepo = Injection.getPriceListRepository(context);

        positions.set(new ArrayList<PositionItemViewModel>());
        allocations.set(new ArrayList<Pair<String,Float>>());
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

    public void delete(PositionWithDividends position) {
        Log.d(TAG, "removing " + position.getSymbol());
        repo.delete(position);

        load();
    }

    public void load() {
        load(false);
    }

    public void load(final boolean forceUpdate) {
        loading.set(true);

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            private List<Symbol> symbols;
            List<PositionItemViewModel> items = new ArrayList<>();

            private Symbol lookupSymbol(String symbol) {
                for(Symbol s : symbols) {
                    if(s.getSymbol().contentEquals(symbol))
                        return s;
                }

                return null;
            }

            @Override
            public void run() {
                symbols = symbolRepo.getAll();
                List<PositionWithDividends> list = repo.getAll();
                //List<Position> list = Temp.getPositions();

                for(PositionWithDividends p : list) {
                    // TODO filter on account
                    //if(p.getAccountId() == accountIndex.get())
                    {
                        //Log.d(TAG,"adding " + p);
                        String desc = p.getSymbol();
                        Symbol s = lookupSymbol(p.getSymbol());
                        if(s != null)
                            desc = desc + " - " + s.getName();

                        PositionItemViewModel vm = new PositionItemViewModel(api, p, desc, priceRepo);
                        //vm.forceUpdate = forceUpdate;
                        items.add(vm);

                        vm.totalValue.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                            @Override
                            public void onPropertyChanged(Observable sender, int propertyId) {
                                updateAllocations();
                            }
                        });
                    }
                }
            }

            @Override
            public void onFinish() {
                // TODO use asynctask doInBackground return result for this
                positions.get().clear();
                positions.get().addAll(items);
                positions.notifyChange();

                updateAllocations();
                loading.set(false);
            }
        });

        task.execute();
    }

    private void updateAllocations() {
        List<PositionItemViewModel> items = positions.get();

        allocations.get().clear();
        float cash = 0;
        for(PositionItemViewModel item : items) {
            Pair<String,Float> a = new Pair<>(item.getPosition().getSymbol(), item.totalValue.get());
            allocations.get().add(a);

            cash += item.getCash();
        }

        if (cash > 0)
            allocations.get().add( new Pair<>("Cash", cash));

        allocations.notifyChange();
    }

    /*
    public void update() {
        loading.set(true);

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {


private Map<String,Quote> getQuotes() {
    Set<String> symbols = new HashSet<>();
    for(Position p : positions)
        symbols.add(p.getSymbol());

    return api.getQuotes(symbols);
}

            @Override
            public void run() {
                //Map<String,Quote> quotes = getQuotes();
                for(PositionItemViewModel vm : positions.get()) {
                    String symbolDescription = vm.getPosition().getSymbol();

                    //Quote q = quotes.get(symbolDescription);
                    // Always do this since quotes not working
                    // if(p.IsDividendsReinvested())

                    try {
                        List<Price> list = api.getPrices(symbolDescription, Interval.DAILY, Constants.START_DATE_DAILY);
                        //p.setPriceHistory(list);

                        List<Dividend> dividends = api.getDividends(symbolDescription);

                        vm.setData(new PriceList(symbolDescription, list), dividends);
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
    */
}
