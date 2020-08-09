package org.cerion.stockcharts.ui.charts.fakes

import org.cerion.stockcharts.repository.PreferenceRepository
import org.cerion.stocks.core.charts.ChartColors
import org.cerion.stocks.core.charts.StockChart
import org.cerion.stocks.core.model.Symbol

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