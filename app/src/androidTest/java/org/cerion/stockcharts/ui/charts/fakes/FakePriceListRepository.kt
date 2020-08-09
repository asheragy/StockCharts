package org.cerion.stockcharts.ui.charts.fakes

import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.repository.IPriceListRepository
import org.cerion.stocks.core.web.FetchInterval

class FakePriceListRepository : IPriceListRepository {
    override fun add(list: PriceList) {
        // Nothing, only need to fake the return
    }

    override fun get(symbol: String, interval: FetchInterval): PriceList? {
        return PriceList.generateSeries(500)
    }
}