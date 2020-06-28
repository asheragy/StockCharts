package org.cerion.stockcharts.ui.charts

import androidx.lifecycle.*
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

    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean>
        get() = _busy

    fun load(symbol: String) {
        _symbol.value = symbol
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            try {
                _busy.value = true
                prices.value = getPrices(_symbol.value!!)
            }
            finally {
                _busy.value = false
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
}