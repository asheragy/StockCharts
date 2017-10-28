package org.cerion.stockcharts.watchlist;


import android.app.Fragment;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cerion.stockcharts.databinding.FragmentWatchlistBinding;

public class WatchListFragment extends Fragment {

    private static final String TAG = WatchListFragment.class.getSimpleName();
    RecyclerViewAdapter adapter;
    WatchListViewModel viewModel;
    FragmentWatchlistBinding binding;
    boolean isRetained;

    public WatchListFragment() {
        super();
        Log.d(TAG, "constructed");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        binding = FragmentWatchlistBinding.inflate(inflater, container, false);
        setRetainInstance(true);

        if (viewModel != null)
            isRetained = true;
        else
            viewModel = new WatchListViewModel();

        binding.setViewModel(viewModel);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        viewModel.items.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                adapter.notifyDataSetChanged();
            }
        });

        // TODO load data here when in database

        // TODO show like this http://www.marketwatch.com/watchlist
        adapter = new RecyclerViewAdapter(viewModel.items.get());
        binding.recyclerView.setAdapter(adapter);

        if (!isRetained)
            viewModel.load();
    }
}
