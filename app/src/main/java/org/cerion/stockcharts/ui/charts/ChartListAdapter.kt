package org.cerion.stockcharts.ui.charts

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarLineChartBase
import org.cerion.stockcharts.R
import org.cerion.stockcharts.charts.views.ChartViewFactory
import org.cerion.stockcharts.common.TAG
import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.charts.StockChart

class StockChartListener(val clickListener: (chart: StockChart) -> Unit) {
    fun onClick(chart: StockChart) = clickListener(chart)
}

class ChartListAdapter(context: Context, private val clickListener: StockChartListener) : RecyclerView.Adapter<ChartListAdapter.ViewHolder>() {

    private var charts: List<StockChart> = emptyList()
    private val factory = ChartViewFactory(context)
    private var prices: PriceList? = null
    private var range: Pair<Int, Int>? = null

    fun setCharts(charts: List<StockChart>, prices: PriceList?) {
        this.charts = charts
        this.prices = prices
        notifyDataSetChanged()
    }

    fun setRange(start: Int, end: Int) {
        range = Pair(start, end)
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
            val tag = Pair(chart, prices)

            val frame = view.findViewById<FrameLayout>(R.id.chart_frame)

            if (frame.childCount == 0 || frame.tag != tag) {
                frame.removeAllViews()
                if (prices.isNullOrEmpty()) {
                    frame.addView(factory.emptyChart)
                    frame.tag = null
                }
                else {
                    val chartView = factory.getChart(chart, prices)
                    chartView.setOnClickListener { clickListener.onClick(chart) }
                    frame.addView(chartView)
                    frame.tag = tag
                }
            }
            else if (range != null) {
                // TODO range actually needs to apply to a new chart (but not blank)
                val currRange = range!!
                Log.d(TAG, "range $currRange")

                val c = frame.getChildAt(0) as BarLineChartBase<*>
                val start = currRange.first.toFloat()
                val end = currRange.second.toFloat()
                c.setVisibleXRangeMinimum(end - start)
                c.setVisibleXRangeMaximum(end - start)
                c.moveViewToX(start)
            }
        }
    }
}