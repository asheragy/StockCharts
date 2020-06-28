package org.cerion.stockcharts.ui.charts

import android.content.Context
import android.graphics.Matrix
import android.os.Handler
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
    private var _viewPortMatrix: Matrix? = null
    private var _viewPortValues: FloatArray? = null

    fun setCharts(charts: List<StockChart>, prices: PriceList?) {
        this.charts = charts
        this.prices = prices
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ViewChartBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }


    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        syncMatrix(holder)
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

                chartView.setOnLongClickListener {
                    chartListener.onClick(chart)
                    true
                }

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

                // Workaround for fixing viewPortOffset issue //TODO look into more as a bug
                Handler().postDelayed({
                    chartView.invalidate()
                    syncMatrix(this) // This was not working unless it was inside here, possibly related to bug OR viewport stuff needs to go AFTER this operation is done
                }, 10)
            }
        }
    }

    private val _otherVals = FloatArray(9)

    fun syncMatrix(matrix: Matrix, values: FloatArray, viewHolder: ChartListAdapter.ViewHolder) {
        _viewPortMatrix = matrix // TODO these should be set to null again, maybe when priceList changes but not chart list since adding new chart should not reset
        _viewPortValues = values

        syncMatrix(viewHolder)
    }

    private fun syncMatrix(viewHolder: ChartListAdapter.ViewHolder) {
        if (_viewPortMatrix == null || _viewPortValues == null)
            return

        val tempChart = viewHolder.itemView.findViewById<BarLineChartBase<*>>(R.id.chart_view)
        val otherMatrix: Matrix = tempChart.viewPortHandler.matrixTouch

        // Realtime refresh is called from a matrix parameter, only need to update if its NOT the chart currently being resized
        if (_viewPortMatrix == otherMatrix)
            return

        // TODO see if there is another way to set the values, if this was the example code it may be the best solution already...
        otherMatrix.getValues(_otherVals)
        _otherVals[Matrix.MSCALE_X] = _viewPortValues!![Matrix.MSCALE_X]
        _otherVals[Matrix.MTRANS_X] = _viewPortValues!![Matrix.MTRANS_X]
        _otherVals[Matrix.MSKEW_X] = _viewPortValues!![Matrix.MSKEW_X]
        otherMatrix.setValues(_otherVals)
        tempChart.viewPortHandler.refresh(otherMatrix, tempChart, true)
    }
}