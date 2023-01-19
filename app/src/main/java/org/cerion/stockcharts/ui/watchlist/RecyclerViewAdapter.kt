package org.cerion.stockcharts.ui.watchlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cerion.stockcharts.databinding.ListItemWatchBinding

internal class RecyclerViewAdapter() : RecyclerView.Adapter<RecyclerViewAdapter.ListItemViewHolder>() {

    private var items = emptyList<WatchItemViewModel>()

    fun setItems(items: List<WatchItemViewModel>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemWatchBinding.inflate(layoutInflater, parent, false)
        return ListItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ListItemViewHolder(var binding: ListItemWatchBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            //binding.weekRange.isEnabled = false
            //binding.yearRange.isEnabled = false
            binding.root.setOnClickListener(this)
        }

        fun bind(viewModel: WatchItemViewModel) {
            //binding.viewModel = viewModel
            //binding.executePendingBindings()
        }

        override fun onClick(v: View) { // TODO open charts activity/fragment
            //Intent intent = ChartsActivity.newIntent(v.getContext(), binding.getViewModel().getSymbol(), binding.getViewModel().getChart());
            //v.getContext().startActivity(intent);
        }
    }
}