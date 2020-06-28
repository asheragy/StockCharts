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
import org.cerion.stocks.core.indicators.AccumulationDistributionLine
import org.cerion.stocks.core.indicators.MACD
import org.cerion.stocks.core.model.Interval
import org.cerion.stocks.core.repository.CachedPriceListRepository

class ChartsViewModel(private val repo: CachedPriceListRepository) : ViewModel() {

    // TODO only prices should be nullable value

    private val _symbol = MutableLiveData("")
    val symbol: LiveData<String>
        get() = _symbol

    private val _interval = MutableLiveData(Interval.DAILY)
    val interval: LiveData<Interval>
        get() = _interval

    val prices = MediatorLiveData<PriceList>()

    private var _charts = mutableListOf(PriceChart(), VolumeChart(), IndicatorChart(MACD()), PriceChart(), VolumeChart(), IndicatorChart(AccumulationDistributionLine()))
    val charts: MutableLiveData<List<StockChart>> = MutableLiveData(_charts)

    private var job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Main )

    fun load(symbol: String) {
        // TODO add source for symbol too
        prices.addSource(interval) {
            scope.launch {
                prices.value = withContext(scope.coroutineContext) {
                    val prices = getPrices(symbol)
                    prices
                }
            }
        }
    }

    fun setInterval(interval: Interval) {
        _interval.value = interval
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

            val list = repo.get(symbol, intervalQuery, startDate)

            when(interval.value) {
                Interval.QUARTERLY -> list.toQuarterly()
                Interval.YEARLY -> list.toYearly()
                else -> list
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}