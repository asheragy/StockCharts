package org.cerion.stockcharts.ui.charts

import androidx.lifecycle.*
import kotlinx.coroutines.*
import org.cerion.stockcharts.repository.PreferenceRepository
import org.cerion.stockcharts.repository.PriceListSQLRepository
import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.charts.*
import org.cerion.stocks.core.indicators.AccumulationDistributionLine
import org.cerion.stocks.core.indicators.MACD
import org.cerion.stocks.core.model.Interval
import org.cerion.stocks.core.model.Symbol
import org.cerion.stocks.core.overlays.BollingerBands
import org.cerion.stocks.core.overlays.ExpMovingAverage
import org.cerion.stocks.core.overlays.ParabolicSAR
import org.cerion.stocks.core.overlays.SimpleMovingAverage
import org.cerion.stocks.core.repository.CachedPriceListRepository

class ChartsViewModel(
        private val repo: CachedPriceListRepository,
        private val sqlRepo: PriceListSQLRepository,
        private val prefs: PreferenceRepository,
        private val colors: ChartColorScheme) : ViewModel() {

    private val DefaultSymbol = Symbol("^GSPC", "S&P 500")

    private val _symbol = MutableLiveData(DefaultSymbol)
    val symbol: LiveData<Symbol>
        get() = _symbol

    private val _interval = MutableLiveData(Interval.DAILY)
    val interval: LiveData<Interval>
        get() = _interval

    val prices = MediatorLiveData<PriceList>()

    private val DefaultCharts = mutableListOf(
            PriceChart(colors),
            VolumeChart(colors))

    private val DefaultChartsTest = mutableListOf(
            PriceChart(colors).apply {
                addOverlay(BollingerBands())
                addOverlay(SimpleMovingAverage())
                addOverlay(ExpMovingAverage())
            },
            VolumeChart(colors),
            IndicatorChart(MACD(), colors),
            PriceChart(colors).apply {
                addOverlay(ParabolicSAR())
            },
            VolumeChart(colors),
            IndicatorChart(AccumulationDistributionLine(), colors)
    )

    private var _charts = mutableListOf<StockChart>()
    val charts: MutableLiveData<List<StockChart>> = MutableLiveData(_charts)

    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean>
        get() = _busy

    private var cleanupCache = true

    init {
        // Load saved charts
        _charts.addAll(prefs.getCharts(colors))
        if (_charts.isEmpty())
            _charts.addAll(DefaultCharts)

        charts.value = _charts
    }

    fun load() {
        val lastSymbol = prefs.getLastSymbol()
        if (lastSymbol != null)
            _symbol.value = lastSymbol

        refresh()
    }

    fun load(symbol: Symbol) {
        _symbol.value = symbol
        refresh()
        prefs.saveLastSymbol(symbol)
    }

    fun setInterval(interval: Interval) {
        _interval.value = interval
        refresh()
    }

    private fun refresh() {
        launchBusy {
            // On the 2nd fetch of this app instance, cleanup database if needed
            if (prices.value != null && cleanupCache) {
                cleanupCache = false
                sqlRepo.cleanupCache()
            }

            prices.value = getPrices(_symbol.value!!.symbol)
        }
    }

    private fun launchBusy(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _busy.value = true
                block()
            }
            finally {
                _busy.value = false
            }
        }
    }

    fun addPriceChart() {
        addChart(PriceChart(colors).apply {
            candleData = false
        })
    }

    fun addIndicatorChart() {
        addChart(IndicatorChart(MACD(), colors))
    }

    fun addVolumeChart() {
        addChart(VolumeChart(colors))
    }

    fun removeChart(chart: StockChart) {
        _charts.remove(chart)
        charts.value = _charts
        saveCharts()
    }

    fun replaceChart(old: StockChart, new: StockChart) {
        _charts = _charts.map { if(it == old) new else it }.toMutableList()
        charts.value = _charts
        saveCharts()
    }

    fun clearCache() {
        launchBusy {
            sqlRepo.clearCache()
        }
    }

    private fun saveCharts() {
        prefs.saveCharts(_charts)
    }

    private fun addChart(chart: StockChart) {
        _charts.add(chart)
        charts.value = _charts
        saveCharts()
    }

    private suspend fun getPrices(symbol: String): PriceList {
        return withContext(Dispatchers.IO) {
            repo.get(symbol, interval.value!!)
        }
    }
}