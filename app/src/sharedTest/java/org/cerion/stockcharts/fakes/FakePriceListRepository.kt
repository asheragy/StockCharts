package org.cerion.stockcharts.fakes

import org.cerion.marketdata.core.PriceList
import org.cerion.marketdata.core.web.FetchInterval
import org.cerion.stockcharts.repository.PriceListRepository

open class FakePriceListRepository : PriceListRepository {
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

    override suspend fun clearCache() {
    }

    override suspend fun cleanupCache() {
    }
}