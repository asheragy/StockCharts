package org.cerion.stockcharts.positions;

import android.content.Context;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.os.AsyncTask;
import android.util.Log;

import org.cerion.stockcharts.Injection;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.repository.PositionRepository;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.model.Dividend;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.model.Quote;
import org.cerion.stocklist.web.CachedDataAPI;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PositionsViewModel {
    private static final String TAG = PositionsViewModel.class.getSimpleName();

    public ObservableList<Position> positions = new ObservableArrayList<>();

    private PositionRepository repo;
    private CachedDataAPI api;
    private IView mView;

    public interface IView {
        void onNewData(); // TEMP
    }

    public PositionsViewModel(Context context, IView view) {
        repo = new PositionRepository(context);
        api = Injection.getAPI(context);
        mView = view;
    }

    public void delete(Position position) {
        Log.d(TAG, "removing " + position.getSymbol());
        repo.delete(position);

        load();
    }

    public void load() {
        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            @Override
            public void run() {
                positions.clear();
                positions.addAll( repo.getAll() );
            }

            @Override
            public void onFinish() {
                mView.onNewData();
            }
        });

        task.execute();
    }

    public void update() {

        AsyncTask task = new AsyncTask<Object, Void, Void>() {
            private Map<String,Quote> getQuotes() {
                Set<String> symbols = new HashSet<>();
                for(Position p : positions)
                    symbols.add(p.getSymbol());

                return api.getQuotes(symbols);
            }

            @Override
            protected Void doInBackground(Object... params) {
                Map<String,Quote> quotes = getQuotes();

                for(Position p : positions) {
                    String symbol = p.getSymbol();
                    Quote q = quotes.get(symbol);

                    if(p.IsDividendsReinvested()) {
                        try {
                            PriceList list = api.getPrices(symbol, Interval.DAILY, 500);
                            p.setPriceHistory(list);
                        } catch (Exception e){
                            continue;
                        }

                    }

                    p.setQuote(q);

                    List<Dividend> list = api.getDividends(symbol);
                    p.addDividends(list);

                    //db.log();

                    publishProgress();
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Void[] values) {
                super.onProgressUpdate(values);
                mView.onNewData();
            }
        };

        task.execute();
    }


    /*
    private void updateDividends() {

        mSwipeRefresh.setRefreshing(true);

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            @Override
            public void run() {
                Map<String, String> downloaded = new HashMap<>(); // TODO convert list to set instead of this
                for(Position p : mPositions)
                {
                    String symbol = p.getSymbol();
                    if(!downloaded.containsKey(symbol)) {
                        List<Dividend> list = dividendRepo.getLatest(symbol);
                        Log.d(TAG, "downloaded new list, size = " + list.size());

                        dividendRepo.add(symbol, list);
                        downloaded.put(symbol, "");
                    }
                }

                repo.log();
            }

            @Override
            public void onFinish() {
                mSwipeRefresh.setRefreshing(false);
            }
        });

        task.execute();
    }
    */
}
