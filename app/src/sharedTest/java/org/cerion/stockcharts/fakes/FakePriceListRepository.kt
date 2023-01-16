package org.cerion.stockcharts.fakes

import org.cerion.marketdata.core.model.OHLCVTable
import org.cerion.marketdata.webclients.FetchInterval
import org.cerion.stockcharts.repository.PriceListRepository
import java.util.*

open class FakePriceListRepository : PriceListRepository {
    override fun add(list: OHLCVTable) {
        // Nothing, only need to fake the return
    }

    override fun get(symbol: String, interval: FetchInterval): Pair<OHLCVTable?, Date?> {
        if (symbol == "<ex>")
            throw Exception("error getting symbol")

        if (symbol == "<null>")
            return Pair(null, null)

        return Pair(OHLCVTable.generateSeries(500), Date())
    }

    override suspend fun clearCache() {
    }

    override suspend fun cleanupCache() {
    }
}