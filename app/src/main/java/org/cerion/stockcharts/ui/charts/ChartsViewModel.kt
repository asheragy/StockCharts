package org.cerion.stockcharts.ui.charts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.cerion.stockcharts.common.Constants
import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.charts.PriceChart
import org.cerion.stocks.core.charts.StockChart
import org.cerion.stocks.core.charts.VolumeChart
import org.cerion.stocks.core.model.Interval
import org.cerion.stocks.core.web.CachedDataAPI

class ChartsViewModel(private val api: CachedDataAPI) : ViewModel() {

    private val _symbol = MutableLiveData("")
    val symbol: LiveData<String>
        get() = _symbol

    private val _interval = MutableLiveData(Interval.DAILY)
    val interval: LiveData<Interval>
        get() = _interval

    val prices = MediatorLiveData<PriceList>()

    val charts: LiveData<List<StockChart>> = MutableLiveData<List<StockChart>>(listOf(PriceChart(), VolumeChart()))

    private var job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main )

    init {
        val symbol = "XLE"
        _symbol.value = symbol

        // TODO add source for symbol too
        prices.addSource(interval) {
            scope.launch {
                prices.value = withContext(scope.coroutineContext) {
                    getPrices(symbol)
                }
            }
        }
    }

    fun setInterval(interval: Interval) {
        _interval.value = interval
    }

    private suspend fun getPrices(symbol: String): PriceList {
        return withContext(Dispatchers.IO) {
            val list = api.getPrices(symbol, interval.value!!, Constants.START_DATE_DAILY)
            PriceList(symbol, list)
        }
    }

}