package org.cerion.stockcharts.charts;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.database.StockDB;
import org.cerion.stockcharts.database.StockDataStore;
import org.cerion.stocklist.model.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ChartsListFragment extends ListFragment {

    private ArrayAdapter<String> mAdapter;
    private List<String> mLists = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.charts_list_fragment, container, false);

        mAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, mLists);
        setListAdapter(mAdapter);
        fillList();

        return view;
    }

    private void fillList() {

        StockDataStore db = StockDB.getInstance(getActivity());
        List<Position> positions = db.getPositions();
        Set<String> items = new TreeSet<>();
        for(Position p : positions) {
            if(!items.contains(p.getSymbol())) {
                mLists.add(p.getSymbol());
                items.add(p.getSymbol());
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent intent = new Intent(getActivity(),ChartViewActivity.class);
        intent.putExtra(ChartViewActivity.EXTRA_SYMBOL, mLists.get(position));
        startActivity(intent);
    }
}
