package org.cerion.stockcharts.ui.watchlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.cerion.marketdata.core.charts.StockChart
import org.cerion.marketdata.core.functions.conditions.ICondition
import org.cerion.marketdata.core.model.Interval
import org.cerion.marketdata.core.model.OHLCVTable
import org.cerion.stockcharts.repository.CachedPriceListRepository

class WatchItemViewModel(private val repo: CachedPriceListRepository, private val condition: ICondition, val symbol: String) {

    // TODO change to livedata
    //val price = ObservableField("--")
    //val change = ObservableField("--")
    //val weekPosition = ObservableField(0)
    //val yearPosition = ObservableField(0)
    //val isTrue = ObservableField(false)

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean>
        get() = _loading

    suspend fun update() {
        withContext(Dispatchers.IO) {
            delay(1000)

            _loading.value = true
            try {
                val table = repo.get(symbol, Interval.DAILY)
                apply(table)
            } catch (e: Exception) {
                // TODO set error state
                e.printStackTrace()
            }

            _loading.value = false
        }
    }

    fun getCondition(): String {
        return condition.toString()
    }

    val chart: StockChart
        get() = condition.chart

    private fun apply(table: OHLCVTable) {
        /*
        val size = table.size
        val price = table.last().close
        val change = table.last().getPercentDiff(table[size - 2])

        // TODO include current quote since its not in pricelist
        var low = table.low[size - 1]
        for (j in size - 5 until size) {
            if (table.low[j] < low) low = table.low[j]
        }

        var high = table.high[size - 1]
        for (j in size - 5 until size) {
            if (table.high[j] > high) high = table.high[j]
        }

        var range = high - low
        var diff = table.last().close - low
        var percent = diff / range
        weekPosition.set((percent * 100).toInt())

        // TODO add function to get highest high in given range to PriceList

        // Year
        var start = 250
        if (table.size < start) start = 0
        low = table.low[size - 1]
        for (j in size - start until size) {
            if (table.low[j] < low) low = table.low[j]
        }

        high = table.high[size - 1]
        for (j in size - start until size) {
            if (table.high[j] > high) high = table.high[j]
        }

        range = high - low
        diff = table.last().close - low
        percent = diff / range

        yearPosition.set((percent * 100).toInt())
        isTrue.set(condition.eval(table))

        // Set strings
        this.price.set(Utils.decimalFormat.format(price.toDouble()))
        this.change.set(Utils.decimalFormat.format(change.toDouble()) + "%")

         */
    }
}