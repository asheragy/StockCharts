package org.cerion.stockcharts.ui.symbols;

import android.content.Intent;
import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cerion.stockcharts.charts.ChartsActivity;
import org.cerion.stockcharts.common.OnListAnyChangeCallback;
import org.cerion.stockcharts.databinding.ListItemSymbolBinding;

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ListItemViewHolder> {

    public static final int CONTEXT_MENU_GROUP_ID = 100; // This id needs to be unique
    public static final int CONTEXT_MENU_DELETE = CONTEXT_MENU_GROUP_ID + 1;

    private ObservableList<SymbolItemViewModel> items;

    public RecyclerViewAdapter(ObservableList<SymbolItemViewModel> symbols) {
        items = symbols;
        items.addOnListChangedCallback(new OnListAnyChangeCallback<ObservableList<SymbolItemViewModel>>() {
            @Override
            public void onAnyChange(ObservableList sender) {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemSymbolBinding binding = ListItemSymbolBinding.inflate(layoutInflater, parent, false);
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

    public final class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

        ListItemSymbolBinding binding;
        public ListItemViewHolder(ListItemSymbolBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setOnCreateContextMenuListener(this);
        }

        public void bind(SymbolItemViewModel item) {
            binding.setViewModel(item);
            binding.executePendingBindings();
        }

        @Override
        public void onClick(View v) {
            String symbol = binding.getViewModel().getSymbol();
            Intent intent = ChartsActivity.newIntent(v.getContext(), symbol);
            v.getContext().startActivity(intent);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(CONTEXT_MENU_GROUP_ID, CONTEXT_MENU_DELETE, getAdapterPosition(), "Delete");
        }
    }
}
