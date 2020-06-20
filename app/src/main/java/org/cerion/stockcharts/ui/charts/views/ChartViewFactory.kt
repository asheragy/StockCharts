package org.cerion.stockcharts.ui.charts.views

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import org.cerion.stockcharts.R
import org.cerion.stockcharts.common.isDarkTheme
import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.charts.*
import org.cerion.stocks.core.charts.CandleDataSet
import org.cerion.stocks.core.charts.DataSet
import org.cerion.stocks.core.model.Interval
import java.math.BigDecimal
import java.math.MathContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.exp

class ColorMap(context: Context) {
    val default = if (context.isDarkTheme()) Color.WHITE else Color.BLACK
    val red = context.getColor(R.color.chart_red)
    val green = context.getColor(R.color.chart_green)
    val blue = context.getColor(R.color.chart_blue)
}

class ChartViewFactory(private val context: Context) {

    companion object {
        private const val CHART_HEIGHT_PRICE = 800
        private const val CHART_HEIGHT = 400
        private val dateFormat: DateFormat = SimpleDateFormat("MMM d, yy", Locale.ENGLISH)
        private val dateFormatMonthly: DateFormat = SimpleDateFormat("MMM ''yy", Locale.ENGLISH)
        private val blankDescription = Description().apply { text = "" }
    }

    private val _colors = ColorMap(context)
    private val _textColor = if (context.isDarkTheme()) context.getColor(R.color.secondaryTextColor) else context.getColor(R.color.primaryColor)

    fun getChart(chart: StockChart, list: PriceList): Chart<*> {
        return when(chart) {
            is PriceChart -> getPriceChart(chart, list)
            is IndicatorChart -> getLineChart(chart, list)
            is VolumeChart -> getVolumeChart(chart, list)
            else -> throw NotImplementedError()
        }
    }

    fun getEmptyChart(): Chart<*> {
        return LineChart(context).apply {
            description = blankDescription
            minimumHeight = CHART_HEIGHT
        }
    }

    private fun getPriceChart(pchart: PriceChart, list: PriceList): Chart<*> {
        val chart: BarLineChartBase<*>
        val sets = getDataSets(pchart, list)
        if (pchart.candleData && pchart.canShowCandleData(list)) {
            val data = CombinedData()
            data.setData(getCandleData(sets))
            data.setData(getLineData(sets))

            chart = CombinedChart(context)
            chart.drawOrder = arrayOf(DrawOrder.CANDLE, DrawOrder.LINE)
            chart.data = data
        }
        else {
            chart = LineChart(context)
            val lineData = getLineData(sets)
            chart.data = lineData
        }

        setChartDefaults(chart, pchart, list)
        chart.minimumHeight = CHART_HEIGHT_PRICE
        if (pchart.logScale)
            chart.axisRight.valueFormatter = logScaleYAxis

        setLegend(chart, sets)
        return chart
    }

    private fun getLineChart(ichart: IndicatorChart, list: PriceList): Chart<*> {
        return LineChart(context).apply {
            setChartDefaults(this, ichart, list)
            minimumHeight = CHART_HEIGHT

            val sets = getDataSets(ichart, list)
            data = getLineData(sets)
            setLegend(this, sets)
        }
    }

    private fun getVolumeChart(vchart: VolumeChart, list: PriceList): Chart<*> {
        return CombinedChart(context).apply {
            setChartDefaults(this, vchart, list)

            val dataSets = getDataSets(vchart, list)
            data = CombinedData().apply {
                setData(getBarData(dataSets))
                setData(getLineData(dataSets))
            }

            setLegend(this, dataSets)
            if (vchart.logScale)
                axisRight.valueFormatter = logScaleYAxis
        }
    }

    private fun setChartDefaults(chart: BarLineChartBase<*>, stockchart: StockChart, list: PriceList) {
        chart.apply {
            description = blankDescription
            minimumHeight = CHART_HEIGHT

            //Set Y axis
            axisLeft.setDrawLabels(false)
            axisRight.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
            axisRight.setLabelCount(3, false)
            axisRight.textColor = _textColor

            xAxis.valueFormatter = getAxisFormatter(stockchart.getDates(list), list.interval)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            // Always start at position 0 even if data set starts after that
            xAxis.axisMinimum = 0f
            xAxis.textColor = _textColor

            setViewPortOffsets(0f, viewPortHandler.offsetTop(), 0f, viewPortHandler.offsetBottom())
        }
    }

