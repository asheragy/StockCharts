package org.cerion.stockcharts.repository

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.cerion.marketdata.core.charts.ChartColors
import org.cerion.marketdata.core.charts.StockChart
import org.cerion.marketdata.core.model.Symbol

private const val KEY_CHARTS = "charts"
private const val KEY_SYMBOL_HISTORY = "symbolHistory"

interface PreferenceRepository {
    fun getCharts(colors: ChartColors): List<StockChart>
    fun saveCharts(charts: List<StockChart>)
    fun getSymbolHistory(): List<Symbol>
    fun addSymbolHistory(symbol: Symbol)
}

class DefaultPreferenceRepository(context: Context) : PreferenceRepository {

    private val app = context.applicationContext

    private val prefs: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(app)

    override fun getCharts(colors: ChartColors): List<StockChart> {
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

    override fun saveCharts(charts: List<StockChart>) {
        val stringList = charts.map { it.serialize() }.joinToString("\t")
        prefs.edit().putString(KEY_CHARTS, stringList).apply()
    }

    override fun getSymbolHistory(): List<Symbol> {
        val saved = prefs.getString(KEY_SYMBOL_HISTORY, "")
        if (!saved.isNullOrEmpty())
            return saved.split("\t").map {
                val split = it.split("|")
                Symbol(split[0], split.getOrNull(1) ?: "", "")
            }

        return emptyList()
    }

    override fun addSymbolHistory(symbol: Symbol) {
        val existing = getSymbolHistory()
                .filter { it.symbol != symbol.symbol }
                .toMutableList()

        existing.add(symbol)
        if (existing.size > 5)
            existing.removeAt(0)

        val items = existing.map { it.symbol + "|" + it.name }
        prefs.edit().putString(KEY_SYMBOL_HISTORY, items.joinToString("\t")).apply()
    }

}