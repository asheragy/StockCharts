package org.cerion.stockcharts.watchlist;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cerion.stockcharts.R;
import org.cerion.stockcharts.common.Utils;

import java.util.List;

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ListItemViewHolder> {

    private List<WatchItem> items;

    RecyclerViewAdapter(List<WatchItem> items) {
        this.items = items;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_watch, parent, false);
        return new ListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {
        WatchItem item = items.get(position);
        holder.symbol.setText(item.getSymbol());
        holder.condition.setText(item.getCondition().toString());

        if(item.price == 0)
            holder.price.setText("--");
        else
            holder.price.setText(Utils.decimalFormat.format(item.price));

        holder.change.setText("--");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public final class ListItemViewHolder extends RecyclerView.ViewHolder {
        TextView symbol;
        TextView price;
        TextView change;
        TextView condition;

        public ListItemViewHolder(View itemView) {
            super(itemView);
            symbol = (TextView) itemView.findViewById(R.id.symbol);
            price = (TextView) itemView.findViewById(R.id.price);
            change = (TextView) itemView.findViewById(R.id.change);
            condition = (TextView) itemView.findViewById(R.id.condition);
        }
    }
}
