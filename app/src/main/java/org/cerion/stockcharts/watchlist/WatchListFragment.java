package org.cerion.stockcharts.watchlist;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.cerion.stockcharts.Injection;
import org.cerion.stockcharts.common.ViewModelFragment;
import org.cerion.stockcharts.databinding.FragmentWatchlistBinding;

public class WatchListFragment extends ViewModelFragment<WatchListViewModel> implements View.OnClickListener {
    RecyclerViewAdapter adapter;
    FragmentWatchlistBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWatchlistBinding.inflate(inflater, container, false);
        binding.setViewModel(getViewModel());
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        binding.recyclerView.setOnClickListener(this);
        // TODO load data here when in database

        // TODO show like this http://www.marketwatch.com/watchlist
        adapter = new RecyclerViewAdapter(getViewModel().items.get());
        binding.recyclerView.setAdapter(adapter);

        if (!isRetained())
            getViewModel().load();
    }

    @Override
    protected WatchListViewModel newViewModel() {
        return new WatchListViewModel(Injection.getAPI(getContext()));
    }

    @Override
    public void onClick(View view) {
        int position = binding.recyclerView.getChildLayoutPosition(view);
        WatchItemViewModel item = getViewModel().items.get().get(position);

        Toast.makeText(getContext(), item.getCondition(), Toast.LENGTH_LONG).show();
    }
}
