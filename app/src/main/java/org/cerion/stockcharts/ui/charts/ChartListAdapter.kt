package org.cerion.stockcharts.ui.charts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.cerion.stocks.core.charts.StockChart


class ChartListAdapter : RecyclerView.Adapter<ChartListAdapter.ViewHolder>() {

    private var charts: List<StockChart> = emptyList()

    fun setCharts(charts: List<StockChart>) {
        this.charts = charts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return charts.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = charts[position]
        holder.bind(item)
    }

    class ViewHolder internal constructor(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(chart: StockChart) {
            val tv = view.findViewById<TextView>(android.R.id.text1)
            tv.text = chart::class.java.simpleName
        }
    }
}