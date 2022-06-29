package org.cerion.stockcharts.ui.charts

import android.content.Context
import android.graphics.Color
import org.cerion.marketdata.core.charts.ChartColors
import org.cerion.stockcharts.R
import org.cerion.stockcharts.common.isDarkTheme

class ChartColorScheme(context: Context) : ChartColors() {

    init {
        primary = if (context.isDarkTheme()) Color.WHITE else Color.BLACK
        primaryBlue = context.getColor(R.color.chart_blue)
        volumneBlue = context.getColor(R.color.chart_bar)
        positiveGreen = context.getColor(R.color.positive_green)
        negativeRed = context.getColor(R.color.negative_red)
        primaryPurple = context.getColor(R.color.chart_purple)

        orange = context.getColor(R.color.chart_orange)
        teal = context.getColor(R.color.chart_teal)
        secondaryGreen = context.getColor(R.color.chart_secondaryGreen)
        yellow = context.getColor(R.color.chart_yellow)
        secondaryBlue = context.getColor(R.color.chart_secondaryBlue)
        secondaryRed = context.getColor(R.color.chart_secondaryRed)
    }
}