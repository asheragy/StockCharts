package org.cerion.stockcharts;

import android.content.Intent;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.cerion.stockcharts.database.StockDB;
import org.cerion.stockcharts.database.StockDataStore;

import java.util.ArrayList;
import java.util.List;


public class SymbolListFragment extends ListFragment {

    private ArrayAdapter<String> mAdapter;
    private List<String> mSymbols = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.generic_list_fragment, container, false);

        mAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, mSymbols);
        setListAdapter(mAdapter);

        //TODO add empty list case

        refresh();

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String s = mSymbols.get(position);

        Intent intent = new Intent(getActivity(),PriceListActivity.class);
        intent.putExtra(PriceListActivity.SYMBOL_EXTRA,s);

        //Intent intent = new Intent(getActivity(),ChartViewActivity.class);
        //intent.putExtra(PriceListActivity.SYMBOL_EXTRA,s);

        startActivity(intent);
    }

    public void refresh()
    {
        StockDataStore db = StockDB.getInstance(this.getContext());
        db.log();
        mSymbols.clear();
        mSymbols.addAll( db.getSymbols() );

        mAdapter.notifyDataSetChanged();
    }

}
