package org.cerion.stockcharts.ui.symbols

import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cerion.stockcharts.databinding.ListItemSymbolBinding
import org.cerion.stockcharts.ui.charts.ChartsActivity.Companion.getIntent
import org.cerion.stocks.core.model.Symbol

internal class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.ListItemViewHolder>() {

    private var items = emptyList<Symbol>()

    fun setItems(items: List<Symbol>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemSymbolBinding.inflate(layoutInflater, parent, false)
        return ListItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ListItemViewHolder(var binding: ListItemSymbolBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener, OnCreateContextMenuListener {

        fun bind(item: Symbol) {
            binding.item = item
            binding.executePendingBindings()
        }

        override fun onClick(v: View) {
            val symbol = binding.item!!.symbol
            val intent = getIntent(v.context, symbol)
            v.context.startActivity(intent)
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenuInfo?) {
            menu?.add(CONTEXT_MENU_GROUP_ID, CONTEXT_MENU_DELETE, adapterPosition, "Delete")
        }

        init {
            binding.root.setOnClickListener(this)
            binding.root.setOnCreateContextMenuListener(this)
        }
    }

    companion object {
        const val CONTEXT_MENU_GROUP_ID = 100 // This id needs to be unique
        const val CONTEXT_MENU_DELETE = CONTEXT_MENU_GROUP_ID + 1
    }
}