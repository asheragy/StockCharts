package org.cerion.stockcharts.ui.charts

import android.content.Context
import android.graphics.Color
import com.github.mikephil.charting.utils.ColorTemplate
import org.cerion.stockcharts.R
import org.cerion.stocks.core.charts.ChartColors

class ChartColorScheme(context: Context) : ChartColors() {

    init {
        primary = Color.WHITE // TODO theme is hard coded so check does not work currently, if (context.isDarkTheme()) Color.WHITE else Color.BLACK
        primaryBlue = context.getColor(R.color.chart_blue)
        volumneBlue = ColorTemplate.VORDIPLOM_COLORS[3] //context.getColor(R.color.chart_bar)
        positiveGreen = context.getColor(R.color.positive_green)
        negativeRed = context.getColor(R.color.negative_red)

        orange = ColorTemplate.VORDIPLOM_COLORS[2]
        yellow = ColorTemplate.VORDIPLOM_COLORS[0]
        purple = context.getColor(R.color.chart_purple)
        secondaryRed = ColorTemplate.VORDIPLOM_COLORS[4]
        secondaryGreen = ColorTemplate.VORDIPLOM_COLORS[1]
        secondaryBlue = context.getColor(R.color.chart_bar)
    }
}