    private fun setLegend(chart: Chart<*>, sets: List<IDataSet>) {
        val entries = mutableListOf<LegendEntry>()
        var lastLabel = ""
        var lastColor = -1

        for (set in sets) {
            val label = set.label
            val color = set.color
            var entry: LegendEntry? = null
            if (lastLabel.contentEquals(label)) {
                if (lastColor != color) {
                    entry = LegendEntry(label, Legend.LegendForm.DEFAULT, Float.NaN, Float.NaN, null, color)
                    entries[entries.size - 1].label = null // label needs to go on the last one added
                }
            } else {
                entry = LegendEntry(label, Legend.LegendForm.DEFAULT, Float.NaN, Float.NaN, null, color)
            }

            if (entry != null)
                entries.add(entry)

            lastLabel = label
            lastColor = color
        }

        chart.legend.apply {
            setCustom(entries)
            setDrawInside(true)
            orientation = Legend.LegendOrientation.VERTICAL
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            isWordWrapEnabled = false
            textColor = _textColor
        }
    }

    private fun getDataSets(chart: StockChart, list: PriceList): List<IDataSet> {
        chart.setPrimaryColors(intArrayOf(_colors.default, _colors.red, _colors.blue, _colors.green))
        chart.setSecondaryColors(intArrayOf(_colors.red, _colors.blue, _colors.green))
        return chart.getDataSets(list)
    }

    private fun getBarData(sets: List<IDataSet>): BarData {
        val result = mutableListOf<IBarDataSet>()

        sets.filter { it.lineType == LineType.BAR }
                .forEach {
                    val set = it as DataSet

                    // TODO need to change corelib but DataSet should be mappable
                    val entries = mutableListOf<BarEntry>()
                    for (i in 0 until set.size)
                        entries.add(BarEntry(i.toFloat(), set[i]))

                    val dataSet = BarDataSet(entries, set.label)
                    dataSet.setDrawValues(false)
                    result.add(dataSet)
                }

        return BarData(result)
    }

    private fun getLineData(sets: List<IDataSet>): LineData {
        val result = mutableListOf<ILineDataSet>()

        for (curr in sets) {
            if (curr.lineType === LineType.LINE || curr.lineType === LineType.DOTTED) {

                val set = curr as DataSet
                val entries = mutableListOf<Entry>()

                for (i in 0 until set.size) {
                    val point = set[i]
                    if (!point.isNaN())
                        entries.add(Entry(i.toFloat(), point))
                }

                val lineDataSet = LineDataSet(entries, set.label).apply {
                    setDrawCircles(false)
                    setDrawValues(false)
                    color = set.color

                    if (set.lineType === LineType.DOTTED) {
                        // https://github.com/PhilJay/MPAndroidChart/pull/2622 This should be Transparent but using White because of bug
                        color = Color.rgb(250, 250, 250)
                        setDrawCircles(true)
                        circleRadius = 1f
                        setCircleColor(set.color)
                    }
                }

                result.add(lineDataSet)
            }
        }

        return LineData(result)
    }

    private fun getCandleData(sets: List<IDataSet>): CandleData {

        for (set in sets) {
            if (set.lineType === LineType.CANDLE) {
                val entries = mutableListOf<CandleEntry>()
                val cds = set as CandleDataSet

                for (i in 0 until set.size)
                    entries.add(CandleEntry(i.toFloat(), cds.getHigh(i), cds.getLow(i), cds.getOpen(i), cds.getClose(i))) // order is high, low, open, close

                val dataSet = com.github.mikephil.charting.data.CandleDataSet(entries, set.label)
                dataSet.setDrawValues(false)
                dataSet.decreasingColor = _colors.red
                dataSet.decreasingPaintStyle = Paint.Style.FILL
                dataSet.increasingColor = _colors.green
                dataSet.increasingPaintStyle = Paint.Style.FILL

                return CandleData(dataSet)
            }
        }

        return CandleData()
    }

    private fun getAxisFormatter(dates: Array<Date>, interval: Interval): ValueFormatter {
        return object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val v = value.toInt()
                return if (interval === Interval.MONTHLY)
                    dateFormatMonthly.format(dates[v])
                else
                    dateFormat.format(dates[v])
            }
        }
    }

    // Round to 2 significant figures
    private val logScaleYAxis: ValueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            // Round to 2 significant figures
            val actual = exp(value.toDouble())
            var bd = BigDecimal(actual)
            bd = bd.round(MathContext(2))
            return bd.toPlainString()
        }
    }
}