package org.cerion.stockcharts.positions;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
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
import android.widget.ArrayAdapter;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.ViewModelFragment;
import org.cerion.stockcharts.databinding.FragmentPositionsBinding;
import org.cerion.stockcharts.databinding.ListItemPositionBinding;
import org.cerion.stocklist.model.Position;

import java.util.List;

public class PositionListFragment extends ViewModelFragment<PositionsViewModel> {
    private static final String TAG = PositionListFragment.class.getSimpleName();

    private PositionListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    private PositionsViewModel vm;
    private FragmentPositionsBinding binding;

    @Override
    protected PositionsViewModel newViewModel() {
        return new PositionsViewModel(getContext());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //vm = new PositionsViewModel(getContext());
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

        //Swipe Refresh
        mSwipeRefresh = binding.swipeRefresh;
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                vm.update();
            }
        });

        mAdapter = new PositionListAdapter(getContext(), R.layout.list_item_position, vm.positions.get());

        vm.positions.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                Log.d(TAG, "thread ID = " + Thread.currentThread().getName());

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        binding.setViewModel(getViewModel());
        binding.list.setAdapter(mAdapter);
        binding.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PositionItemViewModel p = mAdapter.getItem(position);
                Intent intent = PositionDetailActivity.newIntent(getContext(), p.getPosition().getId());
                startActivity(intent);
            }
        });

        registerForContextMenu(binding.list);

        vm.loading.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                mSwipeRefresh.setRefreshing(vm.loading.get());
            }
        });

        if (!isRetained())
            vm.load();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == binding.list.getId()) {
            super.onCreateContextMenu(menu, v, menuInfo);
            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.position_list_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        PositionItemViewModel p = vm.positions.get().get(info.position);
        // TODO add this back

        switch(item.getItemId()) {
            case R.id.edit:
                onEdit(p.getPosition());
                break;
            case R.id.delete:
                vm.delete(p.getPosition());
                break;
            default:
                return super.onContextItemSelected(item);
        }

        return true;
    }

    private void onEdit(Position p) {
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

    private static class PositionListAdapter extends ArrayAdapter<PositionItemViewModel> {
        private int mColorGreen;
        private int mColorRed;
        private LayoutInflater inflater;

        public PositionListAdapter(Context context, int resource, List<PositionItemViewModel> objects) {
            super(context, resource, objects);

            mColorGreen = context.getResources().getColor(R.color.positive_green);
            mColorRed = context.getResources().getColor(R.color.negative_red);

            inflater = LayoutInflater.from(getContext());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PositionItemViewModel p = getItem(position);

            ListItemPositionBinding binding;

            if (convertView == null) {
                //binding = DataBindingUtil.inflate(inflater, R.layout.list_item_position, parent, false);
                binding = ListItemPositionBinding.inflate(inflater, parent, false);
            } else {
                // TODO make sure this works when recycled
                binding = DataBindingUtil.getBinding(convertView);
            }

            //final PositionItemViewModel vm = new PositionItemViewModel(p, mColorGreen, mColorRed);
            binding.setViewmodel(p);

            return binding.getRoot();
        }

    }
}
