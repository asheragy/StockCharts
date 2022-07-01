package org.cerion.stockcharts.fakes

import org.cerion.marketdata.core.charts.ChartColors
import org.cerion.marketdata.core.charts.StockChart
import org.cerion.marketdata.core.model.Symbol
import org.cerion.stockcharts.repository.PreferenceRepository

class FakePreferenceRepository : PreferenceRepository {

    private val _symbolHistory = mutableListOf<Symbol>()
    private val _charts = mutableListOf<StockChart>()

    override fun getCharts(colors: ChartColors) = _charts
    override fun saveCharts(charts: List<StockChart>) {
        _charts.clear()
        _charts.addAll(charts)
    }

    override fun getSymbolHistory() = _symbolHistory
    override fun addSymbolHistory(symbol: Symbol) {
        _symbolHistory.add(symbol)
    }
}