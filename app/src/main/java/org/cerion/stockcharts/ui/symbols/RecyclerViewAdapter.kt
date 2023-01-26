package org.cerion.stockcharts.ui.symbols

import android.graphics.Color
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cerion.marketdata.webclients.tda.Quote
import org.cerion.stockcharts.databinding.ListItemSymbolBinding

internal class RecyclerViewAdapter(private val listener: SymbolListener) : RecyclerView.Adapter<RecyclerViewAdapter.ListItemViewHolder>() {

    interface SymbolListener {
        fun click(symbol: String)
    }

    private var items = emptyList<Pair<String, Quote?>>()

    fun setItems(items: Map<String, Quote?>) {
        this.items = items.toList()
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

        fun bind(item: Pair<String, Quote?>) {
            binding.symbol.text = item.first
            item.second?.apply {
                binding.price.text = price.toString()
                binding.low.text = low52.toString()
                binding.high.text = high52.toString()

                val neg = change < 0
                val changePercent = String.format("%.2f",change)
                binding.change.text = "(${if(neg) "" else "+"}$changePercent%)"

                val red = Color.parseColor("#B71C1C")
                val green = Color.parseColor("#41c300")
                binding.change.setTextColor(if(neg) red else green)

                val percent = (price - low52) / (high52 - low52)
                binding.range.progress = (percent * 100).toInt()
            }
        }

        override fun onClick(v: View) {
            val symbol = binding.symbol.text.toString()
            listener.click(symbol)
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