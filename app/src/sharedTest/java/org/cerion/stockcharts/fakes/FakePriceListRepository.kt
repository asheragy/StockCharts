package org.cerion.stockcharts.fakes

import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.repository.IPriceListRepository
import org.cerion.stocks.core.web.FetchInterval

open class FakePriceListRepository : IPriceListRepository {
    override fun add(list: PriceList) {
        // Nothing, only need to fake the return
    }

    override fun get(symbol: String, interval: FetchInterval): PriceList? {
        if (symbol == "<ex>")
            throw Exception("error getting symbol")

        if (symbol == "<null>")
            return null

        return PriceList.generateSeries(500)
    }
}