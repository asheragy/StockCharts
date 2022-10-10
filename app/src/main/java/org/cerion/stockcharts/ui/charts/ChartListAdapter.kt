package org.cerion.stockcharts.ui.charts

import android.content.Context
import android.graphics.Matrix
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.components.YAxis.AxisDependency
import org.cerion.marketdata.core.charts.StockChart
import org.cerion.marketdata.core.model.OHLCVTable
import org.cerion.stockcharts.R
import org.cerion.stockcharts.common.DefaultChartGestureListener
import org.cerion.stockcharts.databinding.ViewChartBinding
import org.cerion.stockcharts.ui.charts.views.ChartViewFactory


interface StockChartListener {
    fun onClick(chart: StockChart)
    fun onViewPortChange(matrix: Matrix)
}

class ChartListAdapter(context: Context, private val chartListener: StockChartListener) : RecyclerView.Adapter<ChartListAdapter.ViewHolder>() {

    private var charts: List<StockChart> = emptyList()
    private val factory = ChartViewFactory(context)
    private var table: OHLCVTable? = null
    private var intervals: Int = 0
    private var _viewPortMatrix: Matrix? = null
    private var _viewPortValues: FloatArray? = null

    fun setCharts(charts: List<StockChart>, table: OHLCVTable?, intervals: Int) {
        this.charts = charts
        this.table = table
        this.intervals = intervals
        _viewPortMatrix = null
        _viewPortValues = null
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
        holder.bind(item, position == 0)
    }

    inner class ViewHolder internal constructor(val binding: ViewChartBinding) : RecyclerView.ViewHolder(binding.root) {
        val chart: BarLineChartBase<*>
            get() = itemView.findViewById(R.id.chart_view)

        fun bind(chart: StockChart, first: Boolean) {
            val frame = binding.chartFrame
            frame.removeAllViews()

            if (table.isNullOrEmpty()) {
                frame.addView(factory.getEmptyChart())
            }
            else {
                val chartView = factory.getChart(chart, table!!)
                chartView as BarLineChartBase<*>
                if (intervals != 0) {
                    val end = table!!.close.size.toFloat()
                    val start = kotlin.math.max(0.0f, end - intervals.toFloat())

                    chartView.setVisibleXRangeMaximum(intervals.toFloat())
                    chartView.moveViewToX(end - start - 1)
                    chartView.setVisibleXRangeMaximum(table!!.close.size.toFloat()) // Workaround to make viewport manually adjustable again
                }

                // TODO not sure what this does in intervals block above
                chartView.moveViewToX(chartView.getXChartMax())

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

                    override fun onChartSingleTapped(me: MotionEvent?) {
                        super.onChartSingleTapped(me)
                        chartListener.onClick(chart)
                    }
                }

                chartView.id = R.id.chart_view
                frame.addView(chartView)

                /*
                Handler().postDelayed({
                    syncMatrix(this) // This was not working unless it was inside here, possibly related to bug OR viewport stuff needs to go AFTER this operation is done
                }, 10)
                 */

                if (!first)
                    chartView.xAxis.setDrawLabels(false) // Only draw labels on first chart

                syncMatrix(this)
            }
        }
    }

    private val _otherVals = FloatArray(9)

    fun syncMatrix(matrix: Matrix, values: FloatArray, viewHolder: ChartListAdapter.ViewHolder) {
        _viewPortMatrix = matrix // TODO these should be set to null again, maybe when priceList changes but not chart list since adding new chart should not reset
        _viewPortValues = values

        syncMatrix(viewHolder)

        // Adjust Y axis to fit scale of new view
        adjustYaxis(viewHolder.chart)
    }

    private fun syncMatrix(viewHolder: ChartListAdapter.ViewHolder) {
        if (_viewPortMatrix == null || _viewPortValues == null)
            return

        val tempChart = viewHolder.chart
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

    private fun adjustYaxis(chart: BarLineChartBase<*>) {
        val lowestVisibleX: Float = chart.lowestVisibleX
        val highestVisibleX: Float = chart.highestVisibleX

        val chartData = chart.data
        chartData.calcMinMaxY(lowestVisibleX, highestVisibleX)

        chart.xAxis.calculate(chartData.xMin, chartData.xMax)
        calculateMinMaxForYAxis(chart, AxisDependency.LEFT)
        calculateMinMaxForYAxis(chart, AxisDependency.RIGHT)

        chart.calculateOffsets()
    }

    private fun calculateMinMaxForYAxis(chart: BarLineChartBase<*>, axisDependency: AxisDependency) {
        val chartData = chart.data
        val yAxis = chart.getAxis(axisDependency)
        if (yAxis.isEnabled) {
            val yMin = chartData.getYMin(axisDependency)
            val yMax = chartData.getYMax(axisDependency)
            yAxis.calculate(yMin, yMax)
        }
    }
}