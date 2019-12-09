package org.cerion.stockcharts.positions

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Pair
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import org.cerion.stocks.core.model.Position
import java.util.*

class PortfolioPieChart(context: Context?, attrs: AttributeSet?) : PieChart(context, attrs) {

    init {
        legend.isEnabled = false
        description = null
        setEntryLabelColor(Color.BLACK)
    }

    fun setPositions(positions: List<Position>) {
        val total = positions.sumByDouble { it.totalValue }

        val entries = ArrayList<PieEntry>()
        for (p in positions) {
            val value = (100 * p.totalValue / total).toFloat()
            val label = if(p.cash) "Cash" else p.symbol
            entries.add(PieEntry(value, label))
        }

        // Data set
        val dataSet = PieDataSet(entries, "")
        dataSet.sliceSpace = 2f
        dataSet.valueTextSize = 12f
        dataSet.valueFormatter = PercentFormatter()

        // TODO reserve green for cash type
        val colors = ArrayList<Int>()
        colors.add(Color.GRAY)
        colors.add(Color.BLUE)
        colors.add(Color.RED)
        colors.add(Color.GREEN)
        colors.add(Color.CYAN)
        colors.add(Color.YELLOW)
        colors.add(Color.MAGENTA)
        dataSet.colors = colors
        val pieData = PieData(dataSet)
        data = pieData
        invalidate()
    }

    @Deprecated(message = "use positions instead")
    fun setAllocations(allocations: List<Pair<String?, Float>>) {
        var total = 0f
        for (i in allocations.indices) {
            val a = allocations[i]
            total += a.second
        }
        val entries = ArrayList<PieEntry>()
        for (i in allocations.indices) {
            val a = allocations[i]
            entries.add(PieEntry(100 * a.second / total, a.first))
        }
        // Data set
        val dataSet = PieDataSet(entries, "")
        dataSet.sliceSpace = 2f
        dataSet.valueTextSize = 12f
        dataSet.valueFormatter = PercentFormatter()
        //Colors
        val colors = ArrayList<Int>()
        colors.add(Color.GRAY)
        colors.add(Color.BLUE)
        colors.add(Color.RED)
        colors.add(Color.GREEN)
        colors.add(Color.CYAN)
        colors.add(Color.YELLOW)
        colors.add(Color.MAGENTA)
        dataSet.colors = colors
        val pieData = PieData(dataSet)
        data = pieData
        invalidate()
    }
}