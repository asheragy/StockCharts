package org.cerion.stockcharts.ui.symbols;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.SymbolLookupDialogFragment;
import org.cerion.stockcharts.common.ViewModelFragment;


public class SymbolsFragment extends ViewModelFragment<SymbolsViewModel> implements SymbolLookupDialogFragment.OnSymbolListener {

    private static final String TAG = SymbolsFragment.class.getSimpleName();
    private RecyclerViewAdapter adapter;

    @Override
    protected SymbolsViewModel newViewModel() {
        return new SymbolsViewModel(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_symbols, container, false);

        adapter = new RecyclerViewAdapter(getViewModel().items);

        RecyclerView rv = (RecyclerView)view.findViewById(R.id.recycler_view);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(adapter);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == RecyclerViewAdapter.CONTEXT_MENU_GROUP_ID) {
            SymbolItemViewModel vm = getViewModel().items.get(item.getOrder());

            switch (item.getItemId()) {
                case RecyclerViewAdapter.CONTEXT_MENU_DELETE:
                    getViewModel().delete(vm.getSymbol());
                    break;
                default:
                    return super.onContextItemSelected(item);
            }

            return true;
        }

        return false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getViewModel().load();
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

        // FIX symbol add
        Toast.makeText(getContext(), "Not implemented", Toast.LENGTH_SHORT).show();
        /*
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
        */
    }

    @Override
    public void onSymbolEntered(final String name) {
        getViewModel().add(name);
    }
}
