package org.cerion.stockcharts.ui.charts

import android.content.Context
import android.graphics.Matrix
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarLineChartBase
import org.cerion.stockcharts.R
import org.cerion.stockcharts.common.DefaultChartGestureListener
import org.cerion.stockcharts.databinding.ViewChartBinding
import org.cerion.stockcharts.ui.charts.views.ChartViewFactory
import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.charts.StockChart


interface StockChartListener {
    fun onClick(chart: StockChart)
    fun onViewPortChange(matrix: Matrix)
}

class ChartListAdapter(context: Context, private val chartListener: StockChartListener) : RecyclerView.Adapter<ChartListAdapter.ViewHolder>() {

    private var charts: List<StockChart> = emptyList()
    private val factory = ChartViewFactory(context)
    private var prices: PriceList? = null

    fun setCharts(charts: List<StockChart>, prices: PriceList?) {
        this.charts = charts
        this.prices = prices
        notifyDataSetChanged()
    }

    private var viewPortMatrix: Matrix? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ViewChartBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = charts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = charts[position]
        holder.bind(item)
    }

    inner class ViewHolder internal constructor(val binding: ViewChartBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chart: StockChart) {
            val frame = binding.chartFrame
            frame.removeAllViews()

            if (prices.isNullOrEmpty()) {
                frame.addView(factory.getEmptyChart())
            }
            else {
                val chartView = factory.getChart(chart, prices!!)
                /*
                chartView.setOnLongClickListener {
                    chartListener.onClick(chart)
                    true
                }

                 */

                val matrix = chartView.viewPortHandler.matrixTouch

                chartView.onChartGestureListener = object : DefaultChartGestureListener() {
                    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
                        super.onChartScale(me, scaleX, scaleY)
                        chartListener.onViewPortChange(matrix)
                    }

                    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
                        super.onChartTranslate(me, dX, dY)
                        chartListener.onViewPortChange(matrix)
                    }
                }

                chartView.id = R.id.chart_view
                frame.addView(chartView)
            }
        }
    }

    private val otherVals = FloatArray(9)
    fun syncMatrix(matrix: Matrix, values: FloatArray, viewHolder: ChartListAdapter.ViewHolder) {
        val tempChart = viewHolder.itemView.findViewById<BarLineChartBase<*>>(R.id.chart_view)

        val otherMatrix: Matrix = tempChart.viewPortHandler.matrixTouch
        if (matrix == otherMatrix)
            return

        otherMatrix.getValues(otherVals)
        otherVals[Matrix.MSCALE_X] = values[Matrix.MSCALE_X]
        otherVals[Matrix.MTRANS_X] = values[Matrix.MTRANS_X]
        otherVals[Matrix.MSKEW_X] = values[Matrix.MSKEW_X]
        otherMatrix.setValues(otherVals)
        tempChart.viewPortHandler.refresh(otherMatrix, tempChart, true)
    }
}