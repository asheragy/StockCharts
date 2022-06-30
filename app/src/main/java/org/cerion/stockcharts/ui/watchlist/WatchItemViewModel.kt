package org.cerion.stockcharts.ui.watchlist

import androidx.databinding.ObservableField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.cerion.stockcharts.common.Utils
import org.cerion.marketdata.core.PriceList
import org.cerion.marketdata.core.charts.StockChart
import org.cerion.marketdata.core.functions.conditions.ICondition
import org.cerion.marketdata.core.model.Interval
import org.cerion.stockcharts.repository.CachedPriceListRepository

class WatchItemViewModel(private val repo: CachedPriceListRepository, private val condition: ICondition, val symbol: String) {

    // TODO change to livedata
    val price = ObservableField("--")
    val change = ObservableField("--")
    val weekPosition = ObservableField(0)
    val yearPosition = ObservableField(0)
    val isTrue = ObservableField(false)
    val loading = ObservableField(true)

    suspend fun update() {
        withContext(Dispatchers.IO) {
            delay(1000)

            loading.set(true)
            try {
                val prices = repo.get(symbol, Interval.DAILY)
                apply(prices)
            } catch (e: Exception) {
                // TODO set error state
                e.printStackTrace()
            }

            loading.set(false)
        }
    }

    fun getCondition(): String {
        return condition.toString()
    }

    val chart: StockChart
        get() = condition.chart

    private fun apply(list: PriceList) {
        val size = list.size
        val price = list.last().close
        val change = list.last().getPercentDiff(list[size - 2])

        // TODO include current quote since its not in pricelist
        var low = list.low[size - 1]
        for (j in size - 5 until size) {
            if (list.low[j] < low) low = list.low[j]
        }

        var high = list.high[size - 1]
        for (j in size - 5 until size) {
            if (list.high[j] > high) high = list.high[j]
        }

        var range = high - low
        var diff = list.last().close - low
        var percent = diff / range
        weekPosition.set((percent * 100).toInt())

        // TODO add function to get highest high in given range to PriceList

        // Year
        var start = 250
        if (list.size < start) start = 0
        low = list.low[size - 1]
        for (j in size - start until size) {
            if (list.low[j] < low) low = list.low[j]
        }

        high = list.high[size - 1]
        for (j in size - start until size) {
            if (list.high[j] > high) high = list.high[j]
        }

        range = high - low
        diff = list.last().close - low
        percent = diff / range

        yearPosition.set((percent * 100).toInt())
        isTrue.set(condition.eval(list))

        // Set strings
        this.price.set(Utils.decimalFormat.format(price.toDouble()))
        this.change.set(Utils.decimalFormat.format(change.toDouble()) + "%")
    }
}