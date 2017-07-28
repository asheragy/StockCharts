package org.cerion.stockcharts;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.cerion.stockcharts.charts.ChartsActivity;
import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.common.SymbolLookupDialogFragment;
import org.cerion.stockcharts.repository.PositionRepository;
import org.cerion.stockcharts.repository.SymbolRepository;
import org.cerion.stockcharts.viewmodel.SymbolListViewModel;
import org.cerion.stockcharts.viewmodel.SymbolListViewModel.SymbolItem;
import org.cerion.stocklist.model.Position;
import org.cerion.stocklist.model.Symbol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SymbolListFragment extends ListFragment implements SymbolLookupDialogFragment.OnSymbolListener {

    private static final String TAG = SymbolListFragment.class.getSimpleName();
    //private ArrayAdapter<String> mAdapter;
    private SymbolListAdapter mAdapter;
    private List<SymbolListViewModel.SymbolItem> mSymbols = new ArrayList<>();
    private SymbolListViewModel vm;
    private SymbolRepository repo = new SymbolRepository(getContext());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.generic_list_fragment, container, false);

        vm = new SymbolListViewModel(getActivity());

        //mAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, mSymbols);
        mAdapter = new SymbolListAdapter(getContext(), mSymbols);

        setListAdapter(mAdapter);
        setHasOptionsMenu(true);

        //TODO add empty list case
        refresh();
        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String s = mSymbols.get(position).symbol.getSymbol();
        Intent intent = ChartsActivity.newIntent(getContext(), s);

        //Intent intent = new Intent(getActivity(),PriceListActivity.class);
        //intent.putExtra(PriceListActivity.SYMBOL_EXTRA,s);
        //Intent intent = new Intent(getActivity(),ChartsActivity.class);
        //intent.putExtra(PriceListActivity.SYMBOL_EXTRA,s);
        startActivity(intent);
    }

    public void refresh() {
        mSymbols.clear();
        mSymbols.addAll( vm.getItems() );
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.symbol_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.add_symbol:
                onAddSymbol();
                break;

            case R.id.sync_symbols:
                onSyncSymbols();
                break;
            default:
                return super.onContextItemSelected(item);
        }

        return true;
    }

    private static final int DIALOG_FRAGMENT = 1;
    private void onAddSymbol() {
        //DialogFragment newFragment = new SymbolLookupDialogFragment();
        //newFragment.show(getFragmentManager(), "addSymbol");

        // TODO simpler way of doing this?
        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        Fragment prev = getActivity().getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment dialog = new SymbolLookupDialogFragment();
        dialog.setTargetFragment(this, DIALOG_FRAGMENT);
        dialog.show(getFragmentManager().beginTransaction(), "dialog");
    }

    private void onSyncSymbols() {
        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            @Override
            public void run() {
                PositionRepository positionRepo = new PositionRepository(getContext());

                List<Symbol> sList = repo.getAll();
                Set<String> symbols = new HashSet<>();
                for(Symbol s : sList)
                    symbols.add(s.getSymbol());

                List<Position> positions = positionRepo.getAll();
                for(Position p : positions) {
                    if (!symbols.contains(p.getSymbol())) {
                        repo.add(p.getSymbol());
                        symbols.add(p.getSymbol());
                    }
                }
            }

            @Override
            public void onFinish() {
                refresh();
            }
        });

        task.execute();
    }

    @Override
    public void onSymbolEntered(final String name) {

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            boolean result = false;

            @Override
            public void run() {
                result = repo.add(name);
            }

            @Override
            public void onFinish() {
                refresh();
                if(!result)
                    Toast.makeText(getContext(), "Could not find '" + name + "'", Toast.LENGTH_SHORT).show();
            }
        });

        task.execute();
    }

    private class ViewHolder {
        TextView symbol;
        TextView name;
        TextView exchange;
        TextView lists;
    }

    private class SymbolListAdapter extends ArrayAdapter<SymbolItem> {

        private static final int LAYOUT_ID = R.layout.list_item_symbol;

        public SymbolListAdapter(Context context, List<SymbolItem> items) {
            super(context, LAYOUT_ID, items);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SymbolItem item = getItem(position);
            Symbol symbol = item.symbol;
            ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(LAYOUT_ID, parent, false);

                viewHolder.symbol = (TextView) convertView.findViewById(R.id.symbol);
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.exchange = (TextView) convertView.findViewById(R.id.exchange);
                viewHolder.lists = (TextView) convertView.findViewById(R.id.lists);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.symbol.setText(symbol.getSymbol());
            viewHolder.name.setText(symbol.getName());
            viewHolder.exchange.setText(symbol.getExchange());

            if(item.position)
                viewHolder.lists.setText("P");
            else
                viewHolder.lists.setText("");

            return convertView;
        }
    }
}
