package org.cerion.stockcharts.positions;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.Observable;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.ViewModelFragment;
import org.cerion.stockcharts.databinding.FragmentPositionsBinding;
import org.cerion.stocks.core.model.PositionWithDividends;

public class PositionListFragment extends ViewModelFragment<PositionsViewModel> {
    private static final String TAG = PositionListFragment.class.getSimpleName();
    private PositionsViewModel vm;
    private FragmentPositionsBinding binding;

    @Override
    protected PositionsViewModel newViewModel() {
        return new PositionsViewModel(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPositionsBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
        vm = getViewModel();

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final RecyclerViewAdapter adapter = new RecyclerViewAdapter(vm.positions.get());
        binding.setViewModel(getViewModel());
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setAdapter(adapter);

        vm.positions.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                Log.d(TAG, "thread ID = " + Thread.currentThread().getName());

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        vm.allocations.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                binding.chart.setAllocations(vm.allocations.get());
            }
        });

        if (!isRetained())
            vm.load();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (getUserVisibleHint()) {
            PositionItemViewModel p = vm.positions.get().get(item.getOrder());

            switch (item.getItemId()) {
                case RecyclerViewAdapter.CONTEXT_MENU_EDIT:
                    onEdit(p.getPosition());
                    break;
                case RecyclerViewAdapter.CONTEXT_MENU_DELETE:
                    vm.delete(p.getPosition());
                    break;
                default:
                    return super.onContextItemSelected(item);
            }

            return true;
        }

        return false;
    }

    private void onEdit(PositionWithDividends p) {
        Intent intent = PositionEditActivity.newIntent(getContext(), p);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.position_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
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
        vm.load();
    }
}
