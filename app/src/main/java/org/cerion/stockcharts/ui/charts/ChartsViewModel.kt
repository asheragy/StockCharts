package org.cerion.stockcharts.ui.charts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import org.cerion.stockcharts.common.Constants
import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.charts.IndicatorChart
import org.cerion.stocks.core.charts.PriceChart
import org.cerion.stocks.core.charts.StockChart
import org.cerion.stocks.core.charts.VolumeChart
import org.cerion.stocks.core.indicators.MACD
import org.cerion.stocks.core.model.Interval
import org.cerion.stocks.core.web.CachedDataAPI
import kotlin.math.max
import kotlin.math.min

class ChartsViewModel(private val api: CachedDataAPI) : ViewModel() {

    private val _symbol = MutableLiveData("")
    val symbol: LiveData<String>
        get() = _symbol

    private val _interval = MutableLiveData(Interval.DAILY)
    val interval: LiveData<Interval>
        get() = _interval

    private val _range = MediatorLiveData<Pair<Int, Int>>()
    val range: LiveData<Pair<Int, Int>>
        get() = _range

    val prices = MediatorLiveData<PriceList>()

    private val _charts = mutableListOf(PriceChart(), VolumeChart())
    val charts: MutableLiveData<List<StockChart>> = MutableLiveData(_charts)

    private var job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main )

    init {
        _range.value = Pair(0, 0)

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

        _range.addSource(prices) {
            _range.value = Pair(0, it.size - 1)
        }
    }

    fun setInterval(interval: Interval) {
        _interval.value = interval
    }

    fun setRange(start: Int, end: Int) {
        val s = max(0, start)
        val e = min(end, prices.value!!.size - 1)
        if (range.value!!.first != s || range.value!!.second != e)
            _range.value = Pair(s, e)
    }

    fun addPriceChart() {
        addChart(PriceChart().apply {
            candleData = false
        })
    }

    fun addIndicatorChart() {
        addChart(IndicatorChart(MACD()))
    }

    fun addVolumeChart() {
        addChart(VolumeChart())
    }

    private fun addChart(chart: StockChart) {
        _charts.add(chart)
        charts.value = _charts
    }

    private suspend fun getPrices(symbol: String): PriceList {
        return withContext(Dispatchers.IO) {
            val list = api.getPrices(symbol, interval.value!!, Constants.START_DATE_DAILY)
            PriceList(symbol, list)
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}