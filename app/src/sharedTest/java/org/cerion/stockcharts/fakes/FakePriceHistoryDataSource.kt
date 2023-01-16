package org.cerion.stockcharts.fakes

import org.cerion.marketdata.core.model.OHLCVRow
import org.cerion.marketdata.webclients.FetchInterval
import org.cerion.marketdata.webclients.PriceHistoryDataSource
import java.time.LocalDate

class FakePriceHistoryDataSource : PriceHistoryDataSource {
    override fun getPrices(
        symbol: String,
        interval: FetchInterval,
        start: LocalDate?
    ): List<OHLCVRow> {
        TODO("Not yet implemented")
    }

}