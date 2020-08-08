package org.cerion.stockcharts.ui.charts.fakes

import org.cerion.stockcharts.repository.PreferenceRepository
import org.cerion.stocks.core.charts.ChartColors
import org.cerion.stocks.core.charts.StockChart
import org.cerion.stocks.core.model.Symbol

class FakePreferenceRepository : PreferenceRepository {
    override fun getCharts(colors: ChartColors): List<StockChart> {
        TODO("Not yet implemented")
    }

    override fun saveCharts(charts: List<StockChart>) {
        TODO("Not yet implemented")
    }

    override fun getSymbolHistory(): List<Symbol> {
        TODO("Not yet implemented")
    }

    override fun addSymbolHistory(symbol: Symbol) {
        TODO("Not yet implemented")
    }
}