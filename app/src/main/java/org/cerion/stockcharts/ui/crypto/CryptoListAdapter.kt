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
    val decimalFormat = DecimalFormat("#0.00")

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
            item.quote?.also {
                binding.price.text = it.price.toString()
                binding.changeHour.text = decimalFormat.format(it.changeHour)
                binding.changeDay.text = decimalFormat.format(it.changeDay)
                binding.changeWeek.text = decimalFormat.format(it.changeWeek)
            }

            binding.root.setOnClickListener {
                listener.onClick(item.symbol)
            }
        }
    }

}