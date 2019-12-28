package org.cerion.stockcharts.positions;

import android.content.Intent;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.cerion.stockcharts.databinding.ListItemPositionBinding;

import java.util.List;

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ListItemViewHolder> {

    public static final int CONTEXT_MENU_GROUP_ID = 99;
    public static final int CONTEXT_MENU_EDIT = 0;
    public static final int CONTEXT_MENU_DELETE = 1;

    private List<PositionItemViewModel> items;

    public RecyclerViewAdapter(List<PositionItemViewModel> items) {
        this.items = items;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ListItemPositionBinding binding = ListItemPositionBinding.inflate(layoutInflater, parent, false);
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

        ListItemPositionBinding binding;
        public ListItemViewHolder(ListItemPositionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(this);
            binding.getRoot().setOnCreateContextMenuListener(this);
        }

        public void bind(PositionItemViewModel vm) {
            binding.setViewModel(vm);
            binding.executePendingBindings();
            vm.load();
        }

        @Override
        public void onClick(View v) {
            int id = binding.getViewModel().getPosition().getId();
            Intent intent = PositionDetailActivity.newIntent(v.getContext(), id);
            v.getContext().startActivity(intent);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            //menu.setHeaderTitle("Select The Action");
            menu.add(CONTEXT_MENU_GROUP_ID, CONTEXT_MENU_EDIT, getAdapterPosition(), "Edit");
            menu.add(CONTEXT_MENU_GROUP_ID, CONTEXT_MENU_DELETE, getAdapterPosition(), "Delete");
        }

    }
}
