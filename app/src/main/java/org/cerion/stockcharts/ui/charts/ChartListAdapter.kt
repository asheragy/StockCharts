package org.cerion.stockcharts.ui.charts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import org.cerion.stockcharts.R
import org.cerion.stockcharts.charts.views.ChartViewFactory
import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.charts.StockChart


class ChartListAdapter(private val context: Context) : RecyclerView.Adapter<ChartListAdapter.ViewHolder>() {

    private var charts: List<StockChart> = emptyList()
    private val factory = ChartViewFactory(context)
    private var prices: PriceList? = null

    fun setCharts(charts: List<StockChart>, prices: PriceList?) {
        this.charts = charts
        this.prices = prices
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.view_chart, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = charts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = charts[position]
        holder.bind(item)
    }

    inner class ViewHolder internal constructor(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(chart: StockChart) {
            val frame = view.findViewById<FrameLayout>(R.id.chart_frame)
            frame.removeAllViews()

            if (prices.isNullOrEmpty())
                frame.addView(factory.emptyChart)
            else
                frame.addView(factory.getChart(chart, prices))
        }
    }
}