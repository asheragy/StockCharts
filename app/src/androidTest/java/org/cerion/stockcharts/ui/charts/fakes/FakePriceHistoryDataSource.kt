package org.cerion.stockcharts.ui.charts.fakes

import org.cerion.stocks.core.PriceRow
import org.cerion.stocks.core.web.FetchInterval
import org.cerion.stocks.core.web.PriceHistoryDataSource
import java.util.*

class FakePriceHistoryDataSource : PriceHistoryDataSource {
    override fun getPrices(symbol: String, interval: FetchInterval, start: Date?): List<PriceRow> {
        TODO("Not yet implemented")
    }
}