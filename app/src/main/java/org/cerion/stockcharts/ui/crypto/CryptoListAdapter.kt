package org.cerion.stockcharts.ui.crypto

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.cerion.stockcharts.databinding.ListItemCryptoBinding
import java.text.DecimalFormat

interface CryptoListListener {
    fun onClick(symbol: String)
}

class CryptoListAdapter(val listener: CryptoListListener) : RecyclerView.Adapter<CryptoListAdapter.ViewHolder>() {

    private var items = emptyList<CryptoRow>()
    val decimalFormat = DecimalFormat("##.00")

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
            binding.name.text = item.name
            binding.price.text = item.price.toString()
            binding.change.text = decimalFormat.format(item.change24h) + "%"

            binding.root.setOnClickListener {
                listener.onClick(item.symbol)
            }
        }
    }

}