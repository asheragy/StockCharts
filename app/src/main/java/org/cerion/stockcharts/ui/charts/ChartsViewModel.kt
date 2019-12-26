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

    // TODO only prices should be nullable value

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

    private var _charts = mutableListOf(PriceChart(), VolumeChart())
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
                    val prices = getPrices(symbol)
                    prices
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

    fun removeChart(chart: StockChart) {
        _charts.remove(chart)
        charts.value = _charts
    }

    fun replaceChart(old: StockChart, new: StockChart) {
        _charts = _charts.map { if(it == old) new else it }.toMutableList()
        charts.value = _charts
    }

    private fun addChart(chart: StockChart) {
        _charts.add(chart)
        charts.value = _charts
    }

    private suspend fun getPrices(symbol: String): PriceList {
        return withContext(Dispatchers.IO) {
            // TODO repository should handle quarterly/monthly conversion
            val startDate = when(interval.value) {
                Interval.DAILY -> Constants.START_DATE_DAILY
                Interval.WEEKLY -> Constants.START_DATE_WEEKLY
                else -> Constants.START_DATE_MONTHLY
            }

            val intervalQuery = when(interval.value) {
                Interval.DAILY -> Interval.DAILY
                Interval.WEEKLY -> Interval.WEEKLY
                else -> Interval.MONTHLY
            }

            val list = api.getPrices(symbol, intervalQuery, startDate)

            when(interval.value) {
                Interval.QUARTERLY -> PriceList(symbol, list).toQuarterly()
                Interval.YEARLY -> PriceList(symbol, list).toYearly()
                else -> PriceList(symbol, list)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}