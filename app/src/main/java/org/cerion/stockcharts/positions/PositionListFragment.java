package org.cerion.stockcharts.positions;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.repository.DividendRepository;
import org.cerion.stockcharts.repository.PositionRepository;
import org.cerion.stockcharts.repository.PriceListRepository;
import org.cerion.stocklist.PriceList;
import org.cerion.stocklist.model.Dividend;
import org.cerion.stocklist.model.Interval;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.model.Quote;
import org.cerion.stocklist.web.IYahooFinance;
import org.cerion.stocklist.web.YahooFinance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PositionListFragment extends ListFragment {
    private static final String TAG = PositionListFragment.class.getSimpleName();

    private static final String KEY_POSITION_ITEMS = "positionItems";
    private PositionListAdapter mAdapter;
    private List<Position> mPositions = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefresh;
    private DividendRepository dividendRepo;
    private PositionRepository repo;
    private PriceListRepository priceRepo;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Non-view related initialization
        repo = new PositionRepository(getContext());
        dividendRepo = new DividendRepository(getContext());
        priceRepo = new PriceListRepository(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.generic_list_fragment, container, false);
        mAdapter = new PositionListAdapter(getContext(), R.layout.list_item_position, mPositions);
        setListAdapter(mAdapter);

        if(savedInstanceState != null && savedInstanceState.containsKey(KEY_POSITION_ITEMS)) {
            PositionItems savedItems = (PositionItems)savedInstanceState.getSerializable(KEY_POSITION_ITEMS);
            if(savedItems != null)
                refreshList(savedItems.list);
        } else
            refreshList();

        setHasOptionsMenu(true);

        //Swipe Refresh
        mSwipeRefresh = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                PositionListFragment.this.onRefresh();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        registerForContextMenu(getListView());

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Position p = mAdapter.getItem(position);
                Intent intent = PositionViewActivity.newIntent(getContext(), p.getId());
                startActivity(intent);
            }
        });
    }

    /*
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_POSITION_ITEMS, new PositionItems(mPositions));
    }
    */

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        if (v.getId() == android.R.id.list) {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.position_list_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.delete:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Position p = mPositions.get(info.position);
                Log.d(TAG, "removing " + p.getSymbol());

                repo.delete(p);
                refreshList();
                break;
            default:
                return super.onContextItemSelected(item);
        }

        return true;
    }

    private void refreshList() {
        refreshList(null);
    }

    private void refreshList(List<Position> savedPositions) {
        mPositions.clear();

        if(savedPositions != null)
            mPositions.addAll( savedPositions);
        else
            mPositions.addAll( repo.getAll() );

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.position_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.update_dividends:
                updateDividends();
                break;
            case R.id.add_position:
                Intent intent = new Intent(getContext(), PositionEditActivity.class);
                startActivityForResult(intent, 0);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // For now just assume result is just the activity that possibly added a new position entry in the db
        refreshList();
    }

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

    private void onRefresh() {
        mSwipeRefresh.setRefreshing(true);

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            private IYahooFinance api = new YahooFinance();

            private Map<String,Quote> getQuotes(List<Position> positions) {
                Set<String> symbols = new HashSet<>();
                for(Position p : mPositions)
                    symbols.add(p.getSymbol());

                return api.getQuotes(symbols);
            }

            @Override
            public void run() {
                Map<String,Quote> quotes = getQuotes(mPositions);

                for(Position p : mPositions) {
                    String symbol = p.getSymbol();
                    Quote q = quotes.get(symbol);

                    if(p.IsDividendsReinvested()) {
                        try {
                            PriceList list = priceRepo.getLatest(symbol, Interval.DAILY);
                            p.setPriceHistory(list);
                        } catch (Exception e){
                            continue;
                        }

                    }

                    p.setQuote(q);

                    // Check for cached copy of dividends
                    List<Dividend> list = dividendRepo.get(symbol);
                    boolean refreshList = true;

                    // Check if list needs to be refreshed
                    for(Dividend d : list) {
                        if(d.mDate.equals(q.dividendDate)) {
                            refreshList = false;
                            break;
                        }
                    }

                    if(refreshList) {
                        // TODO not sure its possible at the moment to get this so a manual menu option is added for now
                        if(q.dividendDate == null) {
                            Log.d(TAG,symbol + ": Unable to determine if dividends were updated, skipping for now");
                        } else {
                            list = dividendRepo.getLatest(symbol);
                            Log.d(TAG, "downloaded new list, size = " + list.size());
                            dividendRepo.add(symbol, list);
                        }
                    } else {
                        //Log.d(TAG, "using previous list");
                    }

                    p.addDividends(list);

                    //db.log();
                }
            }

            @Override
            public void onFinish() {
                mAdapter.notifyDataSetChanged();
                mSwipeRefresh.setRefreshing(false);
            }
        });

        task.execute();
    }

    private static class PositionItems implements Serializable {
        public List<Position> list;

        public PositionItems(List<Position> list) {
            this.list = list;
        }
    }
}
