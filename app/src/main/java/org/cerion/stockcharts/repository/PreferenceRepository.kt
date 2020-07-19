package org.cerion.stockcharts.repository

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.cerion.stocks.core.charts.ChartColors
import org.cerion.stocks.core.charts.StockChart
import org.cerion.stocks.core.model.Symbol

private const val KEY_LAST_SYMBOL = "lastSymbol"
private const val KEY_CHARTS = "charts"

class PreferenceRepository(context: Context) {

    private val app = context.applicationContext

    private val prefs: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(app)

    fun saveLastSymbol(symbol: Symbol) {
        prefs.edit().putString(KEY_LAST_SYMBOL, symbol.symbol + "\t" + symbol.name).apply()
    }

    fun getLastSymbol(): Symbol? {
        val symbol = prefs.getString(KEY_LAST_SYMBOL, null)
        if (symbol != null) {
            val arr = symbol.split("\t")
            return Symbol(arr[0], arr[1])
        }

        return null
    }

    fun getCharts(colors: ChartColors): List<StockChart> {
        val saved = prefs.getString(KEY_CHARTS, "") ?: ""
        val charts = mutableListOf<StockChart>()

        if (saved.isNotEmpty()) {
            saved.split("\t").forEach {
                try {
                    charts.add(StockChart.deserialize(it, colors))
                } catch (e: Throwable) {
                    // Ignore, new charts should get saved in readable format
                }
            }
        }

        return charts
    }

    fun saveCharts(charts: List<StockChart>) {
        val stringList = charts.map { it.serialize() }.joinToString("\t")
        prefs.edit().putString(KEY_CHARTS, stringList).apply()
    }
}