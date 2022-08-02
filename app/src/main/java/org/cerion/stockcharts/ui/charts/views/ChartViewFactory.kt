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
import org.cerion.marketdata.core.charts.*
import org.cerion.marketdata.core.charts.CandleDataSet
import org.cerion.marketdata.core.charts.DataSet
import org.cerion.marketdata.core.model.Interval
import org.cerion.marketdata.core.model.OHLCVTable
import org.cerion.marketdata.core.platform.KMPDate
import org.cerion.stockcharts.R
import org.cerion.stockcharts.common.isDarkTheme
import java.math.BigDecimal
import java.math.MathContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.exp

class ChartViewFactory(private val context: Context) {

    companion object {
        private const val CHART_HEIGHT_PRICE = 800
        private const val CHART_HEIGHT = 400
        private var dateFormat = DateTimeFormatter.ofPattern("MMM d, yy")
        private var dateFormatMonthly = DateTimeFormatter.ofPattern("MMM ''yy")
        private val blankDescription = Description().apply { text = "" }
    }

    private val _textColor =
            if (context.isDarkTheme())
                context.getColor(R.color.secondaryTextColor)
            else
                context.getColor(R.color.primaryTextColor)

    fun getChart(chart: StockChart, table: OHLCVTable): Chart<*> {
        return when(chart) {
            is PriceChart -> getPriceChart(chart, table)
            is IndicatorChart -> getLineChart(chart, table)
            is VolumeChart -> getVolumeChart(chart, table)
            else -> throw NotImplementedError()
        }
    }

    fun getEmptyChart(): Chart<*> {
        return LineChart(context).apply {
            description = blankDescription
            minimumHeight = CHART_HEIGHT
        }
    }

    private fun getPriceChart(pchart: PriceChart, table: OHLCVTable): Chart<*> {
        val chart: BarLineChartBase<*>
        val sets = getDataSets(pchart, table)
        if (pchart.candleData && pchart.canShowCandleData(table)) {
            val data = CombinedData()
            data.setData(getCandleData(sets))
            data.setData(getLineData(sets))

            chart = CombinedChart(context)
            setChartDefaults(chart, pchart, table)
            chart.drawOrder = arrayOf(DrawOrder.CANDLE, DrawOrder.LINE)
            chart.data = data
        }
        else {
            chart = LineChart(context)
            setChartDefaults(chart, pchart, table)
            val lineData = getLineData(sets)
            chart.data = lineData
        }

        chart.minimumHeight = CHART_HEIGHT_PRICE
        if (pchart.logScale)
            chart.axisRight.valueFormatter = logScaleYAxis

        setLegend(chart, sets)
        return chart
    }

    private fun getLineChart(ichart: IndicatorChart, table: OHLCVTable): Chart<*> {
        return LineChart(context).apply {
            setChartDefaults(this, ichart, table)
            minimumHeight = CHART_HEIGHT

            val sets = getDataSets(ichart, table)
            data = getLineData(sets)
            setLegend(this, sets)
        }
    }

    private fun getVolumeChart(vchart: VolumeChart, table: OHLCVTable): Chart<*> {
        return CombinedChart(context).apply {
            setChartDefaults(this, vchart, table)

            val dataSets = getDataSets(vchart, table)
            data = CombinedData().apply {
                setData(getBarData(dataSets))
                setData(getLineData(dataSets))
            }

            setLegend(this, dataSets)
            if (vchart.logScale)
                axisRight.valueFormatter = logScaleYAxis
        }
    }

    private fun setChartDefaults(chart: BarLineChartBase<*>, stockchart: StockChart, table: OHLCVTable) {
        if (chart.data != null)
            throw AssertionError("chart defaults should be set before data") // Needed for viewPortOffsets to work properly

        chart.apply {
            description = blankDescription
            minimumHeight = CHART_HEIGHT

            //Set Y axis
            axisLeft.setDrawLabels(false)
            axisRight.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
            axisRight.setLabelCount(3, false)
            axisRight.textColor = _textColor

            xAxis.valueFormatter = getAxisFormatter(stockchart.getDates(table), table.interval)
            xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
            // Always start at position 0 even if data set starts after that
            xAxis.axisMinimum = 0f
            xAxis.textColor = _textColor

            setViewPortOffsets(0f, viewPortHandler.offsetTop(), 0f, viewPortHandler.offsetBottom())
        }
    }

    private fun getDataSets(chart: StockChart, table: OHLCVTable): List<IDataSet> {
        return chart.getDataSets(table)
    }

    private fun getBarData(sets: List<IDataSet>): BarData {
        val result = mutableListOf<IBarDataSet>()

        sets.filter { it.lineType == LineType.BAR }
                .forEach {
                    val set = it as DataSet

                    val entries = set.mapIndexed { index, value ->
                        BarEntry(index.toFloat(), value)
                    }

                    val dataSet = BarDataSet(entries, set.label)
                    dataSet.setDrawValues(false)
                    dataSet.color = set.color
                    result.add(dataSet)
                }

        return BarData(result)
    }

    private fun getLineData(sets: List<IDataSet>): LineData {
        val result = mutableListOf<ILineDataSet>()

        for (curr in sets) {
            if (curr.lineType === LineType.LINE || curr.lineType === LineType.DOTTED) {
                val set = curr as DataSet
                val entries = set.mapIndexed { index, value ->
                    // TODO previous code checked for NaN, could be other issues if that comes up...
                    Entry(index.toFloat(), value)
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
                dataSet.decreasingColor = context.getColor(R.color.negative_red)
                dataSet.decreasingPaintStyle = Paint.Style.FILL
                dataSet.increasingColor = context.getColor(R.color.positive_green)
                dataSet.increasingPaintStyle = Paint.Style.FILL

                return CandleData(dataSet)
            }
        }

        return CandleData()
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

    private fun getAxisFormatter(dates: Array<KMPDate>, interval: Interval): ValueFormatter {
        return object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val position = value.toInt()
                if (position >= dates.size)
                    return ""

                val date: LocalDate = dates[position].jvmDate
                return if (interval === Interval.MONTHLY)
                    dateFormatMonthly.format(date)
                else
                    dateFormat.format(date)
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