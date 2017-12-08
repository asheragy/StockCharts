package org.cerion.stockcharts.watchlist;


import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cerion.stockcharts.charts.ChartsActivity;
import org.cerion.stockcharts.databinding.ListItemWatchBinding;

import java.util.List;

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ListItemViewHolder> {

    private List<WatchItemViewModel> items;

    RecyclerViewAdapter(List<WatchItemViewModel> items) {
        this.items = items;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemWatchBinding binding = ListItemWatchBinding.inflate(layoutInflater, parent, false);
        return new ListItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static int defaultColor = -1;
    public final class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ListItemWatchBinding binding;

        public ListItemViewHolder(ListItemWatchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.weekRange.setEnabled(false);
            this.binding.yearRange.setEnabled(false);

            binding.getRoot().setOnClickListener(this);
        }

        public void bind(WatchItemViewModel item) {
            binding.setViewModel(item);
            binding.executePendingBindings();
            item.load();
        }

        @Override
        public void onClick(View v) {
            Intent intent = ChartsActivity.newIntent(v.getContext(), binding.getViewModel().getSymbol(), binding.getViewModel().getChart());
            v.getContext().startActivity(intent);
        }
    }
}
