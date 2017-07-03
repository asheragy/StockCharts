package org.cerion.stockcharts.positions;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import org.cerion.stockcharts.databinding.FragmentPositionsBinding;
import org.cerion.stockcharts.databinding.ListItemPositionBinding;
import org.cerion.stocklist.model.Position;

import java.util.List;

public class PositionListFragment extends ListFragment implements PositionsViewModel.IView {
    private static final String TAG = PositionListFragment.class.getSimpleName();

    private PositionListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefresh;
    private PositionsViewModel vm;
    private FragmentPositionsBinding bindingFrag;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vm = new PositionsViewModel(getContext(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bindingFrag = FragmentPositionsBinding.inflate(inflater, container, false);

        setHasOptionsMenu(true);

        return bindingFrag.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Swipe Refresh
        mSwipeRefresh = bindingFrag.swipeRefresh;
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefresh.setRefreshing(true);
                vm.update();
            }
        });

        mAdapter = new PositionListAdapter(getContext(), R.layout.list_item_position, vm.positions);
        setListAdapter(mAdapter);

        registerForContextMenu(getListView());
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Position p = mAdapter.getItem(position);
                Intent intent = PositionDetailActivity.newIntent(getContext(), p.getId());
                startActivity(intent);
            }
        });

        vm.load();
    }

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
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Position p = vm.positions.get(info.position);

        switch(item.getItemId()) {
            case R.id.edit:
                onEdit(p);
                break;
            case R.id.delete:
                vm.delete(p);
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

    @Override
    public void onNewData() {
        mAdapter.notifyDataSetChanged();
        mSwipeRefresh.setRefreshing(false);
    }

    private static class PositionListAdapter extends ArrayAdapter<Position> {
        private int mColorGreen;
        private int mColorRed;
        private LayoutInflater inflater;

        public PositionListAdapter(Context context, int resource, List<Position> objects) {
            super(context, resource, objects);

            mColorGreen = context.getResources().getColor(R.color.positive_green);
            mColorRed = context.getResources().getColor(R.color.negative_red);

            inflater = LayoutInflater.from(getContext());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Position p = getItem(position);

            ListItemPositionBinding binding;

            if (convertView == null) {
                //binding = DataBindingUtil.inflate(inflater, R.layout.list_item_position, parent, false);
                binding = ListItemPositionBinding.inflate(inflater, parent, false);
            } else {
                // TODO make sure this works when recycled
                binding = DataBindingUtil.getBinding(convertView);
            }

            final PositionItemViewModel vm = new PositionItemViewModel(p, mColorGreen, mColorRed);
            binding.setViewmodel(vm);

            return binding.getRoot();
        }

    }
}
