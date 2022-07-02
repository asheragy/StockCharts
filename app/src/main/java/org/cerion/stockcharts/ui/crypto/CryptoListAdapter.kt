package org.cerion.stockcharts.ui.crypto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cerion.stockcharts.databinding.ListItemCryptoBinding

class CryptoListAdapter : RecyclerView.Adapter<CryptoListAdapter.ViewHolder>() {

    private var items = emptyList<CryptoRow>()

    fun setRows(list: List<CryptoRow>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoListAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemCryptoBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CryptoListAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder internal constructor(val binding: ListItemCryptoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CryptoRow) {
            binding.name.text = item.id
            binding.price.text = item.price.toString()
        }
    }

}